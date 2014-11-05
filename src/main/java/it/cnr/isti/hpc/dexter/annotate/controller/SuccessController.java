/*
 ===========================================================================
 Copyright (c) 2010 BrickRed Technologies Limited

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sub-license, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ===========================================================================

 */

package it.cnr.isti.hpc.dexter.annotate.controller;

import it.cnr.isti.hpc.dexter.annotate.bean.User;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.brickred.socialauth.AuthProvider;
import org.brickred.socialauth.Contact;
import org.brickred.socialauth.Profile;
import org.brickred.socialauth.SocialAuthManager;
import org.brickred.socialauth.spring.bean.SocialAuthTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class SuccessController {

	@Autowired
	private SocialAuthTemplate socialAuthTemplate;

	private static final Logger logger = LoggerFactory
			.getLogger(SuccessController.class);

	SqliteDao dao = SqliteDao.getInstance();

	private final SessionIdentifierGenerator pswGenerator = new SessionIdentifierGenerator();

	@RequestMapping(value = "/authSuccess")
	public ModelAndView getRedirectURL(final HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		SocialAuthManager manager = socialAuthTemplate.getSocialAuthManager();
		AuthProvider provider = manager.getCurrentAuthProvider();

		HttpSession session = request.getSession();

		System.out.println("session-id = " + session.getId());
		if (provider == null) {
			logger.error("cannot find provider");
			RedirectView view = new RedirectView("index.html");
			ModelAndView mv = new ModelAndView(view);
			return mv;

		}
		Profile profile = provider.getUserProfile();
		System.out.println("profile: \n" + profile);
		User user = new User();
		String mail = profile.getEmail();
		if (mail == null) {
			mail = profile.getValidatedId() + "@" + profile.getProviderId()
					+ ".app";
		}
		user.setEmail(mail);
		String first = profile.getFirstName();
		String last = profile.getLastName();
		String display = profile.getDisplayName();

		user.setFirstName((first == null) ? "" : first);

		user.setLastName((last == null || last.isEmpty()) ? mail : last);

		user.setDisplayName(display);
		if (display == null) {
			user.setDisplayName(mail);

		}
		String pwd = pswGenerator.nextSessionId();
		user.setPassword(pwd);

		User u = dao.getUserByMail(user.getEmail());
		System.out.println("------------success---------------");
		if (u != null)
			System.out.println(u.toString());
		System.out.println("----------------------------------");
		if (u != null) {
			logger.info("user ", user.getEmail() + " logged in");
			user = u;
		} else {
			logger.info("add user {}", user.getEmail());
			dao.addUser(user);
		}
		// String type = null;
		// if (session.getAttribute(Constants.REQUEST_TYPE) != null) {
		// type = (String) session.getAttribute(Constants.REQUEST_TYPE);
		// }
		// if (type != null) {
		// if (Constants.REGISTRATION.equals(type)) {
		// return registration(provider);
		// } else if (Constants.IMPORT_CONTACTS.equals(type)) {
		// return importContacts(provider);
		// } else if (Constants.SHARE.equals(type)) {
		// return new ModelAndView("shareForm", "connectedProvidersIds",
		// manager.getConnectedProvidersIds());
		// }
		// }

		RedirectView view = new RedirectView("index.html");
		ModelAndView mv = new ModelAndView(view);

		Cookie cookie = new Cookie("mail", user.getEmail());
		response.addCookie(cookie);
		cookie = new Cookie("psw", user.getPassword());
		response.addCookie(cookie);
		cookie = new Cookie("uid", String.valueOf(user.getId()));
		response.addCookie(cookie);

		// mv.addObject("uid", user.getId());
		// mv.addObject("pwd", user.getPassword());
		return mv;

	}

	@RequestMapping(value = "/accessDeniedAction")
	public ModelAndView accessDeniedAction(final HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		RedirectView view = new RedirectView("index.html");
		ModelAndView mv = new ModelAndView(view);
		return mv;

	}

	private ModelAndView registration(final AuthProvider provider)
			throws Exception {
		Profile profile = provider.getUserProfile();
		System.out.println("profile " + profile.getValidatedId());

		if (profile.getFullName() == null) {
			String name = null;
			if (profile.getFirstName() != null) {
				name = profile.getFirstName();
			}
			if (profile.getLastName() != null) {
				if (profile.getFirstName() != null) {
					name += " " + profile.getLastName();
				} else {
					name = profile.getLastName();
				}
			}
			if (name == null && profile.getDisplayName() != null) {
				name = profile.getDisplayName();
			}
			if (name != null) {
				profile.setFullName(name);
			}
		}
		ModelAndView view = new ModelAndView("registrationForm", "profile",
				profile);
		return view;
	}

	private ModelAndView importContacts(final AuthProvider provider)
			throws Exception {
		List<Contact> contactsList = new ArrayList<Contact>();
		contactsList = provider.getContactList();
		if (contactsList != null && contactsList.size() > 0) {
			for (Contact p : contactsList) {
				if (!StringUtils.hasLength(p.getFirstName())
						&& !StringUtils.hasLength(p.getLastName())) {
					p.setFirstName(p.getDisplayName());
				}
			}
		}
		ModelAndView view = new ModelAndView("showImportContacts", "contacts",
				contactsList);
		return view;
	}

	private final class SessionIdentifierGenerator {
		private final SecureRandom random = new SecureRandom();

		public String nextSessionId() {
			return new BigInteger(130, random).toString(32);
		}
	}

}

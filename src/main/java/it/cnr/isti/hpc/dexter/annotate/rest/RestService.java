/**
 *  Copyright 2013 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/**
 *  Copyright 2013 Diego Ceccarelli
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package it.cnr.isti.hpc.dexter.annotate.rest;

import it.cnr.isti.hpc.dexter.annotate.bean.Action;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedDocument;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotationStatus;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates.EntityAnnotation;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityDescription;
import it.cnr.isti.hpc.dexter.annotate.bean.User;
import it.cnr.isti.hpc.dexter.annotate.bean.UserAnnotation;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao.DocFreq;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao.UserStats;
import it.cnr.isti.hpc.dexter.annotate.result.UserAnnotatedDocument;
import it.cnr.isti.hpc.property.ProjectProperties;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 15, 2013
 */

@Path("/")
public class RestService {

	private static final Logger logger = LoggerFactory.getLogger(RestService.class);

	private final static int STATUS_ERROR = 500;
	private final static int STATUS_SUCCESS = 200;

	private static ProjectProperties properties = new ProjectProperties(RestService.class);

	SqliteDao dao = SqliteDao.getInstance();
	Gson gson = new Gson();
	List<String> admins = Collections.emptyList();

	public RestService() {
		if (!properties.has("admins")) {
			logger.warn("no admins setted");
			return;
		}
		admins = Arrays.asList(properties.get("admins").split(";"));
	}

	private boolean login(User u, String id, String psw) {
		if (id.equals("test"))
			return true;

		if (u == null)
			return false;
		return String.valueOf(u.getId()).equals(id) && u.getPassword().equals(psw);
	}

	private boolean isAdmin(User u) {
		return admins.contains(u.getEmail());
	}

	private Response error(String msg) {
		Response r = Response.status(STATUS_ERROR)
				.entity("{\"error\":\"" + msg + "\"}").build();
		return r;
	}

	private Response ok(Object o) {
		Response r = Response.ok(gson.toJson(o)).build();
		return r;
	}

	private Response databaseError() {
		return error("database connection problems");
	}

	/**
	 * Performs the entity linking on a given text, annotating maximum n
	 * entities.
	 * 
	 * @param text
	 *            the text to annotate
	 * @param n
	 *            the maximum number of entities to annotate
	 * @returns an annotated document, containing the annotated text, and a list
	 *          entities detected.
	 */
	@GET
	@Path("test")
	@Produces({ MediaType.APPLICATION_JSON })
	public String annotate(@QueryParam("v") String text) {
		logger.info("text");
		return text;

	}

	@GET
	@Path("user")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response user(@QueryParam("id") String id,
			@QueryParam("psw") String psw) {
		logger.info("info on user {} ", id);

		User u;

		try {
			u = dao.getUserById(id);

		} catch (SQLException e) {
			return databaseError();
		}
		if (isAdmin(u))
			u.setAdmin(true);
		if (!login(u, id, psw))
			return error("login error: cannot login user " + id);
		return ok(u);
	}
	
	@GET
	@Path("login")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response login(
			@QueryParam("email") String email,
			@QueryParam("psw") String psw) {
		
		logger.info("log in user {} ", email);
		User u;
		try {
			u = dao.getUserByMail(email);
		} catch (SQLException e) {
			return databaseError();
		}
		
		String hashedPsw = cryptWithMD5(psw);
		
		if (isAdmin(u))
			u.setAdmin(true);
		if (!login(u, String.valueOf(u.getId()), hashedPsw))
			return error("login error: cannot login user " + u.getEmail());
		return ok(u);
	}
	
	@GET
	@Path("signup")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response signup(
			@QueryParam("firstName") String firstName,
			@QueryParam("lastName") String lastName,
			@QueryParam("email") String email,
			@QueryParam("psw") String psw) {
		
		logger.info("sign up user {} ", email);

		try {
			User u = dao.getUserByMail(email);
			if (u != null)
				return error("email already registered");
		} catch (SQLException e) {
			return databaseError();
		}
		
		String hashedPsw = cryptWithMD5(psw);

		User u = new User();
		u.setAdmin(false);
		u.setEmail(email);
		u.setPassword(hashedPsw);
		u.setFirstName(firstName);
		u.setLastName(lastName);

		try {
			u = dao.signUp(u);

		} catch (SQLException e) {
			return databaseError();
		}
		if (isAdmin(u))
			u.setAdmin(true);
		return ok(u);
	}
	
	private static String cryptWithMD5(String pass) {
	    try {
	    	MessageDigest md = MessageDigest.getInstance("MD5");
	        byte[] passBytes = pass.getBytes();
	        md.reset();
	        byte[] digested = md.digest(passBytes);
	        StringBuffer sb = new StringBuffer();
	        for(int i=0; i<digested.length; i++) {
	            sb.append(Integer.toHexString(0xff & digested[i]));
	        }
	        return sb.toString();
	    } catch (NoSuchAlgorithmException ex) {
	        return null;
	    }
	}

	@GET
	@Path("users")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response users(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw) {

		User user;

		List<User> users = Collections.emptyList();
		try {
			user = dao.getUserById(id);

			if (!login(user, id, psw))
				return error("login error: cannot login user " + id);

			users = dao.getUsers();

		} catch (SQLException e) {
			return databaseError();
		}

		Map<Integer, User> map = new HashMap<Integer, User>();
		for (User u : users) {
			
			if (!isAdmin(user) && u.getId() != user.getId()) {
				// We remove some critical information from the output
				u.setEmail("");
				u.setPassword("");
				u.setDisplayName("");
				u.setAdmin(false);
			}
			
			map.put(u.getId(), u);
		}
		return ok(map);
	}

	@GET
	@Path("annotations")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response annotations(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docid") String docid) {
		logger.info("annotation on docid {} ", id);

		UserAnnotatedDocument uad = null;
		User u;

		try {
			u = dao.getUserById(id);

			if (!login(u, id, psw))
				return error("login error: can login user " + id);

		} catch (SQLException e) {
			return databaseError();
		}

		try {
			uad = dao.getDocumentAnnotation(Integer.parseInt(docid));
		} catch (NumberFormatException e) {
			return error(e.toString());
		} catch (SQLException e) {
			return databaseError();
		}
		return ok(uad);
	}

	@GET
	@Path("getDocument")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getDocument(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docid") String docId,
			@QueryParam("coll") String collectionId) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);
		Document doc;
		try {
			doc = dao.getDocument(Integer.parseInt(docId), collectionId);
		} catch (NumberFormatException e) {
			return error("invalid document id " + docId);
		} catch (SQLException e) {
			return databaseError();
		}
		return ok(doc);

	}

	@GET
	@Path("getAnnotatedDocument")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getAnnotatedDocument(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("userid") String userId,
			@QueryParam("docid") String docId,
			@QueryParam("coll") String collectionId) {

		User u;
		AnnotatedDocument ad = null;
		try {
			u = dao.getUserById(id);

			if (!login(u, id, psw))
				return error("login error: can login user " + id);
			Document doc = dao.getDocument(Integer.parseInt(docId), collectionId);
			Map<Integer, UserAnnotation> annotations = new HashMap<Integer, UserAnnotation>();

			for (UserAnnotation ua : dao.getUserAnnotations(Integer.valueOf(docId), userId)) {
				annotations.put(ua.getSpotId(), ua);
			}
			List<AnnotatedSpot> spots = dao.getSpots(userId, Integer.parseInt(docId));
			ad = new AnnotatedDocument(doc, spots);
			for (AnnotatedSpot s : spots) {
				if (annotations.containsKey(s.getSpotId())) {
					UserAnnotation ua = annotations.get(s.getSpotId());
					s.setAnnotation(ua);

					// solve problem with manual annotations
					EntityDescription desc = dao.getEntityDescription(ua.getEntityId());
					ad.addDescription(desc);
				}
				for (EntityAnnotation ea : s.getEntities().getCandidates()) {
					EntityDescription desc = dao.getEntityDescription(ea.getEntity());
					ad.addDescription(desc);
				}
			}
			
			// We need to skip the get of an administrator if he is reading docuemnts of other users
			if (Integer.parseInt(userId) == u.getId()) {
				Action action = new Action(Integer.parseInt(docId), u.getId(), Action.Type.GET_DOCUMENT);
				dao.saveAction(action);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			return databaseError();
		}

		return ok(ad);

	}

	@GET
	@Path("getSpots")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getSpots(@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docid") String docid) {

		User u;
		List<AnnotatedSpot> spots = null;
		try {
			u = dao.getUserById(id);

			if (!login(u, id, psw))
				return error("login error: can not login user " + id);
			spots = dao.getSpots(id, Integer.parseInt(docid));
		} catch (SQLException e) {
			e.printStackTrace();
			return databaseError();
		}
		return ok(spots);
	}

	@GET
	@Path("setUserAnnotation")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response setUserAnnotation(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("spotid") String spotId,
			@QueryParam("docid") String docId,
			@QueryParam("spotter") String spotter,
			@QueryParam("e") String entity, 
			@QueryParam("score") String score) {

		User u;
		UserAnnotation ua = null;
		try {
			u = dao.getUserById(id);

			if (!login(u, id, psw))
				return error("login error: can login user " + id);
			ua = dao.getUserAnnotation(Integer.parseInt(spotId), id, spotter);
			if (ua == null) {
				ua = new UserAnnotation();
				ua.setDocId(Integer.parseInt(docId));
				ua.setSpotterId(spotter);
				ua.setSpotId(Integer.parseInt(spotId));
				ua.setUserId(id);
				// Default score for new record (step1)
				ua.setUserScore(0);
			}

			ua.setEntityId(Integer.parseInt(entity));
			// The score parameters is optional (in step1 we don't use it)
			if (score != null)
				ua.setUserScore(Integer.parseInt(score));
			
			dao.addUserAnnotation(ua);
		} catch (SQLException e) {
			e.printStackTrace();
			return databaseError();
		}
		return ok(ua);

	}
	
	@GET
	@Path("deleteUserAnnotation")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteUserAnnotation(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("spotid") String spotId,
			@QueryParam("docid") String docId,
			@QueryParam("spotter") String spotter) {

		User u;
		UserAnnotation ua = null;
		try {
			u = dao.getUserById(id);

			if (!login(u, id, psw))
				return error("login error: can login user " + id);
			ua = dao.getUserAnnotation(Integer.parseInt(spotId), id, spotter);
			if (ua == null)
				return databaseError();
			dao.deleteUserAnnotation(ua);
		} catch (SQLException e) {
			e.printStackTrace();
			return databaseError();
		}
		return ok(ua);
	}
	
	@GET
	@Path("getCollectionTasks")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getCollectionTask(
			@QueryParam("coll") String collectionId) {

		Map.Entry<String, String> entry = new AbstractMap.SimpleEntry<String, String>(
				"tasks", 
				properties.get("annotation.tasks"));
		return ok(entry);
	}

	@GET
	@Path("createSpot")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createSpot(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docid") String docId,
			@QueryParam("field") String field,
			@QueryParam("start") String start, 
			@QueryParam("end") String end,
			@QueryParam("spot") String mention) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);
		AnnotatedSpot spot = new AnnotatedSpot();
		spot.setDocId(Integer.parseInt(docId));
		spot.setField(field);
		spot.setEnd(Integer.parseInt(end));
		spot.setStart(Integer.parseInt(start));
		spot.setMention(mention);
		spot.setUserId(id);
		spot.setSpotterId("user-defined");
		spot.setEntities(new EntityCandidates());
		try {
			dao.addSpot(spot);
		} catch (SQLException e) {
			e.printStackTrace();
			return databaseError();
		}
		return ok(spot);

	}

	@GET
	@Path("collectionStatus")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response status(@QueryParam("id") String id,
			@QueryParam("psw") String psw,
			@QueryParam("coll") String collectionId) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);

		if (collectionId == null) {
			return error("missing collection id");
		}

		List<DocFreq> freqs = null;
		try {
			freqs = dao.collectionStatus(collectionId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ok(freqs);
	}
	
	@GET
	@Path("leaderBoard")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response leaderBoard(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw,
			@QueryParam("coll") String collectionId) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can't login user " + id);

		if (collectionId == null) {
			return error("missing collection id");
		}
		
		int userId = 0;
//		if (!isAdmin(u))
//			userId = u.getId();

		List<UserStats> userStats = null;
		try {
			userStats = dao.leaderBoard(collectionId, userId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ok(userStats);
	}

	@GET
	@Path("saveProgress")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response saveProgress(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docId") String docId,
			@QueryParam("status") String status) {
		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);
		AnnotationStatus.Status newStatusValue = null;
		try {
			newStatusValue = AnnotationStatus.Status.valueOf(status);
		} catch (Exception e) {
		}
		if (newStatusValue == null) {
			return error("wrong status " + status);
		}
		AnnotationStatus as = null;

		try {
			as = dao.getStatus(id, docId);
		} catch (SQLException e1) {
			databaseError();
		}

		if (as == null) {
			as = new AnnotationStatus(Integer.parseInt(docId), id);
		} else {
			// We need to check that the news status is after the previous one
			// If this happens, we returns the old annotationStatus object (no
			// needs to modify it)
			if (newStatusValue != AnnotationStatus.Status.SKIPPED && as.getStatus().getStepOrder() > newStatusValue.getStepOrder())
				return ok(as);
		}
		
		Action.Type type = null;
		switch(newStatusValue) {
			case SKIPPED:
				type = Action.Type.SAVE_SKIPPED;
				break;
			case STEP1:
				// This case will never happens
				break;
			case STEP2:
				type = Action.Type.SAVE_STEP1;
				break;
			case DONE:
				type = Action.Type.SAVE_STEP2;
				break;
		}
		Action action = new Action(Integer.parseInt(docId), u.getId(), type);
		

		as.setStatus(newStatusValue);
		try {
			dao.addStatus(as);
			dao.saveAction(action);
		} catch (SQLException e) {
			databaseError();
		}
		return ok(as);
	}

	@GET
	@Path("saveComment")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response saveComment(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docId") String docId,
			@QueryParam("status") String status,
			@QueryParam("comment") String comment) {
		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);
		AnnotationStatus.Status statusValue = null;
		try {
			statusValue = AnnotationStatus.Status.valueOf(status);
		} catch (Exception e) {
		}
		if (statusValue == null) {
			return error("wrong status " + status);
		}

		AnnotationStatus s = null;

		try {
			s = dao.getStatus(id, docId);
		} catch (SQLException e1) {
			databaseError();
		}

		if (s == null) {
			return error("cannot find doc " + docId + " for user " + id);
		}
		if (statusValue == AnnotationStatus.Status.STEP1) {
			s.setCommentsStep1(comment);
		} else if (statusValue == AnnotationStatus.Status.STEP2) {
			s.setCommentsStep2(comment);
		} else {
			return error("wrong status " + status);
		}
		try {
			dao.addStatus(s);
		} catch (SQLException e) {
			return databaseError();
		}
		return ok(s);
	}

	@GET
	@Path("annotationStatus")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response userStatus(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw, 
			@QueryParam("docid") String userId,
			@QueryParam("docId") String docId) {
		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e) {
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);
		AnnotationStatus as = null;

		try {
			as = dao.getStatus(userId, docId);
		} catch (SQLException e1) {
			databaseError();
		}

		return ok(as);
	}

	@GET
	@Path("userAnnotations")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response userAnnotations(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw,
			@QueryParam("coll") String collectionId) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);

		boolean isAdmin = isAdmin(u);
		List<AnnotationStatus> annotations = null;
		if (collectionId == null) {
			return error("missing collection id");
		}
		try {
			if (isAdmin) {
				annotations = dao.getAllUserAnnotations(false);
			} else {
				annotations = dao.getUserAnnotations(id);
			}
		} catch (SQLException e) {
			return databaseError();
		}
		return ok(annotations);
	}

	@GET
	@Path("next")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response nextDoc(
			@QueryParam("id") String id,
			@QueryParam("psw") String psw,
			@QueryParam("coll") String collectionId) {

		User u;
		try {
			u = dao.getUserById(id);
		} catch (SQLException e1) {
			e1.printStackTrace();
			return databaseError();
		}
		if (!login(u, id, psw))
			return error("login error: can login user " + id);

		int bucketSize = Integer.parseInt(properties.get("bucket.size"));
		int documentAgreement = Integer.parseInt(properties
				.get("document.agreement"));

		if (collectionId == null) {
			return error("missing collection id");
		}

		try {
			List<AnnotationStatus> pendingAnnotations = dao.getPendingAnnotations(id);
			if (pendingAnnotations != null && pendingAnnotations.size() > 0) {
				// We select the last pending annotation of the user
				return ok(pendingAnnotations.get(0));
			}
		} catch (SQLException e) {
			return databaseError();
		}

		// need the next document
		Set<Integer> annotatedDocuments = new HashSet<Integer>();
		List<DocFreq> freqs = null;
		try {
			// Retrieve the annotation frequency of each document in the collection (docId, freq, avgSpots)
			freqs = dao.collectionStatus(collectionId);
			// Retrieve all the annotations made by the current user
			List<AnnotationStatus> ad = dao.getUserAnnotations(id);
			for (AnnotationStatus as : ad) {
				annotatedDocuments.add(as.getDocId());
			}
		} catch (SQLException e) {
			return databaseError();
		}

		int bucketIdx = 0;
		List<Integer> availableDocuments = new ArrayList<Integer>();
		Integer lowestFreq;
		int numBuckets = (int) Math.ceil((float) freqs.size() / bucketSize);
		do {
			// Reset the availableDocuments list and min counter
			availableDocuments.clear();
			lowestFreq = Integer.MAX_VALUE;

			// We calculate the start and end index of the bucket
			int fromIndex = bucketIdx < numBuckets ? bucketSize * bucketIdx : freqs.size();
			int toIndex = fromIndex + bucketSize < freqs.size() ? fromIndex + bucketSize : freqs.size();
			
			// The frequencies are already ordered by descending frequency, docId descending
			// We should look only at documents inside the bucket. We use the subList view.
			for (DocFreq df : freqs.subList(fromIndex, toIndex)) {
				if (annotatedDocuments.contains(df.getDocId())) {
					// the user yet annotated this document
					continue;
				}
				
				if (df.getFreq() > lowestFreq) {
					// there are documents with lower frequency
					continue;
				}
				
				if (df.getFreq() == lowestFreq) {
					// ok, adding this document to the available documents
					availableDocuments.add(df.getDocId());
				}

				if (df.getFreq() < lowestFreq) {
					// we found a document with lower frequency,
					// reset the available documents and restart;
					availableDocuments.clear();
					lowestFreq = df.getFreq();
					availableDocuments.add(df.getDocId());
				}
			}

		} while (++bucketIdx < numBuckets && lowestFreq >= documentAgreement);

		// If each document is annotated by documentAgreement users, we increase
		// the agreement
		// and provide to the user the next doc according to the new agreement
		// value
		if (bucketIdx > numBuckets || lowestFreq >= documentAgreement) {
			documentAgreement++;
			return nextDoc(id, psw, collectionId);
		}

		int randomIdx = (int) (Math.random() * availableDocuments.size());
		int docId = availableDocuments.get(randomIdx);
		AnnotationStatus annotationStatus = new AnnotationStatus(docId, id);
		
		// If the task is to only set the relevance of the entities, we have to 
		// manually create some records related to the step1 (entity linking step)
		if (properties.has("annotation.tasks") && properties.get("annotation.tasks").equals("step2")) {
			
			List<AnnotatedSpot> spots;
			try {
				spots = dao.getSpots(id, docId);
			
				for (AnnotatedSpot spot : spots) {
					
					UserAnnotation ua = new UserAnnotation();
					ua.setDocId(docId);
					ua.setSpotterId(spot.getSpotterId());
					ua.setSpotId(spot.getSpotId());
					ua.setUserId(id);
					// Default score for new record (step1)
					ua.setUserScore(0);
					// Set the first (and usually only) candidate entity from the candidate list 
					ua.setEntityId(spot.getEntities().getCandidates().get(0).getEntity());
	
					dao.addUserAnnotation(ua);
				}
				
			}catch (SQLException e) {
				logger.error("Automatic creation of step1 record failed!");
				return databaseError();
			}
			
			annotationStatus.status = annotationStatus.status.STEP2;
		}		

		try {
			dao.addStatus(annotationStatus);
		} catch (SQLException e) {
			return databaseError();
		}
		return ok(annotationStatus);

	}
}

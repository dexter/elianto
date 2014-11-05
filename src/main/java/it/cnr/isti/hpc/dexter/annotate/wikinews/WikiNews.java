/**
 *  Copyright 2014 Diego Ceccarelli
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
package it.cnr.isti.hpc.dexter.annotate.wikinews;

import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates;
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent.DocumentField;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.io.reader.Filter;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.property.ProjectProperties;
import it.cnr.isti.hpc.wikipedia.article.Article;
import it.cnr.isti.hpc.wikipedia.article.ArticleSummarizer;
import it.cnr.isti.hpc.wikipedia.article.Link;
import it.cnr.isti.hpc.wikipedia.article.Template;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Jun 20, 2014
 */
public class WikiNews extends Article {

	List<Link> newsLinks = new ArrayList<Link>();
	static Gson gson = new Gson();
	ArticleSummarizer cleaner = new ArticleSummarizer();
	private String dateline;

	private static final Logger logger = LoggerFactory
			.getLogger(WikiNews.class);

	private static ProjectProperties properties = new ProjectProperties(
			WikiNews.class);

	public WikiNews() {

	}

	public List<Link> getNewsLinks() {
		return newsLinks;
	}

	public void setNewsLinks(List<Link> newsLinks) {
		this.newsLinks = newsLinks;
	}

	public static String removeTemplates(String s) {
		int status = 0;
		StringBuilder text = new StringBuilder();
		StringBuilder textBuffer = new StringBuilder();
		StringBuilder buffer = new StringBuilder();
		List<String> elems = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			// System.out.println(c + "\t" + status);
			textBuffer.append(c);
			if (status == 0 && c == 'T') {
				status++;
				continue;
			}
			if (status == 1 && c == 'E') {
				status++;
				continue;
			}
			if ((status == 2 || status == 7) && c == 'M') {
				status = 3;
				continue;
			}
			if (status == 3 && c == 'P') {
				status++;
				continue;
			}
			if (status == 4 && c == 'L') {
				status++;
				continue;
			}
			if (status == 5 && c == 'A') {
				status++;
				continue;
			}
			if (status == 6 && c == 'T') {
				status++;
				continue;
			}
			if (status == 7 && c == 'E') {
				status++;
				continue;
			}

			if (status == 8 && c == '[') {
				status++;
				continue;
			}
			if (status == 9 && c != ',' && c != ']') {
				buffer.append(c);
				continue;
			}
			if (status == 9 && c == ',') {
				elems.add(buffer.toString().trim());
				buffer.setLength(0);
				continue;

			}
			if (status == 9 && c == ']') {
				// end of the template
				elems.add(buffer.toString().trim());
				buffer.setLength(0);
				textBuffer.setLength(0);
				// System.out.println("TEMPLATE " + elems);
				String type = elems.get(0);
				// if (type.equals("QuoteLeft")) {
				// textBuffer.append("'''").append(elems.get(1)).append("'''");
				// }
				// if (type.equals("QuoteLeft")) {
				// textBuffer.append("'''").append(elems.get(1)).append("'''");
				// }
				if (type.equalsIgnoreCase("w")) {
					if (elems.size() == 3) {
						textBuffer.append(elems.get(2));
					} else {
						for (int k = 1; k < elems.size(); k++) {
							textBuffer.append(elems.get(k));
							if (k < elems.size() - 1)
								textBuffer.append(", ");
						}
					}
				}
				if (type.equals("date")) {
					for (int k = 1; k < elems.size(); k++) {
						textBuffer.append(elems.get(k));
						if (k < elems.size() - 1)
							textBuffer.append(", ");
					}
				}

				if (type.equals("byline")) {
					for (int k = 1; k < elems.size(); k++) {
						String[] f = elems.get(k).split("=");
						if (f.length == 1) {
							textBuffer.append(", " + f[0]);
							continue;
						}
						if (f[0].equals("date")) {
							textBuffer.append(f[1]);
							// headline ?
						}
						if (f[0].equals("location")
								&& !f[1].equals("(TEMPLATE)")) {
							textBuffer.append(f[1]);
						}

					}

				}
				text.append(textBuffer.toString());
				textBuffer.setLength(0);
				elems.clear();
				status = 0;

				continue;
			}
			text.append(textBuffer.toString());
			status = 0;

			textBuffer.setLength(0);

		}
		text.append(textBuffer.toString());
		status = 0;
		return text.toString();
	}

	private List<Link> getWikiLink() {
		List<Link> wikiLinks = new ArrayList<Link>();
		for (Template t : getTemplates()) {
			if (t.getName().equals("w")) {
				List<String> values = t.getDescription();
				String name = values.get(0);
				String desc = values.get(0);
				if (values.size() > 1) {
					desc = values.get(1);
				}
				Link l = new Link(name, desc);
				wikiLinks.add(l);
			}
		}
		return wikiLinks;
	}

	public Document asAnnotatedDocument() {
		Document d = new Document();
		DocumentContent dc = new DocumentContent();
		d.setTemplate("{{headline}} <br/> {{p}}");
		dc.addField("headline", getTitle());
		dc.addField("dateline", dateline);
		int i = 0;
		for (String p : getParagraphs()) {
			p = removeTemplates(p);
			if (p.trim().isEmpty())
				continue;
			dc.addField(String.format("body_par_%03d", i),
				cleaner.cleanWikiText(p));
			i++;
		}
		d.setDocument(dc);
		d.setDocId(getWikiId());
		d.setCollectionId("wikinews");
		d.setExternalId("wikinews" + getWikiId());

		return d;
	}

	private List<AnnotatedSpot> annotate(DocumentContent.DocumentField field,
			String anchor, int id) {
		String text = field.getValue();
		int start = -1;
		List<AnnotatedSpot> spots = new LinkedList<AnnotatedSpot>();
		while ((start = text.indexOf(anchor, start + 1)) >= 0) {
			AnnotatedSpot spot = new AnnotatedSpot();

			spot.setStart(start);
			spot.setEnd(start + anchor.length());
			spot.setMention(anchor);

			spot.setField(field.getName());
			spot.setSpotterId("std");
			EntityCandidates entities = new EntityCandidates();
			entities.add(id, 0);

			spot.setEntities(entities);
			spots.add(spot);
		}
		return spots;
	}

	public List<AnnotatedSpot> getAnnotatedSpot() {
		Document document = asAnnotatedDocument();
		DocumentContent content = document.getDocument();
		List<AnnotatedSpot> spots = new ArrayList<AnnotatedSpot>();
		Set<String> annotatedSpots = new HashSet<String>();
		Map<String, Integer> wikinameToWikiid = convertWikiNamesToWikiIds();
		for (DocumentContent.DocumentField df : content.getContent()) {
			if (df.getValue() == null) {
				logger.warn("field {} is null, ignoring", df.getName());
				continue;
			}

			for (Map.Entry<String, Integer> entity : wikinameToWikiid
					.entrySet()) {

				spots.addAll(annotate(df, entity.getKey(), entity.getValue()));

			}

		}
		for (AnnotatedSpot s : spots) {
			s.setDocId(document.getDocId());
			annotatedSpots.add(s.getMention());
		}
		if (annotatedSpots.size() != wikinameToWikiid.size()) {
			logger.warn("docuemnt {}", document.getDocId());
			logger.warn(
					"annotated spots {} does not match annotations {}, SKIPPING",
					annotatedSpots.size(), wikinameToWikiid.size());

			// System.out.println("DOCUMENT");
			// System.out.println(document.getDocument());
			//
			// System.out.println("ANNOTATED SPOT");
			//
			// System.out.println(annotatedSpots);
			// System.out.println("WIKINAMES");
			// System.out.println(wikinameToWikiid);
			return null;

		}
		return spots;

	}

	public Map<String, Integer> convertWikiNamesToWikiIds() {

		DexterRestClient client = null;
		Map<String, Integer> entities = new HashMap<String, Integer>();
		try {
			client = new DexterRestClient(properties.get("dexter.rest.api"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (Link l : getLinks()) {
			int id = client.getId(Article.getTitleInWikistyle(l.getCleanId()));
			if (id <= 0) {
				logger.warn("cannot convert {} ", l.getCleanId());
				return null;
			} else {
				entities.put(l.getDescription(), id);
			}
		}

		for (Link l : getNewsLinks()) {
			int id = client.getId(Article.getTitleInWikistyle(l.getCleanId()));
			if (id <= 0) {
				logger.warn("cannot convert NEWS id {} ", l.getCleanId());
				return null;
			} else {
				entities.put(l.getDescription(), id);
			}
		}
		return entities;

	}

	public static void main(String[] args) {
		RecordReader<WikiNews> reader = new RecordReader<WikiNews>(
				"/tmp/news.json.gz", WikiNews.class)
				.filter(new WikiNewsFilter());
		int i = 0;
		int ok = 0;
		for (WikiNews news : reader) {
			System.out.println("$YEAR: " + news.getTimestamp());
			System.out.println("(" + (i++) + ") title " + news.getTitle());
			if (news.convertWikiNamesToWikiIds() != null) {
				List<AnnotatedSpot> spots = news.getAnnotatedSpot();
				if (spots == null)
					continue;
				for (AnnotatedSpot s : spots) {
					System.out.println(s);
				}
				ok++;
				System.out.println("OK = " + ok);
			}
			// for (String p : news.getParagraphs()) {
			// System.out.println(" - " + p);
			// System.out.println(" ** " + removeTemplates(p));
			// }
			// System.out.println();
			// System.out.println();
			//
			// for (Link l : news.getLinks()) {
			// System.out.println("\t" + l);
			// }
			// System.out.println("---- link wikinews");
			// for (Link l : news.getNewsLinks()) {
			// System.out.println("\t" + l);
			// }
			System.out.println();
			System.out.println();
		}
	}

	public static class WikiNewsFilter implements Filter<WikiNews> {

		@Override
		public boolean isFilter(WikiNews news) {
			news.setNewsLinks(news.getLinks());
			news.setLinks(news.getWikiLink());
			if (news.getType() != Article.Type.ARTICLE)
				return true;
			if (news.getTitle().contains("News briefs:")) {
				return true;
			}
			if (news.getTitle().contains("Wikinews Shorts:")) {
				return true;
			}
			Set<String> entities = new HashSet<String>();
			for (Link s : news.getLinks()) {
				entities.add(s.getCleanId());
			}
			for (Link s : news.getNewsLinks()) {
				entities.add(s.getCleanId());
			}

			if (entities.size() < 10 || entities.size() > 25) {
				return true;
			}
			
			int len = 0;
			for (String p : news.getParagraphs()) {
				p = removeTemplates(p);
				if (p.trim().isEmpty())
					continue;
				len += news.cleaner.cleanWikiText(p).length();
			}
			if (len > 2500)
				return true;
			
			return false;
		}
	}

}

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
package it.cnr.isti.hpc.dexter.annotate.util;

import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent;

import java.util.List;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class ConllDocument {

	private static Gson gson = new Gson();

	Document document;

	String external_id;
	String internal_id;
	String collection;

	public ConllDocument() {

	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public String getExternal_id() {
		return external_id;
	}

	public void setExternal_id(String external_id) {
		this.external_id = external_id;
	}

	public String getInternal_id() {
		return internal_id;
	}

	public void setInternal_id(String internal_id) {
		this.internal_id = internal_id;
	}

	public String getCollection() {
		return collection;
	}

	public DocumentContent getContent() {
		DocumentContent content = new DocumentContent();
		content.addField("title", document.getTitle());
		content.addField("headline", document.getHeadline());
		content.addField("byline", document.getByline());
		content.addField("dateline", document.getDateline());
		int i = 0;
		for (String s : document.getText()) {
			content.addField(String.format("body_par_%03d", i++), s);
		}

		return content;
	}

	public void setCollection(String collection) {
		this.collection = collection;
	}

	public static class FlatDocument {
		String headline;
		String text;
		String byline;
		String dateline;
		String title;

		public FlatDocument(Document d) {
			this.headline = d.getHeadline();
			this.byline = byline;
			this.dateline = dateline;
			this.title = title;
			StringBuilder sb = new StringBuilder();
			for (String s : d.getText()) {
				sb.append(s);
			}
			this.text = sb.toString();
		}
	}

	public static class Document {
		String headline;
		List<String> text;
		String byline;
		String dateline;
		String title;

		public String getHeadline() {
			return headline;
		}

		public void setHeadline(String headline) {
			this.headline = headline;
		}

		public List<String> getText() {
			return text;
		}

		public void setText(List<String> text) {
			this.text = text;
		}

		public String getByline() {
			return byline;
		}

		public void setByline(String byline) {
			this.byline = byline;
		}

		public String getDateline() {
			return dateline;
		}

		public void setDateline(String dateline) {
			this.dateline = dateline;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

	}

}

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
package it.cnr.isti.hpc.dexter.annotate.bean;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 16, 2013
 */

@DatabaseTable(tableName = "annotationStatus")
public class AnnotationStatus {

	public enum Status {
		SKIPPED(0), STEP1(0), STEP2(1), DONE(2);
		
		private int stepOrder;
		private Status(int stepOrder) {
			this.stepOrder = stepOrder;
		}
		
		public int getStepOrder() {
			return this.stepOrder;
		}
	};

	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(foreign=true, canBeNull=true, columnName="docId")
	private Document doc = null;

	@DatabaseField()
	private long timestamp;

	@DatabaseField(foreign=true, canBeNull=true, columnName="userId")
	private User user;

	@DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING, format = "UTF-8")
	private String commentsStep1;

	@DatabaseField(canBeNull = true, dataType = DataType.LONG_STRING, format = "UTF-8")
	private String commentsStep2;

	@DatabaseField(canBeNull = false)
	public Status status = Status.STEP1;

	public AnnotationStatus() {
		super();
		this.doc = new Document();
		this.user = new User();
	}

	public AnnotationStatus(int docId, String userId) {
		this();
		doc.setDocId(docId);
		user.setId(Integer.parseInt(userId));
		this.timestamp = System.currentTimeMillis();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getDocId() {
		return doc.getDocId();
	}

	public void setDocId(int docId) {
		doc.setDocId(docId);
	}

	public String getUserId() {
		return String.valueOf(user.getId());
	}

	public void setUserId(String userId) {
		user.setId(Integer.parseInt(userId));
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getCommentsStep1() {
		return commentsStep1;
	}

	public void setCommentsStep1(String commentsStep1) {
		this.commentsStep1 = commentsStep1;
	}

	public String getCommentsStep2() {
		return commentsStep2;
	}

	public void setCommentsStep2(String commentsStep2) {
		this.commentsStep2 = commentsStep2;
	}

}

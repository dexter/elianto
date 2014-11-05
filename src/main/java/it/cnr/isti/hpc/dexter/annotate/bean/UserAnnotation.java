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

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 16, 2013
 */

@DatabaseTable(tableName = "user-annotation")
public class UserAnnotation {

	@DatabaseField(generatedId = true)
	private int annotationId;
	@DatabaseField(foreign=true, canBeNull=true, columnName="userId")
	private User user;
	@DatabaseField(canBeNull = false)
	private int spotId;

	@DatabaseField(canBeNull = false)
	private String spotterId;
	@DatabaseField
	private int entityId;
	@DatabaseField(canBeNull = false)
	private int userScore;
	
	@DatabaseField(foreign=true, canBeNull=true, columnName="docId")
	private Document doc = null;

	@DatabaseField(canBeNull = false)
	private long timestamp;
	@DatabaseField(canBeNull = false)
	private long lastModified;
	
	public UserAnnotation() {
		this.user = new User();
		this.doc = new Document();
	}

	public int getAnnotationId() {
		return annotationId;
	}

	public int getEntityId() {
		return entityId;
	}

	public void setEntityId(int entityId) {
		this.entityId = entityId;
	}

	public int getUserScore() {
		return userScore;
	}

	public void setUserScore(int userScore) {
		this.userScore = userScore;
	}

	public void setAnnotationId(int annotationId) {
		this.annotationId = annotationId;

	}

	public String getUserId() {
		return String.valueOf(user.getId());
	}

	public void setUserId(String userId) {
		user.setId(Integer.parseInt(userId));
	}

	public int getSpotId() {
		return spotId;
	}

	public void setSpotId(int spotId) {
		this.spotId = spotId;
	}

	public int getDocId() {
		return doc.getDocId();
	}

	public void setDocId(int docId) {
		doc.setDocId(docId);
	}

	public String getSpotterId() {
		return spotterId;
	}

	public void setSpotterId(String spotterId) {
		this.spotterId = spotterId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

}

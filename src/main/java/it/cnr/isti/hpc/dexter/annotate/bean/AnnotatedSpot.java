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

@DatabaseTable(tableName = "spots")
public class AnnotatedSpot {

	@DatabaseField(generatedId = true)
	private int spotId;
	@DatabaseField(canBeNull = false)
	private int docId;
	@DatabaseField(canBeNull = false)
	private String spotterId;

	@DatabaseField(canBeNull = false, format = "UTF-8")
	private String mention;
	@DatabaseField
	private int start;
	@DatabaseField
	private int end;
	@DatabaseField(canBeNull = false)
	private String field;
	@DatabaseField(canBeNull = false, dataType = DataType.SERIALIZABLE)
	private EntityCandidates entities;

	private UserAnnotation annotation;

	// == null for all the users, userId -> private spot
	@DatabaseField(canBeNull = true)
	private String userId;

	public AnnotatedSpot() {
		super();
	}

	public int getSpotId() {
		return spotId;
	}

	public void setSpotId(int spotId) {
		this.spotId = spotId;
	}

	public int getDocId() {
		return docId;
	}

	public void setDocId(int docId) {
		this.docId = docId;
	}

	public String getSpotterId() {
		return spotterId;
	}

	public void setSpotterId(String spotterId) {
		this.spotterId = spotterId;
	}

	public String getMention() {
		return mention;
	}

	public void setMention(String mention) {
		this.mention = mention;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public EntityCandidates getEntities() {
		return entities;
	}

	public void setEntities(EntityCandidates entities) {
		this.entities = entities;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public UserAnnotation getAnnotation() {
		return annotation;
	}

	public void setAnnotation(UserAnnotation annotation) {
		this.annotation = annotation;
	}

	@Override
	public String toString() {
		return "AnnotatedSpot [spotId=" + spotId + ", docId=" + docId
				+ ", spotterId=" + spotterId + ", mention=" + mention
				+ ", start=" + start + ", end=" + end + ", field=" + field
				+ ", entities=" + entities + ", annotation=" + annotation
				+ ", userId=" + userId + "]";
	}

	public boolean overlaps(AnnotatedSpot s) {
		if (!field.equals(s.getField())) {
			return false;
		}
		boolean startOverlap = ((s.getStart() >= this.getStart()) && (s
				.getStart() <= this.getEnd()));
		if (startOverlap)
			return true;
		boolean endOverlap = ((s.getEnd() >= this.getStart()) && (s.getEnd() <= this
				.getEnd()));
		return endOverlap;
	}

}

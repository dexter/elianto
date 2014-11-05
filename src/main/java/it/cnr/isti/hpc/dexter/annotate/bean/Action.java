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
 * @author Salvatore Trani <salvatore.trani@isti.cnr.it>
 * 
 *         Created on May 21, 2014
 */

@DatabaseTable(tableName = "actions")
public class Action {

	public enum Type {
		GET_DOCUMENT, SAVE_STEP1, SAVE_STEP2, SAVE_SKIPPED;
	};

	@DatabaseField(generatedId = true)
	int id;

	@DatabaseField(foreign = true, canBeNull = false, columnName = "docId")
	private Document doc;
	
	@DatabaseField(foreign = true, canBeNull = false, columnName = "userId")
	private User user;

	@DatabaseField()
	private long timestamp;
	
//	@DatabaseField()
//	private String coll;

	@DatabaseField(canBeNull = false)
	private Type type;
	
	public Action() {
		this.doc = new Document();
		this.user = new User();
	}

	public Action(int docId, int userId, Type type) {
		this();
		this.doc.setDocId(docId);
		this.user.setId(userId);
		this.type = type;
		this.timestamp = System.currentTimeMillis();
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Document getDoc() {
		return doc;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
	
//	public String getColl() {
//	return coll;
//}
//
//public void setColl(String coll) {
//	this.coll = coll;
//}
	
}

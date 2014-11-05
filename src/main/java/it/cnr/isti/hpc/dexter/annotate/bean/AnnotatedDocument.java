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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 16, 2013
 */
public class AnnotatedDocument {
	Document document;
	List<AnnotatedSpot> spots;
	Map<Integer, EntityDescription> description;

	public AnnotatedDocument(Document document, List<AnnotatedSpot> spots) {
		super();
		this.document = document;
		this.spots = spots;
		description = new HashMap<Integer, EntityDescription>();
	}

	public void addDescription(EntityDescription desc) {
		description.put(desc.getId(), desc);
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public List<AnnotatedSpot> getSpots() {
		return spots;
	}

	public void setSpots(List<AnnotatedSpot> spots) {
		this.spots = spots;
	}

	public Map<Integer, EntityDescription> getDescription() {
		return description;
	}

	public void setDescription(Map<Integer, EntityDescription> description) {
		this.description = description;
	}

}

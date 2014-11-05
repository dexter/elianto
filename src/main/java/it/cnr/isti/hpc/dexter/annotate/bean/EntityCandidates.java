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
package it.cnr.isti.hpc.dexter.annotate.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Jan 2, 2014
 */
public class EntityCandidates implements Serializable {

	public List<EntityAnnotation> candidates = new ArrayList<EntityAnnotation>();

	public EntityCandidates() {

	}

	public void add(int entity, int score) {
		EntityAnnotation ea = new EntityAnnotation(entity, score);
		if (candidates.contains(ea)) {
			candidates.remove(ea);
		}
		candidates.add(ea);
	}

	public List<EntityAnnotation> getCandidates() {
		return candidates;
	}

	public void setCandidates(List<EntityAnnotation> candidates) {
		this.candidates = candidates;
	}

	public static class EntityAnnotation implements Serializable {
		int entity;
		int score;

		public EntityAnnotation(int entity, int score) {
			this.entity = entity;
			this.score = score;
		}

		@Override
		public String toString() {
			return "EntityAnnotation [entity=" + entity + ", score=" + score
					+ "]";
		}

		public int getEntity() {
			return entity;
		}

		public void setEntity(int entity) {
			this.entity = entity;
		}

		public int getScore() {
			return score;
		}

		public void setScore(int score) {
			this.score = score;
		}

		@Override
		public int hashCode() {
			return entity;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			EntityAnnotation other = (EntityAnnotation) obj;
			if (entity != other.entity)
				return false;

			return true;
		}

	}

}

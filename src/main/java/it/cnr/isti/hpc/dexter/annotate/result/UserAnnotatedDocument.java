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
package it.cnr.isti.hpc.dexter.annotate.result;

import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.UserAnnotation;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Apr 21, 2014
 */
public class UserAnnotatedDocument {

	Document document;
	List<Annotation> annotations;

	public UserAnnotatedDocument(Document document) {
		this.document = document;
		annotations = new LinkedList<Annotation>();
	}

	public void addAnnotation(AnnotatedSpot spot, UserAnnotation ua) {
		annotations.add(new Annotation(spot, ua));
	}

	public void addAnnotations(AnnotatedSpot spot, List<UserAnnotation> ua) {
		annotations.add(new Annotation(spot, ua));
	}
	
	public Document getDocument() {
		return document;
	}
	
	public List<Annotation> getAnnotations() {
		return annotations;
	}

	public class Annotation {
		public AnnotatedSpot spot;
		public List<UserAnnotation> annotations;

		public Annotation(AnnotatedSpot spot, UserAnnotation ua) {
			this.spot = spot;
			annotations = new LinkedList<UserAnnotation>();
			annotations.add(ua);
		}

		public Annotation(AnnotatedSpot spot, List<UserAnnotation> ua) {
			this.spot = spot;
			annotations = ua;
		}

		public void add(UserAnnotation ua) {
			annotations.add(ua);
		}

		public AnnotatedSpot getAnnotatedSpot() {
			return spot;
		}
		
		public List<UserAnnotation> getUserAnnotations() {
			return annotations;
		}
	}

}

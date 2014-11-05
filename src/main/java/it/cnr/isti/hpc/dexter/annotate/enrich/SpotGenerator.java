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
package it.cnr.isti.hpc.dexter.annotate.enrich;

import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.dexter.rest.domain.CandidateEntity;
import it.cnr.isti.hpc.dexter.rest.domain.CandidateSpot;
import it.cnr.isti.hpc.dexter.rest.domain.SpottedDocument;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Jun 21, 2014
 */
public class SpotGenerator {

	private static final Logger logger = LoggerFactory
			.getLogger(SpotGenerator.class);

	private final String name;

	private DexterRestClient client = null;

	public SpotGenerator(String name) {
		this.name = name;

		try {
			client = new DexterRestClient(
					"http://node5.novello.isti.cnr.it:8080/dexter-webapp/api/rest");
			client.setWikinames(true);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<AnnotatedSpot> getAnnotatedSpot(Document document) {

		DocumentContent content = document.getDocument();
		List<AnnotatedSpot> spots = new ArrayList<AnnotatedSpot>();
		for (DocumentContent.DocumentField df : content.getContent()) {
			if (df.getValue() == null) {
				logger.warn("field {} is null, ignoring", df.getName());
				continue;
			}
			SpottedDocument sd = client.spot(df.getValue());
			for (CandidateSpot cs : sd.getSpots()) {
				AnnotatedSpot spot = new AnnotatedSpot();
				spot.setDocId(document.getDocId());
				spot.setStart(cs.getStart());
				spot.setEnd(cs.getEnd());
				spot.setMention(cs.getMention());
				spot.setField(df.getName());
				spot.setSpotterId(name);
				EntityCandidates entities = new EntityCandidates();
				for (CandidateEntity e : cs.getCandidates()) {
					entities.add(e.getEntity(), 0);
				}

				spot.setEntities(entities);
				spots.add(spot);
			}

		}
		return spots;

	}

}

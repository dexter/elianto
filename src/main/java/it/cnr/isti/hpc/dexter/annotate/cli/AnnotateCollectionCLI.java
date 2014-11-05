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
package it.cnr.isti.hpc.dexter.annotate.cli;

import it.cnr.isti.hpc.cli.AbstractCommandLineInterface;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.dexter.rest.domain.CandidateEntity;
import it.cnr.isti.hpc.dexter.rest.domain.CandidateSpot;
import it.cnr.isti.hpc.dexter.rest.domain.SpottedDocument;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class AnnotateCollectionCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { INPUT, OUTPUT, "spotter" };
	private static final Logger logger = LoggerFactory
			.getLogger(AnnotateCollectionCLI.class);

	public AnnotateCollectionCLI(String[] args) {
		super(
				args,
				params,
				"java -jar $jar "
						+ AnnotateCollectionCLI.class
						+ " -input collection-name -output annoteted-spots.json[.gz]");
	}

	public static void main(String[] args) {

		AnnotateCollectionCLI cli = new AnnotateCollectionCLI(args);

		SqliteDao dao = SqliteDao.getInstance();
		Iterator<Document> iterator = null;
		try {
			iterator = dao.getDocumentIterator(cli.getInput());
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(-1);
		}
		String spotter = "std";
		DexterRestClient client = null;
		try {
			client = new DexterRestClient(
					"http://node5.novello.isti.cnr.it:8080/dexter-webapp/api/rest");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.openOutput();
		if (!iterator.hasNext()) {
			logger.error("no documents");
			System.exit(-1);
		}
		ProgressLogger pl = new ProgressLogger("indexed {} documents", 10);

		do {
			Document document = iterator.next();
			pl.up();
			DocumentContent content = document.getDocument();
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
					spot.setSpotterId(spotter);
					EntityCandidates entities = new EntityCandidates();
					for (CandidateEntity e : cs.getCandidates()) {
						entities.add(e.getEntity(), 0);
					}

					spot.setEntities(entities);
					cli.writeLineInOutput(gson.toJson(spot));
				}

			}

		} while (iterator.hasNext());

		cli.closeOutput();

	}
}

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
import it.cnr.isti.hpc.dexter.annotate.bean.EntityCandidates.EntityAnnotation;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityDescription;
import it.cnr.isti.hpc.dexter.article.ArticleDescription;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class RetrieveEntityDescriptionCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { INPUT, OUTPUT };
	private static final Logger logger = LoggerFactory
			.getLogger(RetrieveEntityDescriptionCLI.class);

	public RetrieveEntityDescriptionCLI(String[] args) {
		super(
				args,
				params,
				"java -jar $jar "
						+ RetrieveEntityDescriptionCLI.class
						+ " -input annotated-spots.json -output entity-description.json.gz");
	}

	public static void main(String[] args) {

		RetrieveEntityDescriptionCLI cli = new RetrieveEntityDescriptionCLI(
				args);
		RecordReader<AnnotatedSpot> reader = new RecordReader<AnnotatedSpot>(
				cli.getInput(), AnnotatedSpot.class);

		DexterRestClient client = null;
		try {
			client = new DexterRestClient(
					"http://node5.novello.isti.cnr.it:8080/dexter-webapp/api/rest");
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cli.openOutput();
		ProgressLogger pl = new ProgressLogger("processed {} spots", 1000);
		Set<Integer> entities = new HashSet<Integer>();
		for (AnnotatedSpot spot : reader) {
			pl.up();
			for (EntityAnnotation ea : spot.getEntities().getCandidates()) {
				entities.add(ea.getEntity());
			}

		}
		logger.info("identified {} distinct entities", entities.size());
		pl = new ProgressLogger("processed {} entities", 100);
		for (Integer e : entities) {
			pl.up();
			ArticleDescription desc = client.getDesc(e);
			EntityDescription ed = new EntityDescription(e);
			ed.setDescription(desc.getDescription());
			ed.setImage(desc.getImage());
			ed.setTitle(desc.getTitle());
			ed.setUrl(desc.getUrl());
			cli.writeLineInOutput(gson.toJson(ed));
		}
		cli.closeOutput();

	}
}

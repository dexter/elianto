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
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent.DocumentField;
import it.cnr.isti.hpc.dexter.annotate.wikinews.WikiNews;
import it.cnr.isti.hpc.dexter.annotate.wikinews.WikiNews.WikiNewsFilter;
import it.cnr.isti.hpc.io.IOUtils;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class AnnotateWikiNewsJsonCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { INPUT, "documents", "spots" };
	private static final Logger logger = LoggerFactory
			.getLogger(AnnotateWikiNewsJsonCLI.class);

	public AnnotateWikiNewsJsonCLI(String[] args) {
		super(
				args,
				params,
				"java -jar $jar "
						+ AnnotateWikiNewsJsonCLI.class
						+ " -input wikinews.json -documents documents.json -spots annotated-spots.json ");
	}

	public static void main(String[] args) throws IOException {

		AnnotateWikiNewsJsonCLI cli = new AnnotateWikiNewsJsonCLI(args);
		BufferedWriter docsOut = IOUtils.getPlainOrCompressedUTF8Writer(cli.getParam("documents"));
		BufferedWriter spotsOut = IOUtils.getPlainOrCompressedUTF8Writer(cli.getParam("spots"));
		RecordReader<WikiNews> reader = new RecordReader<WikiNews>(
				cli.getInput(), WikiNews.class).filter(new WikiNewsFilter());
		ProgressLogger pl = new ProgressLogger("{} news dumped", 1);
		int id = 1;
		for (WikiNews news : reader) {

			if (news.convertWikiNamesToWikiIds() != null) {
				List<AnnotatedSpot> spots = news.getAnnotatedSpot();
				if (spots == null)
					continue;
				for (AnnotatedSpot s : spots) {
					s.setDocId(id);
					spotsOut.write(gson.toJson(s));
					spotsOut.newLine();
				}
				Document doc = news.asAnnotatedDocument();
				doc.setDocId(id);
				doc.setCollectionId("conll");
				docsOut.write(gson.toJson(doc));
				docsOut.newLine();
				pl.up();
				
				int len = 0;
				for (DocumentField df: doc.getDocument().getContent()) {
					if (df.getName().startsWith("body_par_"))
						len += df.getValue().length();
				}
				
				// used to generate just 10 news
				// if (pl.getStatus() == 10)
				// break;
				
				id++;
			}
		}

		docsOut.close();
		spotsOut.close();
	}
}
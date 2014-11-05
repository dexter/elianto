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
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.util.ConllDocument;
import it.cnr.isti.hpc.io.reader.RecordReader;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class ConvertConllDocumentsToDocumentsCLI extends
		AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	public ConvertConllDocumentsToDocumentsCLI(String[] args) {
		super(args);
	}

	public static void main(String[] args) {
		ConvertConllDocumentsToDocumentsCLI cli = new ConvertConllDocumentsToDocumentsCLI(
				args);
		RecordReader<ConllDocument> reader = new RecordReader<ConllDocument>(
				cli.getInput(), ConllDocument.class);
		cli.openOutput();
		int id = 0;
		for (ConllDocument document : reader) {
			Document d = new Document();
			d.setCollectionId(document.getCollection());
			d.setExternalId(d.getExternalId());
			d.setDocId(id);
			d.setDocument(document.getContent());
			d.setTemplate("{{title}} <br/> {{body}}");
			cli.writeLineInOutput(gson.toJson(d));
		}
		cli.closeOutput();

	}

}

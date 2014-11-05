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
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.sql.SQLException;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class IndexCollectionCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { INPUT };

	public IndexCollectionCLI(String[] args) {
		super(args, params, "java -jar $jar " + IndexCollectionCLI.class
				+ " -input collection.json");
	}

	public static void main(String[] args) {
		IndexCollectionCLI cli = new IndexCollectionCLI(args);
		RecordReader<Document> reader = new RecordReader<Document>(
				cli.getInput(), Document.class);
		SqliteDao dao = SqliteDao.getInstance();
		ProgressLogger pl = new ProgressLogger("indexed {} documents", 10);
		for (Document document : reader) {
			pl.up();
			try {
				dao.addDocument(document);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}

		}

	}

}

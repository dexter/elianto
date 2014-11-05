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
import it.cnr.isti.hpc.dexter.annotate.bean.EntityDescription;
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.log.ProgressLogger;

import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class IndexEntityDescriptionsCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { INPUT };

	private static final Logger logger = LoggerFactory
			.getLogger(IndexEntityDescriptionsCLI.class);

	public IndexEntityDescriptionsCLI(String[] args) {
		super(args, params, "java -jar $jar "
				+ IndexEntityDescriptionsCLI.class + " -input collection.json");
	}

	public static void main(String[] args) {
		IndexEntityDescriptionsCLI cli = new IndexEntityDescriptionsCLI(args);
		RecordReader<EntityDescription> reader = new RecordReader<EntityDescription>(
				cli.getInput(), EntityDescription.class);
		SqliteDao dao = SqliteDao.getInstance();
		ProgressLogger pl = new ProgressLogger("indexed {} entities", 1000);
		for (EntityDescription entity : reader) {
			pl.up();
			try {
				dao.addEntity(entity);
			} catch (SQLException e) {
				e.printStackTrace();
				System.exit(-1);
			}

		}

	}
}

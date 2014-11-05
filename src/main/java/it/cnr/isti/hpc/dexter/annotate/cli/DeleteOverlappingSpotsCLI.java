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
import it.cnr.isti.hpc.dexter.annotate.dao.SqliteDao;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.io.reader.RecordReader;
import it.cnr.isti.hpc.property.ProjectProperties;

import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Feb 11, 2014
 */
public class DeleteOverlappingSpotsCLI extends AbstractCommandLineInterface {
	private static Gson gson = new Gson();

	private static String[] params = new String[] { OUTPUT };
	private static final Logger logger = LoggerFactory
			.getLogger(DeleteOverlappingSpotsCLI.class);

	static ProjectProperties properties = new ProjectProperties(
			DeleteOverlappingSpotsCLI.class);

	static DexterRestClient client = null;

	public DeleteOverlappingSpotsCLI(String[] args) {
		super(args, params, "java -jar $jar " + DeleteOverlappingSpotsCLI.class
				+ " -input annotatedSpots");
	}

	public static void main(String[] args) throws SQLException,
			URISyntaxException {
		client = new DexterRestClient(properties.get("dexter.rest.api"));
		DeleteOverlappingSpotsCLI cli = new DeleteOverlappingSpotsCLI(args);
		cli.openOutput();

		RecordReader<AnnotatedSpot> reader = new RecordReader<AnnotatedSpot>(
				cli.getInput(), AnnotatedSpot.class);
		Map<Integer, List<AnnotatedSpot>> map = new HashMap<Integer, List<AnnotatedSpot>>();

		SqliteDao dao = SqliteDao.getInstance();
		for (AnnotatedSpot s : dao.getAllSpots()) {
			int i = s.getDocId();

			if (!map.containsKey(i)) {
				map.put(i, new LinkedList<AnnotatedSpot>());
			}
			map.get(i).add(s);
		}
		int count = 0;
		int different = 0;
		Set<Integer> overlappingDocs = new HashSet<Integer>();
		Set<AnnotatedSpot> blackList = new HashSet<AnnotatedSpot>();

		for (List<AnnotatedSpot> list : map.values()) {

			Set<Integer> entitySet = new HashSet<Integer>();

			Set<Integer> annotatedEntitiesSet = new HashSet<Integer>();
			int docid = list.get(0).getDocId();
			Document doc = dao.getDocumentByDocId(docid);
			for (int x = 0; x < list.size(); x++) {
				if (list.get(x).getEntities().getCandidates().isEmpty()) {
					logger.warn("list of candidates is empty");
					continue;
				}
				entitySet.add(list.get(x).getEntities().getCandidates().get(0)
						.getEntity());
				if (list.get(x).getField().equals("headline")) {
					dao.removeSpot(list.get(x));
					blackList.add(list.get(x));
					continue;
				}
				// if (!checkSpot(list.get(x), doc)) {
				// logger.warn("REMOVING SPOT {}", list.get(x));
				// dao.removeSpot(list.get(x));
				// blackList.add(list.get(x));
				// continue;
				// }
				int xentity = list.get(x).getEntities().getCandidates().get(0)
						.getEntity();
				if (blackList.contains(x))
					continue;
				entitySet.add(list.get(x).getEntities().getCandidates().get(0)
						.getEntity());

				List<AnnotatedSpot> overlap = new ArrayList<AnnotatedSpot>();
				overlap.add(list.get(x));
				for (int y = x + 1; y < list.size(); y++) {
					if (list.get(y).getField().equals("headline")) {
						blackList.add(list.get(y));
						dao.removeSpot(list.get(y));
						continue;
					}
					// if (!checkSpot(list.get(y), doc) && ) {
					// logger.warn("REMOVING SPOT {}", list.get(y));
					// blackList.add(list.get(y));
					// dao.removeSpot(list.get(y));
					// continue;
					// }
					if (list.get(x).overlaps(list.get(y))) {
						logger.info("{} overlaps with {} ", list.get(x),
								list.get(y));
						overlap.add(list.get(y));
					}

				}
				if (overlap.size() == 1) {
					logger.info("adding {} ", xentity);
					annotatedEntitiesSet.add(xentity);
				} else {
					AnnotatedSpot better = overlap.get(0);
					logger.warn("overlaps ");
					// logger.warn("{} {}", 0, better);
					// the missing entity in the set or the longest spot
					for (int k = 1; k < overlap.size(); k++) {
						AnnotatedSpot candidate = overlap.get(k);
						// logger.warn("{} {}", k, candidate);

						if (annotatedEntitiesSet.contains(better.getEntities()
								.getCandidates().get(0).getEntity())
								|| candidate.getMention().length() > better
										.getMention().length()) {
							blackList.add(better);
							dao.removeSpot(better);

							better = candidate;
						} else {
							blackList.add(candidate);
							dao.removeSpot(candidate);

						}
					}
					for (int k = 0; k < overlap.size(); k++) {
						if (overlap.get(k).equals(better))
							continue;
						cli.writeLineInOutput("doc=" + docid + "\tdel:"
								+ spot(overlap.get(k)) + "\t" + spot(better));
					}
					logger.warn("selected {}", better);

					annotatedEntitiesSet.add(better.getEntities()
							.getCandidates().get(0).getEntity());
				}

			}

			if (entitySet.size() != annotatedEntitiesSet.size()) {
				logger.warn("different number of annotations doc id {}", list
						.get(0).getDocId());
				cli.writeLineInOutput("removed doc " + list.get(0).getDocId());
				logger.warn("removing {}", list.get(0).getDocId());
				dao.removeDoc(list.get(0).getDocId());

				different++;
				List<Integer> xx = new ArrayList<Integer>(entitySet);
				Collections.sort(xx);
				List<Integer> yy = new ArrayList<Integer>(annotatedEntitiesSet);
				Collections.sort(yy);
				logger.warn("\n\nentities {} \nannotated:{} \n", xx, yy);
				entitySet.removeAll(annotatedEntitiesSet);
				logger.warn("\nmissing: {}", entitySet);

			} else {
				logger.warn("docid" + list.get(0).getDocId()
						+ " \n\nentities {}  \n", annotatedEntitiesSet);

			}
		}
		logger.info("{} spots overlaps", count);
		logger.info("{} different number of annotations", different);
		cli.closeOutput();
		logger.info("{} docs contains overlappings", overlappingDocs.size());

	}

	private static String spot(AnnotatedSpot spot) {
		int id = spot.getEntities().getCandidates().get(0).getEntity();
		return "[" + client.getDesc(id).getTitle() + "," + id + "|"
				+ spot.getMention() + "]";

	}

	private static boolean checkSpot(AnnotatedSpot s, Document d) {
		String field = s.getField();
		if (d == null)
			return false;
		char before = ' ';
		char after = ' ';
		for (DocumentField f : d.getDocument().getContent()) {
			if (f.getName().equals(field)) {
				System.out.println("\t"
						+ f.getValue().substring(s.getStart(), s.getEnd()));
				int start = Math.max(0, s.getStart() - 10);
				if (s.getStart() > 0) {
					before = f.getValue().charAt(s.getStart() - 1);
				}
				if (s.getEnd() < f.getValue().length()) {
					after = f.getValue().charAt(s.getEnd());
				}
				int end = Math.min(f.getValue().length(), s.getEnd() + 10);
				System.out.println("\tcontext: "
						+ f.getValue().substring(start, end));
				if (Character.isLetter(before) || Character.isLetter(after)) {
					return false;
				} else {
					return true;
				}

			}
		}
		return false;
	}
}

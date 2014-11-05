/**
 *  Copyright 2013 Diego Ceccarelli
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
 *  Copyright 2013 Diego Ceccarelli
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
package it.cnr.isti.hpc.dexter.annotate.dao;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.DocumentContent;
import it.cnr.isti.hpc.dexter.annotate.bean.User;

import java.io.File;
import java.sql.SQLException;

import org.junit.Test;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 16, 2013
 */
public class SqliteDaoTest {

	@Test
	public void test() throws SQLException {
		File db = new File("/tmp/db.sqlite");
		if (db.exists())
			db.delete();
		SqliteDao dao = new SqliteDao(db);
		User u = new User();
		u.setEmail("diego.ceccarelli@gmail.com");
		u.setFirstName("Diego");
		u.setLastName("Ceccarelli");
		u.setName("doeg");

		u.setPassword("asdadasdas");
		dao.addUser(u);
		User account2 = dao.getUserById(String.valueOf(u.getId()));
		assertTrue(dao.login(String.valueOf(u.getId()), "asdadasdas"));
		assertFalse(dao.login(String.valueOf(u.getId() + "nooooo"),
				"asdadasdas"));
		assertFalse(dao.login(String.valueOf(u.getId()), "asdadasdas1"));
	}

	@Test
	public void testDoc() {
		File db = new File("/tmp/db-doc.sqlite");
		if (db.exists())
			db.delete();
		SqliteDao dao = new SqliteDao(db);
		Document d = new Document();
		d.setCollectionId("test");

		d.setDocument(new DocumentContent());
		// FIXME
		// dao.addDocument(d);
		// d = new Document();
		// d.setCollectionId("test");
		// d.setContent("asd1");
		// d.setPos(1);
		// dao.addDocument(d);
		//
		// Document f = dao.getDocument(0, "test");
		// Assert.assertEquals("asd", f.getContent());
		// f = dao.getDocument(1, "test");
		// Assert.assertEquals("asd1", f.getContent());

	}

	// @Test
	// public void testSpot() {
	// File db = new File("/tmp/db-doc.sqlite");
	// if (db.exists())
	// db.delete();
	// SqliteDao dao = new SqliteDao(db);
	// AnnotatedSpot spot = new AnnotatedSpot();
	// spot.setDocId(1);
	// spot.setStart(1);
	// spot.setEnd(10);
	// spot.setEntityCandidates("ads");
	// spot.setField("pippo");
	// spot.setSpotterId("spotter");
	// spot.setMention("pippo");
	// dao.addSpot(spot);
	// List<AnnotatedSpot> aspots = dao.getSpots(1, "spotter");
	//
	// Assert.assertEquals(1, aspots.size());
	// aspots = dao.getSpots(2, "spotter");
	// Assert.assertEquals(0, aspots.size());
	// spot = new AnnotatedSpot();
	// spot.setDocId(1);
	// spot.setStart(1);
	// spot.setEnd(10);
	// spot.setEntityCandidates("ads");
	// spot.setField("pippo");
	// spot.setSpotterId("spotter");
	// spot.setMention("pippo");
	// dao.addSpot(spot);
	// spot = new AnnotatedSpot();
	// spot.setDocId(1);
	// spot.setStart(1);
	// spot.setEnd(10);
	// spot.setEntityCandidates("ads");
	// spot.setField("pippo");
	// spot.setSpotterId("spotter1");
	// spot.setMention("pippo");
	// dao.addSpot(spot);
	//
	// aspots = dao.getSpots(1, "spotter");
	// Assert.assertEquals(2, aspots.size());
	// aspots = dao.getSpots(1, "spotter1");
	// Assert.assertEquals(1, aspots.size());
	//
	// }
	//
	// @Test
	// public void testUserAnnotations() {
	// File db = new File("/tmp/db-doc.sqlite");
	// if (db.exists())
	// db.delete();
	// SqliteDao dao = new SqliteDao(db);
	// UserAnnotation ua = new UserAnnotation();
	// ua.setCandidates(" asd");
	// ua.setDocId(1);
	// ua.setSpotId(1);
	// ua.setUserId("diego");
	// ua.setSpotterId("default");
	// dao.addUserAnnotation(ua);
	// UserAnnotation ann = dao.getUserAnnotation(1, "diego", "default");
	// assertEquals(ua, ann);
	// ua = new UserAnnotation();
	// ua.setCandidates(" asd");
	// ua.setDocId(1);
	// ua.setSpotId(2);
	// ua.setUserId("diego");
	// ua.setSpotterId("default");
	// dao.addUserAnnotation(ua);
	//
	// ann = dao.getUserAnnotation(2, "diego", "default");
	// assertEquals(ua, ann);
	//
	// List<UserAnnotation> list = dao.getUserAnnotations(1, "diego",
	// "default");
	// assertEquals(2, list.size());
	// ua = new UserAnnotation();
	// ua.setCandidates(" asd");
	// ua.setDocId(1);
	// ua.setSpotId(2);
	// ua.setUserId("diego");
	// ua.setSpotterId("default1");
	// dao.addUserAnnotation(ua);
	// list = dao.getUserAnnotations(1, "diego", "default");
	// assertEquals(2, list.size());
	// list = dao.getUserAnnotations(1, "diego", "default1");
	// assertEquals(1, list.size());
	// ua = new UserAnnotation();
	// ua.setCandidates(" asd");
	// ua.setDocId(1);
	// ua.setSpotId(2);
	// ua.setUserId("pippo");
	// ua.setSpotterId("default");
	// dao.addUserAnnotation(ua);
	//
	// list = dao.getAnnotations(1, "default");
	// assertEquals(3, list.size());
	//
	// }
}

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

import it.cnr.isti.hpc.dexter.annotate.bean.Action;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotatedSpot;
import it.cnr.isti.hpc.dexter.annotate.bean.AnnotationStatus;
import it.cnr.isti.hpc.dexter.annotate.bean.Document;
import it.cnr.isti.hpc.dexter.annotate.bean.EntityDescription;
import it.cnr.isti.hpc.dexter.annotate.bean.User;
import it.cnr.isti.hpc.dexter.annotate.bean.UserAnnotation;
import it.cnr.isti.hpc.dexter.annotate.result.UserAnnotatedDocument;
import it.cnr.isti.hpc.dexter.article.ArticleDescription;
import it.cnr.isti.hpc.dexter.rest.client.DexterRestClient;
import it.cnr.isti.hpc.property.ProjectProperties;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * @author Diego Ceccarelli <diego.ceccarelli@isti.cnr.it>
 * 
 *         Created on Dec 16, 2013
 */
public class SqliteDao {

	private static final Logger logger = LoggerFactory
			.getLogger(SqliteDao.class);
	Dao<User, String> accountDao;
	Dao<Document, String> documentDao;
	Dao<AnnotatedSpot, String> annotatedSpotDao;
	Dao<UserAnnotation, String> userAnnotationDao;
	Dao<EntityDescription, String> entityDescriptionsDao;
	Dao<AnnotationStatus, String> annotationStatusDao;
	Dao<Action, String> actionDao;

	ConnectionSource connectionSource;
	private static ProjectProperties properties = new ProjectProperties(
			SqliteDao.class);

	private static DexterRestClient client;
	private static SqliteDao dao = new SqliteDao();

	private SqliteDao() {
		this(new File(properties.get("db")));
	}

	public static SqliteDao getInstance() {
		return dao;
	}

	public void removeSpot(AnnotatedSpot spot) throws SQLException {
		logger.warn("removing spot {} ", spot);
		annotatedSpotDao.delete(spot);
	}

	public SqliteDao(File file) {

		String dbtype = properties.get("db.type");
		String databaseUrl = null;
		if (dbtype.equals("mysql")) {
			databaseUrl = "jdbc:mysql://localhost:3306/" + file.getName();
		} else if (dbtype.equals("sqlite")) {

			databaseUrl = "jdbc:sqlite:" + file.getAbsolutePath();
		} else {
			logger.error("cannot manage the database type {}", dbtype);
		}
		connectionSource = null;

		try {
			if (dbtype.equals("mysql")) {
				connectionSource = new JdbcPooledConnectionSource(databaseUrl,
						properties.get("mysql.username"),
						properties.get("mysql.password"));

			} else {
				try {
					Class.forName("org.sqlite.JDBC");
				} catch (ClassNotFoundException e1) {
					logger.error("sqlite problems {}", e1.toString());

				}
				connectionSource = new JdbcConnectionSource(databaseUrl);

			}
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			TableUtils.createTableIfNotExists(connectionSource, Document.class);
			TableUtils.createTableIfNotExists(connectionSource,
					AnnotatedSpot.class);
			TableUtils.createTableIfNotExists(connectionSource,
					UserAnnotation.class);
			TableUtils.createTableIfNotExists(connectionSource,
					EntityDescription.class);
			TableUtils.createTableIfNotExists(connectionSource,
					AnnotationStatus.class);
			TableUtils.createTableIfNotExists(connectionSource, Action.class);

		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();

		}
		logger.info("opened database successfully");
		try {
			accountDao = DaoManager.createDao(connectionSource, User.class);
			documentDao = DaoManager
					.createDao(connectionSource, Document.class);
			annotatedSpotDao = DaoManager.createDao(connectionSource,
					AnnotatedSpot.class);
			userAnnotationDao = DaoManager.createDao(connectionSource,
					UserAnnotation.class);
			entityDescriptionsDao = DaoManager.createDao(connectionSource,
					EntityDescription.class);
			annotationStatusDao = DaoManager.createDao(connectionSource,
					AnnotationStatus.class);
			actionDao = DaoManager.createDao(connectionSource, Action.class);

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// System.exit(-1);
		} // if you need to create the
			// 'accounts' table make
			// this call
			// TableUtils.createTable(connectionSource,
			// Account.class); // create
			// an instance of Account
		try {
			client = new DexterRestClient(properties.get("dexter.rest.api"));
		} catch (URISyntaxException e) {
			logger.error("connecting to dexter rest service {} ({})",
					properties.get("dexter.rest.api"), e.toString());
			// System.exit(-1);
		}
	}

	public User getUserById(String id) throws SQLException {
		User u = null;

		// Patch for testing the API with the test user
		if (id.equals("test")) {
			u = new User();
			u.setId(0);
			u.setAdmin(false);
			u.setFirstName("Test");
			u.setLastName("User");
			return u;
		}

		u = accountDao.queryForId(id);
		return u;
	}

	public List<AnnotationStatus> userAnnotation(String userId,
			String collection) throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotations = annotationStatusDao
				.queryBuilder();
		annotations.where().eq("userId", userId);

		QueryBuilder<Document, String> documents = documentDao.queryBuilder();
		documents.where().eq("coll", "collection");

		return annotations.join(documents).query();

	}

	public UserAnnotatedDocument getDocumentAnnotation(Integer docId)
			throws SQLException {
		Document d = documentDao.queryForId(String.valueOf(docId));

		UserAnnotatedDocument uad = new UserAnnotatedDocument(d);
		List<AnnotationStatus> statuses = annotationStatusDao.queryBuilder()
				.where().eq("docId", docId).and()
				.eq("status", AnnotationStatus.Status.DONE).query();

		Set<String> users = new HashSet<String>();
		for (AnnotationStatus u : statuses) {
			users.add(u.getUserId());
		}

		// Map< SpotId, List<UserAnnotation> >
		Map<Integer, List<UserAnnotation>> annotations = new HashMap<Integer, List<UserAnnotation>>();
		for (String user : users) {
			List<UserAnnotation> uas = getUserAnnotations(docId, user);
			for (UserAnnotation ua : uas) {
				if (!annotations.containsKey(ua.getSpotId())) {
					annotations.put(ua.getSpotId(), new ArrayList<UserAnnotation>());
				}
				annotations.get(ua.getSpotId()).add(ua);
			}
		}

		for (Map.Entry<Integer, List<UserAnnotation>> entry : annotations.entrySet()) {
			AnnotatedSpot spot = annotatedSpotDao.queryForId(String.valueOf(entry.getKey()));
			uad.addAnnotations(spot, entry.getValue());
		}

		return uad;

	}

	public AnnotationStatus getStatus(String userId, String docId)
			throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotations = annotationStatusDao
				.queryBuilder();
		AnnotationStatus as = annotations.where().eq("userId", userId).and()
				.eq("docId", docId).queryForFirst();
		return as;

	}

	public List<AnnotationStatus> getPendingAnnotations(String userId)
			throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotationStatusQB = annotationStatusDao
				.queryBuilder();
		annotationStatusQB.orderBy("timestamp", false);

		Where<AnnotationStatus, String> where = annotationStatusQB.where();
		where.and(where.eq("userId", userId), where.or(
				where.eq("status", AnnotationStatus.Status.STEP1),
				where.eq("status", AnnotationStatus.Status.STEP2)));

		// It still need to be tested (lines below that will replace lines
		// above)
		// where.eq("status", AnnotationStatus.Status.STEP1);
		// where.eq("status", AnnotationStatus.Status.STEP2);
		// where.or(2);
		// where.eq("userId", userId);
		// where.and(2);

		return annotationStatusQB.query();
	}

	public AnnotationStatus getLastUserAnnotation(String userId)
			throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotations = annotationStatusDao
				.queryBuilder();
		AnnotationStatus as = annotations.orderBy("timestamp", false).where()
				.eq("userId", userId).queryForFirst();
		return as;

	}

	public AnnotationStatus addStatus(AnnotationStatus s) throws SQLException {
		annotationStatusDao.createOrUpdate(s);
		return s;
	}

	public EntityDescription getEntityDescription(int id) throws SQLException {
		EntityDescription u = null;

		u = entityDescriptionsDao.queryForId(String.valueOf(id));
		if (u == null) {
			ArticleDescription desc = client.getDesc(id);
			u = new EntityDescription(id);
			u.setDescription(desc.getDescription());
			u.setImage(desc.getImage());
			u.setTitle(desc.getTitle());
			u.setUrl(desc.getUrl());
			entityDescriptionsDao.createOrUpdate(u);

		}

		return u;

	}

	public Document getDocument(int pos, String collection) throws SQLException {
		Document doc = null;

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("docId", pos);
		query.put("collectionId", collection);
		List<Document> res = documentDao.queryForFieldValues(query);
		if (res.size() > 1) {
			logger.warn("inconsistence state in db (more docs {} in pos {})",
					res.size(), pos);
			// System.exit(-1);
		}
		if (res.size() == 1) {
			doc = res.get(0);
		} else {
			logger.warn("no doc in pos {} ", pos);
		}

		return doc;

	}

	public List<DocFreq> collectionStatus(String collectionId)
			throws SQLException {
		String query = "SELECT documents.docId as docId, IFNULL(num, 0) as freq, IFNULL(avgSpots, 0) as avgSpots "
				+ "FROM documents LEFT JOIN ("
				+ "SELECT documents.docId, COUNT(*) as num "
				+ "FROM documents JOIN annotationStatus ON documents.docId = annotationStatus.docId "
				+ "WHERE documents.collectionId = '"
				+ collectionId
				+ "' "
				+ "GROUP BY documents.docId "
				+ ") q1 ON documents.docId = q1.docId LEFT JOIN ("
				+ "SELECT docId, count(*) / count(distinct userId) as avgSpots "
				+ "FROM `user-annotation` "
				+ "GROUP BY docId "
				+ ") q2 on documents.docId = q2.docId "
				+ "ORDER BY freq DESC, docId DESC";

		GenericRawResults<DocFreq> rawResults = documentDao.queryRaw(query,
				new DocFreqMapper());
		Iterator<DocFreq> it = rawResults.iterator();
		List<DocFreq> freqs = new LinkedList<DocFreq>();
		while (it.hasNext()) {
			freqs.add(it.next());
		}
		return freqs;
	}

	public static class DocFreq {
		int docId;
		int freq;
		float avgSpots;

		public DocFreq(int docId, int freq, float avgSpots) {
			super();
			this.docId = docId;
			this.freq = freq;
			this.avgSpots = avgSpots;
		}

		public int getDocId() {
			return docId;
		}

		public void setDocId(int docId) {
			this.docId = docId;
		}

		public int getFreq() {
			return freq;
		}

		public void setFreq(int freq) {
			this.freq = freq;
		}

		public float getAvgSpots() {
			return avgSpots;
		}

		public void setAvgSpots(float avgSpots) {
			this.avgSpots = avgSpots;
		}

	}

	private static class DocFreqMapper implements RawRowMapper<DocFreq> {
		@Override
		public DocFreq mapRow(String[] columnNames, String[] resultColumns)
				throws SQLException {
			int doc = Integer.parseInt(resultColumns[0]);
			int freq = Integer.parseInt(resultColumns[1]);
			float avgSpots = Float.parseFloat(resultColumns[2]);

			return new DocFreq(doc, freq, avgSpots);

		}
	}

	public List<UserStats> leaderBoard(String collectionId, int userId)
			throws SQLException {

		String conditionUser = "";
		if (userId > 0)
			conditionUser = "WHERE userId=" + userId;

		String query = "SELECT userId, COUNT(DISTINCT docId) as numDocs, COUNT(*) as numSpots "
				+ "FROM `user-annotation` "
				+ " "
				+ conditionUser
				+ " "
				+ "GROUP BY userId " + "ORDER BY numSpots DESC, numDocs DESC";

		GenericRawResults<UserStats> rawResults = documentDao.queryRaw(query,
				new UserStatsMapper());
		Iterator<UserStats> it = rawResults.iterator();
		List<UserStats> userStats = new LinkedList<UserStats>();
		while (it.hasNext()) {
			userStats.add(it.next());
		}
		return userStats;
	}

	public static class UserStats {
		int userId;
		int numDocs;
		int numSpots;

		public UserStats(int userId, int numDocs, int numSpots) {
			this.userId = userId;
			this.numDocs = numDocs;
			this.numSpots = numSpots;
		}

		public int getUserId() {
			return userId;
		}

		public void setUserId(int userId) {
			this.userId = userId;
		}

		public int getNumDocs() {
			return numDocs;
		}

		public void setNumDocs(int numDocs) {
			this.numDocs = numDocs;
		}

		public int getNumSpots() {
			return numSpots;
		}

		public void setNumSpots(int numSpots) {
			this.numSpots = numSpots;
		}
	}

	private static class UserStatsMapper implements RawRowMapper<UserStats> {
		@Override
		public UserStats mapRow(String[] columnNames, String[] resultColumns)
				throws SQLException {
			int userId = Integer.parseInt(resultColumns[0]);
			int numDocs = Integer.parseInt(resultColumns[1]);
			int numSpots = Integer.parseInt(resultColumns[2]);

			return new UserStats(userId, numDocs, numSpots);

		}
	}

	public void addSpot(AnnotatedSpot spot) throws SQLException {

		annotatedSpotDao.createOrUpdate(spot);

	}

	public void addEntity(EntityDescription entity) throws SQLException {

		entityDescriptionsDao.createOrUpdate(entity);

	}

	public List<AnnotatedSpot> getSpots(String userId, int docId)
			throws SQLException {

		QueryBuilder<AnnotatedSpot, String> qb = annotatedSpotDao
				.queryBuilder();
		qb.orderBy("field", true);
		qb.orderBy("start", true);
		Where<AnnotatedSpot, String> w = qb.where();
		w.eq("docId", docId);
		w.eq("spotterId", "std").or().eq("userId", userId);
		w.and(2);
		List<AnnotatedSpot> res = qb.query();
		for (AnnotatedSpot s : res)
			System.out.println("spot " + s.getMention());

		return res;

	}
	
	public List<AnnotatedSpot> getSpots(int docId)
			throws SQLException {

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("docId", docId);
		query.put("spotterId", "std");

		List<AnnotatedSpot> res = annotatedSpotDao.queryForFieldValues(query);
		return res;
	}

	public List<AnnotatedSpot> getAllSpots() throws SQLException {

		QueryBuilder<AnnotatedSpot, String> qb = annotatedSpotDao
				.queryBuilder();
		qb.orderBy("docId", true);
		return qb.query();
	}

	public void addUser(User u) throws SQLException {
		logger.info("add user {} ", u.toString());

		accountDao.createOrUpdate(u);

	}

	public void addUserAnnotation(UserAnnotation ua) throws SQLException {
		long time = System.currentTimeMillis();
		ua.setTimestamp(time);
		ua.setLastModified(time);
		UserAnnotation toUpdate = getUserAnnotation(ua.getSpotId(),
				ua.getUserId(), ua.getSpotterId());
		if (toUpdate != null) {
			logger.info("update user annotation for {} - {}", ua.getSpotId(),
					ua.getUserId());

			toUpdate.setEntityId(ua.getEntityId());
			toUpdate.setUserScore(ua.getUserScore());
			toUpdate.setLastModified(time);
			ua = toUpdate;
		}

		userAnnotationDao.createOrUpdate(ua);
	}

	public void deleteUserAnnotation(UserAnnotation ua) throws SQLException {
		userAnnotationDao.delete(ua);
	}

	public List<UserAnnotation> getUserAnnotations(boolean onlyDone)
			throws SQLException {
		// QueryBuilder<UserAnnotation, String> userAnnotations =
		// annotationDao.queryBuilder();
		// List<UserAnnotation> res = null;
		// if (onlyDone) {
		// QueryBuilder<AnnotationStatus, String> annotationsStatus =
		// annotationStatus.queryBuilder();
		// annotationsStatus.where().eq("status", AnnotationStatus.Status.DONE);
		// res = userAnnotations.join(annotationsStatus).query();
		// } else {
		// res = userAnnotations.query();
		// }

		String query = "SELECT u.annotationId, u.userId, u.spotId, u.spotterId, u.entityId, u.userScore, u.docId, u.timestamp, u.lastModified "
				+ "FROM `user-annotation` AS u LEFT JOIN `annotationStatus` AS a "
				+ "ON (u.docId = a.docId AND u.userId = a.userId) ";
		if (onlyDone) {
			query += " WHERE a.status = 'DONE'";
		}

		GenericRawResults<UserAnnotation> rawResults = documentDao.queryRaw(
				query, new UserAnnotationMapper());
		return rawResults.getResults();
	}

	private static class UserAnnotationMapper implements
			RawRowMapper<UserAnnotation> {
		@Override
		public UserAnnotation mapRow(String[] columnNames,
				String[] resultColumns) throws SQLException {
			UserAnnotation ua = new UserAnnotation();

			ua.setAnnotationId(Integer.parseInt(resultColumns[0]));
			ua.setUserId(resultColumns[1]);
			ua.setSpotId(Integer.parseInt(resultColumns[2]));
			ua.setSpotterId(resultColumns[3]);
			ua.setEntityId(Integer.parseInt(resultColumns[4]));
			ua.setUserScore(Integer.parseInt(resultColumns[5]));
			ua.setDocId(Integer.parseInt(resultColumns[6]));
			ua.setTimestamp(Long.parseLong(resultColumns[7]));
			ua.setLastModified(Long.parseLong(resultColumns[8]));

			return ua;
		}
	}

	public Iterator<Document> getDocumentIterator(String collection)
			throws SQLException {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("collectionId", collection);

		return documentDao.queryBuilder().where()
				.eq("collectionId", collection).iterator();

	}

	public List<UserAnnotation> getUserAnnotations(int docId, String userId, String spotterId) 
			throws SQLException {

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("docId", docId);
		query.put("spotterId", spotterId);
		query.put("userId", userId);
		List<UserAnnotation> res = userAnnotationDao.queryForFieldValues(query);
		return res;
	}

	public List<UserAnnotation> getUserAnnotations(int docId, String userId)
			throws SQLException {

		Map<String, Object> query = new HashMap<String, Object>();
		query.put("docId", docId);
		query.put("userId", userId);
		List<UserAnnotation> res = userAnnotationDao.queryForFieldValues(query);
		return res;
	}
	
	public List<UserAnnotation> getUserAnnotations(int docId, boolean onlyDone)
			throws SQLException {

//		Map<String, Object> query = new HashMap<String, Object>();
//		query.put("docId", docId);
//		List<UserAnnotation> res = userAnnotationDao.queryForFieldValues(query);
//		return res;		
		
		String query = "SELECT u.annotationId, u.userId, u.spotId, u.spotterId, u.entityId, u.userScore, u.docId, u.timestamp, u.lastModified "
				+ "FROM `user-annotation` AS u LEFT JOIN `annotationStatus` AS a "
				+ "ON (u.docId = a.docId AND u.userId = a.userId) "
				+ "WHERE u.docId = " + String.valueOf(docId);
		if (onlyDone)
			query += " AND a.status = 'DONE'";

		GenericRawResults<UserAnnotation> rawResults = documentDao.queryRaw(query, new UserAnnotationMapper());
		return rawResults.getResults();
	}
	
	public List<AnnotationStatus> getUserAnnotations(String userId)
			throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotations = annotationStatusDao.queryBuilder();
		List<AnnotationStatus> as = annotations.orderBy("timestamp", false)
				.where().eq("userId", userId).query();
		return as;

	}

	public List<AnnotationStatus> getAllUserAnnotations(boolean onlyDone)
			throws SQLException {
		QueryBuilder<AnnotationStatus, String> annotations = annotationStatusDao
				.queryBuilder();
		if (onlyDone)
			annotations.where().eq("status", AnnotationStatus.Status.DONE);
		List<AnnotationStatus> as = annotations.orderBy("timestamp", true)
				.query();
		return as;
	}

	// public List<UserAnnotation> getAnnotations(int docId, String spotterId) {
	// try {
	// Map<String, Object> query = new HashMap<String, Object>();
	// query.put("docId", docId);
	// query.put("spotterId", spotterId);
	// List<UserAnnotation> res = annotationDao.queryForFieldValues(query);
	// return res;
	// } catch (SQLException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return Collections.emptyList();
	// }

	public UserAnnotation getUserAnnotation(int spotId, String userId,
			String spotterId) throws SQLException {
		UserAnnotation ua = null;

		QueryBuilder<UserAnnotation, String> q = userAnnotationDao
				.queryBuilder();
		Where<UserAnnotation, String> w = q.where();
		w.eq("spotId", spotId).and().eq("userId", userId);
		w.eq("spotterId", spotterId).or().eq("spotterId", "user-defined");

		List<UserAnnotation> res = w.and(2).query();
		if (res.size() > 1) {
			logger.warn(
					"inconsist state in db (more annotations for spot {} for user {})",
					spotId, userId);
			// System.exit(-1);
		}
		if (res.size() == 1) {
			ua = res.get(0);
		} else {
			logger.warn("no annotation for spot {} user {} ", spotId, userId);
		}

		return ua;
	}

	public void addDocument(Document d) throws SQLException {
		documentDao.createOrUpdate(d);

	}

	public boolean login(String id, String password) throws SQLException {
		if (id.equals("test"))
			return true;
		User u = getUserById(id);
		if (u == null)
			return false;
		return u.getPassword().equals(password);
	}

	public void close() {
		try {
			connectionSource.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

	}

	public User getUserByMail(String email) throws SQLException {
		Map<String, Object> query = new HashMap<String, Object>();
		query.put("email", email);

		List<User> res = accountDao.queryForFieldValues(query);
		if (res.size() > 1) {
			logger.warn("more than one user with the same email {}", email);

		}
		if (res.size() > 0) {
			return res.get(0);
		}
		return null;
	}

	public User signUp(User u) throws SQLException {
		accountDao.createOrUpdate(u);
		// We return the updated object
		return getUserByMail(u.getEmail());
	}

	public List<User> getUsers() {
		List<User> users = new LinkedList<User>();
		Iterator<User> elems = accountDao.iterator();
		while (elems.hasNext())
			users.add(elems.next());
		return users;
	}

	public Action saveAction(Action action) throws SQLException {
		actionDao.create(action);
		return action;
	}

	public List<Action> getActions(int userId, int docId, String coll)
			throws SQLException {

		QueryBuilder<Action, String> qb = actionDao.queryBuilder();
		qb.orderBy("timestamp", true);
		Where<Action, String> w = qb.where();
		w.eq("docId", docId);
		w.eq("userId", userId);
		w.eq("coll", coll);
		w.and(3);
		return qb.query();
	}

	public List<Action> getAllActions() throws SQLException {

		QueryBuilder<Action, String> qb = actionDao.queryBuilder();
		qb.orderBy("timestamp", true);
		return qb.query();
	}

	public void removeDoc(int docId) throws SQLException {
		logger.warn("removing document {}", docId);
		QueryBuilder<Document, String> qb = documentDao.queryBuilder();

		Where<Document, String> w = qb.where();
		w.eq("docId", docId);
		documentDao.delete(w.query());

		QueryBuilder<AnnotatedSpot, String> qs = annotatedSpotDao
				.queryBuilder();

		Where<AnnotatedSpot, String> ws = qs.where();
		ws.eq("docId", docId);
		annotatedSpotDao.delete(ws.query());

		QueryBuilder<UserAnnotation, String> qu = userAnnotationDao
				.queryBuilder();

		Where<UserAnnotation, String> wd = qu.where();
		wd.eq("docId", docId);
		userAnnotationDao.delete(wd.query());

	}

	public Document getDocumentByDocId(int docId) throws SQLException {

		QueryBuilder<Document, String> qb = documentDao.queryBuilder();

		Where<Document, String> w = qb.where();
		w.eq("docId", docId);
		return documentDao.queryForFirst(qb.prepare());

	}
}

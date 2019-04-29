package storage;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import model.CrawlerConfig;
import model.Doc;
import model.User;
import org.jsoup.nodes.Document;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class StorageFactory {

	private static StorageInterface database = null;

	public static StorageInterface getDatabaseInstance(String directory) {
		if (database == null) {
			database = new StorageInterface() {

				private final String USER_CATALOG = User.class.toString();
				private final String Document_CATALOG = Doc.class.toString();
				private final String Channel_CATALOG = Doc.class.toString();
				DatabaseConfig dbConfig = getDbConfig();
				int userCnt = 0;
				private int corpusSize = 0;
				private int lexiconSize = 0;
				private Environment env = new Environment(new File(directory), getEnvConfig());

				Database userCatalogDB = env.openDatabase(null, USER_CATALOG,
								dbConfig);
				Database documentCatalogDB = env.openDatabase(null, Document_CATALOG,
								dbConfig);

				Database userDB = env.openDatabase(null, "user_store", dbConfig);
				Database documentDB = env.openDatabase(null, "document_store", dbConfig);


				private StoredClassCatalog userJavaCatalog = new StoredClassCatalog(userCatalogDB);
				EntryBinding userKeyBinding = new SerialBinding(userJavaCatalog, String.class);
				EntryBinding userDataBinding = new SerialBinding(userJavaCatalog, User.class);

				private StoredClassCatalog documentJavaCatalog = new StoredClassCatalog(documentCatalogDB);
				EntryBinding docKeyBinding = new SerialBinding(documentJavaCatalog, String.class);
				EntryBinding docDataBinding = new SerialBinding(documentJavaCatalog, Doc.class);

				private Map<String, User> userMap = new StoredSortedMap(userDB, userKeyBinding, userDataBinding, true);
				private Map<String, Doc> docMap = new StoredSortedMap(documentDB, docKeyBinding, docDataBinding, true);

				EnvironmentConfig getEnvConfig() {
					EnvironmentConfig envConf = new EnvironmentConfig();
					envConf.setTransactional(true);
					envConf.setAllowCreate(true);
					return envConf;
				}

				DatabaseConfig getDbConfig() {
					DatabaseConfig dbConfig = new DatabaseConfig();
					dbConfig.setTransactional(true);
					dbConfig.setAllowCreate(true);
					return dbConfig;
				}

				/**
				 * How many documents so far?
				 */
				@Override
				public int getCorpusSize() {
					return docMap.size();
				}

				/**
				 * Add a new document, getting its ID
				 *
				 * @param url
				 * @param document
				 */
				@Override
				public int addDocument(String url, Document document) {
					StringBuilder sb = new StringBuilder();
					try {
						MessageDigest md = MessageDigest.getInstance("MD5");
						byte[] hashInBytes = md.digest(document.outerHtml().getBytes());
						for (byte b : hashInBytes) sb.append(String.format("%02x", b));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					String md5 = sb.toString();
					int id = corpusSize;
					if (docMap.containsKey(url)) {
						id = docMap.get(url).getId();
					} else {
						corpusSize++;
					}
					Doc doc = new Doc(id, url, document.outerHtml(), new Date(), md5, false);
					docMap.put(url, doc);
					return id;
				}

				@Override
				public int addDocument(String url, String documentContents, boolean isHtml) {
					StringBuilder sb = new StringBuilder();
					try {
						MessageDigest md = MessageDigest.getInstance("MD5");
						byte[] hashInBytes = md.digest(documentContents.getBytes());
						for (byte b : hashInBytes) sb.append(String.format("%02x", b));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					String md5 = sb.toString();
					int id = corpusSize;
					if (docMap.containsKey(url)) {
						id = docMap.get(url).getId();
					} else {
						corpusSize++;
					}
					Doc doc = new Doc(id, url, documentContents, new Date(), md5, isHtml);
					docMap.put(url, doc);
					checkIfFull();
					return id;
				}

				/**
				 * Add a new document, getting its ID
				 *
				 * @param url
				 * @param documentContents
				 */
				@Override
				public int addDocument(String url, String documentContents) {
					StringBuilder sb = new StringBuilder();
					try {
						MessageDigest md = MessageDigest.getInstance("MD5");
						byte[] hashInBytes = md.digest(documentContents.getBytes());
						for (byte b : hashInBytes) sb.append(String.format("%02x", b));
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					String md5 = sb.toString();
					int id = corpusSize;
					if (docMap.containsKey(url)) {
						id = docMap.get(url).getId();
					} else {
						corpusSize++;
					}
					Doc doc = new Doc(id, url, documentContents, new Date(), md5, false);
					docMap.put(url, doc);
					return id;
				}

				/**
				 * How many keywords so far?
				 */
				@Override
				public int getLexiconSize() {
					return lexiconSize;
				}

				/**
				 * Gets the ID of a word (adding a new ID if this is a new word)
				 *
				 * @param keyword
				 */
				@Override
				public int addOrGetKeywordId(String keyword) {
					return 0;
				}

				/**
				 * Adds a user and returns an ID
				 *
				 * @param username
				 * @param password
				 */
				@Override
				public int addUser(String username, String password, String firstName, String lastName) {
					User user = new User(userCnt, username, getSHA256Hashing(password), firstName, lastName);
					userMap.put(username, user);
					userCnt++;
					return userCnt;
				}

				/**
				 * Tries to log in the user, or else throws a HaltException
				 *
				 * @param username
				 * @param password
				 */
				@Override
				public boolean getSessionForUser(String username, String password) {
					return userMap.containsKey(username) && userMap.get(username).getPassword().equals(getSHA256Hashing(password));
				}

				private String getSHA256Hashing(String pwd) {
					MessageDigest md = null;
					try {
						md = MessageDigest.getInstance("SHA-256");
						byte[] hashInBytes = md.digest(pwd.getBytes());
						byte[] hashedByetArray = md.digest(hashInBytes);
						return Base64.getEncoder().encodeToString(hashedByetArray);
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					return null;
				}

				/**
				 * Retrieves a document's contents by URL
				 *
				 * @param url
				 */
				@Override
				public String getDocument(String url) {
					if (!docMap.containsKey(url)) return null;
					return docMap.get(url).getContent();
				}

				@Override
				public Date getDocumentCrawledTime(String url) {
					if (docMap == null) return null;
					if (docMap.containsKey(url)) return docMap.get(url).getCrawledTime();
					return null;
				}

				/**
				 * Shuts down / flushes / closes the storage system
				 */
				@Override
				public void close() {
					userDB.close();
					documentDB.close();
					userJavaCatalog.close();
					documentJavaCatalog.close();
					userCatalogDB.close();
					documentCatalogDB.close();
					env.close();
				}

				/*
				 * Get the first and last name for a given user. Should be used when the user has already logged in.
				 */
				@Override
				public String[] getFirstAndLastName(String username, String password) {
					if (getSessionForUser(username, password)) {
						User user = userMap.get(username);
						return new String[]{user.getFirstName(), user.getLastName()};
					}
					return null;
				}

				@Override
				public boolean ifMD5Exists(String content) {
					StringBuilder sb = new StringBuilder();
					MessageDigest md = null;
					try {
						md = MessageDigest.getInstance("MD5");
						byte[] hashInBytes = md.digest(content.getBytes());
						for (byte b : hashInBytes) sb.append(String.format("%02x", b));
						Set<String> md5Set = new HashSet<>();
						for (Doc doc : docMap.values()) {
							md5Set.add(doc.getMd5Hash());
						}
						return md5Set.contains(sb.toString());
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
						return false;
					}
				}


				@Override
				public List<Doc> getAllDocsList() {
					return new ArrayList<>(docMap.values());
				}


				@Override
				public boolean checkIfFull() {
					return docMap.size() >= CrawlerConfig.getCount();
				}
			};
		}
		return database;
	}
}

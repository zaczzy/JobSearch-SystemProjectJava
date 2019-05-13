package storage;

import aws.dynamoDB.DynamoDBService;
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

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class StorageFactory {

	private static StorageInterface database = null;

	public static StorageInterface getDatabaseInstance(String directory) {
		if (database == null) {
			database = new StorageInterface() {

				private final String Document_CATALOG = Doc.class.toString();

				DatabaseConfig dbConfig = getDbConfig();
				private int lexiconSize = 0;
				private Environment env = new Environment(new File(directory), getEnvConfig());

				Database md5CatalogDB = env.openDatabase(null, String.class.toString(),
								dbConfig);

				Database documentCatalogDB = env.openDatabase(null, Document_CATALOG,
								dbConfig);

				Database documentDB = env.openDatabase(null, "document_store", dbConfig);
				Database md5DB = env.openDatabase(null, "md5_store", dbConfig);


				private StoredClassCatalog documentJavaCatalog = new StoredClassCatalog(documentCatalogDB);
				private StoredClassCatalog md5JavaCatalog = new StoredClassCatalog(md5CatalogDB);


				EntryBinding docKeyBinding = new SerialBinding(documentJavaCatalog, String.class);
				EntryBinding docDataBinding = new SerialBinding(documentJavaCatalog, Doc.class);


				EntryBinding md5KeyBinding = new SerialBinding(md5JavaCatalog, String.class);
				EntryBinding md5DataBinding = new SerialBinding(md5JavaCatalog, String.class);

				private Map<String, Doc> docMap = new StoredSortedMap(documentDB, docKeyBinding, docDataBinding, true);
				private Map<String, String> md5Map = new StoredSortedMap(md5DB, md5KeyBinding, md5DataBinding, true);

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
				 */
				@Override
				public boolean addDocument(String url, String md5, String id) {
					boolean needUpdate = true;
					if (docMap.containsKey(url)) {
						//No need to update anything, keep the previous docID
						if (md5.equals(docMap.get(url).getMd5Hash())) {
							System.out.println(url + " is up to date");
							needUpdate = false;
							id = docMap.get(url).getId();
						} else {
							//Update the dynamoDB
							String oldID = docMap.get(url).getId();
							String[][] updateFields = new String[1][2];
							updateFields[0][0] = "isNew";
							updateFields[0][1] = "false";
							DynamoDBService.getInstance().update(oldID, updateFields);
						}
					}
					Doc doc = new Doc(id, url, new Date(), md5);
					docMap.put(url, doc);
					md5Map.put(md5, "");
					return needUpdate;
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


				private String getSHA256Hashing(String pwd) {
					MessageDigest md;
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
					return "";
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
					documentDB.close();
					documentJavaCatalog.close();
					documentCatalogDB.close();
					env.close();
				}

				@Override
				public boolean ifMD5Exists(String md5) {
						return md5Map.containsKey(md5);
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

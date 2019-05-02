package storage;

import model.Doc;
import org.jsoup.nodes.Document;

import java.util.Date;
import java.util.List;

public interface StorageInterface {

	/**
	 * How many documents so far?
	 */
	int getCorpusSize();

	/**
	 * Add a new document, getting its ID
	 */
	int addDocument(String url, String documentContents);

	int addDocument(String url, Document document);


	/**
	 * How many keywords so far?
	 */
	int getLexiconSize();

	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	int addOrGetKeywordId(String keyword);

	/**
	 * Adds a user and returns an ID
	 */
	int addUser(String username, String password, String firstName, String lastName);

	/**
	 * Tries to log in the user, or else throws a HaltException
	 */
	boolean getSessionForUser(String username, String password);

	/**
	 * Retrieves a document's contents by URL
	 */
	String getDocument(String url);

	/**
	 * Shuts down / flushes / closes the storage system
	 */
	void close();

	/*
	 * Get the first and last name for a given user. Should be used when the user has already logged in.
	 */
	String[] getFirstAndLastName(String username, String password);

	/*
			Get the crawler time for document with url
	 */

	Date getDocumentCrawledTime(String url);
	/*
			Check if md5 hash exist for given document content
	 */

	boolean ifMD5Exists(String content);


	List<Doc> getAllDocsList();

	/*
		Subscribe channel for user with username
	 */
	/*
			Get all subscribed channel for user with username
	 */
	/*
		Check if we have met the size of corpus
	 */
	boolean checkIfFull();
}

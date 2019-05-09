package storage;

import model.Doc;

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
	boolean addDocument(String url, String documentContents, String id);

	/**
	 * How many keywords so far?
	 */
	int getLexiconSize();

	/**
	 * Gets the ID of a word (adding a new ID if this is a new word)
	 */
	int addOrGetKeywordId(String keyword);

	/**
	 * Retrieves a document's contents by URL
	 */
	String getDocument(String url);

	/**
	 * Shuts down / flushes / closes the storage system
	 */
	void close();
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

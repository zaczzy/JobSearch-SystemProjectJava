package model;

import java.util.HashMap;
import java.util.Map;

public class CrawlerConfig {
	static String startURL;
	static String databaseDir;
	static int size;
	static int count;
	static boolean whetherEnd;
	static int bufSize = 0;

	static int requestReceived = 0;
	static int pagesStored = 0;
	static int urlAdded2Queue = 0;
	static int totalWorker = 0;
	static int myIndex = 0;
	static String linksFileLocation = "./links/out_links_" + myIndex;

	public static Map<Integer, String> contactMap = new HashMap<>();

	public static String getStartURL() {
		return startURL;
	}

	public static void setStartURL(String startURL) {
		CrawlerConfig.startURL = startURL;
	}

	public static String getDatabaseDir() {
		return databaseDir;
	}

	public static void setDatabaseDir(String databaseDir) {
		CrawlerConfig.databaseDir = databaseDir;
	}

	public static int getSize() {
		return size;
	}

	public static void setSize(int size) {
		CrawlerConfig.size = size;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		CrawlerConfig.count = count;
	}

	public static boolean isWhetherEnd() {
		return whetherEnd;
	}

	public static void setWhetherEnd(boolean whetherEnd) {
		CrawlerConfig.whetherEnd = whetherEnd;
	}

	public static void increamentBuf() {
		CrawlerConfig.bufSize++;
	}

	public static void decreamentBuf() {
		CrawlerConfig.bufSize--;
	}

	public static boolean bufEmpty() {
		return CrawlerConfig.bufSize == 0;
	}


	public static void incRequestReceived() {
	  CrawlerConfig.requestReceived++;
  }

  public static void incPagesStored() {
    CrawlerConfig.pagesStored++;
  }

  public static void incAddedUrl() {
    CrawlerConfig.urlAdded2Queue++;
  }

	public static int getRequestReceived() {
		return requestReceived;
	}

	public static int getPagesStored() {
		return pagesStored;
	}

	public static int getUrlAdded2Queue() {
		return urlAdded2Queue;
	}

	public static int getMyIndex() {
		return myIndex;
	}

	public static void setMyIndex(int myIndex) {
		CrawlerConfig.myIndex = myIndex;
	}

	public static String getLinksFileLocation() {
		return linksFileLocation;
	}

	public static int getTotalWorker() {
		return totalWorker;
	}

	public static void setTotalWorker(int totalWorker) {
		CrawlerConfig.totalWorker = totalWorker;
	}
}

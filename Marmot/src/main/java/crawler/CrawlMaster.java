package crawler;

import crawler.info.URLInfo;

public interface CrawlMaster {
	/**
	 * Returns true if it's permissible to access the site right now
	 * eg due to robots, etc.
	 */
	boolean isOKtoCrawl(String site, int port, boolean isSecure);

	/**
	 * Returns true if the crawl delay says we should wait
	 */
	boolean deferCrawl(String site);

	/**
	 * Returns true if it's permissible to fetch the content,
	 * eg that it satisfies the path restrictions from robots.txt
	 */
	boolean isOKtoParse(URLInfo url);

	/**
	 * Returns true if the document content looks worthy of indexing,
	 * eg that it doesn't have a known signature
	 */
	boolean isIndexable(String content);

	/**
	 * We've indexed another document
	 */
	void incCount();

	/**
	 * Workers can poll this to see if they should exit, ie the
	 * crawl is done
	 */
	boolean isDone();

	/**
	 * Workers should notify when they are processing an URL
	 */
	void setWorking(boolean working);

	/**
	 * Workers should call this when they exit, so the master
	 * knows when it can shut down
	 */
	void notifyThreadExited();
}

package crawler;

import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import model.CrawlerConfig;
import storage.StorageFactory;
import storage.StorageInterface;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RobotsHelper {
	/**
	 * Returns true if it's permissible to fetch the content,
	 * eg that it satisfies the path restrictions from robots.txt
	 */
//	static Logger log = LogManager.getLogger(RobotsHelper.class);

	static StorageInterface db = StorageFactory.getDatabaseInstance(CrawlerConfig.getDatabaseDir());



	public static boolean isOKtoParse(URLInfo url, RobotsTxtInfo robotsTxtInfo) {
		if (robotsTxtInfo != null) {
			List<String> disallowListA = robotsTxtInfo.getDisallowedLinks("cis455crawler");
			List<String> disallowListB = robotsTxtInfo.getDisallowedLinks("*");
			Set<String> allowSetA = null;
			Set<String> allowSetB = null;
			if (robotsTxtInfo.getAllowedLinks("cis455crawler") != null) {
				allowSetA = new HashSet<>(robotsTxtInfo.getAllowedLinks("cis455crawler"));
			}
			if (robotsTxtInfo.getAllowedLinks("cis455crawler") != null) {
				allowSetB = new HashSet<>(robotsTxtInfo.getAllowedLinks("*"));
			}
			if (disallowListA != null) {
				for (String s : disallowListA) {
					if (s.equals(url.getFilePath()) && allowSetA != null && !allowSetA.contains(s)) {
						return false;
					}
				}
			}
			if (disallowListB != null) {
				for (String s : disallowListB) {
					if (s.equals(url.getFilePath()) && allowSetB != null && !allowSetB.contains(s)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public static boolean isOKtoCrawl(URLInfo urlInfo, String OriURL, CrawlerTask task) {
		try {
			String protocol = urlInfo.isSecure() ? "https" : "http";
			URL url = new URL(protocol, urlInfo.getHostName(), urlInfo.getPortNo(), urlInfo.getFilePath());
			URLConnection connection =  url.openConnection();

			if (db.getDocument(OriURL) != null) {
				Date lastCrawledTime = db.getDocumentCrawledTime(OriURL);
				connection.setIfModifiedSince(lastCrawledTime.getTime());
			}
			if (urlInfo.isSecure()) {
				((HttpsURLConnection)connection).setRequestMethod("HEAD");
				connection.setConnectTimeout(5000);
				if (((HttpsURLConnection) connection).getResponseCode() != 200) {
					if (((HttpsURLConnection) connection).getResponseCode() == 304) {
//						log.info(OriURL + ": Not Modified");
						return false;
					}
				}

				int size = connection.getContentLength() / (1024 * 1024);
				String type = connection.getContentType();
				if (size > CrawlerConfig.getSize()) { return false; }
				if (type == null) return false;
				if (type.contains("text/html")) {
					task.setHtml(true);
				}
				return (type.contains("text/html") || type.contains("xml"));
			} else {
				return false;
//				((HttpURLConnection)connection).setRequestMethod("HEAD");
//				if (((HttpURLConnection) connection).getResponseCode() != 200) {
//					if (((HttpsURLConnection) connection).getResponseCode() == 304) {
//						log.info(OriURL + ": Not Modified");
//						return false;
//					}
//				}
//				int size = connection.getContentLength() / (1024 * 1024);
//				String type = connection.getContentType();
//				if (type == null) return false;
//				if (type.contains("text/html")) {
//					task.setHtml(true);
//				}
//				if (size > CrawlerConfig.getSize()) return false;
//				return (type.contains("text/html") || type.contains("xml"));
			}
		} catch (IOException | ClassCastException | IllegalArgumentException e) {
			return false;
		}
	}

	public static RobotsTxtInfo parseRobotsTxt(String site) {
		RobotsTxtInfo robotsTxtInfo = new RobotsTxtInfo();
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(new URL(site).openStream()));
			String line;
			String userAgent = "";
			while ((line = in.readLine()) != null) {
				if (line.startsWith("User-agent: ")) {
					userAgent = line.substring(line.indexOf(" ") + 1);
					robotsTxtInfo.addUserAgent(userAgent);
				} else if (line.startsWith("Allow: ")) {
					if (line.contains("/")) robotsTxtInfo.addAllowedLink(userAgent, line.substring(line.indexOf("/")));
				} else if (line.startsWith("Disallow: ")) {
					if (line.contains("/")) robotsTxtInfo.addDisallowedLink(userAgent, line.substring(line.indexOf("/")));
				} else if (line.startsWith("Crawl-delay: ")) {
					robotsTxtInfo.addCrawlDelay(userAgent, Double.valueOf(line.substring(line.indexOf(" ") + 1)));
				} else if (line.startsWith("Sitemap: ")) {
					robotsTxtInfo.addSitemapLink(line.substring(line.indexOf(" ") + 1));
				}
			}
		} catch (IOException | NumberFormatException e) {
//			e.printStackTrace();
			return null;
		}
		return robotsTxtInfo;
	}
}

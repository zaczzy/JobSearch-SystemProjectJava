package crawler;

import crawler.info.RobotsTxtInfo;

import java.util.Date;

public class CrawlerTask {
	private String url;
	private RobotsTxtInfo robotsTxtInfo;
	private Date addTime;

	public CrawlerTask(String url, RobotsTxtInfo robotsTxtInfo) {
		this.url = url;
		this.robotsTxtInfo = robotsTxtInfo;
		this.addTime = new Date();
	}

	public String getUrl() {
		return this.url;
	}

	public Date getAddTime() {
		return this.addTime;
	}

	public RobotsTxtInfo getRobotsTxtInfo() {
		return this.robotsTxtInfo;
	}

}

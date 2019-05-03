package crawler;

import crawler.info.RobotsTxtInfo;

import java.util.Date;

public class CrawlerTask {
	private String url;
	private RobotsTxtInfo robotsTxtInfo;
	private Date addTime;
	private boolean isHtml;

	public CrawlerTask(String url, RobotsTxtInfo robotsTxtInfo) {
		this.url = url;
		this.robotsTxtInfo = robotsTxtInfo;
		this.addTime = new Date();
		this.isHtml = false;
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

	public boolean getIsHtml() {
		return isHtml;
	}

	public void setHtml(boolean ishtml) {
		isHtml = ishtml;
	}
}

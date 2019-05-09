package model;

import java.io.Serializable;
import java.util.Date;

public class Doc implements Serializable {

	String id;

	String url;
	Date crawledTime;
	String md5Hash;

	public Doc(String id, String url, Date crawledTime, String md5Hash) {
		this.id = id;
		this.url = url;
		this.crawledTime = crawledTime;
		this.md5Hash = md5Hash;
	}

	public String getId() {
		return this.id;
	}

	public Date getCrawledTime() {
		return crawledTime;
	}

	public void setCrawledTime(Date time) {
		this.crawledTime = time;
	}

	public String getMd5Hash() {
		return md5Hash;
	}

	public String getUrl() {
		return url;
	}

}

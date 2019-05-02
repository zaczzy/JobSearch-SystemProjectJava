package model;

import java.io.Serializable;
import java.util.Date;

public class Doc implements Serializable {

	int id;

	String url;
	String content;
	Date crawledTime;
	String md5Hash;

	public Doc(int id, String url, String content, Date crawledTime, String md5Hash) {
		this.id = id;
		this.url = url;
		this.content = content;
		this.crawledTime = crawledTime;
		this.md5Hash = md5Hash;
	}

	public int getId() {
		return this.id;
	}

	public String getContent() {
		return content;
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

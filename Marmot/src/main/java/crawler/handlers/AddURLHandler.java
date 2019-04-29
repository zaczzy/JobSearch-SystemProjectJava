package crawler.handlers;

import crawler.CrawlerTask;
import crawler.FilterSharedFactory;
import crawler.QueueFactory;
import crawler.RobotsHelper;
import crawler.info.RobotsTxtInfo;
import crawler.info.URLInfo;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;

public class AddURLHandler implements Route {


	@Override
	public String handle(Request req, Response resp) throws HaltException {
		String url = req.queryParams("url");
		URLInfo info = new URLInfo(url);
		String robotsLocation = info.isSecure() ? "https://" : "http://";
		info.setPortNo(info.isSecure() ? 443 : 80);
		robotsLocation += info.getHostName() + "/robots.txt";
		info.setFilePath(robotsLocation);
		RobotsTxtInfo robotsTxtInfo;
		if (FilterSharedFactory.robotsTxtInfoHashMap.containsKey(robotsLocation)) {
			robotsTxtInfo = FilterSharedFactory.robotsTxtInfoHashMap.get(robotsLocation);
		} else {
			robotsTxtInfo = RobotsHelper.parseRobotsTxt(robotsLocation);
			if (robotsTxtInfo != null) {
				FilterSharedFactory.robotsTxtInfoHashMap.put(robotsLocation, robotsTxtInfo);
			}
		}
		CrawlerTask task = new CrawlerTask(url, robotsTxtInfo);
		QueueFactory.getQueueInstance().add(task);
		return "";
	}
}

package crawler.handlers;

import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;
import storage.StorageInterface;

public class HomePageHandler implements Route {
	StorageInterface db;

	public HomePageHandler(StorageInterface db) {
		this.db = db;
	}

	@Override
	public String handle(Request req, Response resp) throws HaltException {
		StringBuilder body = new StringBuilder("Welcome " + req.session(false).attribute("firstName") + " " + req.session(false).attribute("lastName"));
		body.append("\r\n");
		return body.toString();
	}
}

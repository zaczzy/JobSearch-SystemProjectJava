package crawler.handlers;

import storage.StorageInterface;
import spark.HaltException;
import spark.Request;
import spark.Response;
import spark.Route;

public class LookupHandler implements Route {
	StorageInterface db;

	public LookupHandler(StorageInterface db) {
		this.db = db;
	}

	@Override
	public String handle(Request req, Response resp) throws HaltException {
		String url = req.queryParams("url");
		resp.type("text/html");
		return db.getDocument(url);
	}
}

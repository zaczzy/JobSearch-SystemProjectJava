package crawler.handlers;

import storage.StorageInterface;
import spark.*;

public class RegistrationHandler implements Route {
	StorageInterface db;

	public RegistrationHandler(StorageInterface db) {
		this.db = db;
	}

	@Override
	public String handle(Request req, Response resp) throws HaltException {
		String username = req.queryParams("username");
		String password = req.queryParams("password");
		String firstName = req.queryParams("firstName");
		String lastName = req.queryParams("lastName");
		int uId = db.addUser(username, password, firstName, lastName);
		Session session = req.session(true);
		session.maxInactiveInterval(300);
		session.attribute("username", username);
		session.attribute("firstName", firstName);
		session.attribute("lastName", lastName);
		session.attribute("uId", uId);
		resp.redirect("/index.html", 301);
		return "";
	}
}

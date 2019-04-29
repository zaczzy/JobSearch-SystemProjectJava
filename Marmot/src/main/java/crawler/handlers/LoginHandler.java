package crawler.handlers;

import storage.StorageInterface;
import spark.*;

public class LoginHandler implements Route {
	StorageInterface db;

	public LoginHandler(StorageInterface db) {
		this.db = db;
	}

	@Override
	public String handle(Request req, Response resp) throws HaltException {
		String user = req.queryParams("username");
		String pass = req.queryParams("password");

		System.err.println("Login request for " + user + " and " + pass);
		if (db.getSessionForUser(user, pass)) {
			System.err.println("Logged in!");
			Session session = req.session();
			String[] names = db.getFirstAndLastName(user, pass);
			session.maxInactiveInterval(300);
			session.attribute("username", user);
			session.attribute("firstName", names[0]);
			session.attribute("lastName", names[1]);
			resp.redirect("/index.html");
		} else {
			System.err.println("Invalid credentials");
			resp.redirect("/login-form.html");
		}


		return "";
	}
}

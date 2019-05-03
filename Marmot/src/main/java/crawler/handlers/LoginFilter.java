package crawler.handlers;

import storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Filter;
import spark.Request;
import spark.Response;


public class LoginFilter implements Filter {
	Logger logger = LogManager.getLogger(LoginFilter.class);

	public LoginFilter(StorageInterface db) {

	}

	@Override
	public void handle(Request req, Response response) throws Exception {
		if (!req.pathInfo().equals("/login-form.html") &&
						!req.pathInfo().equals("/login") &&
						!req.pathInfo().equals("/register") &&
						!req.pathInfo().equals("/register.html")
		) {
			logger.info("Request is NOT login/registration");
			if (req.session(false) == null) {
//                logger.info
				System.err.println("Not logged in - redirecting!");
				response.redirect("/login-form.html");
			} else {
//                logger.info
				System.err.println("Logged in!");
				req.attribute("user", req.session().attribute("user"));
			}

		} else {
//            logger.info
			System.err.println("Request is LOGIN FORM");
		}
	}
}

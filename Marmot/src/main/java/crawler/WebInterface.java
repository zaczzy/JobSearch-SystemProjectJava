package crawler;

import crawler.handlers.*;
import storage.StorageFactory;
import storage.StorageInterface;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;

public class WebInterface {
	public static void main(String[] args) {
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.INFO);
		if (args.length < 1 || args.length > 2) {
			System.out.println("Syntax: WebInterface {path} {root}");
			System.exit(1);
		}

		if (!Files.exists(Paths.get(args[0]))) {
			try {
				Files.createDirectory(Paths.get(args[0]));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		port(8080);
		StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);

		LoginFilter testIfLoggedIn = new LoginFilter(database);

		if (args.length == 2) {
			staticFiles.externalLocation(args[1]);
			staticFileLocation(args[1]);
		}

		before("/*", "POST", testIfLoggedIn);
		post("/register", new RegistrationHandler(database));
		get("/index.html", new HomePageHandler(database));
		post("/login", new LoginHandler(database));
		get("/login", new HomePageHandler(database));
		get("/logout", (request, response) -> {
			request.session(true);
			response.redirect("/login", 301);
			return "";
		});

		get("/lookup", new LookupHandler(database));
		awaitInitialization();
	}
}

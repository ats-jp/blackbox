package jp.ats.blackbox.backend;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Main {

	public static void main(String[] args) throws Exception {
		var properties = getApplicationPath().resolve("blackbox.properties");

		var config = new Properties();
		config.load(Files.newInputStream(properties));

		var application = (Application) Class.forName(config.getProperty("application-class")).getConstructor().newInstance();

		application.start(config);
	}

	public static Path getApplicationPath() {
		try {
			var uri = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			return Paths.get(uri).getParent();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}
}

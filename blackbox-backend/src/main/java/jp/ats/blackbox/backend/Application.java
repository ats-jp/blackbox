package jp.ats.blackbox.backend;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;

import jp.ats.blackbox.backend.web.CORSFilter;
import jp.ats.blackbox.web.BlendeeTransactionFilter;

public class Application {

	public static void main(String[] args) throws Exception {
		var path = getApplicationPath();
		var properties = path.resolve("blackbox.properties");

		var config = new Properties();
		config.load(Files.newInputStream(properties));

		var context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");

		context.setInitParameter("blendee-schema-names", "bb bb_stock");
		context.setInitParameter("blendee-log-stacktrace-filter", ".");
		context.setInitParameter("blendee-transaction-factory-class", "org.blendee.util.DriverManagerTransactionFactory");
		context.setInitParameter("blendee-error-converter-class", "org.blendee.dialect.postgresql.PostgreSQLErrorConverter");
		context.setInitParameter("blendee-use-lazy-transaction", "true");
		context.setInitParameter("blendee-logger-class", "org.blendee.jdbc.VoidLogger");
		context.setInitParameter("blendee-table-facade-package", "sqlassist");

		context.setInitParameter("blendee-jdbc-driver-class-name", "org.postgresql.Driver");
		context.setInitParameter("blendee-jdbc-url", config.getProperty("jdbc-url"));
		context.setInitParameter("blendee-jdbc-user", config.getProperty("jdbc-user"));
		context.setInitParameter("blendee-jdbc-password", config.getProperty("jdbc-password"));

		var server = new Server(Integer.parseInt(config.getProperty("server-port")));

		server.setHandler(context);

		var servletHolder = context.addServlet(ServletContainer.class, "/api/*");
		servletHolder.setInitParameter("jersey.config.server.provider.packages", "jp.ats.blackbox.backend.api.core");
		servletHolder.setInitParameter("jersey.config.server.provider.scanning.recursive", "false");

		var cors = context.addFilter(CORSFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
		cors.setInitParameter("cors-allowed-origins", config.getProperty("cors-allowed-origins"));

		context.addFilter(BlendeeTransactionFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));

		server.start();
		server.join();
	}

	public static Path getApplicationPath() {
		try {
			var uri = Application.class.getProtectionDomain().getCodeSource().getLocation().toURI();
			return Paths.get(uri).getParent();
		} catch (URISyntaxException e) {
			throw new IllegalStateException(e);
		}
	}
}

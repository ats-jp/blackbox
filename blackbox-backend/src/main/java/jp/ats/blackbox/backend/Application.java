package jp.ats.blackbox.backend;

import java.util.EnumSet;
import java.util.Properties;

import javax.servlet.DispatcherType;

import org.blendee.dialect.ToStringSQLExtractor;
import org.blendee.dialect.postgresql.PostgreSQLErrorConverter;
import org.blendee.util.DriverManagerTransactionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.glassfish.jersey.servlet.ServletContainer;

import jp.ats.blackbox.backend.web.CORSFilter;
import jp.ats.blackbox.backend.web.SecurityValuesFilter;
import jp.ats.blackbox.web.BlendeeTransactionFilter;

public interface Application {

	String blendeeSchemaNames();

	String[] apiPackages();

	default void start(Properties config) throws Exception {
		var context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
		context.setContextPath("/");

		context.setInitParameter("blendee-schema-names", blendeeSchemaNames());
		context.setInitParameter("blendee-log-stacktrace-filter", ".");
		context.setInitParameter("blendee-transaction-factory-class", DriverManagerTransactionFactory.class.getName());
		context.setInitParameter("blendee-error-converter-class", PostgreSQLErrorConverter.class.getName());
		context.setInitParameter("blendee-use-lazy-transaction", "true");
		context.setInitParameter("blendee-logger-class", config.getProperty("blendee-logger-class"));
		context.setInitParameter("blendee-log-stacktrace-filter", "blackbox");
		context.setInitParameter("blendee-sql-extractor-class", ToStringSQLExtractor.class.getName());
		context.setInitParameter("blendee-table-facade-package", "sqlassist");

		context.setInitParameter("blendee-jdbc-driver-class-name", "org.postgresql.Driver");
		context.setInitParameter("blendee-jdbc-url", config.getProperty("jdbc-url"));
		context.setInitParameter("blendee-jdbc-user", config.getProperty("jdbc-user"));
		context.setInitParameter("blendee-jdbc-password", config.getProperty("jdbc-password"));

		var server = new Server(Integer.parseInt(config.getProperty("server-port")));

		server.setHandler(context);

		var servletHolder = context.addServlet(ServletContainer.class, "/api/*");
		servletHolder.setInitParameter("jersey.config.server.provider.packages", String.join(",", apiPackages()));
		servletHolder.setInitParameter("jersey.config.server.provider.scanning.recursive", "false");

		//CORSをOFF テスト用
		if (!"off".equals(config.getProperty("cors-filter"))) {
			var cors = context.addFilter(CORSFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
			cors.setInitParameter("cors-allowed-origins", config.getProperty("cors-allowed-origins"));
		}

		context.addFilter(BlendeeTransactionFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));

		context.addFilter(SecurityValuesFilter.class, "/api/*", EnumSet.of(DispatcherType.REQUEST));

		server.start();
		server.join();
	}
}

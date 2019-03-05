package jp.ats.blackbox.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.JournalExecutor;

public class TransferManager implements ServletContextListener {

	private static final Logger logger = LogManager.getLogger(TransferManager.class);

	private static final JournalExecutor executor = new JournalExecutor();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		var context = sce.getServletContext();
		BlendeeStarter.start(context);

		executor.start();
		JobExecutor.start();
	}

	public static JournalExecutor getTransferExecutor() {
		return executor;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			//ここでエラーが起きても↓をstopするためにtry catchする
			executor.stop();
		} catch (Throwable t) {
			logger.fatal(t.getMessage(), t);
		}
	}
}

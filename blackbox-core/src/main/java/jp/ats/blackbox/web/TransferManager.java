package jp.ats.blackbox.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import jp.ats.blackbox.executor.JobExecutor;
import jp.ats.blackbox.executor.TransferExecutor;

public class TransferManager implements ServletContextListener {

	private static final TransferExecutor executor = new TransferExecutor();

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		var context = sce.getServletContext();
		BlendeeStarter.start(context);

		executor.start();
		JobExecutor.start();
	}

	public static TransferExecutor getTransferExecutor() {
		return executor;
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
			//ここでエラーが起きても↓をstopするためにtry catchする
			executor.stop();
		} catch (Throwable t) {
			//TODO エラーのログ出力
			t.printStackTrace();
		}

		try {
			JobExecutor.stop();
		} catch (Throwable t) {
			//TODO エラーのログ出力
			t.printStackTrace();
		}
	}
}

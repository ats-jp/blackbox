package jp.ats.blackbox.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.blendee.jdbc.BlendeeManager;
import org.blendee.jdbc.Transaction;
import org.blendee.util.Blendee;

/**
 * {@link Filter} の範囲でトランザクションを管理するためのクラスです。
 * @author 千葉 哲嗣
 */
public class BlendeeTransactionFilter implements Filter {

	private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

	/**
	 * 現在のトランザクションを返します。
	 *
	 * @return 現在のトランザクション
	 */
	public static Transaction transaction() {
		return transaction.get();
	}

	@Override
	public void init(final FilterConfig config) throws ServletException {
		BlendeeStarter.start(config.getServletContext());
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
		throws IOException, ServletException {
		if (BlendeeManager.get().startsTransaction()) {
			chain.doFilter(request, response);
			return;
		}

		try {
			Blendee.execute(t -> {
				transaction.set(t);
				chain.doFilter(request, response);
			});
		} catch (Throwable t) {
			Throwable rootCause = ServletUtils.getRootCause(t);
			throw new ServletException(rootCause);
		} finally {
			transaction.remove();
		}
	}

	@Override
	public void destroy() {}
}

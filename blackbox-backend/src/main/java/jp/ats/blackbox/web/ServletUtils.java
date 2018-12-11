package jp.ats.blackbox.web;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

class ServletUtils {

	private ServletUtils() {}

	static Throwable getRootCause(Throwable t) {
		if (t instanceof ServletException)
			return getRootCause((ServletException) t);
		if (t instanceof JspException)
			return getRootCause((JspException) t);
		return t;
	}

	static void noCache(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store, max-age=0, no-cache");
		response.addHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "no-cache");
	}

	private static Throwable getRootCause(ServletException e) {
		Throwable rootCause = e.getRootCause();
		if (rootCause == null)
			return e;
		return getRootCause(rootCause);
	}

	private static Throwable getRootCause(JspException e) {
		Throwable rootCause = e.getCause();
		if (rootCause == null)
			return e;
		return getRootCause(rootCause);
	}
}

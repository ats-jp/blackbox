package jp.ats.blackbox.backend.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import jp.ats.blackbox.common.U;
import jp.ats.blackbox.core.persistence.SecurityValues;

public class SecurityValuesFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			SecurityValues.start(U.PRIVILEGE_ID);
			chain.doFilter(request, response);
		} finally {
			SecurityValues.end();
		}
	}

	@Override
	public void destroy() {
	}
}

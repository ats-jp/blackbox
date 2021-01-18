package jp.ats.blackbox.backend.web;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CORSFilter implements Filter {

	private String[] origins = {};

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
		throws IOException, ServletException {

		var request = (HttpServletRequest) servletRequest;
		var response = (HttpServletResponse) servletResponse;

		var clientOrigin = request.getHeader("Origin");
		if (!origins().stream().anyMatch(origin -> origin.equals(clientOrigin)))
			return;

		response.setHeader("Access-Control-Allow-Origin", clientOrigin);
		response.setHeader("Access-Control-Allow-Credentials", "true");
		response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		response.setHeader("Access-Control-Max-Age", "3600");

		chain.doFilter(request, servletResponse);
	}

	@Override
	public void init(FilterConfig config) throws ServletException {
		setOrigins(config.getInitParameter("cors-allowed-origins").split(" +"));
	}

	private synchronized void setOrigins(String[] origins) {
		this.origins = origins;
	}

	private synchronized List<String> origins() {
		return Arrays.asList(origins);
	}

	@Override
	public void destroy() {
	}
}

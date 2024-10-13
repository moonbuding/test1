package com.unimelb.swen90007.jspapp.controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * This filter handles CORS (Cross-Origin Resource Sharing) for all servlets.
 */
@WebFilter("/*")
public class CorsFilter implements Filter {

    /**
     * Applies CORS headers to the response and passes the request and response
     * to the next filter in the chain.
     *
     * @param request  the request object.
     * @param response the response object.
     * @param chain    the filter chain.
     * @throws IOException      if an input or output error occurs during the
     *                          filter operation.
     * @throws ServletException if a servlet-specific error occurs during the
     *                          filter operation.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        httpResponse.setHeader("Access-Control-Allow-Methods", "PUT, POST, GET, OPTIONS, DELETE");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        httpResponse.setHeader("Access-Control-Allow-Headers", "x-requested-with, Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        chain.doFilter(request, response);
    }

    /**
     * Initializes the filter.
     *
     * @param filterConfig the filter configuration object.
     */
    @Override
    public void init(FilterConfig filterConfig) {
    }

    /**
     * Destroys the filter and performs any necessary cleanup.
     */
    @Override
    public void destroy() {
    }
}

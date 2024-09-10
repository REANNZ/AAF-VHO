package aaf.base.util.http;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

// This is adapted from a helpful answer at 
// http://stackoverflow.com/questions/1046721/accessing-the-raw-body-of-a-put-or-post-request
// Solves issues with API needing raw access to request body

public class MultiReadServletFilter implements Filter {

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    if(servletRequest instanceof HttpServletRequest) {
      HttpServletRequest request = (HttpServletRequest) servletRequest;

      filterChain.doFilter(new MultiReadHttpServletRequest(request), servletResponse);
    }
  }

  public void init(FilterConfig filterConfig) throws ServletException {
  }

  public void destroy() {
  }
}

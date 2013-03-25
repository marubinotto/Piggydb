package marubinotto.util.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CustomizedSecurityFilter implements Filter {

  private static Log logger = LogFactory.getLog(CustomizedSecurityFilter.class);

  public void init(FilterConfig config) throws ServletException {
  }

  public void doFilter(
    ServletRequest request, 
    ServletResponse response, 
    FilterChain chain) 
  throws IOException, ServletException {
    logger.debug("doFilter");
    chain.doFilter(new CustomizedSecurityRequestWrapper((HttpServletRequest)request), response);
  }

  public void destroy() {
  }
}

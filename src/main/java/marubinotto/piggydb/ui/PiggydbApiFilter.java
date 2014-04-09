package marubinotto.piggydb.ui;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import spark.Access;
import spark.route.RouteMatcherFactory;
import spark.webserver.MatcherFilter;

public class PiggydbApiFilter implements Filter {
  
  private String filterPath;
  private MatcherFilter matcherFilter;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    Access.runFromServlet();
      
    ApplicationContext applicationContext = 
      WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());

    final PiggydbApi piggydbApi = new PiggydbApi();
    piggydbApi.setApplicationContext(applicationContext);
    piggydbApi.init();

    this.filterPath = getFilterPath(filterConfig);
    this.matcherFilter = new MatcherFilter(RouteMatcherFactory.get(), true, false);
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
  throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request; // NOSONAR
    final String relativePath = getRelativePath(httpRequest, filterPath);
    // System.out.println("filterPath: " + filterPath + ", relativePath: " + relativePath);
    HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(httpRequest) {
      @Override
      public String getRequestURI() {
        return relativePath;
      }
    };
    this.matcherFilter.doFilter(requestWrapper, response, chain);
  }

  @Override
  public void destroy() {
      // ignore
  }
  
  
  // from spark.servlet.FilterTools
  
  private static final String SLASH_WILDCARD = "/*";
  private static final String SLASH = "/";
  private static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";
  
  static String getRelativePath(HttpServletRequest request, String filterPath) {
    String path = request.getRequestURI();
    String contextPath = request.getContextPath();

    path = path.substring(contextPath.length());
    if (path.length() > 0) {
      path = path.substring(1);
    }
    if (!path.startsWith(filterPath) && filterPath.equals(path + SLASH)) {
      path += SLASH;
    }
    if (path.startsWith(filterPath)) {
      path = path.substring(filterPath.length());
    }
    if (!path.startsWith(SLASH)) {
      path = SLASH + path;
    }
    return path;
  }

  static String getFilterPath(FilterConfig config) {
    String result = config.getInitParameter(FILTER_MAPPING_PARAM);
    if (result == null || result.equals(SLASH_WILDCARD)) {
      return "";
    } 
    else if (!result.startsWith(SLASH) || !result.endsWith(SLASH_WILDCARD)) {
      throw new RuntimeException(
        "The " + FILTER_MAPPING_PARAM + " must start with \"/\" and end with \"/*\". It's: " + result); // NOSONAR
    }
    return result.substring(1, result.length() - 1);
  }
}

package marubinotto.piggydb.api;

import static java.lang.Integer.parseInt;
import static marubinotto.util.CollectionUtils.map;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNumeric;
import static spark.Spark.before;
import static spark.Spark.get;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.service.DomainModelBeans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.servlet.SparkApplication;

public class PiggydbApi implements SparkApplication {
  
  private static Log log = LogFactory.getLog(PiggydbApi.class);
  
  protected ApplicationContext applicationContext;
  protected DomainModelBeans domain;
  
  protected Session session;
  protected User user;
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.domain = new DomainModelBeans(this.applicationContext);
  }

  @Override
  public void init() {
    before(new Filter() {
      @Override
      public void handle(Request request, Response response) {
        log.debug("path: " + request.raw().getRequestURI());
        
        session = new Session(
          request.raw(), 
          response.raw(), 
          domain.getAuthentication().isEnableAnonymous());
        
        user = session.getUser();
        if (user == null) user = autoLoginAsAnonymous();
        
        if (!request.raw().getRequestURI().equals("/login")) {
          if (user == null) {
            halt(401, "Unauthorized");
          }
        }
      }
    });
    
    get(new ApiRoute("/login") {
      @Override
      protected Object doHandle(Request request, Response response) throws Exception {
        String userName = request.queryParams("user");
        String password = request.queryParams("password");
        String maxAge = request.queryParams("maxAge");

        session.invalidateIfExists();
        if (isNotBlank(userName) && isNotBlank(password)) {
          user = domain.getAuthentication().authenticate(userName, password);
        }
        
        if (user == null) {
          return error("invalid-credentials", "Couldn't log in");
        }
        
        session.start(user, isNumeric(maxAge) ? parseInt(maxAge) : null);
        
        return map("sessionId", session.getId());
      }
    });
    
    get(new ApiRoute("/logout") {
      @Override
      protected Object doHandle(Request request, Response response) throws Exception {
        session.invalidateIfExists();
        return "Bye";
      }
    });
    
    get(new ApiRoute("/hello") {
      @Override
      protected Object doHandle(Request request, Response response) throws Exception {
        return "Hello!";
      }
    });
  }
  
  
  // Internals
  
  protected User autoLoginAsAnonymous() {
    User user = this.domain.getAuthentication().authenticateAsAnonymous();
    if (user == null) return null;

    this.session.start(user, null);
    log.debug("Anonymous session created");
    return user;
  }
}

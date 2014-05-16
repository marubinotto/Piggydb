package marubinotto.piggydb.api;

import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.service.DomainModelBeans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import static spark.Spark.*;
import spark.*;
import spark.servlet.SparkApplication;

public class PiggydbApi implements SparkApplication {
  
  private static Log log = LogFactory.getLog(PiggydbApi.class);
  
  protected ApplicationContext applicationContext;
  protected DomainModelBeans domain;
  
  protected Session session;
  protected User user;
  
  protected static final ObjectWriter JSON = 
    new ObjectMapper().writerWithDefaultPrettyPrinter();
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
    this.domain = new DomainModelBeans(this.applicationContext);
  }
  
  protected User autoLoginAsAnonymous() {
    User user = this.domain.getAuthentication().authenticateAsAnonymous();
    if (user == null) return null;

    this.session.start(user, null);
    log.debug("Anonymous session created");
    return user;
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
    
    get(new Route("/login") {
      @Override
      public Object handle(Request request, Response response) {
        return "Hello World!";
      }
    });
    
    get(new Route("/logout") {
      @Override
      public Object handle(Request request, Response response) {
        session.invalidateIfExists();
        return "Bye";
      }
    });
  }
}

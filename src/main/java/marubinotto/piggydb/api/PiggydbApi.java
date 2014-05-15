package marubinotto.piggydb.api;

import marubinotto.piggydb.ui.page.common.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import static spark.Spark.*;
import spark.*;
import spark.servlet.SparkApplication;

public class PiggydbApi implements SparkApplication {
  
  private static Log log = LogFactory.getLog(PiggydbApi.class);
  
  private ApplicationContext applicationContext;
  private Session session;
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void init() {
    before(new Filter() {
      @Override
      public void handle(Request request, Response response) {
        // session = new Session(request.raw(), response.raw()
      }
    });
    
    get(new Route("/login") {
      @Override
      public Object handle(Request request, Response response) {
         return "Hello World!";
      }
    });
  }
}

package marubinotto.piggydb.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;

import static spark.Spark.*;
import spark.*;
import spark.servlet.SparkApplication;

public class PiggydbApi implements SparkApplication {
  
  private static Log log = LogFactory.getLog(PiggydbApi.class);
  
  private ApplicationContext applicationContext;
  
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public void init() {
    get(new Route("/hello") {
      @Override
      public Object handle(Request request, Response response) {
         return "Hello World!";
      }
    });
  }
}

package marubinotto.piggydb.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Session {

  private static Log logger = LogFactory.getLog(Session.class);
  
  private HttpServletRequest request;
  
  public Session(HttpServletRequest request) {
    this.request = request;
  }
}

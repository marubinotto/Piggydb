package marubinotto.piggydb.api;

import static marubinotto.util.CollectionUtils.map;

import org.apache.commons.lang.UnhandledException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import spark.Request;
import spark.Response;
import spark.Route;

abstract class ApiRoute extends Route {

  public ApiRoute(String path) {
    super(path);
  }
  
  public ApiRoute(String path, String acceptType) {
    super(path, acceptType);
  }

  @Override
  public Object handle(Request request, Response response) {
    try {
      response.type(CONTENT_TYPE_JSON);
      return json(doHandle(request, response));
    }
    catch (Exception e) {
      return error("system-error", e.toString());
    }
  }
  
  
  // JSON

  abstract protected Object doHandle(Request request, Response response)
  throws Exception;
  
  public static final String CONTENT_TYPE_JSON = "application/json";
  protected static final ObjectWriter JSON = 
    new ObjectMapper().writerWithDefaultPrettyPrinter();
  
  public static String json(Object object) {
    try {
      return JSON.writeValueAsString(object);
    }
    catch (JsonProcessingException e) {
      throw new UnhandledException(e);
    }
  }
  
  
  // Utilities
  
  public static Object error(String code, String message) {
    return map("error", code).map("message", message);
  }
}

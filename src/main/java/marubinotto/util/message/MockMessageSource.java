package marubinotto.util.message;

import org.apache.commons.lang.ArrayUtils;

public class MockMessageSource implements MessageSource {

  public String getMessage(String code) {
    return code;
  }

  public String getMessage(String code, Object arg) {
    return code + " " + arg;
  }

  public String getMessage(String code, Object[] args) {
    return code + " " + ArrayUtils.toString(args);
  }

  public String getMessage(MessageCode messageCode) {
    return getMessage(messageCode.getCode(), messageCode.getArguments());
  }
}

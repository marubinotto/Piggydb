package marubinotto.piggydb.ui.page.command;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

public class CompleteTagName2 extends AbstractCommand {

  public static final String CONTENT_TYPE = "application/json";
  
  public String term;
  
  @Override 
  protected void execute() throws Exception {
    this.term = modifyIfGarbledByTomcat(this.term);
    
    List<String> names = isNotBlank(this.term) ? 
      getDomain().getTagRepository().getNamesLike(this.term) : 
        new ArrayList<String>();
    
    HttpServletResponse response = getContext().getResponse();
    response.setContentType(CONTENT_TYPE);
    JSON.writeValue(response.getOutputStream(), names);
  }
}

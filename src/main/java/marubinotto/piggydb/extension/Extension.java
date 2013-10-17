package marubinotto.piggydb.extension;

import javax.servlet.ServletContext;

import marubinotto.piggydb.ui.WarSetting;
import marubinotto.piggydb.ui.page.common.HtmlFragments;
import marubinotto.piggydb.ui.page.common.WebResourcePaths;

import org.springframework.context.ApplicationContext;

public abstract class Extension {
  
  protected WarSetting warSetting;
  protected WebResourcePaths resourcePaths;
  protected HtmlFragments htmlFragments;

  public final void init(
    ServletContext servletContext,
    ApplicationContext appContext) 
  throws Exception {
    this.warSetting = (WarSetting)appContext.getBean("warSetting");
    this.resourcePaths = new WebResourcePaths(
      servletContext.getContextPath(), warSetting.getPiggydbVersion());
    this.htmlFragments = new HtmlFragments(resourcePaths);
    doInit(servletContext, appContext);
  }
  
  protected abstract void doInit(
    ServletContext servletContext,
    ApplicationContext appContext) 
  throws Exception;
}

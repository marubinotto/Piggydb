package marubinotto.piggydb.ui.page;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import marubinotto.piggydb.impl.db.H2JdbcUrl;
import marubinotto.piggydb.model.entity.RawEntity;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.piggydb.ui.page.common.AbstractBorderPage;
import marubinotto.piggydb.ui.page.common.DatabaseSpecificBeans;
import marubinotto.util.Size;
import marubinotto.util.procedure.Procedure;
import net.sf.click.control.Form;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.text.StrBuilder;

public class SystemInfoPage extends AbstractBorderPage {

  private DatabaseSpecificBeans dbSpecificBeans;

  @Override
  protected String[] getAuthorizedRoles() {
    return new String[]{Role.OWNER.getName()};
  }

  //
  // Control
  //

  public static final int DATABASE_TITLE_MAX_LENGTH = 50;
  public Form databaseTitleForm = new Form();
  private TextField titleField = new TextField("title", false);

  @Override
  public void onInit() {
    super.onInit();
    initControls();
    this.dbSpecificBeans = new DatabaseSpecificBeans(getApplicationContext());
  }

  private void initControls() {
    this.titleField.setSize(40);
    this.titleField.setMaxLength(DATABASE_TITLE_MAX_LENGTH);
    this.titleField.setAttribute("class", "watermarked");
    this.titleField.setTitle(getMessage("SystemInfoPage-database-title"));
    try {
      this.titleField.setValue(getDomain().getGlobalSetting().getDatabaseTitle());
    }
    catch (Exception e) {
      throw new UnhandledException(e);
    }
    this.databaseTitleForm.add(this.titleField);
    this.databaseTitleForm.add(
      new Submit("update", getMessage("update"), this, "onUpdateDatabaseTitleClick"));
  }

  public boolean onUpdateDatabaseTitleClick() throws Exception {
    if (!this.databaseTitleForm.isValid()) {
      return true;
    }

    final String title = this.titleField.getValue();
    getDomain().getTransaction().execute(new Procedure() {
      public Object execute(Object input) throws Exception {
        getDomain().getGlobalSetting().setDatabaseTitle(title);
        return null;
      }
    });

    setRedirectToThisPage(getMessage("SystemInfoPage-database-title-updated"));
    return false;
  }

  //
  // Model
  //

  public static class Settings {
    public String databasePath;
    public boolean anonymousAccess;
    public boolean entityChangeableOnlyForCreator;
  }

  public static class DatabaseStatistics {
    public Long fragments;
    public Long relations;
    public Long tags;
    public Long taggings;
    public Long filters;
  }

  public Settings settings;
  public DatabaseStatistics databaseStatistics;
  public String debugInfo;

  @Override
  protected void setModels() throws Exception {
    super.setModels();

    importCss("style/piggydb-system-info.css", true, null);
    getFormat().setEmptyString("N/A");

    setSettings();
    setDatabaseStatistics();

    try {
      setDebugInfo();
    }
    catch (Exception e) {
      StrBuilder info = new StrBuilder();
      info.appendln(e.toString());
      for (StackTraceElement ste : e.getStackTrace())
        info.appendln(ste.toString());
      this.debugInfo = info.toString();
    }
  }

  private void setSettings() throws SQLException {
    this.settings = new Settings();
    H2JdbcUrl h2JdbcUrl = this.dbSpecificBeans.getH2JdbcUrl();
    if (h2JdbcUrl != null) this.settings.databasePath = h2JdbcUrl.getDatabasePath();
    this.settings.anonymousAccess = getDomain().getAuthentication().isEnableAnonymous();
    this.settings.entityChangeableOnlyForCreator = RawEntity.changeableOnlyForCreator;
  }

  private void setDatabaseStatistics() throws Exception {
    this.databaseStatistics = new DatabaseStatistics();
    this.databaseStatistics.fragments = getDomain().getFragmentRepository().size();
    this.databaseStatistics.relations = getDomain().getFragmentRepository().countRelations();
    this.databaseStatistics.tags = getDomain().getTagRepository().size();
    this.databaseStatistics.taggings = getDomain().getTagRepository().countTaggings();
    this.databaseStatistics.filters = getDomain().getFilterRepository().size();
  }

  private void setDebugInfo() throws Exception {
    StrBuilder info = new StrBuilder();
    
    // Java
    info.appendln("Java:");
    info.appendln("  version: " + System.getProperty("java.version"));
    info.appendln("  vendor: " + System.getProperty("java.vendor"));
    info.appendln("  directory: " + System.getProperty("java.home"));

    // Memory
    Runtime rt = Runtime.getRuntime();
    info.appendln(
      "Memory: free " + new Size(rt.freeMemory()) + 
      " / total " + new Size(rt.totalMemory()) + 
      " / max " + new Size(rt.maxMemory()));

    // Database version
    DatabaseMetaData metaData = this.dbSpecificBeans.getJdbcConnection().getMetaData();
    String databaseVersion = 
      metaData.getDatabaseProductName() + " " + 
      metaData.getDatabaseProductVersion();
    info.appendln("Database version: " + databaseVersion);

    this.debugInfo = info.toString();
  }
}

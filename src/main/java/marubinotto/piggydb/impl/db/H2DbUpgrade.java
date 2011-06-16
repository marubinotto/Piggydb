package marubinotto.piggydb.impl.db;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import marubinotto.piggydb.impl.PigDump;
import marubinotto.util.RdbUtils;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.procedure.Transaction;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.dataset.DataSetException;
import org.springframework.beans.factory.InitializingBean;

public class H2DbUpgrade implements InitializingBean {
	
	private static Log log = LogFactory.getLog(H2DbUpgrade.class);
	
	private H2JdbcUrl h2JdbcUrl;
	private DataSource dataSource;
	private String username;
	private String password;
	private Transaction transaction;
	private DatabaseSchema databaseSchema;
	
	private File databaseDir;
	
	private String oldVersion;
	private String newVersion;
	
	public H2DbUpgrade() {
	}

	public void setH2JdbcUrl(H2JdbcUrl h2JdbcUrl) {
		this.h2JdbcUrl = h2JdbcUrl;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setTransaction(Transaction transaction) {
		this.transaction = transaction;
	}
	
	public void setDatabaseSchema(DatabaseSchema databaseSchema) {
		this.databaseSchema = databaseSchema;
	}

	public void afterPropertiesSet() throws Exception {
		if (!isDatabaseFileFormatVersion1_1()) {
			log.debug("No need for upgrading: the database file format is not v1.1");
			return;
		}
		
		log.info("Upgrading the H2 database files ...");
		File exportFilePath = exportDatabase();
		renameDatabaseFiles(exportFilePath);
		restoreDatabaseWithNewVersion(exportFilePath);
		log.info("Completed upgrading the database: " + this.oldVersion + " -> " + this.newVersion);
	}
	
	protected boolean isDatabaseFileFormatVersion1_1() throws MalformedURLException {
		String databasePath = this.h2JdbcUrl.getDatabasePath();
		if (!databasePath.startsWith("file:")) return false;
		
		this.databaseDir = FileUtils.toFile(new URL(this.h2JdbcUrl.getDatabasePrefix()));
		if (!this.databaseDir.isDirectory()) return false;
		
		if (FileUtils.toFile(new URL(databasePath + ".h2.db")).isFile()) return false;
		if (!FileUtils.toFile(new URL(databasePath + ".data.db")).isFile()) return false;
		return true;
	}
	
	private static final String JDBC_URL_PREFIX_V1_1 = "jdbc:h2v1_1:";
	
	protected Connection connectWithVersion1_1() 
	throws SQLException, IOException, ClassNotFoundException {
		// DB_CLOSE_DELAY=0 because it will avoid file locking after exporting
		String url = JDBC_URL_PREFIX_V1_1 + 
			this.h2JdbcUrl.getDatabasePath() + ";DB_CLOSE_DELAY=0";
		
		log.info("Connecting to the v1.1 database: " + url);
		
		Class.forName("org.h2.upgrade.v1_1.Driver");
        Properties info = new Properties();
        info.setProperty("user", this.username);
        info.setProperty("password", this.password);
        return DriverManager.getConnection(url, info);
    }
	
	protected File getExportFilePath() throws MalformedURLException {
		return FileUtils.toFile(new URL(this.h2JdbcUrl.getDatabasePath() + ".dump.xml"));
	}
	
	protected File exportDatabase() 
	throws SQLException, IOException, DataSetException, ClassNotFoundException {
		File exportFilePath = getExportFilePath();
		exportFilePath.delete();
		
		log.info("Exporting the old dababase to: " + exportFilePath);
		Connection connection = connectWithVersion1_1();
		this.oldVersion = connection.getMetaData().getDatabaseProductVersion();
		log.info("oldVersion: " + this.oldVersion);
		
		OutputStream output = new BufferedOutputStream(new FileOutputStream(exportFilePath, false));
		try {
			RdbUtils.exportAsXml(connection, PigDump.TABLES, output);
		}
		finally {
			output.close();
			connection.close();
		}
		
		return exportFilePath;
	}
	
	private static final String BACKUP_SUFFIX = ".v1_1";
	
	protected void renameDatabaseFiles(File exportFilePath) {
		log.info("Renaming the old database files ...");
		String databaseName = this.h2JdbcUrl.getDatabaseName();
		for (File file : this.databaseDir.listFiles()) {
			if (file.isDirectory()) continue;
			if (file.equals(exportFilePath)) continue;
			if (file.getName().startsWith(databaseName + ".")) {
				file.renameTo(new File(file.getPath() + BACKUP_SUFFIX));
			}
		}
	}
	
	protected void restoreDatabaseWithNewVersion(File exportFilePath) throws Exception {
		log.info("Restoring the database with new version ...");
		final InputStream fileInput = FileUtils.openInputStream(exportFilePath);
		try {
			this.transaction.execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					databaseSchema.update();
					
					log.info("Importing the exported XML file ...");
					Connection connection = RdbUtils.getSpringTransactionalConnection(dataSource);
					newVersion = connection.getMetaData().getDatabaseProductVersion();
					log.info("newVersion: " + newVersion);
					RdbUtils.cleanImportXml(connection,  fileInput);
					return null;
				}
			});
		}
		finally {
			fileInput.close();
		}
	}
}

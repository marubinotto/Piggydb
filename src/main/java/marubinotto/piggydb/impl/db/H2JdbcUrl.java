package marubinotto.piggydb.impl.db;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;

import marubinotto.util.Assert;
import marubinotto.util.FileSystemUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.Resource;
import org.springframework.web.context.ServletContextAware;

public class H2JdbcUrl implements ServletContextAware {

	private static Log logger = LogFactory.getLog(H2JdbcUrl.class);
	
	public static final String DEFAULT_DATABASE_NAME = "piggydb";
	
	private String prefix;
	private String databaseName;
	private String deployName;
	private Resource logFile;

	public void setServletContext(ServletContext context) {
		String path = null;
		try {
			path = context.getContextPath();
		}
		catch (NoSuchMethodError e) {
			logger.info("Servlet 2.5 is not available.");
		}
		logger.debug("getContextPath: " + path);
		if (path == null) return;
		
		if (path.startsWith("/")) path = path.substring(1);
		this.deployName = path;
		logger.debug("deployName: " + this.deployName);
	}

	public void setDatabasePrefix(String databasePrefix) throws Exception {
		Assert.Arg.notNull(databasePrefix, "databasePrefix");
		this.prefix = preparePrefix(databasePrefix);
	}
	
	public String getDatabasePrefix() {
		return this.prefix;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getDatabaseName() {
		if (StringUtils.isNotBlank(this.databaseName)) {
			return this.databaseName;
		}
		if (StringUtils.isNotBlank(this.deployName)) {
			return this.deployName;
		}
		return DEFAULT_DATABASE_NAME;
	}
	
	public boolean isInMemory() {
		Assert.Property.requireNotNull(prefix, "prefix");
		return isInMemoryPrefix(this.prefix);
	}

	public void setLogFile(Resource logFile) {
		this.logFile = logFile;
	}
	
	private static final String URL_PREFIX = "jdbc:h2:";
	private static final String URL_SUFFIX = ";DB_CLOSE_DELAY=-1";
	
	/**
	 * Database path = prefix + name
	 */
	public String getDatabasePath() {
		Assert.Property.requireNotNull(prefix, "prefix");
		
		String path = this.prefix + StringUtils.defaultString(getDatabaseName(), "");
		return path;
	}
	
	public String getUrl() throws IOException {
		String url = URL_PREFIX + getDatabasePath() + URL_SUFFIX;
		logUrl(url);
		return url;
	}
	
	
// Internals
	
	private static boolean isInMemoryPrefix(String prefix) {
		return prefix.startsWith("mem:");
	}
	
	public static String toUrlWithoutEscape(File file) {
		String url = file.toURI().toString();
		try {
			return URLDecoder.decode(url, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
	}

	private static String preparePrefix(String original) throws Exception {
		String prepared = null;
		if (original.startsWith("~") || original.startsWith("file:~")) {
			String homeUrl = toUrlWithoutEscape(FileSystemUtils.getUserHome(false));
			if (homeUrl.endsWith("/")) homeUrl = StringUtils.chop(homeUrl);
			prepared = homeUrl + original.substring(original.indexOf('~') + 1);
		}
		else {
			prepared = original;
		}
		
		if (!isInMemoryPrefix(prepared) && !prepared.endsWith("/")) {
			prepared += "/";
		}
		return prepared;
	}

	@SuppressWarnings("unchecked")
	private void logUrl(String url) throws IOException {
		logger.info("jdbcUrl: " + url);
		if (this.logFile == null) return;
		
		File logFilePath = this.logFile.getFile();
		synchronized (getClass()) {
			List<String> urlList = new ArrayList<String>();
			if (logFilePath.isFile()) urlList = FileUtils.readLines(logFilePath);
			if (!urlList.contains(url)) {
				urlList.add(url);
				FileUtils.writeLines(logFilePath, urlList);
			}
		}
	}
}

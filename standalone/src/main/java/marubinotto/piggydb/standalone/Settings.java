package marubinotto.piggydb.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.beanutils.BeanUtils;

public class Settings {
	
	public static final String FILE_NAME = "launcher.properties";
	
	private File baseDir;
	
	private int port;
	private boolean launchBrowserWhenStartup;

	public Settings(File baseDir) throws Exception {
		this.baseDir = baseDir;
		
		Properties properties = new Properties();
		InputStream input = new FileInputStream(new File(this.baseDir, FILE_NAME));
		try {
			properties.load(input);
		}
		finally {
			input.close();
		}
		BeanUtils.populate(this, properties);
	}
	
	public File getResourcePath(String relativePath) {
		return new File(this.baseDir, relativePath);
	}
	
	public int getPort() {
		return this.port;
	}
	
	public void setPort(int port) {
		this.port = port;
	}

	public boolean isLaunchBrowserWhenStartup() {
		return this.launchBrowserWhenStartup;
	}

	public void setLaunchBrowserWhenStartup(boolean launchBrowserWhenStartup) {
		this.launchBrowserWhenStartup = launchBrowserWhenStartup;
	}
}
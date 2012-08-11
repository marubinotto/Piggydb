package marubinotto.piggydb.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

// TODO private static deployWebappFiles(ServletContext)
// TODO public static deployAll(ServletContext)
// TODO init(ServletContext)
// TODO user menu
public abstract class Extension {
	
	public static final String DEF_FILE_NAME = "META-INF/piggydb-extension-class";
	public static final String WEBAPP_DIR = "META-INF/webapp";

	public static Enumeration<URL> allDefFiles() throws IOException {
		return Extension.class.getClassLoader().getResources(DEF_FILE_NAME);
	}
}

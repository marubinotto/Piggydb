package marubinotto.piggydb.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.context.ApplicationContext;

import marubinotto.util.Assert;

// TODO init(ServletContext, ApplicationContext)
// TODO user menu
public abstract class Extension {
	
	private static Log logger = LogFactory.getLog(Extension.class);
	
	public static final String DEF_FILE_NAME = "META-INF/piggydb-extension-class";
	public static final String WEBAPP_DIR = "META-INF/webapp";

	public static Enumeration<URL> allDefFiles() throws IOException {
		return getResources(DEF_FILE_NAME);
	}
	
	private static Enumeration<URL> getResources(String path) throws IOException {
		return Extension.class.getClassLoader().getResources(path);
	}
	
	private static void deployWebappFiles(ServletContext servletContext) 
	throws IOException {
		FileSystemManager fsManager = VFS.getManager();
		FileObject webappDir = fsManager.resolveFile(servletContext.getRealPath("/"));
		logger.info("Webapp dir: " + webappDir.getName());
		
		for (Enumeration<URL> dirUrls = getResources(WEBAPP_DIR); dirUrls.hasMoreElements();) {
			FileObject extWebappDir = fsManager.resolveFile(dirUrls.nextElement().toExternalForm());
			if (extWebappDir.getType().hasChildren()) {
				logger.info("Deploying webapp files: " + extWebappDir.getName());
				webappDir.copyFrom(extWebappDir, new AllFileSelector());
			}
		}
	}
	
	public static void deployAll(
		ServletContext servletContext,
		ApplicationContext appContext) 
	throws IOException {
		Assert.Arg.notNull(servletContext, "servletContext");
		deployWebappFiles(servletContext);
	}
}

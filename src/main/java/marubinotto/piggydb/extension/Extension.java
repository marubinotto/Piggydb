package marubinotto.piggydb.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.springframework.context.ApplicationContext;

import marubinotto.util.Assert;

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
	
	public static void deployWebappFiles(ServletContext servletContext) 
	throws IOException {
		Assert.Arg.notNull(servletContext, "servletContext");
		
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
	
	public static void initAll(
		ServletContext servletContext,
		ApplicationContext appContext) 
	throws IOException {
		Assert.Arg.notNull(servletContext, "servletContext");
		Assert.Arg.notNull(appContext, "appContext");
		
		// initialize extensions
		for (Enumeration<URL> files = allDefFiles(); files.hasMoreElements();) {
			URL defFile = files.nextElement();
			try {
				initExtension(defFile, servletContext, appContext);
			}
			catch (Exception e) {
				logger.error("Extension initialization error: " + defFile.toExternalForm());
			}
		}
	}
	
	private static void initExtension(
		URL defFile, 
		ServletContext servletContext,
		ApplicationContext appContext) 
	throws Exception {
		String className = IOUtils.toString(defFile).trim();
		logger.info("Initializing extension: " + className);
		Extension extension = (Extension)Class.forName(className).newInstance();
		extension.init(servletContext, appContext);
	}
	
	public void init(
		ServletContext servletContext,
		ApplicationContext appContext) 
	throws Exception {
		// Do nothing by default
	}
}

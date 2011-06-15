package marubinotto.piggydb.ui.page;

import static marubinotto.util.CollectionUtils.set;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.UnhandledException;

import marubinotto.util.Assert;

public class WebResources {

	private String contextPath;
	private String piggydbVersion;
	
	public WebResources(String contextPath, String piggydbVersion) {
		Assert.Arg.notNull(contextPath, "contextPath");
		Assert.Arg.notNull(piggydbVersion, "piggydbVersion");
		this.contextPath = contextPath;
		this.piggydbVersion = piggydbVersion;
	}
	
	public String contextPath() {
		return this.contextPath;
	}
	
	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String resourcePath(String relativePath, boolean versioning) {
		String path = this.contextPath + "/" + relativePath;
		if (versioning) path = path + "?" + this.piggydbVersion;
		return path;
	}

	public String fragmentPath(Long id) {
		return this.contextPath + "/fragment.htm?id=" + id;
	}
	
	public String tagPath(Long id) {
		return this.contextPath + "/tag.htm?id=" + id;
	}

	public String tagPathByName(String name) {
		URLCodec codec = new URLCodec();
		try {
			return this.contextPath + "/tag.htm?name=" + codec.encode(name, AbstractPage.CHAR_ENCODING);
		}
		catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
	}
	
	public String commandPath(String name) {
		return this.contextPath + "/command/" + name + ".htm";
	}
	
	public String fragmentFilePath(Long id) {
		return commandPath("get-file") + "?id=" + id;
	}
	
	public String fileIconPath(String extension) {
		return this.contextPath + "/images/file-icons/" + getFileIconName(extension);
	}
	
	public String userPath(String name) {
		URLCodec codec = new URLCodec();
		try {
			return this.contextPath + "/user.htm?name=" + codec.encode(name, AbstractPage.CHAR_ENCODING);
		} 
		catch (UnsupportedEncodingException e) {
			throw new UnhandledException(e);
		}
	}
	
// Internal
	
	private static final Set<String> AVAILABLE_FILE_ICONS = set("doc", "pdf", "ppt", "xls");
	private static final String DEFAULT_FILE_ICON = "default.png";
	
	private static String getFileIconName(String extension) {
		if (extension != null && AVAILABLE_FILE_ICONS.contains(extension)) {
			return extension + ".png";
		}
		else {
			return DEFAULT_FILE_ICON;
		}
	}
}

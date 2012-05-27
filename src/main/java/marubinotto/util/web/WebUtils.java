package marubinotto.util.web;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import marubinotto.util.Assert;
import marubinotto.util.time.DateTime;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WebUtils {

	private static Log logger = LogFactory.getLog(WebUtils.class);

	public static String toQueryString(Map<String, Object> parameters, String encoding) 
	throws UnsupportedEncodingException {
		StringBuilder qs = new StringBuilder();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof Object[]) {
				Object[] array = (Object[])value;
				for (Object element : array) {
					appendQueryEntry(key, element != null ? element.toString() : null, encoding, qs);
				}
			}
			else {
				appendQueryEntry(key, value != null ? value.toString() : null, encoding, qs);
			}
		}
		return qs.toString();
	}

	private static void appendQueryEntry(String key, String value, String encoding, StringBuilder qs) 
	throws UnsupportedEncodingException {
		if (qs.length() > 0) {
			qs.append('&');
		}
		qs.append(URLEncoder.encode(key, encoding));
		qs.append('=');
		if (value != null) {
			qs.append(URLEncoder.encode(value, encoding));
		}
	}

	public static String makeHostUrl(HttpServletRequest request) {
		StringBuilder url = new StringBuilder();
		url.append(request.getScheme());
		url.append("://");
		url.append(request.getServerName());
		if (request.getServerPort() != 80) {
			url.append(":");
			url.append(request.getServerPort());
		}
		return url.toString();
	}

	public static String makeContextUrl(HttpServletRequest request) {
		return makeHostUrl(request) + request.getContextPath();
	}

	private static String[][] HTML_ESCAPE = {
		{"&", "&amp;"}, 
		{"<", "&lt;"}, 
		{">", "&gt;"}, 
		{"\"", "&quot;"}
	};

	public static String escapeHtml(Object html) {
		if (html == null) {
			return null;
		}
		String sanitized = html.toString();
		for (String[] mapping : HTML_ESCAPE) {
			sanitized = StringUtils.replace(sanitized, mapping[0], mapping[1]);
		}
		return sanitized;
	}

	public static String unescapeHtml(Object escaped) {
		if (escaped == null) {
			return null;
		}
		String unescaped = escaped.toString();
		for (String[] mapping : HTML_ESCAPE) {
			unescaped = StringUtils.replace(unescaped, mapping[1], mapping[0]);
		}
		return unescaped;
	}

	public static String dump(String string) {
		if (string == null) {
			return null;
		}
		StringBuffer dump = new StringBuffer();
		char[] charArray = string.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if (i > 0) {
				dump.append(" ");
			}
			dump.append(Integer.toHexString(charArray[i]));
		}
		return dump.toString();
	}

	public static void setFileName(HttpServletResponse response, String fileName) {
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
	}

	public static File forceGetFile(FileItem fileItem) throws Exception {
		Assert.Arg.notNull(fileItem, "fileItem");

		if (fileItem instanceof DiskFileItem) {
			File file = ((DiskFileItem)fileItem).getStoreLocation();
			if (file != null && file.isFile()) return file;
		}

		File tempDir = new File(System.getProperty("java.io.tmpdir"));
		String tempFileName = WebUtils.class.getName() + "-" + 
			DateTime.getCurrentTime().format("yyyyMMddHHmmssS") + ".tmp";
		File tempFile = new File(tempDir, tempFileName);
		logger.info("Creating a temp file: " + tempFile.getAbsolutePath());
		fileItem.write(tempFile);
		return tempFile;
	}

	public static final String REQ_HEADER_USER_AGENT = "User-Agent";
	public static final String UA_MSIE = "MSIE ";

	public static int[] getMsieVersion(HttpServletRequest request) {
		Assert.Arg.notNull(request, "request");

		String userAgent = request.getHeader(REQ_HEADER_USER_AGENT);
		int beginIndex = userAgent.indexOf(UA_MSIE);
		if (beginIndex == -1) return null;

		int endIndex = userAgent.indexOf(';', beginIndex);
		if (endIndex == -1) return null;

		// Mozilla/4.0 (compatible; MSIE X.X
		String version = userAgent.substring(beginIndex + UA_MSIE.length(), endIndex);

		String[] numbers = StringUtils.split(version, '.');
		int[] result = new int[numbers.length];
		try {
			for (int i = 0; i < numbers.length; i++) {
				result[i] = Integer.parseInt(numbers[i]);
			}
		}
		catch (NumberFormatException e) {
			logger.warn("Invalid User-Agent: " + userAgent, e);
			return null;
		}
		return result;
	}

	// NOTE http://hudson.gotdns.com/wiki/display/HUDSON/Tomcat#Tomcat-i18n
	public static String modifyIfGarbledByTomcat(
		String value, 
		String srcEnc, 
		HttpServletRequest request, 
		ServletContext servletContext)
	throws UnsupportedEncodingException {
		String serverInfo = servletContext.getServerInfo();
		if (StringUtils.isBlank(value) || serverInfo == null) {
			return value;
		}
		if (request.getMethod().equalsIgnoreCase("GET") && serverInfo.toLowerCase().indexOf("tomcat") != -1) {
			String modified = new String(value.getBytes("iso-8859-1"), srcEnc);
			if (logger.isDebugEnabled()) logger.debug(value + " -> " + modified);
			return modified;
		}
		else {
			return value;
		}
	}
}

package marubinotto.piggydb.ui.page.common;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.UnhandledException;

import marubinotto.util.Assert;
import marubinotto.util.web.WebUtils;
import net.sf.click.Context;
import net.sf.click.Page;

public class PageUrl {

	private Class<? extends Page> pageClass;
	private Context clickContext;
	
	public Map<String, Object> parameters = new LinkedHashMap<String, Object>();
	
	public PageUrl(Class<? extends Page> pageClass, Context clickContext) {
		Assert.Arg.notNull(pageClass, "pageClass");
		Assert.Arg.notNull(clickContext, "clickContext");
		
		this.pageClass = pageClass;
		this.clickContext = clickContext;
	}
	
	public String getPagePath() {
		return getPagePath(null);
	}
	
	/**
	 * Click page path
	 */
	public String getPagePath(Map<String, Object> additionalParameters) {
		StringBuilder path = new StringBuilder();
		path.append(this.clickContext.getPagePath(this.pageClass));
		
		Map<String, Object> allParameters = new LinkedHashMap<String, Object>();
		allParameters.putAll(this.parameters);
		if (additionalParameters != null) {
			allParameters.putAll(additionalParameters);
		}
		
		if (!allParameters.isEmpty()) {
			path.append("?");
			try {
				path.append(WebUtils.toQueryString(allParameters, AbstractWebResource.CHAR_ENCODING));
			}
			catch (UnsupportedEncodingException e) {
				throw new UnhandledException(e);
			}
		}
		
		return path.toString();
	}
	
	public String getPageUrl() {
		return getPageUrl(null);
	}
	
	public String getPageUrl(String key, Object value) {
		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put(key, value);
		return getPageUrl(additionalParameters);
	}
	
	public String getPageUrl(String key1, Object value1, String key2, Object value2) {
		Map<String, Object> additionalParameters = new HashMap<String, Object>();
		additionalParameters.put(key1, value1);
		additionalParameters.put(key2, value2);
		return getPageUrl(additionalParameters);
	}
	
	public String getPageUrl(Map<String, Object> additionalParameters) {
		String url = getPagePath(additionalParameters);
		if (url.charAt(0) == '/') {
			url = this.clickContext.getRequest().getContextPath() + url;
		}
		return url;
	}
	
	@Override
	public String toString() {
		return getPageUrl();
	}
}

package marubinotto.piggydb.ui.page.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import marubinotto.util.RegexUtils;
import marubinotto.util.ThreadLocalCache;
import marubinotto.util.RegexUtils.MatchProcessor;
import marubinotto.util.velocity.InescapableWrapper;
import net.sf.click.util.MessagesMap;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class TemplateUtils {

	public static final TemplateUtils INSTANCE = new TemplateUtils();

	private static Pattern menuTitlePattern;
	static {
		try {
			menuTitlePattern = new Perl5Compiler().compile("(>(\\w|-)+?<)");
		}
		catch (MalformedPatternException e) {
			throw new UnhandledException(e);
		}
	}

	public Object nullToEmpty(Object object) {
		return object == null ? "" : object;
	}

	@SuppressWarnings("rawtypes")
	public Object getWithDefault(Map map, String key, String defaultValue) {
		Object value = map.get(key);
		return value != null ? value : defaultValue;
	}

	public Object renderMenuItem(Object item, final MessagesMap messages) {
		if (item == null) {
			return "";
		}
		return raw(RegexUtils.substitute(ThreadLocalCache.get(Perl5Matcher.class),
			menuTitlePattern, 1, new MatchProcessor() {
				public String process(String match) {
					String title = match.substring(1, match.length() - 1);
					return ">" + messages.get(title) + "<";
				}

			}, item.toString()));
	}

	public String sanitizeNewlines(Object src) throws IOException {
		if (src == null) {
			return "";
		}
		StringBuffer result = new StringBuffer();
		BufferedReader reader = new BufferedReader(new StringReader(src.toString()));
		String line = null;
		while ((line = reader.readLine()) != null) {
			result.append(line);
		}
		return result.toString();
	}

	public Object link(String url) {
		return raw("<a href=\"" + url + "\">" + url + "</a>");
	}

	public Object raw(Object object) {
		return object == null ? "" : new InescapableWrapper(object);
	}

	public Object escapeAmp(Object object) {
		return object == null ? "" : StringUtils.replace(object.toString(), "&",
			"&amp;");
	}

	public Object escapeXml(Object object) {
		return object == null ? null : raw(StringEscapeUtils.escapeXml(object
			.toString()));
	}

	public Object defaultIfNull(Object object, Object defaultValue) {
		return object != null ? object : defaultValue;
	}

	public Object urlEncode(Object object) throws UnsupportedEncodingException {
		return object == null ? null : new URLCodec().encode(object.toString(),
			WebResource.CHAR_ENCODING);
	}
}
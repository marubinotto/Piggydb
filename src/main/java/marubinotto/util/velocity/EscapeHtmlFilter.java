package marubinotto.util.velocity;

import java.util.ArrayList;
import java.util.List;

import marubinotto.util.ThreadLocalCache;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;

public class EscapeHtmlFilter implements ReferenceInsertionEventHandler, RuntimeServicesAware {
	
	private static Log logger = LogFactory.getLog(EscapeHtmlFilter.class);
	
	public static final String KEY_INESCAPABLES = "eventhandler.EscapeHtmlFilter.inescapables";
	
	// Config: inescapables
	private static PatternCompiler compiler = new Perl5Compiler();	// not thread safe
	private List<Pattern> inescapablePatterns = new ArrayList<Pattern>();

	public void setRuntimeServices(RuntimeServices rs) {
		String[] inescapables = rs.getConfiguration().getStringArray(KEY_INESCAPABLES);
		if (inescapables == null) return;
		
		this.inescapablePatterns.clear();
		for (String pattern : inescapables) {
			if (StringUtils.isBlank(pattern)) continue;
			
			pattern = pattern.trim();
			logger.info("Inescapable pattern: " + pattern);
			try {
				this.inescapablePatterns.add(compiler.compile(pattern));
			}
			catch (MalformedPatternException e) {
				throw new UnhandledException(e);
			}
		}
	}

	public Object referenceInsert(String reference, Object value) {
		if (value == null) {
            return "";
        }
		if (value instanceof Inescapable) {
			return value;
		}
		for (Pattern pattern : this.inescapablePatterns) {
			if (ThreadLocalCache.get(Perl5Matcher.class).contains(reference, pattern)) {
				return value;
			}
		}
		if (logger.isDebugEnabled()) logger.debug("escaping reference: " + reference);
		return StringEscapeUtils.escapeHtml(value.toString());
	}
}

package marubinotto.util;

import static marubinotto.util.CollectionUtils.set;

import java.util.Set;

import org.apache.commons.lang.UnhandledException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Substitution;
import org.apache.oro.text.regex.Util;

public class RegexUtils {

	private static PatternCompiler compiler = new Perl5Compiler();

	public static Pattern compile(String pattern) {
		synchronized (RegexUtils.class) {
			try {
				return compiler.compile(pattern);
			}
			catch (MalformedPatternException e) {
				throw new UnhandledException(e);
			}
		}
	}

	/**
	 * s/pattern/substitution/ge
	 */
	public static String substitute(
		PatternMatcher matcher, 
		Pattern pattern, 
		int group, 
		MatchProcessor processor, 
		String input) {
		Assert.Arg.notNull(matcher, "matcher");
		Assert.Arg.notNull(pattern, "pattern");
		Assert.Arg.notNull(processor, "processor");
		Assert.Arg.notNull(input, "input");

		Substitution substitution = new MatchProcessorSubstitution(processor, group);
		return Util.substitute(matcher, pattern, substitution, input, Util.SUBSTITUTE_ALL);
	}

	public static interface MatchProcessor {
		public String process(String match);
	}

	static class MatchProcessorSubstitution implements Substitution {
		private MatchProcessor processor;
		private int group;

		public MatchProcessorSubstitution(MatchProcessor processor, int group) {

			Assert.Arg.notNull(processor, "processor");
			this.processor = processor;
			this.group = group;
		}

		public void appendSubstitution(
			StringBuffer buffer, 
			MatchResult match, 
			int substitutionCount, 
			PatternMatcherInput originalInput,
			PatternMatcher matcher, 
			Pattern pattern) {

			buffer.append(this.processor.process(match.group(this.group)));
		}
	}

	private static final Set<Character> METACHARS = 
		set('\\', '+', '-', '?', '*', '.', '[', ']', '(', ')', '{', '}', '^', '$', '|');

	public static String escapeRegex(String pattern) {
		if (pattern == null) {
			return pattern;
		}
		StringBuffer escaped = new StringBuffer();
		for (int i = 0; i < pattern.length(); i++) {
			char character = pattern.charAt(i);
			if (METACHARS.contains(character)) {
				escaped.append("\\");
			}
			escaped.append(character);
		}
		return escaped.toString();
	}
}

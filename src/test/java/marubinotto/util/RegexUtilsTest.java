package marubinotto.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class RegexUtilsTest {

	private PatternCompiler compiler = new Perl5Compiler();
	private PatternMatcher matcher = new Perl5Matcher();

	@Test
	public void shouldProcessMatchedTokenByMatchProcessor() throws Exception {
		String result = RegexUtils.substitute(
			this.matcher, 
			this.compiler.compile("\\d+"), 
			new RegexUtils.MatchProcessor() {
				public String process(MatchResult match) {
					return "(" + match.group(0) + ")";
				}
			}, 
			"foo10hoge45huga");
		assertEquals("foo(10)hoge(45)huga", result);
	}

	@Test
	public void escapeMetacharsForJava() throws Exception {
		assertTrue("fo]o(b\\ar".matches(RegexUtils.escapeRegex("fo]o(b\\ar")));
	}

	@Test
	public void escapeMetacharsForOro() throws Exception {
		String result = RegexUtils.substitute(
			this.matcher, 
			this.compiler.compile(RegexUtils.escapeRegex("fo]o(b\\ar")), 
			new RegexUtils.MatchProcessor() {
				public String process(MatchResult match) {
					return "(" + match.group(0) + ")";
				}
			}, 
			"foo fo]o(b\\ar bar");
		assertEquals("foo (fo]o(b\\ar) bar", result);
	}
}

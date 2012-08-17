package marubinotto.piggydb.spike;

import static marubinotto.util.RegexUtils.compile;
import static org.junit.Assert.assertEquals;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class SpanRegexSpike {

	private PatternMatcher matcher = new Perl5Matcher();
	
	public static final Pattern P_SPAN = compile("\\{\\{\\[([A-Za-z0-9-_\\s]+?)\\](.+?)\\}\\}");
	
	@Test
	public void typical() throws Exception {
		assertEquals(
			true, 
			this.matcher.contains("{{[red]Hello world!}}", P_SPAN));

		MatchResult matchResult = matcher.getMatch();
		assertEquals("red", matchResult.group(1));
		assertEquals("Hello world!", matchResult.group(2));
	}
	
	@Test
	public void invalidChar() throws Exception {
		assertEquals(
			false, 
			this.matcher.contains("{{[daisuke@morita]Hello world!}}", P_SPAN));
	}
	
	@Test
	public void hyphen() throws Exception {
		assertEquals(
			true, 
			this.matcher.contains("{{[fragment-title]Hello world!}}", P_SPAN));

		MatchResult matchResult = matcher.getMatch();
		assertEquals("fragment-title", matchResult.group(1));
		assertEquals("Hello world!", matchResult.group(2));
	}
	
	@Test
	public void underscore() throws Exception {
		assertEquals(
			true, 
			this.matcher.contains("{{[fragment_title]Hello world!}}", P_SPAN));

		MatchResult matchResult = matcher.getMatch();
		assertEquals("fragment_title", matchResult.group(1));
		assertEquals("Hello world!", matchResult.group(2));
	}
	
	@Test
	public void multiple() throws Exception {
		assertEquals(
			true, 
			this.matcher.contains("{{[red large]Hello world!}}", P_SPAN));

		MatchResult matchResult = matcher.getMatch();
		assertEquals("red large", matchResult.group(1));
		assertEquals("Hello world!", matchResult.group(2));
	}
}

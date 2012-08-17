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
	
	public static final Pattern P_SPAN = compile("\\{\\{\\[(.+?)\\](.+?)\\}\\}");
	
	@Test
	public void typical() throws Exception {
		assertEquals(
			true, 
			this.matcher.contains("{{[red]This is important.}}", P_SPAN));

		MatchResult matchResult = matcher.getMatch();
		assertEquals("red", matchResult.group(1));
		assertEquals("This is important.", matchResult.group(2));
	}
}

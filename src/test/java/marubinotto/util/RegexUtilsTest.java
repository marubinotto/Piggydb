package marubinotto.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class RegexUtilsTest {

    static class Bracket implements RegexUtils.MatchProcessor {
        public String process(String match) {
            return "(" + match + ")";
        }       
    }
      
    private PatternCompiler compiler = new Perl5Compiler();
    private PatternMatcher matcher = new Perl5Matcher();

    @Test
    public void shouldProcessMatchedTokenByMatchProcessor() throws Exception {
        String input = "foo10hoge45huga";
        String pattern = "\\d+";
        String result = RegexUtils.substitute(
            this.matcher, this.compiler.compile(pattern), 0, new Bracket(), input);
        assertEquals("foo(10)hoge(45)huga", result);
    }
    
    @Test
    public void escapeMetacharsForJava() throws Exception {
    	assertTrue("fo]o(b\\ar".matches(RegexUtils.escapeRegex("fo]o(b\\ar")));
    }
    
    @Test
    public void escapeMetacharsForOro() throws Exception {
    	String input = "foo fo]o(b\\ar bar";
        String pattern = RegexUtils.escapeRegex("fo]o(b\\ar");
        String result = RegexUtils.substitute(
            this.matcher, this.compiler.compile(pattern), 0, new Bracket(), input);
        assertEquals("foo (fo]o(b\\ar) bar", result);
    }
}

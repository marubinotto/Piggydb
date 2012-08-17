package marubinotto.piggydb.spike;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.StringReader;
import java.net.URI;
import java.text.BreakIterator;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.junit.Test;

public class HodgepodgeSpike {

	private PatternCompiler compiler = new Perl5Compiler();
	private PatternMatcher matcher = new Perl5Matcher();

	@Test
	public void filePathAndUrl() throws Exception {
		String userHome = System.getProperty("user.home");
		System.out.println("userHome: " + userHome);

		File userHomePath = new File(userHome);
		System.out.println("userHomePath: " + userHomePath);

		URI userHomeURI = userHomePath.toURI();
		System.out.println("userHomeURI: " + userHomeURI);

		File userHomePath2 = FileUtils.toFile(userHomeURI.toURL());
		System.out.println("userHomePath2: " + userHomePath2);
	}

	@Test
	public void fileToUrlWithEscaping() throws Exception {
		File file = new File("/my docs/file.txt");
		System.out.println("toURI: " + file.toURI());
		// System.out.println("toURL: " + file.toURL());
	}

	@Test
	public void youtubeUrl() throws Exception {
		Pattern pattern = compiler.compile("youtube\\.com/watch\\?v=([^&\\s]+)");
		boolean result = this.matcher.contains("http://jp.youtube.com/watch?v=iEL2grmKSnM&feature=related", pattern);
		assertTrue(result);

		MatchResult matchResult = matcher.getMatch();
		System.out.println(matchResult.group(1));
	}

	@Test
	public void tableRow() throws Exception {
		Pattern pattern = compiler.compile("^(\\|\\|).+(\\|\\|\\s*)$");
		boolean result = this.matcher.matches("||hoge||fuga||", pattern);
		System.out.println(result);
	}

	@Test
	public void match() throws Exception {
		Pattern pattern = compiler.compile("^(hogehoge)$");
		boolean result = this.matcher.contains("hogehoge", pattern);
		System.out.println(result);
	}

	@Test
	public void userHome() throws Exception {
		System.out.println("IS_OS_WINDOWS: " + SystemUtils.IS_OS_WINDOWS);
		System.out.println("user.home: " + System.getProperty("user.home"));
		System.out.println("HOME: " + System.getenv("HOME"));
		System.out.println("HOMEDRIVE: " + System.getenv("HOMEDRIVE"));
		System.out.println("HOMEPATH: " + System.getenv("HOMEPATH"));
		System.out.println("USERPROFILE: " + System.getenv("USERPROFILE"));
		System.out.println("ALLUSERSPROFILE: " + System.getenv("ALLUSERSPROFILE"));
		System.out.println("SYSTEMDRIVE: " + System.getenv("SYSTEMDRIVE"));
	}

	private static Analyzer analyzer = new CJKAnalyzer();

	@Test
	public void tokenize() throws Exception {
		String text = "Piggydb is an easy-to-use Web application for building a personal knowledge repository.";
		text = text + " Piggydbは個人向けの知識を管理するためのWebアプリケーションです。";

		// Standard
		System.out.println("Standard ----");
		StringTokenizer tokenizer = new StringTokenizer(text, " \t\n\r\f+\"*%&/()=?'!,.;:-_#@|^~`{}[]");
		while (tokenizer.hasMoreTokens()) {
			System.out.println("  word: " + tokenizer.nextToken());
		}

		// N-gram
		System.out.println("N-gram ----");
		NGramTokenizer nGramTokenizer = new NGramTokenizer(new StringReader(text), 2, 2);
		Token token = new Token();
		while ((token = nGramTokenizer.next(token)) != null) {
			System.out.println("  word: " + token.term());
		}

		// CJKAnalyzer
		System.out.println("CJKAnalyzer ----");
		TokenStream stream = analyzer.tokenStream("F", new StringReader(text));
		token = new Token();
		while ((token = stream.next(token)) != null) {
			System.out.println("  word: " + token.term());
		}
	}

	@Test
	public void sentenceIterator() throws Exception {
		printFirstSentence("");
		printFirstSentence("without a period");
		printFirstSentence("This is a pen. Hello world.");
		printFirstSentence("日本語です。どうですか？");
	}

	private void printFirstSentence(String text) {
		BreakIterator iterator = BreakIterator.getSentenceInstance();
		iterator.setText(text);
		int end = iterator.next();
		if (end == BreakIterator.DONE) {
			System.out.println("DONE");
		}
		else {
			System.out.println(text.substring(0, end));
		}
	}
}

package marubinotto.piggydb.ui.wiki;

import static marubinotto.util.RegexUtils.compile;
import marubinotto.piggydb.ui.wiki.HtmlBuilder.UrlProcessor;

import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;

public class YouTubeUrlProcessor implements UrlProcessor {
  
  private static final Pattern P_YOUTUBE_URL = compile("youtube\\.com/watch\\?v=([^&\\s]+)");
  
  public String process(String url, PatternMatcher matcher) {
    if (!matcher.contains(url, P_YOUTUBE_URL)) return null;
      
    MatchResult matchResult = matcher.getMatch();
    String youtubeId = matchResult.group(1);
    return makeEmbeddedYoutubeHtml(youtubeId);
  }
  
  private static String makeEmbeddedYoutubeHtml(String id) {
    int width = 560;
    int height = 340;
    String mvUrl = "http://www.youtube.com/v/" + id + "&hl=en&fs=1";

    StringBuilder html = new StringBuilder();
    html.append("<object width=\"" + width + "\" height=\"" + height + "\">");
    html.append("<param name=\"movie\" value=\"" + mvUrl + "\"></param>");
    html.append("<param name=\"allowFullScreen\" value=\"true\"></param>");
    html.append("<param name=\"allowscriptaccess\" value=\"always\"></param>");
    html.append("<embed src=\"" + mvUrl + "\"" + 
      " type=\"application/x-shockwave-flash\"" + 
      " allowscriptaccess=\"always\"" + 
      " allowfullscreen=\"true\"" + 
      " width=\"" + width + "\" height=\"" + height + "\"></embed>");
    html.append("</object>");
    return html.toString();
  }
}
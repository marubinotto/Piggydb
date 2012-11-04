package marubinotto.piggydb.ui.page.common;

import java.util.Map;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.auth.User;
import marubinotto.piggydb.model.predicate.Preformatted;
import marubinotto.piggydb.ui.wiki.WikiParser;
import marubinotto.util.Assert;
import marubinotto.util.web.WebUtils;
import marubinotto.util.xml.XmlStringBuilder;

public class HtmlFragments {

	private WebResourcePaths webResources;
	
	public HtmlFragments(WebResourcePaths webResources) {
		Assert.Arg.notNull(webResources, "webResources");
		this.webResources = webResources;
	}
	
	public String cssImport(String relativePath, boolean versioning, String media) {
		String cssPath = this.webResources.resourcePath(relativePath, versioning);
		XmlStringBuilder builder = XmlStringBuilder.create("link")
			.attribute("rel", "stylesheet")
			.attribute("type", "text/css")
			.attribute("href", cssPath);
		if (media != null) builder.attribute("media", media);
		return builder.toString();
	}
	
	public String jsImport(String relativePath, boolean versioning) {
		String jsPath = this.webResources.resourcePath(relativePath, versioning);
		return XmlStringBuilder.create("script")
			.attribute("type", "text/javascript")
			.attribute("src", jsPath)
			.text("")
			.toString();
	}
	
	public String fragmentImage(Fragment fragment) {
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		
		String imageUrl = this.webResources.fragmentFilePath(fragment.getId()) + 
			"&t=" + fragment.getUpdateDatetime().getTime();
		
		return XmlStringBuilder.create("a")
			.attribute("class", "img-link")
			.attribute("href", imageUrl)
			.element("img")
				.attribute("class", "fragment-img")
				.attribute("src", imageUrl)
				.attribute("border", "0")
				.attribute("alt", "")
				.toString();
	}
	
	public String fileIcon(String fileType) {
		return XmlStringBuilder.create("img")	
			.attribute("src", this.webResources.fileIconPath(fileType))
			.attribute("border", "0")
			.attribute("style", "vertical-align: middle;")
			.attribute("alt", "")
			.toString();
	}
	
	public String filterIconMini() {
		return XmlStringBuilder.create("span")	
		.attribute("class", "filter-icon-mini")
		.text("&nbsp;")
		.toString();
	}
	
	public String linkToFragment(long fragmentId) {
		return XmlStringBuilder.create("a")
			.attribute("href", this.webResources.fragmentPath(fragmentId))
			.text("#" + fragmentId)
			.toString();
	}
	
	public String linkToFragmentFileWithSize(Fragment fragment) {
		Assert.Arg.notNull(fragment, "fragment");
		return XmlStringBuilder.create("span")
			.element("a")
				.attribute("class", "file-name")
				.attribute("href", this.webResources.fragmentFilePath(fragment.getId()))
				.text(WebUtils.escapeHtml(fragment.getFileName())).end()
			.text(" ")
			.element("span")
				.attribute("class", "file-size")
				.text("(" + fragment.getFileSize() + ")")
			.toString();
	}
	
	public String fragmentInMessage(Fragment fragment) {
		StringBuilder sb = new StringBuilder(linkToFragment(fragment.getId()));
		String headline = fragment.makeHeadline(20);
		if (headline != null) {
			sb.append(" <span class=\"fragment-title\">");
			sb.append(WebUtils.escapeHtml(headline));
			sb.append("</span>");
		}
		return sb.toString();
	}
	
	public String fragmentHeadline(Fragment fragment) {
		if (fragment.getTitle() != null) {
			return WebUtils.escapeHtml(fragment.getTitle());
		}
		if (fragment.isFile()) {
			return WebUtils.escapeHtml(fragment.getFileName());
		}
		
		String headline = fragment.makeContentHeadline();
		if (headline != null) {
			return "<span class=\"fragment-headline\">" + WebUtils.escapeHtml(headline) + "</span>";
		}
		
		return null;
	}
	
	public String preformattedContent(Fragment fragment, WikiParser wikiParser, User user) 
	throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(wikiParser, "wikiParser");
		Assert.Arg.notNull(user, "user");
		
		XmlStringBuilder builder = XmlStringBuilder.create("pre");
		
		StringBuilder classes = new StringBuilder();
		classes.append("pre-fragment");
		if (Preformatted.INSTANCE.isCode(fragment)) {
			classes.append(" prettyprint");
			String lang = Preformatted.INSTANCE.getLanguageName(fragment);
			if (lang != null) classes.append(" lang-" + lang);
		}
		builder.attribute("class", classes);
		
		if (fragment.getContent() != null) {
			builder.text(
				wikiParser.parsePreformattedText(
					fragment.getContent(), user, this.webResources));
		}
		return builder.toString();
	}
	
	public String linkToTag(String tagName) {
		return XmlStringBuilder.create("a")
			.attribute("href", this.webResources.tagPathByName(tagName))
			.text(WebUtils.escapeHtml(tagName))
			.toString();
	}
	
	public String fragmentTableClass(Fragment fragment) {
		return "fragment fragment-" + fragment.getId();
	}
	
	public String fragmentHeaderClass(long fragmentId, Map<Long, String> selected) {
		String c = "fragment-header fragment-header-" + fragmentId;
		if (selected != null && selected.containsKey(fragmentId)) c = c + " selected-fragment";
		return c;
	}
	
	public String tagIconClass(String tagName) {
		Assert.Arg.notNull(tagName, "tagName");
		String c = "tagIcon";
		if (tagName.startsWith("#")) {
			c = c + " tagIcon-" + WebUtils.escapeHtml(tagName.substring(1));
		}
		return c;
	}
	
	public String miniTagIconClass(String tagName) {
		Assert.Arg.notNull(tagName, "tagName");
		String c = "miniTagIcon";
		if (tagName.startsWith("#")) {
			c = c + " miniTagIcon-" + WebUtils.escapeHtml(tagName.substring(1));
		}
		return c;
	}
	
	public String fragmentsViewSwitch(String selectedMode, String pageIndexName, PageUrl pageUrl) {
		Assert.Arg.notNull(selectedMode, "selectedMode");
		Assert.Arg.notNull(pageIndexName, "pageIndexName");
		Assert.Arg.notNull(pageUrl, "pageUrl");
		
		StringBuilder html = new StringBuilder();
		html.append(fragmentsViewSwitchButton("detail",  selectedMode, pageIndexName, pageUrl));
		html.append(fragmentsViewSwitchButton("list",  selectedMode, pageIndexName, pageUrl));
		html.append(fragmentsViewSwitchButton("titles",  selectedMode, pageIndexName, pageUrl));
		return html.toString();
	}
	
	private String fragmentsViewSwitchButton(
		String mode, 
		String selectedMode, 
		String pageIndexName, 
		PageUrl pageUrl) {
		
		XmlStringBuilder builder =  XmlStringBuilder.create("button").attribute("type", "button");
		if (mode.equals(selectedMode)) {
			builder.attribute("class", "selected");
		}
		else {
			String switchUrl = null;
			if (pageIndexName != null) {
				// Unless overwriting the page index, it will be restored by pageUrl.
				// When changing the view, the page index should be reset to 0.
				switchUrl = WebUtils.escapeHtml(
					pageUrl.getPageUrl("fragmentsViewMode", mode, pageIndexName, 0));
			}
			else {
				switchUrl = WebUtils.escapeHtml(pageUrl.getPageUrl("fragmentsViewMode", mode));
			}
			builder.attribute("onclick", "javascript:document.location.href='" + switchUrl + "';");
		}

		builder.element("img")
			.attribute("src", this.webResources.contextPath() + "/images/view-" + mode + ".png")
			.attribute("alt", mode)
			.attribute("border", "0");
		
		return builder.toString();
	}
}

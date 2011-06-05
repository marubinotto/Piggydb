package marubinotto.piggydb.presentation.wiki;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.Assert;

public class FragmentUrl {
	
	private static Log logger = LogFactory.getLog(FragmentUrl.class);

	public static final String PREFIX = "fragment:";
	public static final String CMD_TITLE = "title";
	public static final String CMD_DETAIL = "detail";
	public static final String CMD_EMBED = "embed";

	private String url;
	
	private Long id;
	private String command;
	
	public FragmentUrl(String url) {
		Assert.Arg.notNull(url, "url");
		Assert.require(
			url.startsWith(PREFIX), 
			"Fragment url should start with: " + PREFIX + 
				" (actual " + url + ")");
		
		this.url = url;
		processUrl();
	}
	
	public String getUrl() {
		return this.url;
	}

	public Long getId() {
		return this.id;
	}

	public String getCommand() {
		return this.command;
	}

	public String toMarkup(DocumentBuilder documentBuilder, ParseContext context) {
		if (getId() == null) return getUrl();
		
		if (getCommand() == null) {
			return documentBuilder.makeLinkToFragment(context, getId(), getUrl());
		}
		
		Fragment fragment = null;
		try {
			fragment = context.getFragmentRepository().get(id);
		} 
		catch (Exception e) {
			throw new UnhandledException(e);
		}
		if (fragment == null) return getUrl();
		
		// authentication
		if (!context.isAuthenticated() && !fragment.isPublic()) return getUrl();
		
		// commands
		if (getCommand().equals(CMD_TITLE)) {
			return documentBuilder.makeLinkToFragment(
				context, 
				fragment.getId(), 
				StringUtils.isNotBlank(fragment.getTitle()) ? fragment.getTitle() : "[No title]");
		}
		else if (getCommand().equals(CMD_DETAIL)) {
			return documentBuilder.makeLinkToFragmentWithDetail(context, fragment);
		}
		else if (getCommand().equals(CMD_EMBED)) {
			String embedded = documentBuilder.makeEmbeddedFragment(context, fragment);
			return embedded != null ? embedded : getUrl();
		}
		return getUrl();
	}

// Internal
	
	private void processUrl() {
		String urlBody = this.url.substring(PREFIX.length()); // ex. "1:title"
		String[] parts = StringUtils.split(urlBody, ':');
		
		// ID
		if (parts.length == 0 || !NumberUtils.isDigits(parts[0])) {
			logger.debug("Missing ID: " + this.url);
			return;
		}
		this.id = Long.parseLong(parts[0]);
		
		// command
		if (parts.length == 2) this.command = parts[1];
	}
}

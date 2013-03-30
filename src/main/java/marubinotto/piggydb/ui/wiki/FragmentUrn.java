package marubinotto.piggydb.ui.wiki;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.UnhandledException;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.Fragment;
import marubinotto.util.Assert;

public class FragmentUrn {
	
	private static Log logger = LogFactory.getLog(FragmentUrn.class);

	public static final String PREFIX = "fragment:";
	public static final String CMD_TITLE = "title";
	public static final String CMD_DETAIL = "detail";
	public static final String CMD_EMBED = "embed";

	private String urn;
	
	private Long id;
	private String command;
	
	public FragmentUrn(String urn) {
		Assert.Arg.notNull(urn, "urn");
		Assert.require(
			urn.startsWith(PREFIX), 
			"Fragment urn should start with: " + PREFIX + 
				" (actual " + urn + ")");
		
		this.urn = urn;
		processUrn();
	}
	
	public String getUrn() {
		return this.urn;
	}

	public Long getId() {
		return this.id;
	}

	public String getCommand() {
		return this.command;
	}

	public String toMarkup(DocumentBuilder documentBuilder, ParseContext context) {
		if (getId() == null) return getUrn();
		
		if (getCommand() == null) {
			return documentBuilder.makeLinkToFragment(context, getId(), getUrn());
		}
		
		Fragment fragment = null;
		try {
			fragment = context.getFragmentRepository().get(id);
		} 
		catch (Exception e) {
			throw new UnhandledException(e);
		}
		if (fragment == null) return getUrn();
		
		// authentication
		if (!context.isAuthenticated() && !fragment.isPublic()) return getUrn();
		
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
			return embedded != null ? embedded : getUrn();
		}
		return getUrn();
	}

// Internal
	
	private void processUrn() {
		String urnBody = this.urn.substring(PREFIX.length()); // ex. "1:title"
		String[] parts = StringUtils.split(urnBody, ':');
		
		// ID
		if (parts.length == 0 || !NumberUtils.isDigits(parts[0])) {
			logger.debug("Missing ID: " + this.urn);
			return;
		}
		this.id = Long.parseLong(parts[0]);
		
		// command
		if (parts.length == 2) this.command = parts[1];
	}
}

package marubinotto.piggydb.ui.wiki;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Stack;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.TagRepository;
import marubinotto.piggydb.model.User;
import marubinotto.piggydb.ui.page.common.WebResourcePaths;
import marubinotto.util.Assert;

public abstract class WikiParser {
	
	private static Log logger = LogFactory.getLog(WikiParser.class);

    private FragmentRepository fragmentRepository;
    private TagRepository tagRepository;

    public WikiParser() {
    }

	public void setFragmentRepository(FragmentRepository fragmentRepository) {
		this.fragmentRepository = fragmentRepository;
	}

	public void setTagRepository(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}
	
	public String parse(
		String wikiText, 
		Long targetFragment,
		User user,
		WebResourcePaths webResources) 
	throws Exception {
		Stack<Long> fragmentStack = new Stack<Long>();
		if (targetFragment != null) {
			fragmentStack.push(targetFragment);
		}
		return parseNestedly(wikiText, fragmentStack, user, webResources);
	}
	
	public String parseNestedly(
		String wikiText, 
		Stack<Long> fragmentStack,
		User user,
		WebResourcePaths webResources) 
	throws Exception {
		StringWriter output = new StringWriter();
		invokeParse(wikiText, output, fragmentStack, user, webResources);
		return output.toString();
	}
	
	public void parse(
		String wikiText, 
		Writer output, 
		Long targetFragment,
		User user,
		WebResourcePaths webResources) 
	throws Exception {
		Stack<Long> fragmentStack = new Stack<Long>();
		if (targetFragment != null) {
			fragmentStack.push(targetFragment);
		}
		invokeParse(wikiText, output, fragmentStack, user, webResources);
	}

	private void invokeParse(
		String wikiText, 
		Writer output, 
		Stack<Long> fragmentStack,
		User user,
		WebResourcePaths webResources)
	throws Exception {
		Assert.Arg.notNull(output, "output");
		Assert.Arg.notNull(fragmentStack, "fragmentStack");
		Assert.Arg.notNull(webResources, "webResources");
		
		if (wikiText == null) return;
		
		if (logger.isDebugEnabled()) logger.debug("invokeParse by: " + user + " " + fragmentStack);
		ParseContext context = createContext(output, fragmentStack, user, webResources);
		doParse(wikiText, context);
	}
	
	protected abstract void doParse(String wikiText, ParseContext context)
	throws Exception;
	
	protected ParseContext createContext(
			Writer output, 
			Stack<Long> fragmentStack,
			User user,
			WebResourcePaths webResources) {
		Assert.Property.requireNotNull(fragmentRepository, "fragmentRepository");
		Assert.Property.requireNotNull(tagRepository, "tagRepository");
		
		return new ParseContext(
			output,
			this,
			fragmentStack,
			user,
			webResources, 
			this.fragmentRepository, 
			this.tagRepository);
	}
	
	public String parsePreformattedText(
		String preformattedText, 
		User user,
		WebResourcePaths webResources) 
	throws Exception {
		StringWriter output = new StringWriter();
		if (preformattedText != null) {
			ParseContext context = createContext(output, null, user, webResources);
			doParsePreformattedText(preformattedText, context);
		}
		return output.toString();
	}
	
	protected abstract void doParsePreformattedText(String preformattedText, ParseContext context)
	throws Exception;
}

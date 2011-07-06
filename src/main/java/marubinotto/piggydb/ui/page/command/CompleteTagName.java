package marubinotto.piggydb.ui.page.command;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.net.URLCodec;

import marubinotto.util.Assert;

public class CompleteTagName extends Command {
	
	public static final String CONTENT_TYPE = "text/plain; charset=UTF-8";

	public String q;
	public Integer limit;
	
	@Override
	protected boolean onPreInit() throws Exception {
		if (this.q == null) return false;
		
		this.q = new URLCodec().decode(this.q, CHAR_ENCODING);
		getLogger().debug("q: " + this.q);
		return true;
	}
	
	@Override 
	protected void execute() throws Exception {
		Assert.Property.requireNotNull(q, "q");
		
		List<String> names = getDomain().getTagRepository().getNamesLike(this.q);
		if (getLogger().isDebugEnabled()) {
			getLogger().debug(this.q + " " + names);
		}
		
		HttpServletResponse response = getContext().getResponse();
		response.setContentType(CONTENT_TYPE);	
		PrintWriter out = response.getWriter();
		
		for (String name : names) out.println(name);
		
		response.flushBuffer();
	}
}

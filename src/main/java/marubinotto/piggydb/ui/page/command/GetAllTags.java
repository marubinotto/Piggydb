package marubinotto.piggydb.ui.page.command;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.model.Tag;
import marubinotto.util.paging.Page;

public class GetAllTags extends AbstractCommand {
	
	private int pageSize = 200;
	public int pi = 0;	

	@Override 
	protected void execute() throws Exception {
		Page<Tag> page = getTagRepository().orderByName(this.pageSize, this.pi);
		
		HttpServletResponse response = getContext().getResponse();
		response.setContentType(JsonUtils.CONTENT_TYPE);
		
		PrintWriter out = response.getWriter();
		out.println("[");
		JsonUtils.printPageInfo(page, out);
		out.println(",");
		JsonUtils.printTags(page, null, out);
		out.println("]");
		response.flushBuffer();
	}
}

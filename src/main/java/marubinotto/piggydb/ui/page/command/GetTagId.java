package marubinotto.piggydb.ui.page.command;

import javax.servlet.http.HttpServletResponse;

public class GetTagId extends AbstractCommand {
	
	public String name;

	@Override 
	protected void execute() throws Exception {
		getLogger().debug("name: " + name);
		Long id = getDomain().getTagRepository().getIdByName(this.name);
		if (id == null) {
			return;
		}
		
		HttpServletResponse response = getContext().getResponse();
		response.getWriter().print(id);
		response.flushBuffer();
	}
}

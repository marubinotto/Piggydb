package marubinotto.piggydb.ui.page.command;

import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.ui.page.model.TagCloud;

public class GetPopularTags extends AbstractCommand {

	public static final int MAX_SIZE = 200;
	public static final int MAX_FONT_SIZE = 36;
	public static final int MIN_FONT_SIZE = 12;

	@Override
	protected void execute() throws Exception {
		TagCloud tagCloud = new TagCloud(MAX_SIZE, MAX_FONT_SIZE, MIN_FONT_SIZE);
		tagCloud.setTagRepository(getDomain().getTagRepository());
		Set<Tag> tags = tagCloud.getTags();

		HttpServletResponse response = getContext().getResponse();
		response.setContentType(JsonUtils.CONTENT_TYPE);

		JsonUtils.printTags(tags, null, response.getWriter());
		response.flushBuffer();
	}
}

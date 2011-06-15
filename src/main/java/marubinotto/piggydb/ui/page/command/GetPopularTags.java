package marubinotto.piggydb.ui.page.command;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.model.Tag;

public class GetPopularTags extends AbstractCommand {
	
	public static final int MAX_SIZE = 200;

	@Override 
	protected void execute() throws Exception {
		List<Tag> tags = getTagRepository().getPopularTags(MAX_SIZE);
		updatePopularityAsFontSize(tags);
		
		// Sort tags by name
		SortedSet<Tag> sortedTags = new TreeSet<Tag>(Tag.TAG_NAME_COMPARATOR);
		sortedTags.addAll(tags);
		
		HttpServletResponse response = getContext().getResponse();
		response.setContentType(JsonUtils.CONTENT_TYPE);
		
		JsonUtils.printTags(sortedTags, null, response.getWriter());
		response.flushBuffer();
	}
	
	public static final int MAX_FONT_SIZE = 36;
	public static final int MIN_FONT_SIZE = 12;
	
	private void updatePopularityAsFontSize(List<Tag> tags) {
		if (tags.isEmpty()) return;
		
		long max = tags.get(0).getPopularity();
		long min = tags.get(tags.size() - 1).getPopularity();
		getLogger().debug("Popularity range: " + max + " - " + min);
		
		for (Tag tag : tags) {
			tag.setPopularity(calculateFontSize(tag.getPopularity(), max, min));
		}
	}
	
	private long calculateFontSize(long popularity, long max, long min) {
		popularity = popularity - min;
	    if (popularity == 0) return MIN_FONT_SIZE;
	    
	    double percent = (double)popularity / (max - min);
	    if (getLogger().isDebugEnabled())
	    	getLogger().debug("percent: " + percent);
	    
	    return Math.round(MIN_FONT_SIZE + 
	      ((MAX_FONT_SIZE - MIN_FONT_SIZE) * percent));
	}
}

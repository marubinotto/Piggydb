package marubinotto.piggydb.ui.page.model;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import marubinotto.piggydb.model.Tag;
import marubinotto.piggydb.model.TagRepository;

public class TagCloud {

	private int maxTagCount;
	private int maxFontSize;
	private int minFontSize;
	
	private TagRepository tagRepository;
	
	public TagCloud(int maxTagCount, int maxFontSize, int minFontSize) {
		this.maxTagCount = maxTagCount;
		this.maxFontSize = maxFontSize;
		this.minFontSize = minFontSize;
	}
	
	public TagRepository getTagRepository() {
		return this.tagRepository;
	}

	public void setTagRepository(TagRepository tagRepository) {
		this.tagRepository = tagRepository;
	}

	public Set<Tag> getTags() throws Exception {
		List<Tag> tags = this.tagRepository.getPopularTags(this.maxTagCount);
		setFontSizeTo(tags);

		SortedSet<Tag> sortedTags = new TreeSet<Tag>(Tag.TAG_NAME_COMPARATOR);
		sortedTags.addAll(tags);
		return sortedTags;
	}
	
	private void setFontSizeTo(List<Tag> tags) {
		if (tags.isEmpty()) return;

		long maxPopularity = tags.get(0).getPopularity();
		long minPopularity = tags.get(tags.size() - 1).getPopularity();

		for (Tag tag : tags) {
			long fontSize = calculateFontSize(tag.getPopularity(), maxPopularity, minPopularity);
			tag.getAttributes().put("fontSize", fontSize);
		}
	}
	
	private long calculateFontSize(long popularity, long maxPopularity, long minPopularity) {
		popularity = popularity - minPopularity;
		if (popularity == 0) return this.minFontSize;
		double percent = (double) popularity / (maxPopularity - minPopularity);
		return Math.round(this.minFontSize + ((this.maxFontSize - this.minFontSize) * percent));
	}
}

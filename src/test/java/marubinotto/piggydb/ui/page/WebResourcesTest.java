package marubinotto.piggydb.ui.page;

import static junit.framework.Assert.assertEquals;

import marubinotto.piggydb.ui.page.WebResources;

import org.junit.Test;

public class WebResourcesTest {

	private WebResources object = new WebResources("/context", "1.0");
	
	@Test
	public void resourcePath() throws Exception {
		String result = this.object.resourcePath("style/piggydb-about.css", false);
		assertEquals("/context/style/piggydb-about.css", result);
	}
	
	@Test
	public void resourcePathWithVersioning() throws Exception {
		String result = this.object.resourcePath("style/piggydb-about.css", true);
		assertEquals("/context/style/piggydb-about.css?1.0", result);
	}
}

package marubinotto.util.web;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;

public class WebUtilsTest {

	@Test
    public void toQueryString() throws Exception {
		Map<String, Object> parameters = new LinkedHashMap<String, Object>();
		parameters.put("foo", "bar");
		parameters.put("hoge", "huga");
		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		
		assertEquals("foo=bar&hoge=huga", result);
	}
	
	@Test
	public void getMsieVersion() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.addHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
		
		int[] version = WebUtils.getMsieVersion(request);
		assertEquals(6, version[0]);
		assertEquals(0, version[1]);
	}
}

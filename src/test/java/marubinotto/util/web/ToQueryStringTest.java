package marubinotto.util.web;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class ToQueryStringTest {
	
	private Map<String, Object> parameters;
	
	@Before
	public void given() throws Exception {
		this.parameters = new LinkedHashMap<String, Object>();
	}

	@Test
	public void oneParameter() throws Exception {
		this.parameters.put("name", "value");		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		assertEquals("name=value", result);
	}

	@Test
	public void twoParameters() throws Exception {
		this.parameters.put("name1", "value1");		
		this.parameters.put("name2", "value2");		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		assertEquals("name1=value1&name2=value2", result);
	}
	
	@Test
	public void multipleValues() throws Exception {
		this.parameters.put("name", new Object[]{"value1", "value2"});		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		assertEquals("name=value1&name=value2", result);
	}
	
	@Test
	public void longValue() throws Exception {
		this.parameters.put("name", new Long(100));		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		assertEquals("name=100", result);
	}
	
	@Test
	public void nullValue() throws Exception {
		this.parameters.put("name", null);		
		String result = WebUtils.toQueryString(parameters, "UTF-8");
		assertEquals("name=", result);
	}
}

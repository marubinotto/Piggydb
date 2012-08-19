package marubinotto.piggydb.extension;

import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

public class ExtensionTest {

	@Test
	public void allDefFiles() throws Exception {
		for (Enumeration<URL> files = Extension.allDefFiles(); files.hasMoreElements();) {
			System.out.println(files.nextElement());
		}
	}
	
	@Test
	public void testClassLoader() throws Exception {
		Extension.testClassLoader();
	}
}

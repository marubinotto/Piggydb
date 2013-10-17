package marubinotto.piggydb.extension;

import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

public class ExtensionTest {

	@Test
	public void allDefFiles() throws Exception {
		for (Enumeration<URL> files = ExtensionDeployer.allDefFiles(); files.hasMoreElements();) {
			System.out.println(files.nextElement());
		}
	}
	
	@Test
	public void testClassLoaderResources() throws Exception {
		ExtensionDeployer.testClassLoaderResources();
	}
}

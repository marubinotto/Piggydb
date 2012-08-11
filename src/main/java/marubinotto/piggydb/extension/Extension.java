package marubinotto.piggydb.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

// TODO static deployAllExtensions
// TODO deploy
// TODO register web files
// TODO user menu
public abstract class Extension {
	
	public static final String DEF_FILE_NAME = "piggydb-extension-class";

	public static Enumeration<URL> allDefFiles() throws IOException {
		return Extension.class.getClassLoader().getResources(DEF_FILE_NAME);
	}
}

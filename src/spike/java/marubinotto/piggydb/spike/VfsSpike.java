package marubinotto.piggydb.spike;

import java.net.URL;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

public class VfsSpike {

	@Test
	public void vfsPackage() throws Exception {
		URL url = getClass().getResource("/org/apache/commons/vfs2");
		System.out.println(url);
		
		FileSystemManager fsManager = VFS.getManager();
		FileObject dir = fsManager.resolveFile(url.toExternalForm());
		for (FileObject file : dir.getChildren()) {
			System.out.println(file.getName().getBaseName());
			System.out.println("  " + file.getType());
		}
	}
}

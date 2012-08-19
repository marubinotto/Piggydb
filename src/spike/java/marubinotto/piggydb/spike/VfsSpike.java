package marubinotto.piggydb.spike;

import java.net.URL;

import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.junit.Test;

public class VfsSpike {

	@Test
	public void listJarContents() throws Exception {
		URL url = getClass().getResource("/org/apache/commons/vfs2");
		System.out.println(url);
		
		FileSystemManager fsManager = VFS.getManager();
		FileObject dir = fsManager.resolveFile(url.toExternalForm());
		for (FileObject file : dir.getChildren()) {
			System.out.println(file.getName().getBaseName());
			System.out.println("  " + file.getType());
		}
	}
	
	@Test
	public void copyDir() throws Exception {
		FileSystemManager fsManager = VFS.getManager();
		FileObject tmpDir = fsManager.resolveFile(System.getProperty("java.io.tmpdir"));
		System.out.println("tmpDir: " + tmpDir.getName());
		
		String url = getClass().getResource("/org/apache/commons/vfs2").toExternalForm();
		System.out.println("url1: " + url);
		FileObject src = fsManager.resolveFile(url);
		System.out.println("url2: " + src.getName());
		
		tmpDir.copyFrom(src, new AllFileSelector());
	}
}

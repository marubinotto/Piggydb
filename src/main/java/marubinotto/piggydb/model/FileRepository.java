package marubinotto.piggydb.model;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import marubinotto.util.Assert;
import marubinotto.util.ZipUtils;

import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public interface FileRepository {

	public void putFile(Fragment fragment) throws Exception;
	
	public void getFile(OutputStream output, Fragment fragment)
	throws Exception;
	
	public int size() throws Exception;
	
	public Set<String>getFileNames() throws Exception;
	
	public void outputAll(String namePrefix, ZipOutputStream zipOut)
	throws Exception;
	
	public void clear() throws Exception;
	
	public ZipUtils.EntryReader getEntryReader() throws Exception;
	
	public void deleteFile(Fragment fragment) throws Exception;
	
	
	public static abstract class Base implements FileRepository {
		
		protected static String getFragmentFileKey(Fragment fragment) { 
			String key = fragment.getId().toString();
			if (fragment.getFileType() != null) {
				key = key + "." + fragment.getFileType();
			}
			return key;
		}		
	}
	
	
	public static class InMemory extends Base {
		
		private Map<String, byte[]> files = new HashMap<String, byte[]>();

		private Map<String, byte[]> getFiles() {
			return files;
		}

		public void putFile(Fragment fragment) throws Exception {
			Assert.Arg.notNull(fragment, "fragment");
			Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
			Assert.Arg.notNull(fragment.getFileInput(), "fragment.getFileInput()");
			
			deleteOldFile(fragment.getId());
			this.files.put(getFragmentFileKey(fragment), fragment.getFileInput().get());
		}
		
		private void deleteOldFile(Long id) {
			this.files.remove(id.toString());
			
			String prefix = id.toString() + ".";
			for (String key : new HashSet<String>(this.files.keySet())) {
				if (key.startsWith(prefix)) this.files.remove(key);
			}
		}

		public void getFile(OutputStream output, Fragment fragment)
		throws Exception {
			Assert.Arg.notNull(output, "output");
			Assert.Arg.notNull(fragment, "fragment");
			Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
			
			byte[] file = this.files.get(getFragmentFileKey(fragment));
			if (file == null) {
				return;
			}
			output.write(file);
		}
		
		public int size() {
			return this.files.size();
		}
		
		public Set<String>getFileNames() {
			return this.files.keySet();
		}
		
		public void outputAll(String namePrefix, ZipOutputStream zipOut)
		throws Exception {
			Assert.Arg.notNull(namePrefix, "namePrefix");
			Assert.Arg.notNull(zipOut, "zipOut");
			
			for (String key : this.files.keySet()) {
				ZipEntry zipEntry = new ZipEntry(namePrefix + key);
		        zipOut.putNextEntry(zipEntry);
		        zipOut.write(this.files.get(key));
			}
		}
		
		public void clear() throws Exception {
			this.files.clear();
		}
		
		public ZipUtils.EntryReader getEntryReader() throws Exception {
			return new ZipUtils.EntryReader() {
				public void readEntry(String name, InputStream input)
				throws Exception {
					getFiles().put(name, IOUtils.toByteArray(input));
				}
			};
		}
		
		public void deleteFile(Fragment fragment) throws Exception {
			Assert.Arg.notNull(fragment, "fragment");
			Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
			
			this.files.remove(getFragmentFileKey(fragment));
		}
	}
}

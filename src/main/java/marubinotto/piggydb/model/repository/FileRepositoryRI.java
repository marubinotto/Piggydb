package marubinotto.piggydb.model.repository;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.util.Assert;
import marubinotto.util.ZipUtils;

import org.apache.commons.io.IOUtils;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipOutputStream;

public class FileRepositoryRI extends FileRepository.Base {

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

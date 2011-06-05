package marubinotto.piggydb.external.file;

import static marubinotto.util.CollectionUtils.set;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.repository.AbstractFileRepository;
import marubinotto.piggydb.model.repository.FileRepositoryRI;
import marubinotto.util.Assert;
import marubinotto.util.FileSystemUtils;
import marubinotto.util.ZipUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipOutputStream;

public class FileRepositoryImpl extends AbstractFileRepository {
	
	private static Log logger = LogFactory.getLog(FileRepositoryImpl.class);
	
	private static final String DIR_SUFFIX = "-files";
	
	private File baseDirectory;	
	private FileRepository delegate;

	public void setDatabasePath(String databasePath) throws MalformedURLException {
		Assert.Arg.notNull(databasePath, "databasePath");
		
		this.delegate = null;
		if (databasePath.startsWith("mem:")) {
			logger.info("Memory mode");
			this.delegate = new FileRepositoryRI();
		}
		else if (databasePath.startsWith("file:")) {
			this.baseDirectory = FileUtils.toFile(new URL(databasePath + DIR_SUFFIX));
		}
		else if (databasePath.startsWith("~/")) {
			String path = System.getProperty("user.home") + databasePath.substring(1) + DIR_SUFFIX;
			this.baseDirectory = new File(path);
		}
		else {
			throw new IllegalArgumentException("Invalid database path: " + databasePath);
		}
		if (this.baseDirectory != null) 
			logger.info("baseDirectory: " + this.baseDirectory.getAbsolutePath());
	}

	public void setBaseDirectory(File baseDirectory) {
		Assert.Arg.notNull(baseDirectory, "baseDirectory");
		Assert.require(baseDirectory.isDirectory(), "baseDirectory.isDirectory()");
		
		this.baseDirectory = baseDirectory;
		this.delegate = null;
	}

	public File getBaseDirectory() {
		return this.baseDirectory;
	}

	public void putFile(Fragment fragment) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		Assert.Arg.notNull(fragment.getFileInput(), "fragment.getFileInput()");
		
		if (this.delegate != null) {
			this.delegate.putFile(fragment);
			return;
		}
		
		ensureBaseDirectoryCreated();
		
		deleteOldFile(fragment.getId());
		
		File filePath = getFragmentFilePath(fragment);
		logger.info("Putting a file: " + filePath);
		fragment.getFileInput().write(filePath);
	}
	
	private void deleteOldFile(Long id) {
		File noExtension = new File(this.baseDirectory, id.toString());
		if (noExtension.isFile()) {
			logger.info("Deleting a file: " + noExtension);
			noExtension.delete();
		}
		
		final String prefix = id.toString() + ".";
		File[] filesWithExtension = this.baseDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(prefix);
			}
		});
		for (File file : filesWithExtension) {
			logger.info("Deleting a file: " + file);
			file.delete();
		}
	}
	
	public void getFile(OutputStream output, Fragment fragment)
	throws Exception {
		Assert.Arg.notNull(output, "output");
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		
		if (this.delegate != null) {
			this.delegate.getFile(output, fragment);
			return;
		}
		
		ensureBaseDirectoryCreated();
		File filePath = getFragmentFilePath(fragment);
		InputStream input = FileUtils.openInputStream(filePath);
        try {
            IOUtils.copyLarge(input, output);
        }
        finally {
            IOUtils.closeQuietly(input);
        }
	}
	
	public int size() throws Exception {
		if (this.delegate != null) return this.delegate.size();
		
		ensureBaseDirectoryCreated();
		return this.baseDirectory.listFiles().length;
	}
	
	public Set<String>getFileNames() throws Exception {
		if (this.delegate != null) return this.delegate.getFileNames();
		
		ensureBaseDirectoryCreated();
		return set(this.baseDirectory.list());
	}
	
	public void outputAll(String namePrefix, ZipOutputStream zipOut)
	throws Exception {
		Assert.Arg.notNull(namePrefix, "namePrefix");
		Assert.Arg.notNull(zipOut, "zipOut");
		
		if (this.delegate != null) {
			logger.debug("Delegating outputAll ...");
			this.delegate.outputAll(namePrefix, zipOut);
			return;
		}
		
		ensureBaseDirectoryCreated();
		ZipUtils.appendDirectory(
			namePrefix, 
			getBaseDirectory(), 
			getBaseDirectory(), 
			TrueFileFilter.INSTANCE, 
			zipOut);
	}
	
	public void clear() throws Exception {
		if (this.delegate != null) {
			this.delegate.clear();
			return;
		}
		
		ensureBaseDirectoryCreated();
		FileUtils.cleanDirectory(this.baseDirectory);
	}
	
	public ZipUtils.EntryReader getEntryReader() throws Exception {
		if (this.delegate != null) return this.delegate.getEntryReader();
		
		ensureBaseDirectoryCreated();
		return new ZipUtils.Directory(this.baseDirectory);
	}
	
	public void deleteFile(Fragment fragment) throws Exception {
		Assert.Arg.notNull(fragment, "fragment");
		Assert.Arg.notNull(fragment.getId(), "fragment.getId()");
		
		if (this.delegate != null) {
			this.delegate.deleteFile(fragment);
			return;
		}
		
		ensureBaseDirectoryCreated();
		File filePath = getFragmentFilePath(fragment);
		logger.info("Deleting file: " + filePath);
		FileSystemUtils.forceDeleteIfExist(filePath);
	}
	
// Internal
	
	private void ensureBaseDirectoryCreated() throws IOException {
		Assert.Property.requireNotNull(baseDirectory, "baseDirectory");
		
		if (!this.baseDirectory.isDirectory()) {
			logger.info("Creating baseDirectory: " + this.baseDirectory.getAbsolutePath());
			FileUtils.forceMkdir(this.baseDirectory);
		}
	}
	
	private File getFragmentFilePath(Fragment fragment) {
		Assert.Property.requireNotNull(baseDirectory, "baseDirectory");
		return new File(this.baseDirectory, getFragmentFileKey(fragment));
	}	
}

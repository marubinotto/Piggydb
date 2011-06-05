package marubinotto.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;

public class ZipUtils {
	
	private static Log logger = LogFactory.getLog(ZipUtils.class);
	
// Write
	
    public static void zipDirectory(
    	String namePrefix,
        File baseDir,
        FileFilter fileFilter,
        File zipFile,
        String nameEncoding)
    throws IOException {
        Assert.Arg.notNull(baseDir, "baseDir");
        Assert.Arg.notNull(fileFilter, "fileFilter");
        Assert.Arg.notNull(zipFile, "zipFile");
        Assert.Arg.notNull(nameEncoding, "nameEncoding");

        OutputStream output = FileUtils.openOutputStream(zipFile);
        try {
        	zipDirectory(namePrefix, baseDir, fileFilter, output, nameEncoding);
        }
        finally {
            output.close();
        }
    }

    public static void zipDirectory(
    	String namePrefix,
        File baseDir,
        FileFilter fileFilter,
        OutputStream output,
        String nameEncoding)
    throws IOException {
        Assert.Arg.notNull(baseDir, "baseDir");
        Assert.Arg.notNull(fileFilter, "fileFilter");
        Assert.Arg.notNull(output, "output");
        Assert.Arg.notNull(nameEncoding, "nameEncoding");

        ZipOutputStream zipOut = new ZipOutputStream(output);
        try {
            zipOut.setEncoding(nameEncoding);
            appendDirectory(namePrefix, baseDir, baseDir, fileFilter, zipOut);
        }
        finally {
            zipOut.close();
        }
    }

	public static void appendDirectory(
		String namePrefix, 
		File baseDir, 
		File targetDir,
        FileFilter fileFilter, 
		ZipOutputStream zipOut)
	throws IOException {
		Assert.Arg.notNull(baseDir, "baseDir");
		Assert.Arg.notNull(targetDir, "targetDir");
		Assert.Arg.notNull(fileFilter, "fileFilter");
		Assert.Arg.notNull(zipOut, "zipOut");
		
		File[] files = targetDir.listFiles(fileFilter);
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + targetDir);
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
            	appendDirectory(namePrefix, baseDir, files[i], fileFilter, zipOut);
            }
            else {
            	appendFile(namePrefix, baseDir, files[i], zipOut);
            }
        }
	}
	
	public static void appendFile(
		String namePrefix, 
		File baseDir, 
		File file, 
		ZipOutputStream zipOut)
	throws IOException {
		Assert.Arg.notNull(baseDir, "baseDir");
		Assert.Arg.notNull(file, "file");
		Assert.Arg.notNull(zipOut, "zipOut");
		
		if (namePrefix == null) namePrefix = "";
		
		String path = FileSystemUtils.getRelativePath(baseDir, file);
        ZipEntry zipEntry = new ZipEntry(namePrefix + path);
        zipOut.putNextEntry(zipEntry);
        
        InputStream fileInput = FileUtils.openInputStream(file);
        try {
        	org.apache.commons.io.IOUtils.copyLarge(fileInput, zipOut);
        }
        finally {
            fileInput.close();
        }
	}
	
// Read

    public static boolean isZip(File file) throws IOException {
        Assert.Arg.notNull(file, "file");
        
        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(file);
        }
        catch (Exception e) {
        	logger.info("Not zip: " + e.toString());
            return false;
        }
        finally {
            if (zipFile != null) zipFile.close();
        }
        return true;
    }
    
    public static boolean containsEntry(File zipFilePath, String name, String nameEncoding)
    throws IOException {
        Assert.Arg.notNull(zipFilePath, "zipFilePath");
        Assert.Arg.notNull(name, "name");
        Assert.Arg.notNull(nameEncoding, "nameEncoding");

        ZipFile zipFile = new ZipFile(zipFilePath, nameEncoding);
        ZipEntry entry = zipFile.getEntry(name);
        zipFile.close();
        return entry != null;
    }

    @SuppressWarnings("unchecked")
	public static List<String> getEntryNames(File zipFilePath, String nameEncoding) 
    throws IOException {
        Assert.Arg.notNull(zipFilePath, "zipFilePath");
        Assert.Arg.notNull(nameEncoding, "nameEncoding");

        List<String> names = new ArrayList<String>();
        ZipFile zipFile = new ZipFile(zipFilePath, nameEncoding);
        for (Enumeration e = zipFile.getEntries(); e.hasMoreElements();) {
            ZipEntry entry = (ZipEntry)e.nextElement();
            names.add(entry.getName());
        }
        zipFile.close();
        return names;
    }

    @SuppressWarnings("unchecked")
    public static void extract(
    	File zipFilePath, 
    	String namePrefix,
    	String nameEncoding, 
    	EntryReader entryReader) 
    throws Exception {
        Assert.Arg.notNull(zipFilePath, "zipFilePath");
        Assert.Arg.notNull(nameEncoding, "nameEncoding");
        Assert.Arg.notNull(entryReader, "entryReader");

        ZipFile zipFile = new ZipFile(zipFilePath, nameEncoding);
        try {
            for (Enumeration e = zipFile.getEntries(); e.hasMoreElements();) {
                ZipEntry entry = (ZipEntry)e.nextElement();
                if (entry.isDirectory()) {
                    continue;
                }
                
                // File name
                String fileName = entry.getName();
                if (namePrefix != null) {
                	if (!entry.getName().startsWith(namePrefix)) {
                		continue;
                	}
                	fileName = fileName.substring(namePrefix.length());
                }
                
                // Output
                InputStream input = zipFile.getInputStream(entry);
                try {
                	entryReader.readEntry(fileName, input);
                }
                finally {
                	input.close();
                }
            }
        }
        finally {
            if (zipFile != null) zipFile.close();
        }
    }
    
    public static interface EntryReader {
    	public void readEntry(String name, InputStream input) throws Exception;
    }
    
    public static class Directory implements EntryReader {
    	private File directory;
    	
    	public Directory(File directory) {
    		Assert.Arg.notNull(directory, "directory");
    		this.directory = directory;
    	}
    	
		public void readEntry(String name, InputStream input) throws Exception {
			File file = new File(this.directory, name);
			OutputStream output = new BufferedOutputStream(
				FileUtils.openOutputStream(file));
			try {
                org.apache.commons.io.IOUtils.copy(input, output);
            }
            finally {
            	output.close();
            }
		}
    }
}

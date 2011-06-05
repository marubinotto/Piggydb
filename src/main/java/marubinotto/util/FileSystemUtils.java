package marubinotto.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileSystemUtils {
	
	private static Log logger = LogFactory.getLog(FileSystemUtils.class);
	
    private static File testDirBase;

    public static File getEmptyDirectory() throws IOException {
        if (testDirBase == null) {
            testDirBase = (new File("target/temp/testdirs/")).getAbsoluteFile();
            testDirBase.mkdirs();
        }
        File dir = null;
        while (true) {
        	dir = new File(testDirBase, String.valueOf(System.currentTimeMillis()));
        	if (!dir.exists()) {
        		dir.mkdirs();
        		break;
        	}
        }
        return dir;
    }

    public static void forceDeleteIfExist(File file) throws IOException {
        if (file.exists()) {
            FileUtils.forceDelete(file);
        }
    }

    public static List<Object> listFilesRecursively(File baseDir, boolean absolute)
    throws IOException {
        return listFilesRecursively(baseDir, TrueFileFilter.INSTANCE, absolute);
    }

    public static List<Object> listFilesRecursively(
        File baseDir,
        FileFilter fileFilter,
        boolean absolute)
    throws IOException {
        if (!baseDir.isDirectory()) {
            return new ArrayList<Object>();
        }
        List<Object> list = new ArrayList<Object>();
        doListFilesRecursively(baseDir, baseDir, fileFilter, list, absolute);
        return list;
    }

    private static void doListFilesRecursively(
        File baseDir,
        File targetDir,
        FileFilter fileFilter,
        List<Object> list,
        boolean absolute)
    throws IOException {
        File[] files = targetDir.listFiles(fileFilter);
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + targetDir);
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                doListFilesRecursively(baseDir, files[i], fileFilter, list, absolute);
            }
            else {
                String path = getRelativePath(baseDir, files[i]);
                if (absolute) {
                    list.add(files[i]);
                }
                else {
                    list.add(path);
                }
            }
        }
    }

    public static String getRelativePath(File baseDir, File targetFile) {
    	Assert.Arg.notNull(baseDir, "baseDir");
    	Assert.Arg.notNull(targetFile, "targetFile");
    	
        String baseDirPath = FilenameUtils.normalizeNoEndSeparator(baseDir.getAbsolutePath());
        String targetFilePath = FilenameUtils.normalizeNoEndSeparator(targetFile.getAbsolutePath());
        String path = StringUtils.replace(targetFilePath, baseDirPath + File.separator, "");
        path = path.replace('\\', '/');
        return path;
    }

    public static File createFile(File base, String path, String content)
    throws IOException {
        File file = new File(base, path);
        FileUtils.writeStringToFile(file, content);
        return file;
    }

    public static File createFile(File base, String path, String content, String encoding)
    throws IOException {
        File file = new File(base, path);
        FileUtils.writeStringToFile(file, content, encoding);
        return file;
    }

    public static File createFile(File base, String path, String[] content)
    throws IOException {
        File file = new File(base, path);
        FileUtils.writeLines(file, Arrays.asList(content));
        return file;
    }

    public static File createFile(File base, String path, String[] content, String encoding)
    throws IOException {
        File file = new File(base, path);
        FileUtils.writeLines(file, encoding, Arrays.asList(content));
        return file;
    }
    
    public static File urlToFile(URL url) {
        if (url == null) {
            return null;
        }
        File file = null;
        try {
            file = new File(new URI(url.toExternalForm()));
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e.toString());
        }
        return file;
    }
    
    @SuppressWarnings("unchecked")
	public static File getPackageDirectory(Class c) {
        Assert.Arg.notNull(c, "c");

        URL url = c.getResource(ClassUtils.getShortClassName(c) + ".class");
        File file = FileSystemUtils.urlToFile(url);
        if (file == null) {
            return null;
        }
        return file.getParentFile();
    }

    @SuppressWarnings("unchecked")
    public static File getFile(Class base, String path) {
        return urlToFile(base.getResource(path));
    }
    
    public static File getUserHome(boolean enableWindowsHome) {
    	if (!SystemUtils.IS_OS_WINDOWS) {
    		return new File(SystemUtils.USER_HOME);
    	}
    	
    	File home = null;
    	
    	if (enableWindowsHome) {
	    	home = returnIfPathIsDirectory(System.getenv("HOME"));
	    	if (home != null) {
	    		logger.info("getUserHome: return Windows HOME");
	    		return home;
	    	}
	    	
	    	String drive = System.getenv("HOMEDRIVE");
	    	String path = System.getenv("HOMEPATH");
	    	if (drive != null && path != null) {
	    		home = returnIfPathIsDirectory(drive + path);
	    		if (home != null) {
	    			logger.info("getUserHome: return Windows HOMEDRIVE + HOMEPATH");
	    			return home;
	    		}
	    	}
    	}
    	
    	home = returnIfPathIsDirectory(System.getenv("USERPROFILE"));
    	if (home != null) {
    		logger.info("getUserHome: return Windows USERPROFILE");
    		return home;
    	}
    	
    	home = returnIfPathIsDirectory(System.getenv("ALLUSERSPROFILE"));
    	if (home != null) {
    		logger.info("getUserHome: return Windows ALLUSERSPROFILE");
    		return home;
    	}
    	
    	home = returnIfPathIsDirectory(System.getenv("SYSTEMDRIVE") + "\\");
    	if (home != null) {
    		logger.info("getUserHome: return Windows SYSTEMDRIVE");
    		return home;
    	}
    	
    	logger.info("getUserHome: return Windows C:\\ as fallback");
    	return new File("C:\\");
    }
    
    private static File returnIfPathIsDirectory(String path) {
    	if (path == null) return null;
    	
    	File file = new File(path);
    	if (file.isDirectory()) {
    		return file;
    	}
    	else {
    		return null;
    	}
    }
}

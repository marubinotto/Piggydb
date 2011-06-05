package marubinotto.util.fixture;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import marubinotto.util.Assert;
import marubinotto.util.FileSystemUtils;
import marubinotto.util.xml.XmlTree;
import marubinotto.util.xml.XmlTreeImpl;

import org.apache.commons.io.FileUtils;

import junitx.framework.ArrayAssert;
import junitx.framework.FileAssert;

/**
 * marubinotto.util.fixture.FileSystemFixture
 */
public class FileSystemFixture {

    private File baseDirectory;

    public FileSystemFixture(File baseDirectory) {
        Assert.Arg.notNull(baseDirectory, "baseDirectory");
        this.baseDirectory = baseDirectory;
    }

    public File getBaseDirectory() {
        return this.baseDirectory;
    }

    public void clean() throws IOException {
        if (this.baseDirectory.exists()) {
            FileUtils.cleanDirectory(this.baseDirectory);
        }
    }

    public File getFile(String path) {
        return new File(this.baseDirectory, path);
    }

    public File put(String path, String content)
    throws IOException {
        return FileSystemUtils.createFile(this.baseDirectory, path, content);
    }

    public File put(String path, String content, String encoding)
    throws IOException {
        return FileSystemUtils.createFile(this.baseDirectory, path, content, encoding);
    }

    public String[] getAllFilePaths() throws IOException {
        List<Object> paths = FileSystemUtils.listFilesRecursively(this.baseDirectory, false);
        return (String[])paths.toArray(new String[0]);
    }

    public File[] getAllFiles() throws IOException {
        List<Object> files = FileSystemUtils.listFilesRecursively(this.baseDirectory, true);
        return (File[])files.toArray(new File[0]);
    }

    public XmlTree getAsXml(String path) throws Exception {
        XmlTree projectXml = new XmlTreeImpl();
        projectXml.readFrom(getFile(path));
        return projectXml;
    }

    public void fileContentShouldEqual(String path, String content, String encoding)
    throws IOException {
        junit.framework.Assert.assertEquals(
            "Invalid file content: " + path,
            content,
            FileUtils.readFileToString(new File(this.baseDirectory, path), encoding));
    }

    public void shouldBeEmpty() {
        if (this.baseDirectory.isDirectory()) {
            junit.framework.Assert.assertEquals(0, this.baseDirectory.list().length);
        }
    }

    public void shouldContainFile(String filePath) {
        File file = new File(this.baseDirectory, filePath);
        junit.framework.Assert.assertTrue(
            "Should be a file: " + file,
            file.isFile());
    }

    public void shouldContain(Map<String, String> files, String encoding) throws IOException {
        for (Iterator<String> i = files.keySet().iterator(); i.hasNext();) {
            String path = i.next();
            String content = (String)files.get(path);
            if (content != null) {
                fileContentShouldEqual(path, content, encoding);
            }
            else {
                shouldContainFile(path);
            }
        }
    }

    public void shouldEqual(Map<String, String> files, String encoding) throws IOException {
        fileSetShouldEqual(files.keySet());
        shouldContain(files, encoding);
    }

    public void fileSetShouldEqual(Set<String> expectedPaths) throws IOException {
        Set<String> actualPaths = new HashSet<String>(Arrays.asList(getAllFilePaths()));
        junit.framework.Assert.assertTrue(
            "Expected: " + expectedPaths + " But: " + actualPaths,
            actualPaths.equals(expectedPaths));
    }

    public void fileShouldEqual(String path, File expectedContent) {
        FileAssert.assertBinaryEquals(expectedContent, getFile(path));
    }

    public void fileShouldEqual(String path, byte[] data) throws IOException {
        ArrayAssert.assertEquals(
            data, FileUtils.readFileToByteArray(getFile(path)));
    }
}

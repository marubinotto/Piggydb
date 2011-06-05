package marubinotto.util.fixture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;
import marubinotto.util.FileSystemUtils;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * @see FileSystemFixture
 */
public class FileSystemFixtureTest {

    private FileSystemFixture object;
    private File baseDirectory;

    @Before
    public void givenAnInstanceWithEmptyDirectory() 
    throws Exception {
        this.baseDirectory = FileSystemUtils.getEmptyDirectory();
        this.object = new FileSystemFixture(this.baseDirectory);
    }

    @Test
    public void shouldCreateFileWithContent() throws Exception {
    	// When
        this.object.put("path/to/file", "content");

        // Then
        File expectedFile = new File(this.baseDirectory, "path/to/file");
        assertTrue(expectedFile.isFile());
        assertEquals("content", FileUtils.readFileToString(expectedFile, null));
    }

    @Test
    public void shouldReturnAllFilePathsWhichExist() throws Exception {
    	// Given
        this.object.put("path/to/file", "content");

        // When
        String[] paths = this.object.getAllFilePaths();

        // Then
        assertEquals(1, paths.length);
        assertEquals("path/to/file", paths[0]);
    }

    @Test
    public void shouldEnsureSpecifiedFileIsContained() throws Exception {
    	// Given
    	this.object.put("path/to/file", "content");
    	
    	// When
    	this.object.shouldContainFile("path/to/file");
    }

    @Test(expected=AssertionFailedError.class)
    public void shouldThrowExceptionWhenEnsureNonexistentFileIsContained() 
    throws Exception {
    	// Given
        this.object.put("path/to/file", "content");
        
        // When
        this.object.shouldContainFile("no-such-path");
    }

    @Test
    public void shouldEnsureCotentOfSpecifiedFileIsAsExpected() 
    throws Exception {
    	// Given
        this.object.put("path/to/file", "content");
        
        // When
        this.object.fileContentShouldEqual("path/to/file", "content", null);
    }

    @Test(expected=AssertionFailedError.class)
    public void shouldThrowExceptionWhenEnsureCotentOfSpecifiedFileEqualsToInvalidValue()
    throws Exception {
    	// Given
        this.object.put("path/to/file", "content");
        
        // When
        this.object.fileContentShouldEqual("path/to/file", "invalid-content", null);
    }

    @Test
    public void shouldEnsureAllFilesAreAsExpected() 
    throws Exception {
    	// Given
        this.object.put("file1", "content1");
        this.object.put("file2", "content2");

        // When
        Map<String, String> files = new HashMap<String, String>();
        files.put("file1", "content1");
        files.put("file2", "content2");
        this.object.shouldEqual(files, null);
    }

    @Test(expected=AssertionFailedError.class)
    public void shouldThrowExceptionWhenEnsureTheContentsOfAllFilesEqualToInvalidSetOfFiles() 
    throws Exception {
    	// Given
        this.object.put("file1", "content1");
        this.object.put("file2", "content2");
        this.object.put("file3", "content3");

        // When
        Map<String, String> files = new HashMap<String, String>();
        files.put("file1", "content1");
        files.put("file2", "content2");
        this.object.shouldEqual(files, null);
    }

    @Test(expected=AssertionFailedError.class)
    public void shouldThrowExceptionWhenEnsureTheContentsOfAllFilesEqualToInvalidContents()
    throws Exception {
    	// Given
        this.object.put("file1", "content1");
        this.object.put("file2", "content2");

        // When
        Map<String, String> files = new HashMap<String, String>();
        files.put("file1", "content1");
        files.put("file2", "invalid-content");
        this.object.shouldEqual(files, null);
    }

    @Test
    public void shouldEnsureAllFilesAreAsExpectedWithoutContent()
    throws Exception {
    	// Given
        this.object.put("file1", "content1");
        this.object.put("file2", "content2");

        // When
        Map<String, String> files = new HashMap<String, String>();
        files.put("file1", "content1");
        files.put("file2", null);
        this.object.shouldEqual(files, null);
    }
}

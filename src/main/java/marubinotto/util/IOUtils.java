package marubinotto.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class IOUtils {
	
    public static Writer createWriter(File file, String encode)
    throws UnsupportedEncodingException, FileNotFoundException {
    	Assert.Arg.notNull(file, "file");
    	Assert.Arg.notNull(encode, "encode");
    	
        return new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(file), encode));
    }
}

package marubinotto.piggydb.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import marubinotto.piggydb.model.FileRepository;
import marubinotto.util.Assert;
import marubinotto.util.RdbUtils;
import marubinotto.util.ZipUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
import org.dbunit.DatabaseUnitException;
import org.dbunit.dataset.DataSetException;

public class PigDump {
	
	private static Log logger = LogFactory.getLog(PigDump.class);

	public static final String PATH_RDB_DUMP = "rdb-dump.xml";
	public static final String PATH_FILES = "files/";
	
	public static final String FILE_NAME_ENCODING = "UTF-8";
	
	public static final String[] TABLES = new String[]{
		"global_setting", "tag", "tagging", "fragment", "filter", "fragment_relation"
	};
	
	private DataSource dataSource;
	private FileRepository fileRepository;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setFileRepository(FileRepository fileRepository) {
		this.fileRepository = fileRepository;
	}
	
	private Connection getJdbcConnection() {
		return RdbUtils.getSpringTransactionalConnection(this.dataSource);
	}

	public void outputDump(OutputStream output) throws Exception {
		Assert.Arg.notNull(output, "output");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");
		
		ZipOutputStream zipOut = new ZipOutputStream(output);
		try {
			zipOut.setEncoding(FILE_NAME_ENCODING);
			outputRdbDump(zipOut);
			this.fileRepository.outputAll(PATH_FILES, zipOut);
		}
		finally {
            zipOut.close();
        }
	}
	
	private void outputRdbDump(ZipOutputStream zipOut) 
	throws IOException, DataSetException, SQLException {
		Assert.Property.requireNotNull(dataSource, "dataSource");
		
		ZipEntry zipEntry = new ZipEntry(PATH_RDB_DUMP);
		zipOut.putNextEntry(zipEntry);
		
		RdbUtils.exportAsXml(getJdbcConnection(), TABLES, zipOut);
	}
	
	public boolean checkDumpFile(File dumpFile) throws IOException {
		Assert.Arg.notNull(dumpFile, "dumpFile");
		
		if (!ZipUtils.isZip(dumpFile)) {
			logger.debug("Not zip: " + dumpFile);
			return false;
		}
		if (!ZipUtils.containsEntry(dumpFile, PATH_RDB_DUMP, FILE_NAME_ENCODING)) {
			logger.debug("Does not contain a rdb dump: " + dumpFile);
			return false;
		}
		return true;
	}
	
	public void restore(File dumpFile) throws Exception {
		Assert.Arg.notNull(dumpFile, "dumpFile");
		Assert.Property.requireNotNull(fileRepository, "fileRepository");
		
		restoreRdb(dumpFile);
		
		this.fileRepository.clear();
		ZipUtils.extract(
			dumpFile, 
			PATH_FILES, 
			FILE_NAME_ENCODING, 
			this.fileRepository.getEntryReader());
	}
	
	private void restoreRdb(File dumpFile) 
	throws IOException, SQLException, DatabaseUnitException {
		Assert.Property.requireNotNull(dataSource, "dataSource");
		
		ZipFile zipFile = new ZipFile(dumpFile, FILE_NAME_ENCODING);
		try {
			ZipEntry entry = zipFile.getEntry(PATH_RDB_DUMP);
			if (entry != null) {
				InputStream input = zipFile.getInputStream(entry);
				try {
					RdbUtils.cleanImportXml(getJdbcConnection(), input);
				}
				finally {
					input.close();
				}
			}
		}
		finally {
			zipFile.close();
		}
	}
}

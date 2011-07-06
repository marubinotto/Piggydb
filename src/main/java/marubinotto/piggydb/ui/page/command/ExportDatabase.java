package marubinotto.piggydb.ui.page.command;

import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.impl.PigDump;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.piggydb.ui.page.common.DatabaseSpecificBeans;
import marubinotto.util.RdbUtils;
import marubinotto.util.time.DateTime;
import marubinotto.util.web.WebUtils;

public class ExportDatabase extends Command {

	@Override
	protected String[] getAuthorizedRoles() {
		return new String[]{Role.OWNER.getName()};
	}

	@Override
	protected void execute() throws Exception {
		HttpServletResponse response = getContext().getResponse();
		response.setContentType("application/octet-stream");

		String timeStamp = DateTime.getCurrentTime().format("yyyyMMddHHmmss");
		if (getDomain().getFileRepository().size() > 0) {
			getLogger().info("Exporting as a pig dump ...");
			WebUtils.setFileName(response, "piggydb-" + timeStamp + ".pig");
			getPigDump().outputDump(response.getOutputStream());
		}
		else {
			getLogger().info("Exporting as an XML ...");
			WebUtils.setFileName(response, "piggydb-" + timeStamp + ".xml");
			RdbUtils.exportAsXml(
				new DatabaseSpecificBeans(
					getApplicationContext()).getJdbcConnection(), 
					PigDump.TABLES, 
					response.getOutputStream());
		}
		response.flushBuffer();
	}

	private PigDump getPigDump() {
		return (PigDump) getBean("pigDump");
	}
}

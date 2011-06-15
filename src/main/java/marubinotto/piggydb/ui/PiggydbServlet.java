package marubinotto.piggydb.ui;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import marubinotto.piggydb.impl.jdbc.DatabaseSchema;
import marubinotto.piggydb.impl.jdbc.SequenceAdjusterList;
import marubinotto.piggydb.ui.util.ModifiedClickContext;
import marubinotto.util.procedure.Procedure;
import marubinotto.util.procedure.Transaction;
import net.sf.click.Context;
import net.sf.click.extras.spring.SpringClickServlet;
import net.sf.click.util.ClickLogger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PiggydbServlet extends SpringClickServlet {
	
	private static Log log = LogFactory.getLog(PiggydbServlet.class);
	
	private static final String BEAN_TRANSACTION = "transaction";
	private static final String BEAN_DATABASE_SCHEMA = "databaseSchemaForInit";
	private static final String BEAN_SEQUENCE_ADJUSTER_LIST = "sequenceAdjusterList";

	@Override
	public void init() throws ServletException {
		super.init();
		
		log.info("ServerInfo: " + getServletContext().getServerInfo());
		log.info("ContextName: " + getServletContext().getServletContextName());
		// log.info("ContextPath: " + getServletContext().getContextPath());  // Servlet 2.5
		
		// Suppress click logger
		this.logger.setLevel(ClickLogger.ERROR_ID + 1);
		
		
		// Update database schema
		//
		// The transaction doesn't make any sense because H2 doesn't support transactional DDL
		// (an implicit 'commit' occurs after a DDL statement is executed). So database schema 
		// consistency cannot be assured.
		Transaction transaction = (Transaction)this.applicationContext.getBean(BEAN_TRANSACTION);
		final DatabaseSchema schema = (DatabaseSchema)
			this.applicationContext.getBean(BEAN_DATABASE_SCHEMA);
		try {
			transaction.execute(new Procedure() {
				public Object execute(Object input) throws Exception {
					schema.update();
					return null;
				}
			});
		}
		catch (Exception e) {
			// Just log the error to avoid a situation in which the server cannot be started
			// for database inconsistency. When the database is found to be inconsistent,
			// the user should export their data and delete the H2 database files, then
			// restart the server to re-create a database and restore it with the exported data.
			//
			// FIXME It's possible that a user cannot export the database because of an exception 
			// for inconsistent schema.
			log.error(e);	
		}
		log.info("Schema updated to: " + schema.getVersion());
		
		
		// Workaround for H2 sequence problem after restart
		// 
		// sequences are incremented not by one but by 32 on the file system. 
		// Internally (in memory) they are not of course, 
		// but after a system crash the value read 
		// from the disk will be at most 32 too high (average 16). 
		SequenceAdjusterList adjusterList = (SequenceAdjusterList)
			this.applicationContext.getBean(BEAN_SEQUENCE_ADJUSTER_LIST);
		try {
			adjusterList.adjust();
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	protected Context createContext(
		HttpServletRequest request,
		HttpServletResponse response, 
		boolean isPost) {

		return new ModifiedClickContext(
			getServletContext(),
			getServletConfig(),
			request,
			response,
			isPost,
			this);
	}
}

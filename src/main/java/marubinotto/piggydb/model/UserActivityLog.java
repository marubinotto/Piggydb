package marubinotto.piggydb.model;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserActivityLog {

	private static UserActivityLog instance = new UserActivityLog();
	
	public static UserActivityLog getInstance() {
		return instance;
	}
	
	private static Log logger = LogFactory.getLog(UserActivityLog.class);
	
	public void log(String userName, String action) {
		if (!logger.isInfoEnabled()) return;
		logger.info(userName + " - " + action);
	}
	
	public void log(String userName, String action, Fragment fragment) {
		if (!logger.isInfoEnabled()) return;
		
		String headline = StringUtils.defaultString(fragment.makeHeadline());
		if (StringUtils.isNotBlank(headline)) headline = " \"" + headline + "\"";
		if (fragment.isFile()) headline = headline + " (file: " + fragment.getFileSize() + ")";
		log(userName, action + " - #" + fragment.getId() + headline);
	}
	
	public void log(String userName, String action, Tag tag) {
		if (!logger.isInfoEnabled()) return;
		
		log(userName, action + " - tag:" + tag.getId() + " \"" + tag.getName() + "\"");
	}
}

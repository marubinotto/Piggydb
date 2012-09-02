package marubinotto.piggydb.model.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.GlobalSetting;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.util.Assert;
import marubinotto.util.PasswordDigest;

public class OwnerAuth implements Password {

	private static Log logger = LogFactory.getLog(OwnerAuth.class);

	public static final String USER_NAME_OWNER = "owner";
	public static final String DEFAULT_PASSWORD_OWNER = "owner";
	public static final String GSK_OWNER_PASSWORD = "owner.password";

	private GlobalSetting globalSetting;

	public void setGlobalSetting(GlobalSetting globalSetting) {
		this.globalSetting = globalSetting;
	}

	public boolean validatePassword(String password) throws Exception {
		Assert.Property.requireNotNull(globalSetting, "globalSetting");

		String storedPassword = this.globalSetting.get(GSK_OWNER_PASSWORD);
		if (storedPassword == null) {
			return DEFAULT_PASSWORD_OWNER.equals(password);
		}

		PasswordDigest pd = new PasswordDigest();
		String encrypted = pd.digestWithStoredSalt(password, storedPassword);

		return encrypted.equals(storedPassword);
	}

	public boolean authorizeAsOwner(User user, String password) throws Exception {
		Assert.Arg.notNull(user, "user");
		Assert.Arg.notNull(password, "password");

		if (!USER_NAME_OWNER.equals(user.getName())) {
			logger.info("Not owner's name: " + user.getName());
			return false;
		}
		if (!validatePassword(password)) {
			logger.info("Owner auth NG: " + user);
			return false;
		}

		logger.info("Owner auth OK: " + user);
		user.addRole(Role.OWNER);
		user.addRole(Role.INTERNAL_USER);
		return true;
	}

	public void changePassword(String password) throws Exception {
		Assert.Arg.notNull(password, "password");
		Assert.Property.requireNotNull(globalSetting, "globalSetting");

		PasswordDigest pd = new PasswordDigest();
		String encrypted = pd.createSshaDigest(password);

		this.globalSetting.put(GSK_OWNER_PASSWORD, encrypted);
	}
}

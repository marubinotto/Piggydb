package marubinotto.piggydb.model.auth;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.FragmentRepository;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.util.Assert;

public class DefaultAuth {

	private static Log logger = LogFactory.getLog(DefaultAuth.class);

	private FragmentRepository fragmentRepository;

	public void setFragmentRepository(FragmentRepository fragmentRepository) {
		this.fragmentRepository = fragmentRepository;
	}

	public boolean authorizeAsNormalUser(User user, String password) throws Exception {
		Assert.Arg.notNull(user, "user");
		Assert.Arg.notNull(password, "password");
		Assert.Property.requireNotNull(fragmentRepository, "fragmentRepository");

		Fragment fragment = this.fragmentRepository.getUserFragment(user.getName());
		if (fragment == null) {
			logger.info("Not an internal user: " + user);
			return false;
		}

		if (!fragment.validatePassword(password)) {
			logger.info("Invalid password: " + user);
			return false;
		}

		user.homeFragmentId = fragment.getId();
		user.addRole(Role.INTERNAL_USER);
		return true;
	}
}

package marubinotto.piggydb.ui.page;

import marubinotto.piggydb.model.Fragment;
import marubinotto.piggydb.model.auth.OwnerAuth;
import marubinotto.piggydb.model.auth.Password;
import marubinotto.piggydb.model.enums.Role;
import marubinotto.piggydb.ui.page.common.AbstractBorderPage;
import marubinotto.piggydb.ui.page.common.AbstractFragmentsPage;
import net.sf.click.control.Form;
import net.sf.click.control.PasswordField;
import net.sf.click.control.Submit;

public class PasswordPage extends AbstractBorderPage {

	@Override
	protected String[] getAuthorizedRoles() {
		return new String[]{Role.OWNER.getName(), Role.INTERNAL_USER.getName()};
	}

	//
	// Control
	//

	public Form passwordForm = new Form();
	private PasswordField currentPasswordField = new PasswordField("currentPassword", true);
	private PasswordField newPasswordField = new PasswordField("newPassword", true);
	private PasswordField confirmNewPasswordField = new PasswordField("confirmNewPassword", true);

	@Override
	public void onInit() {
		super.onInit();
		initControls();
	}

	private void initControls() {
		this.currentPasswordField.setLabel(getMessage("PasswordPage-current-password"));
		this.currentPasswordField.setSize(30);
		this.passwordForm.add(this.currentPasswordField);

		this.newPasswordField.setLabel(getMessage("PasswordPage-new-password"));
		this.newPasswordField.setSize(30);
		this.passwordForm.add(this.newPasswordField);

		this.confirmNewPasswordField.setLabel(getMessage("PasswordPage-confirm-new-password"));
		this.confirmNewPasswordField.setSize(30);
		this.passwordForm.add(this.confirmNewPasswordField);

		this.passwordForm.add(new Submit("ok", "  OK  ", this, "onOkClick"));
	}

	public boolean onOkClick() throws Exception {
		if (!this.passwordForm.isValid()) {
			return true;
		}

		Password password = getPassword();
		if (!password.validatePassword(this.currentPasswordField.getValue())) {
			this.passwordForm.setError(getMessage("PasswordPage-invalid-current-password"));
			return true;
		}

		String newPassword = this.newPasswordField.getValue();
		String confirmNewPassword = this.confirmNewPasswordField.getValue();
		if (!newPassword.equals(confirmNewPassword)) {
			this.passwordForm.setError(getMessage("PasswordPage-password-compare-error"));
			return true;
		}

		password.changePassword(newPassword);
		ensurePasswordStored(password); // a little bit awkward ...

		this.passwordForm.clearValues();

		setRedirectWithMessage(HomePage.class, getMessage("PasswordPage-password-changed"));
		return false;
	}

	private Password getPassword() throws Exception {
		if (getUser().homeFragmentId != null) {
			Fragment fragment = getDomain().getFragmentRepository().get(getUser().homeFragmentId);
			if (fragment == null) {
				throw new IllegalStateException("User fragment not found: " + getUser());
			}
			return fragment;
		}
		else if (getUser().isInRole(Role.OWNER)) {
			return getOwnerAuth();
		}
		else {
			throw new IllegalStateException("Illegal user: " + getUser());
		}
	}

	private OwnerAuth getOwnerAuth() {
		return (OwnerAuth) getBean("ownerAuth");
	}

	private void ensurePasswordStored(final Password password) throws Exception {
		if (password instanceof Fragment) {
			Fragment userFragment = (Fragment)password;
			userFragment.touch(getUser(), true);	
			getDomain().saveFragment(userFragment, getUser());
			AbstractFragmentsPage.highlightFragment(userFragment.getId(), getContext());
		}
	}
}

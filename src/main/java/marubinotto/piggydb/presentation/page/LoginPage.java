package marubinotto.piggydb.presentation.page;

import javax.servlet.http.HttpSession;

import marubinotto.piggydb.model.User;
import marubinotto.piggydb.model.UserActivityLog;
import net.sf.click.control.Checkbox;
import net.sf.click.control.Form;
import net.sf.click.control.HiddenField;
import net.sf.click.control.PasswordField;
import net.sf.click.control.Submit;
import net.sf.click.control.TextField;

import org.apache.commons.lang.StringUtils;

public class LoginPage extends BorderPage {
    
    @Override
    protected boolean needsAuthentication() {
        return false;
    }
    
    @Override
    protected User getUserInSession() {
    	User user = super.getUserInSession();
    	if (user == null) {
    		return null;
    	}
    	if (user.isAnonymous()) {
    		getContext().getSession().invalidate();
    		getLogger().info("The anonymous session has been invalidated.");
    		return null;
    	}
    	return user;
    }
    
    @Override
    protected User autoLoginAsAnonymous() {
    	return null;	// Disable anonymous auto login
    }

	
	//
	// Input
	//

    public String original;
 
    
	//
	// Control
	//
    
    public Form loginForm = new Form();
    private TextField userNameField = new TextField("userName", true);
    private PasswordField passwordField = new PasswordField("password", true);
    private Checkbox rememberMeField = new Checkbox("rememberMe", false);
    private HiddenField originalPathField = new HiddenField("original", String.class);

    @Override
    public void onInit() {
        super.onInit();    
        initControls();
    }
    
	private void initControls() {
		this.loginForm.add(this.originalPathField);
		
		this.userNameField.setLabel(getMessage("LoginPage-userName"));
		this.userNameField.setSize(30);
    	this.loginForm.add(this.userNameField);
    	
    	this.passwordField.setLabel(getMessage("LoginPage-password"));
    	this.passwordField.setSize(30);
    	this.loginForm.add(this.passwordField);
    	
    	this.rememberMeField.setLabel(getMessage("LoginPage-rememberMe"));
    	this.loginForm.add(this.rememberMeField);
    	
    	this.loginForm.add(new Submit("ok", "  OK  ", this, "onOkClick"));
    }
    
    public boolean onOkClick() throws Exception {
        if (!this.loginForm.isValid()) return true;
        
        HttpSession oldSession = getContext().getRequest().getSession(false);
        if (oldSession != null) oldSession.invalidate();
        
        User user = getAuthentication().authenticate(
            this.userNameField.getValue(), this.passwordField.getValue());
        if (user == null) {
        	this.loginForm.setError(getMessage("LoginPage-login-error"));
        	return true;
        }

        HttpSession newSession = newSession(user);
    	
        if (this.rememberMeField.isChecked()) {
        	storeSession(newSession);
        	user.setSessionPersisted(true);
        	getLogger().debug("Set the session persisted");
        }
        
        String originalPath = this.originalPathField.getValue();
        // Avoid redirecting to external unknown URLs
        if (StringUtils.isNotBlank(originalPath) && originalPath.startsWith("/")) {	
        	setRedirect(originalPath);
        }
        else {
        	setRedirect(HomePage.class);
        }
        
        UserActivityLog.getInstance().log(user.getName(), "logged in");
        return false;
    }
    
    @Override
	public void onGet() {
		super.onGet();
		
		if (isAuthenticated()) {
			getLogger().debug("Already authenticated. Redirecting to HomePage ...");
			setRedirect(HomePage.class);
		}
	}

	@Override
	public void onRender() {
		super.onRender();
		if (this.original != null) this.originalPathField.setValue(this.original);
	}
}

package marubinotto.util.web;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import marubinotto.util.Assert;

public class CustomizedSecurityRequestWrapper extends HttpServletRequestWrapper {

    public CustomizedSecurityRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public GenericUser getUser() {
        return (GenericUser)getAttribute(GenericUser.KEY);
    }

    public String getRemoteUser() {
        GenericUser user = getUser();
        if (user == null) {
            return null;
        }
        return user.getName();
    }

    public Principal getUserPrincipal() {
        return getUser();
    }

    public boolean isUserInRole(String role) {
        Assert.Arg.notNull(role, "role");
        GenericUser user = getUser();
        if (user == null) {
            return false;
        }
        return user.isInRole(role);
    }
}

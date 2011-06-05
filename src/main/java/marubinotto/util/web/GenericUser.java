package marubinotto.util.web;

import java.io.Serializable;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;

import marubinotto.util.Assert;

public class GenericUser implements Principal, Serializable {

    public static final String KEY = "user";

    private String name;
    private List<String> roleNames = new ArrayList<String>();

    public GenericUser() {
    }

    public GenericUser(String name) {
        Assert.Arg.notNull(name, "name");
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void addRole(String roleName) {
    	Assert.Arg.notNull(roleName, "roleName");
    	this.roleNames.add(roleName);
    }
    
    public List<String> getRoles() {
        return this.roleNames;
    }
    
    public boolean isInRole(String roleName) {
        Assert.Arg.notNull(roleName, "roleName");
        return this.roleNames.contains(roleName);
    }
    
    public String toString() {
        return getName() + " " + this.roleNames;
    }

    public int hashCode() {
        return ObjectUtils.hashCode(getName());
    }

    public boolean equals(Object another) {
        if (another == null || !(another instanceof GenericUser)) {
            return false;
        }
        GenericUser anotherUser = (GenericUser)another;
        return ObjectUtils.equals(anotherUser.getName(), getName());
    }
}

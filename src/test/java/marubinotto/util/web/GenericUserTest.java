package marubinotto.util.web;

import java.security.Principal;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @see GenericUser
 */
public class GenericUserTest {

	@Test
    public void shouldSetNameViaConstructor() throws Exception {
        GenericUser user = new GenericUser("name");
        assertEquals("name", user.getName());
    }
    
	@Test
    public void shouldHaveNullNameByDefault() throws Exception {
        GenericUser user = new GenericUser();
        assertNull(user.getName());
    }
    
	@Test
    public void shouldHaveNoRolesByDefault() throws Exception {
        GenericUser user = new GenericUser("name");
        assertEquals(0, user.getRoles().size());
    }
    
	@Test
    public void shouldBeInAddedRole() throws Exception {
        GenericUser user = new GenericUser("name");
        user.addRole("programmer");
        assertTrue(user.isInRole("programmer"));
    }

	@Test
    public void shouldReturnFalseWhenNotInRoleSpecified() throws Exception {
    	GenericUser user = new GenericUser("name");
        assertFalse(user.isInRole("programmer"));
    }
    
	@Test
    public void shouldBeCompatibleWithPrincipal() throws Exception {
        Principal principal = new GenericUser("name");
        assertEquals("name", principal.getName());
    }
    
	@Test
    public void shouldBeEqualToAnotherWhenNamesAreIdentical() throws Exception {
        GenericUser user1 = new GenericUser("name");
        GenericUser user2 = new GenericUser("name");
        assertTrue(user1.equals(user2));
    }
}

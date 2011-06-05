package marubinotto.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

/**
 * @see PasswordDigest
 */
public class PasswordDigestTest {

    private PasswordDigest object;

    @Before
    public void given() throws Exception {
        this.object = new PasswordDigest();
    }

    @Test
    public void shouldDigestPasswordWithStoredSalt1() throws Exception {
    	// When
    	String password = "pe00001";
    	String storedDigest = "{SSHA}KyTPouHDohrf6NSxhT3z8F7dsyDSTwlhJSfRfg==";   	
    	String actual = this.object.digestWithStoredSalt(password, storedDigest);
    	
    	// Then
    	assertThat(actual, is(storedDigest));
    }

    @Test
    public void shouldDigestPasswordWithStoredSalt2() throws Exception {
    	// When
    	String password = "pass";
    	String storedDigest = "{SSHA}B78f8i/RpNC+CyFdKLH2odaK8hlPNjlOOUUyMA==";
    	String actual = this.object.digestWithStoredSalt(password, storedDigest);
    	
    	// Then
    	assertThat(actual, is(storedDigest));
    }

    @Test
    public void digestInvalidPassword() throws Exception {
    	// When
    	String invalidPassword = "hoge";
        String storedDigest = "{SSHA}KyTPouHDohrf6NSxhT3z8F7dsyDSTwlhJSfRfg==";
        String actual = this.object.digestWithStoredSalt(invalidPassword, storedDigest);
        
        // Then
        assertThat(actual, is(not(storedDigest)));
    }

    @Test
    public void shouldCreateSshaDigest() throws Exception {
    	// When
        String password = "pe00001";
        String digest = this.object.createSshaDigest(password);

        // Then
        String actual = this.object.digestWithStoredSalt(password, digest);
        assertThat(actual, is(digest));
    }
}

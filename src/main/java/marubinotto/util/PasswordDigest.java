package marubinotto.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.UnhandledException;

/**
 * marubinotto.util.PasswordDigest
 */
public class PasswordDigest {

    public static final String SSHA_LABEL = "{SSHA}";
    public static final int SSHA_DIGEST_SIZE = 20;
    public static final int SSHA_SALT_SIZE = 8;

    public String digestWithStoredSalt(String password, String storedDigest) {
        Assert.Arg.notNull(password, "password");
        Assert.Arg.notNull(storedDigest, "storedDigest");

        if (!storedDigest.startsWith(SSHA_LABEL)) {
            throw new UnsupportedOperationException("Unsupported algorithm.");
        }

        String base64Digest = storedDigest.substring(SSHA_LABEL.length());
        byte[] digestWithSalt = Base64.decodeBase64(base64Digest.getBytes());

        byte[] salt = new byte[SSHA_SALT_SIZE];
        System.arraycopy(digestWithSalt, SSHA_DIGEST_SIZE, salt, 0, SSHA_SALT_SIZE);

        return createSshaDigest(password, salt);
    }

    public String createSshaDigest(String password) {
        Assert.Arg.notNull(password, "password");
        return createSshaDigest(password, createSalt(SSHA_SALT_SIZE));
    }

    private String createSshaDigest(String password, byte[] salt) {
        byte[] passwordWithSalt = ArrayUtils.addAll(password.getBytes(), salt);
        byte[] encryptedPassword = DigestUtils.sha(passwordWithSalt);
        byte[] encryptedPasswordWithSalt = ArrayUtils.addAll(encryptedPassword, salt);

        String result = new String(Base64.encodeBase64(encryptedPasswordWithSalt));

        return SSHA_LABEL + result;
    }

    private byte[] createSalt(int size) {
        byte[] salt = new byte[size];
        SecureRandom random;
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
            throw new UnhandledException(e);
        }
        random.nextBytes(salt);
        return salt;
    }
}

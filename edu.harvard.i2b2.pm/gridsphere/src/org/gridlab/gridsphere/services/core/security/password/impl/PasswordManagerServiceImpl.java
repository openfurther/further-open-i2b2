/*
 * @version: $Id: PasswordManagerServiceImpl.java,v 1.1 2007/08/24 17:24:58 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.security.password.impl;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
import org.gridlab.gridsphere.portlet.PortletLog;
import org.gridlab.gridsphere.portlet.User;
import org.gridlab.gridsphere.portlet.impl.SportletLog;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
import org.gridlab.gridsphere.services.core.security.password.InvalidPasswordException;
import org.gridlab.gridsphere.services.core.security.password.Password;
import org.gridlab.gridsphere.services.core.security.password.PasswordEditor;
import org.gridlab.gridsphere.services.core.security.password.PasswordManagerService;

public class PasswordManagerServiceImpl
        implements PortletServiceProvider, PasswordManagerService {

    private static PortletLog _log = SportletLog.getInstance(PasswordManagerServiceImpl.class);
    private static PersistenceManagerRdbms pm = null;
    private static final String HMAC_ALG = "HMACSHA256";
    private static final long ONE_DAY = 86400;
    private String userPasswordImpl = PasswordImpl.class.getName();
    private Properties props = null;


    public PasswordManagerServiceImpl() {
    	
    }

    public void init(PortletServiceConfig config) {
        pm = PersistenceManagerFactory.createGridSphereRdbms();
    }

    public void destroy() {
    }

    public Password getPassword(User user) {
        return getPasswordImpl(user);
    }


    /**
     * This method returns the <code>PasswordImpl</code> associated with
     * a user and is called internally by other methods in this class.
     */
    private PasswordImpl getPasswordImpl(User user) {
        PasswordImpl password = null;
        String query = "select pw from "
                + this.userPasswordImpl
                + " pw where pw.sportletUser.oid='" + user.getID() + "'";
        try {
            password = (PasswordImpl)pm.restore(query);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to retrieve password for user", e);
        }
        return password;
    }

    public void validateSuppliedPassword(User user, String value)
            throws InvalidPasswordException {
    	if (value.matches("FURTHER\\{.*\\}")) {
    		if (props == null) {
    			InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("secret.properties");
    	    	if (inputStream != null) {
    	    		props = new Properties();
    	    		try {
    					props.load(inputStream);
    				} catch (IOException e) {
    					throw new RuntimeException("Unable to load properties in secret.properties from inputstream", e);
    				}
    	    	} else {
    	    		throw new RuntimeException("Unable to load secret.properties from inputstream");
    	    	}
    		}
    		if (props.getProperty("hmac.secret") == null) {
    			throw new RuntimeException("Unable to use SSO configuration, hmac.secret has not been set in secret.properties");
    		}
    		Pattern pattern = Pattern.compile("FURTHER\\{(.*)\\}");
    		Matcher matcher = pattern.matcher(value);
    		matcher.matches();
    		String input = matcher.group(1);
    		String[] split = input.split("\\+");
    		String hash = split[0];
    		String time = split[1];
    		long currentTimeSec = new Date().getTime() / 1000;
    		long hashTimeSec = Long.valueOf(time).longValue();
    		if (currentTimeSec > hashTimeSec + ONE_DAY) {
    			throw new RuntimeException("Invalid user token, expired");
    		}
    		String hashed = hmac(time + user.getUserName(), props.getProperty("hmac.secret"));
    		if (!hashed.equals(hash)) {
    			throw new InvalidPasswordException("Invalid user token");
    		}
    	} else {
    		PasswordImpl password = getPasswordImpl(user);
            if (password == null) {
                _log.debug("No password found for user");
                throw new InvalidPasswordException("No password found for user!");
            }
            //_log.debug("Stored value is " + password.getValue());
            //_log.debug("Provided value is " + value);

            // MD5 hash of password value
            try {
                MessageDigest md5 = MessageDigest.getInstance("MD5");
                md5.update(value.getBytes());
                value = toHex(md5.digest());

                //_log.debug("Hash of value is " + value);
                if (!password.getValue().equals(value)) {
                    throw new InvalidPasswordException("Supplied password does not match user password!");
                }
            } catch (NoSuchAlgorithmException e) {
                _log.error("No such algorithm: MD5", e);
            }
    	}
    }

    public void savePassword(Password editor) {
        try {
            if (editor instanceof PasswordImpl) {
                PasswordImpl pass = (PasswordImpl) editor;
                try {
                    MessageDigest md5 = MessageDigest.getInstance("MD5");
                    md5.update(pass.getValue().getBytes());
                    String value = toHex(md5.digest());
                    pass.setValue(value);
                } catch (NoSuchAlgorithmException e) {
                    throw new PersistenceManagerException("Can't get MD5 algorithm! " + e.getMessage());
                }
                pm.saveOrUpdate(pass);
            }
        } catch (PersistenceManagerException e) {
            _log.error("Unable to create or update password for user", e);
        }
    }

    public String getHashedPassword(String pass) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(pass.getBytes());
            return toHex(md5.digest());
        } catch (NoSuchAlgorithmException e) {
            _log.error("NoSuchAlgorithm MD5!", e);    
        }
        return null;
    }

    public void saveHashedPassword(Password editor) {
        try {
            if (editor instanceof PasswordImpl) {
                PasswordImpl pass = (PasswordImpl) editor;
                pm.saveOrUpdate(pass);
            }
        } catch (PersistenceManagerException e) {
            _log.error("Unable to create or update password for user", e);
        }
    }

    public void deletePassword(User user) {
        Password password = getPassword(user);
        if (password != null) {
            deletePassword(password);
        }
    }

    private void deletePassword(Password password) {
        try {
            pm.delete(password);
        } catch (PersistenceManagerException e) {
            _log.error("Unable to delete password", e);
        }
    }

    public boolean hasPassword(User user) {
        Password password = getPassword(user);
        return (password != null);
    }

    public PasswordEditor editPassword(User user) {
        PasswordImpl password = this.getPasswordImpl(user);
        if (password == null) {
            password = new PasswordImpl();
            password.setUser(user);
            password.setDateCreated(Calendar.getInstance().getTime());
            this.savePassword(password);
        }
        return password;
    }

    /**
     * Return an 8 byte representation of the 32 byte MD5 digest
     *
     * @param digest the message digest
     * @return String 8 byte hexadecimal
     */
    private static String toHex(byte[] digest) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < digest.length; i++) {
            buf.append(Integer.toHexString((int) digest[i] & 0x00FF));
        }
        return buf.toString();
    }
    
    private static final String hmac(final String value, final String key) throws InvalidPasswordException {
    	try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(key.getBytes(), HMAC_ALG));
            
            byte[] bytes = mac.doFinal(value.getBytes());
            
            BigInteger bi = new BigInteger(1, bytes);
            return String.format("%0" + (bytes.length << 1) + "x", bi);

        } catch (Exception e) {
            throw new InvalidPasswordException("Invalid user token");
        }
    }

}

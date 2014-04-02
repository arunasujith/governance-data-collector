package org.wso2.carbon.utils;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Load the configurations needed
 */
public class Configurations {

    private static String BAM_HOME;
    private static String GREG_HOME;

    private static String BAM_HOST_NAME;
    private static String BAM_PORT;
    private static String BAM_USERNAME;
    private static String BAM_PASSWORD;

    private static String GREG_URL;
    private static String GREG_USERNAME;
    private static String GREG_PASSWORD;

    private static String WSO2_BAMBOO_URL;
    private static String WSO2_GIT_URL;

    private static String GTIHUB_TOKEN;

    private static String WSO2_JENKINS_URL;


    private static Logger log = Logger.getLogger(Configurations.class);
    private static SecurePasswordVault securePasswordVault;

    public static boolean loadConfigs() {

        boolean isLoaded = false;
        try {
            Configuration config = new PropertiesConfiguration("greg.configurations");

            GREG_HOME = config.getString("GREG_HOME");
            BAM_HOME = config.getString("BAM_HOME");

            BAM_HOST_NAME = config.getString("BAM_HOST_NAME");
            BAM_PORT = config.getString("BAM_PORT");
            BAM_USERNAME = config.getString("BAM_USERNAME");
            //BAM_PASSWORD = config.getString("BAM_PASSWORD");

            GREG_URL = config.getString("GREG_URL");
            GREG_USERNAME = config.getString("GREG_USERNAME");
            //GREG_PASSWORD = config.getString("GREG_PASSWORD");

            WSO2_BAMBOO_URL = config.getString("WSO2_BAMBOO_URL");
            WSO2_GIT_URL = config.getString("WSO2_GIT_URL");

            GTIHUB_TOKEN = config.getString("GTIHUB_TOKEN");

            WSO2_JENKINS_URL = config.getString("WSO2_JENKINS_URL");

            List<String> list = new ArrayList<String>();
            list.add("BAM_PW");
            list.add("UES_PW");
            SecurePasswordVault securePasswordVault = new SecurePasswordVault("secure.vault", list.toArray(new String[list.size()]));

            BAM_PASSWORD = securePasswordVault.getSecureData("BAM_PW");
            GREG_PASSWORD = securePasswordVault.getSecureData("UES_PW");

            isLoaded = true;
        } catch (ConfigurationException e) {
            log.error("Exception Loading Configurations", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isLoaded;
    }

    public static String getGTIHUB_TOKEN() {
        return GTIHUB_TOKEN;
    }

    public static String getBAM_HOST_NAME() {
        return BAM_HOST_NAME;
    }

    public static String getBAM_PORT() {
        return BAM_PORT;
    }

    public static String getBAM_USERNAME() {
        return BAM_USERNAME;
    }

    public static String getBAM_PASSWORD() {
        return BAM_PASSWORD;
    }

    public static String getGREG_URL() {
        return GREG_URL;
    }

    public static String getGREG_USERNAME() {
        return GREG_USERNAME;
    }

    public static String getGREG_PASSWORD() {
        return GREG_PASSWORD;
    }

    public static String getBAM_HOME() {
        return BAM_HOME;
    }

    public static String getGREG_HOME() {
        return GREG_HOME;
    }

    public static String getWSO2_BAMBOO_URL() {
        return WSO2_BAMBOO_URL;
    }

    public static String getWSO2_GIT_URL() {
        return WSO2_GIT_URL;
    }

    public static String getWSO2_JENKINS_URL() {
        return WSO2_JENKINS_URL;
    }
}

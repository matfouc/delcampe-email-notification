package com.lafouche.delcampeEmailNotification;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;


public class ConfigUtils {

    private static ConfigUtils      instance;
    private final Properties        properties;

    private ConfigUtils() {
        this.properties = readProperties();
    }

    public static ConfigUtils getInstance() {
        if (instance == null)
            instance = new ConfigUtils();

        return instance;
    }

    public Properties getProperties() {
        return properties;
    }

    /**
     * Read application.properties file on resources folder
     *
     * @return Properties
     */
    private Properties readProperties() {
        Properties properties = new Properties();
        
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(property -> {properties.put(property.getKey(), property.getValue());});
                
        if( ! properties.containsKey("SCRAPED_FOLDER"))
            properties.put("SCRAPED_FOLDER", "/scrapedFolder");

        if( ! properties.containsKey("NOTIFICATION_HOUR"))
            properties.put("NOTIFICATION_HOUR", "19");

        return properties;
    }

}

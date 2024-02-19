package com.lafouche.delcampeEmailNotification;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.jsoup.Connection;


public class WebScraperUtils {
    
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(WebScraperUtils.class);

    private static final Properties config = ConfigUtils.getInstance().getProperties();
    
    
    public static void saveHTMLonDisk(Connection.Response response, String pageFileName) {
        
        try (FileOutputStream out = new FileOutputStream(
                    new File(config.getProperty("SCRAPED_FOLDER"), pageFileName) )) {
            
            out.write(response.bodyAsBytes());  
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        };
    }   
}

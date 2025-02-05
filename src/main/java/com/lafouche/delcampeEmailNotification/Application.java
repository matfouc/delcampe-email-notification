package com.lafouche.delcampeEmailNotification;

import static com.lafouche.delcampeEmailNotification.EmailUtils.sendEmailNotification;
import static com.lafouche.delcampeEmailNotification.WebScraperUtils.saveHTMLonDisk;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.swing.SwingUtilities;
import org.apache.logging.log4j.LogManager;
import static org.jsoup.Connection.Method.GET;
import static org.jsoup.Connection.Method.POST;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class Application {

    private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger(Application.class);

    private static final Properties CONFIG = ConfigUtils.getInstance().getProperties();

    private static final String DELCAMPE_LOGIN_PAGE = "https://www.delcampe.net/fr/my-account/login";
    private static final String DELCAMPE_BIDS_PAGE = "https://www.delcampe.net/fr/collections/sell/item-for-sale/ongoing-with-offers";
    
    private static int NOTIFICATION_HOUR = 0;
    private static ZoneId TIMEZONE;
    
    public static void main(String[] args) {  
        if( ! validateEnvironmentVariables())
            return;
        
        SwingUtilities.invokeLater(() -> {
            new Application();
        });        
    }
    
    private static boolean validateEnvironmentVariables() {
        try {
            NOTIFICATION_HOUR = Integer.parseInt(
                                    CONFIG.getProperty("NOTIFICATION_HOUR"));
            
        } catch(NumberFormatException ex) {
            LOGGER.error("NOTIFICATION_HOUR environment variable must be an integer.");
            return false;
        }
        
        if(NOTIFICATION_HOUR < 0 || NOTIFICATION_HOUR > 23) {
            LOGGER.error("NOTIFICATION_HOUR environment variable must be in interval [0-23].");
            return false;
        }
        
        try {
            TIMEZONE = ZoneId.of(CONFIG.getProperty("TZ"));
        } catch (Exception e) {
            LOGGER.error("TZ environment variable must correspond to a correct timezone.");
            return false;
        }    
        
        return true;
    }
    
    private Application() {       
        
        LOGGER.info("Ongoing Delcampe Bids Notifier started");
        LOGGER.info("Application will be executed every day at " + 
                        NOTIFICATION_HOUR + (NOTIFICATION_HOUR > 12 ? "PM" : "AM"));
        
        /*
            send a new email for all items with active bids every day at a certain hour   
        */        
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        
        executorService.scheduleAtFixedRate(() -> {

                Map<String, String> cookies = connect2Delcampe();

                if( ! cookies.isEmpty() ) {
                     List<ItemWithBid> activeBids = getActiveBidsFromDelcampeProfile(cookies);

                     if( ! activeBids.isEmpty() )                        
                    //activeBids.add(new ItemWithBid(123456, "Y123 - côté 125€", LocalDateTime.of(2024, Month.FEBRUARY, 5, 12, 23), "100€", "Mister1", "", ""));
                    //activeBids.add(new ItemWithBid(392417847, "Y1 - côté 2000€", LocalDateTime.of(2024, Month.FEBRUARY, 28, 23, 05), "250€", "BuyMe!!", "", ""));
                        sendEmailNotification(activeBids);            
                }
            }, 
            computeNextInitialStartDelay(), 
            TimeUnit.DAYS.toSeconds(1), 
            TimeUnit.SECONDS);   
    }
    
    private long computeNextInitialStartDelay() {
        
        ZonedDateTime now = ZonedDateTime.now(TIMEZONE);
        ZonedDateTime nextRun = now.withHour(NOTIFICATION_HOUR).withMinute(0).withSecond(0);
        
        /*
            if the application starts after the notification hour, 
            we immediatly execute the application
        */
        if(now.compareTo(nextRun) > 0)
            nextRun = now;

        Duration duration = Duration.between(now, nextRun);
        
        return duration.getSeconds();
    }
    
    private Map<String, String> connect2Delcampe() {
        try {            
            /*
                first access to login page
            */
            Response res = Jsoup
                                .connect(DELCAMPE_LOGIN_PAGE)
                                .method(GET)
                                .execute();  
            
            Document doc = res.parse();  
            
            LOGGER.info(
                "request 1 - Access to Login page to Delcampe: " + res.url() + "\r\n" +
                "Reply: HTTP code " + res.statusCode() + ", " + res.statusMessage() +
                ", returned page: " + doc.title());

            /*
                now provide credentials login page
            */
            String token = doc.getElementById("user_login__token").attr("value");
            String csrf_token = doc.getElementsByAttributeValue("name", "_csrf_token").first().attr("value");                       
                    
            res = Jsoup
                                .connect(DELCAMPE_LOGIN_PAGE)
                                .data(  "_target_path", "",
                                        "user_login[nickname]", CONFIG.getProperty("DELCAMPE_LOGIN"), 
                                        "user_login[password]", CONFIG.getProperty("DELCAMPE_PASSWORD"),                                         
                                        "_remember_me", "on",                                       
                                        "user_login[_token]", token,
                                        "_csrf_token", csrf_token,
                                        "_target_path", "")
                                .cookies(res.cookies())
                                .method(POST)
                                .execute();
            
            doc = res.bufferUp().parse();  
            
            LOGGER.info(
                "request 2 - Send Credentials to Delcampe: " + res.url() + "\r\n" +
                "Reply: HTTP code " + res.statusCode() + ", " + res.statusMessage() +
                ", returned page: " + doc.title());
                        
            saveHTMLonDisk(res, "ResultAfterLogin.html");
                 
            if(res.url().toString().matches(DELCAMPE_LOGIN_PAGE)) {
                LOGGER.error("Unable to login to Delcampe. Please check your credentials");
                return Map.of();
            } else
                return res.cookies();

        } catch (IOException ex) {
            LOGGER.error(ex);
            
            return Map.of();
        }
    }
 
    private List<ItemWithBid> getActiveBidsFromDelcampeProfile(Map<String, String> cookies) {
        try {         
            /*
                finally access to the ongoing items with offers page
            */
            Response res = Jsoup
                                .connect(DELCAMPE_BIDS_PAGE)
                                .cookies(cookies)
                                .method(GET)
                                .execute();

            Document doc = res.bufferUp().parse();

            LOGGER.info(
                "request 3 - Access to my ongoing sale(s) with bids: " + res.url() + "\r\n" +
                "Reply: HTTP code " + res.statusCode() + ", " + res.statusMessage() +
                ", returned page: " + doc.title());

            saveHTMLonDisk(res, "ResultOngoingSaleWithBids.html");
                                           
            /*
                we now parse the items with bids
            */
            return  extractOngoingSalesWithBids(doc);
        } catch (IOException ex) {
            LOGGER.error(ex);
            
            return List.of();
        }        
    }
        
    private List<ItemWithBid> extractOngoingSalesWithBids(Document doc) {
        Elements itemsWithBids = doc.select(".table-list .table-body-list");
        
        if( ! itemsWithBids.isEmpty()) {
            return itemsWithBids.stream().map(itemWithBidElement -> {
                
                //TODO parse item with bid from HTML
                String itemTitle = itemWithBidElement.select(".info-item a")
                                        .first().ownText();
                long itemReference = Long.parseLong(
                                        itemWithBidElement.select(".info-item .item-id")
                                            .first().text().substring(1));
                
                String endDateString = itemWithBidElement.select(".list-date span")
                                    .text();                 
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE d MMM yyyy 'à' HH:mm", Locale.FRENCH);
                LocalDateTime endDate = LocalDateTime.parse(endDateString, formatter);
                
                String itemPrice = 
                        itemWithBidElement.select(".list-info-item .selling-type")
                            .next().text();
             
                String itemCurrentBuyer = 
                        itemWithBidElement.select(".list-info-item .user-status-short")
                            .first().text();
                        
                String imageSrcPath = 
                        itemWithBidElement.select(".list-info .image-small").first().attr("src");
               
                String itemLink =
                        itemWithBidElement.select(".list-info .item-link").first().attr("href");
                
                return new ItemWithBid(
                                itemReference, 
                                itemTitle,
                                endDate, 
                                itemPrice, 
                                itemCurrentBuyer, 
                                imageSrcPath,
                                itemLink);          
                
            }).collect(Collectors.toList());            
        }
                
        return new ArrayList<ItemWithBid>();
    }    
}


package com.lafouche.delcampeEmailNotification;

import static com.lafouche.delcampeEmailNotification.EmailUtils.sendEmailNotification;
import static com.lafouche.delcampeEmailNotification.WebScraperUtils.saveHTMLonDisk;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
    
    public static void main(String[] args) {        
        Integer notificationHour = 0;
        try {
            notificationHour = Integer.parseInt(
                                    CONFIG.getProperty("NOTIFICATION_HOUR"));
        } catch(NumberFormatException ex) {
            LOGGER.error("NOTIFICATION_HOUR must be an integer. Default 0 value is being used");
            notificationHour = 0;
        }
        
        if(notificationHour < 0 || notificationHour > 23) {
            LOGGER.error("NOTIFICATION_HOUR must be in interval [0-23]. Default 0 value is being used");
            notificationHour = 0;
        }
        
        /*
            send a new email for all items with active bids every day at a certain hour   
        */        
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(() -> {
                
            connect2Delcampe();
            
        }, TimeUnit.HOURS.toHours(notificationHour), 1, TimeUnit.DAYS);   
    }

    private static void connect2Delcampe() {
        try {            
            /*
                first access to login page
            */
            Response res1 = Jsoup
                                .connect(DELCAMPE_LOGIN_PAGE)
                                .method(GET)
                                .execute();  
            
            Document doc = res1.parse();  
            
            LOGGER.info(
                "request 1 - Access to Login page to Delcampe: " + res1.url() + "\r\n" +
                "Reply: HTTP code " + res1.statusCode() + ", " + res1.statusMessage() +
                ", returned page: " + doc.title());

            /*
                now provide credentials login page
            */
            
            //we need to parse certain hidden values from the page
            String token = doc.getElementById("user_login__token").attr("value");
            String csrf_token = doc.getElementsByAttributeValue("name", "_csrf_token").first().attr("value");                       
                    
            Response res2 = Jsoup
                                .connect(DELCAMPE_LOGIN_PAGE)
                                .data(  "_target_path", "",
                                        "user_login[nickname]", CONFIG.getProperty("DELCAMPE_LOGIN"), 
                                        "user_login[password]", CONFIG.getProperty("DELCAMPE_PASSWORD"),                                         
                                        "_remember_me", "on",                                       
                                        "user_login[_token]", token,
                                        "_csrf_token", csrf_token,
                                        "_target_path", "")
                                .cookies(res1.cookies())
                                .method(POST)
                                .execute();
            
            doc = res2.bufferUp().parse();  
            
            LOGGER.info(
                "request 2 - Send Credentials to Delcampe: " + res2.url() + "\r\n" +
                "Reply: HTTP code " + res2.statusCode() + ", " + res2.statusMessage() +
                ", returned page: " + doc.title());
                        
            saveHTMLonDisk(res2, "ResultAfterLogin.html");
                 
            if(res2.url().toString().matches(DELCAMPE_LOGIN_PAGE)) {
                LOGGER.error("Unable to login to Delcampe. Please check your credentials");
                return;
            }
                
            /*
                finally access to the ongoing items with offers page
            */
            Response res3 = Jsoup
                                .connect(DELCAMPE_BIDS_PAGE)
                                .cookies(res2.cookies())
                                .method(GET)
                                .execute();
            
            doc = res3.bufferUp().parse();
            
            LOGGER.info(
                "request 3 - Access to my ongoing sale(s) with bids: " + res3.url() + "\r\n" +
                "Reply: HTTP code " + res3.statusCode() + ", " + res3.statusMessage() +
                ", returned page: " + doc.title());
            
            saveHTMLonDisk(res3, "ResultOngoingSaleWithBids.html");
                               
            /*
                we now parse the items with bids
            */
            List<ItemWithBid> list = extractOngoingSalesWithBids(doc);
            
//            list.add(new ItemWithBid(123456, "Y123 - côté 125€", LocalDate.of(2024, Month.FEBRUARY, 5), "100€", "Mister1"));
//            list.add(new ItemWithBid(392417847, "Y1 - côté 2000€", LocalDate.of(2024, Month.FEBRUARY, 28), "250€", "BuyMe!!"));
            
            /*
                we now filtered only the new items with (and remove sold items)
                before we send a new email notification
            */            
            if( ! list.isEmpty() )
                sendEmailNotification(list);
            
        } catch (IOException ex) {
            LOGGER.error(ex);
        }
    }
    
    private static List<ItemWithBid> extractOngoingSalesWithBids(Document doc) {
        Elements itemsWithBids = doc.getElementsByAttributeValue("name", "marketplace_item_ongoing");
        
        if( ! itemsWithBids.isEmpty()) {
            return itemsWithBids.stream().map(itemWithBidElement -> {
                ItemWithBid item = new ItemWithBid();   
                
                //TODO parse item with bid from HTML
                
                return item;                
            }).collect(Collectors.toList());            
        }
        
        
        return new ArrayList<ItemWithBid>();
    } 
}

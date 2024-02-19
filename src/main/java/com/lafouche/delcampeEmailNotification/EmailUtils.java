package com.lafouche.delcampeEmailNotification;

import java.util.Date;
import java.util.List;
import java.util.Properties;
import static javax.mail.Message.RecipientType.TO;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;


public class EmailUtils {
    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(EmailUtils.class);

    private static final Properties config = ConfigUtils.getInstance().getProperties();
    
    public static void sendEmailNotification(List<ItemWithBid> list) {
        
        Properties props = new Properties();
        props.put("mail.smtp.host", config.getProperty("SMTP_HOST"));
        Session session = Session.getInstance(props, null);

        try {
            MimeMessage msg = new MimeMessage(session);
            
            msg.setFrom(config.getProperty("SMTP_FROM"));
            msg.setRecipients(TO, config.getProperty("SMTP_TO"));

            msg.setSubject("New received bids on Delcampe. Congratulations !!!");
            msg.setSentDate(new Date());
            
            //TODO recopy perhaps same HTML than on Delcampe page
            StringBuilder htmlMsg = new StringBuilder("<body><h4>Here is the list of active items with bids:</h4><br><table>");
            htmlMsg.append("<tr><td>Item Reference</td><td>Price</td><td>End Date</td><td>Buyer</td>");
            
            list.stream().forEach(item -> {
                    htmlMsg.append("<tr><td>")
                        .append(item.getReference())
                        .append("</td><td>")
                        .append(item.getCurrentPrice())
                        .append("</td><td>")
                        .append(item.getBuyer())
                        .append("</td><td>")
                        .append(item.getEndDate())
                        .append("</td></tr>");
            });
            htmlMsg.append("</table></body>");
            
            msg.setContent(htmlMsg.toString(), "text/html");            
            
            Transport.send( msg, 
                            config.getProperty("SMTP_USERNAME"), 
                            config.getProperty("SMTP_PASSWORD"));
            
            logger.info("email successfully sent to " + msg.getFrom()[0]);
            
        } catch (MessagingException mex) {
            logger.error(mex);
        }        
    }    
}

package com.lafouche.delcampeEmailNotification;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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

        Double totalBids = calculateTotalBidPrice(list);
        
        try {
            MimeMessage msg = new MimeMessage(session);
            
            msg.setFrom(config.getProperty("SMTP_FROM"));
            msg.setRecipients(TO, config.getProperty("SMTP_TO"));

            msg.setSubject("Ongoing bids on Delcampe. Congratulations !!!");
            msg.setSentDate(new Date());
            
            //TODO recopy perhaps same HTML than on Delcampe page
            StringBuilder htmlMsg = new StringBuilder("""
                <!DOCTYPE html>
                    <html lang="en">
                        <head>
                            <style>
                                body {
                                    background-color: #f2f2f2;                     
                                }

                                .main {
                                    background-color: #ffffff;
                                    border-collapse: collapse;
                                    table-layout: fixed; 
                                    width: 800px;                     
                                    font-family: arial,helvetica,sans-serif;
                                    color: #22486d;
                                    font-size: 13px;        
                                    margin-left: auto;
                                    margin-right: auto;                 
                                }       

                                #delcampe-logo {
                                    width: 150px; 
                                    height:auto; 
                                    display: block; 
                                    margin: auto;
                                }

                                .table-items {
                                    border-collapse: collapse;
                                    border:1px solid;                
                                    table-layout: fixed;                
                                    font-family: arial,helvetica,sans-serif;
                                    font-size: 13px;    
                                    color: #22486d;
                                    margin-left: auto;
                                    margin-right: auto;    
                                    background-color: #f8f8f8;                                                                  
                                }

                                td, th {
                                    padding: 25px;
                                }   

                                .table-items, .table-items-header {
                                    border: 1px solid #808080;
                                    text-align: center;                    
                                }                 

                                .table-items-header { 
                                    font-weight: bold;
                                    background-color: #f2f2f2;
                                }

                                .table-items-row:nth-child(even) {
                                    background-color: #f8f8f8;
                                }
                                .table-items-row:nth-child(odd) {
                                    background-color: #f1f1f1;
                                }

                                .horizontal-bar {
                                    height: 1px; 
                                    line-height: 1px; 
                                    font-size: 1px; 
                                    border-top: 1px solid #e9e9e9;
                                }

                                .image-div {
                                    height: 60px;
                                    width: 60px;
                                    justify-content: center;
                                }

                                .text-align-left {
                                    text-align:left;
                                    padding-top: 10px;
                                    padding-bottom: 10px;
                                }
                            </style>
                            <title>Delcampe - Active bids</title>
                        </head>   

                        <body>        
                            <table class="main" role="presentation">
                                <tr>
                                    <td colspan="3">
                                        <a href="https://www.delcampe.net/" >
                                            <img id="delcampe-logo" src="https://www.delcampe.net/images/mail/delcampe.png">
                                        </a>        
                                    </td>
                                </tr>        
                                <tr>
                                    <td colspan="3" height="1" class="horizontal-bar"></td>                    
                                </tr>
                                <tr>
                                    <td colspan="3" class="text-align-left">
                                        Here is the list of active items with bids for a total of             
                """);
            htmlMsg.append("<b>" + NumberFormat.getCurrencyInstance().format(totalBids) + "</b>");
            
            htmlMsg.append(""" 
                           :             
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3" style="padding-top:10px">
                                        <table class="table-items" role="presentation">   
                           """);                         
            htmlMsg.append("""
                <tr>
                     <th class="table-items-header">Image</th>
                     <th class="table-items-header" width="4000">Item</th>
                     <th class="table-items-header">Price</th>
                     <th class="table-items-header">Current buyer</th>
                     <th class="table-items-header">End date</th>
                </tr> 
            """);
            
            list.stream().forEach(item -> {
                    htmlMsg.append("""
                        <tr class="table-items-row">
                            <td>
                                <div class="image-div"> 
                                   <img src="
                        """ + item.getImageSrcPath() +
                        """
                                    ">
                                </div>
                            </td>
                        """)

                        .append(
                        """
                            <td>
                                <a href="https://www.delcampe.com """ + 
                                    item.getItemLink() + "\">" +
                                    item.getTitle() + 
                        """                                
                                </a>
                            </td>
                        """)
                            
                        .append(
                        """
                            <td> 
                        """ + item.getCurrentPrice() +
                        """
                            </td>
                        """)
                            
                        .append(
                        """
                            <td> 
                        """ + item.getBuyer() +
                        """
                            </td>
                        """)
                            
                        .append(
                        """
                            <td> 
                        """ + item.getEndDate().format(
                                DateTimeFormatter.ofLocalizedDateTime(
                                    FormatStyle.MEDIUM)) +
                        """
                            </td>
                        </tr>
                        """);
            });
            
            htmlMsg.append(
            """                     
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
            """);
            
            msg.setContent(htmlMsg.toString(), "text/html;charset=utf-8");            
            
            Transport.send( msg, 
                            config.getProperty("SMTP_USERNAME"), 
                            config.getProperty("SMTP_PASSWORD"));
            
            logger.info("email successfully sent to " + msg.getFrom()[0]);
            
        } catch (MessagingException mex) {
            logger.error(mex);
        }        
    }    

    private static double calculateTotalBidPrice(List<ItemWithBid> list) {
        return list.stream()
            .mapToDouble(item -> {
                return Double.parseDouble(
                        item.getCurrentPrice()
                                .replace("€", "")
                                .replace(",", "."));
            })
            .reduce(Double::sum).orElse(0);        
    }
}

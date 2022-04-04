/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lafouche.delcampeEmailNotification;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Matthieu
 */
public class delcampeEmailNotification {
    public static void main(String[] args) {
        try {            
            //Document ongoingOffers = Jsoup.connect("https://www.delcampe.net/fr/collections/sell/item-for-sale/ongoing-with-offers").get();
            
            Response response = Jsoup
                                    .connect("https://www.delcampe.net/fr/my-account/login")
                                    .method(Method.GET)
                                    .execute();  

            System.out.println("request to " + response.url() + ", reply: " + response.statusCode() + ", " + response.statusMessage());
            saveHTML_Page(response, "delcampeLoginPage.html");
            
            Document doc = response.parse();            
            String login_token = doc.getElementById("user_login__token").attr("value");
            System.out.println("login_token: " + login_token);
//            Elements posts = doc.getElementsByTag("form");
//            System.out.println(target_path);
//            
            Element loginForm = doc.getElementsContainingOwnText("user_login").first();
            
            Response res = Jsoup
                                .connect("https://www.delcampe.net/fr/my-account/login_check")
                                .data(  "nickname", "matfoucafad", 
                                        "password", "!yBA8Fh3^@!F&j7ckK5wP4D9P", 
                                        "_token", login_token/*,
//                                .data(  "user_login[nickname]", "matfoucafad", 
//                                        "user_login[password]", "!yBA8Fh3^@!F&j7ckK5wP4D9P", 
//                                        "user_login[_token]", login_token/*,
                                        "_remember_me", "checked",
                                        "_target_path", ""*/)
                                .cookies(response.cookies())
                                .method(Method.POST)
                                .execute();
            
            System.out.println("request to " + res.url() + ", reply: " + res.statusCode() + ", " + res.statusMessage());
            saveHTML_Page(response, "resultLogin.html");
            
            Elements tests = res.parse().getElementsByClass("alert-red");
                        
            Map<String, String> cookies = res.cookies();

            Response res2 = Jsoup
                                        .connect("https://www.delcampe.net/fr/collections/sell/item-for-sale/ongoing-with-offers")
                                        .cookies(cookies)
                                        .method(Method.GET)
                                        .execute();
            
            System.out.println("request to " + res2.url() + ", reply: " + res2.statusCode() + ", " + res2.statusMessage());
            saveHTML_Page(response, "delcampeAuctions_with_bids.html");
            Document doc2 = res2.parse();
            System.out.println(doc2.title());

        } catch (Exception ex) {
            Logger.getLogger(delcampeEmailNotification.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void saveHTML_Page(Response response, String pageFileName) {
        FileOutputStream out = null;
        try {
            
            out = new FileOutputStream(
                    new java.io.File(
                            "C:\\Dev_Matthieu\\local_github_projects\\delcampeEmailNotification",
                            pageFileName) );
            
            out.write(response.bodyAsBytes());  
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(delcampeEmailNotification.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(delcampeEmailNotification.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(delcampeEmailNotification.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

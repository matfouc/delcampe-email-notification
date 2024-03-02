package com.lafouche.delcampeEmailNotification;

import java.time.LocalDateTime;


public class ItemWithBid implements Comparable<ItemWithBid> {
    
    private int                 reference;
    private String              title;
    private LocalDateTime       endDate;
    private String              currentPrice;
    private String              buyer;
    private String              imageSrcPath;
    private String              itemLink;

    public ItemWithBid() {
        this(-1, "", LocalDateTime.now(), "0€", "", "", "");
    }
        
    public ItemWithBid(int reference, String title, LocalDateTime endDate, String currentPrice, String buyer, String imageSrcPath, String itemLink) {
        this.reference = reference;
        this.title = title;
        this.endDate = endDate;
        this.currentPrice = currentPrice;
        this.buyer = buyer;
        this.imageSrcPath = imageSrcPath;
        this.itemLink = itemLink;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(String currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getBuyer() {
        return buyer;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }    

    public String getImageSrcPath() {
        return imageSrcPath;
    }

    public void setImageSrcPath(String imageSrcPath) {
        this.imageSrcPath = imageSrcPath;
    }

    public String getItemLink() {
        return itemLink;
    }

    public void setItemLink(String itemLink) {
        this.itemLink = itemLink;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.reference;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ItemWithBid other = (ItemWithBid) obj;
        return this.reference == other.reference;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ItemWithBid{");
        sb.append("reference=").append(reference);
        sb.append(", title=").append(title);
        sb.append(", endDate=").append(endDate);
        sb.append(", currentPrice=").append(currentPrice);
        sb.append(", buyer=").append(buyer);
        sb.append(", imageSrcPath=").append(imageSrcPath);
        sb.append(", itemLink=").append(itemLink);
        sb.append('}');
        return sb.toString();
    }   

    @Override
    public int compareTo(ItemWithBid item) {
        return Integer.compare(
                this.getReference(), 
                item.getReference());
    }
}

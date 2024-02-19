package com.lafouche.delcampeEmailNotification;

import java.time.LocalDate;


public class ItemWithBid implements Comparable<ItemWithBid> {
    
    private int                 reference;
    private String              title;
    private LocalDate           endDate;
    private String              currentPrice;
    private String              buyer;

    public ItemWithBid() {
        this(-1, "", LocalDate.now(), "0€", "");
    }
        
    public ItemWithBid(int reference, String title, LocalDate endDate, String currentPrice, String buyer) {
        this.reference = reference;
        this.title = title;
        this.endDate = endDate;
        this.currentPrice = currentPrice;
        this.buyer = buyer;
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

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
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

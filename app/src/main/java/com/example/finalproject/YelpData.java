package com.example.finalproject;

public class YelpData {
    private String name;
    private String rating;
    private String address;
    private String openClosed;
    private String phoneNumber;
    private String latitude;
    private String longitude;

    public YelpData() {}

    public void setName(String businessName) { this.name = businessName; }

    public void setRating(String businessRating) { this.rating = businessRating; }

    public void setAddress(String businessAddress) { this.address = businessAddress; }

    public void setOpenClosed(String businessOpenClose) { this.openClosed = businessOpenClose; }

    public void setPhoneNumber(String businessPhoneNumber) { this.phoneNumber = businessPhoneNumber; }

    public void setLat(String businessLat) { this.latitude = businessLat; }

    public void setLong(String businessLong) { this.longitude = businessLong; }

    public String getName() { return this.name; }

    public String getRating() { return this.rating; }

    public String getAddress() { return this.address; }

    public String getOpenClosed() { return this.openClosed; }

    public String getPhoneNumber() { return this.phoneNumber; }

    public String getLat() { return this.latitude; }

    public String getLong() { return this.longitude; }
}
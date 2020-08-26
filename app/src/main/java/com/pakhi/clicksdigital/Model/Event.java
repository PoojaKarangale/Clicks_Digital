package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

public class Event implements Serializable {

    String  eventId;
    String  eventName;
    String  description;
    String  event_image;
    String  venu;
    String  city;
    String  address;
    Long    timeStamp;
    String  startDate;
    String  endDate;
    String  startTime;
    String  endTime;
    String  eventType;
    boolean payable;
    int     totalFee;
    String  category;
    String  creater_id;

    public Event() {
    }

    public Event(String eventKey, String eventName, String eventDescription, String category, String picImageUrlString, String event_type, String venuStr, String cityStr, String addressStr, Long timeStamp, String startDate, String endDate, String startTime, String endTime, boolean payable, int totalFee, String creater_id) {

        this.eventId=eventKey;
        this.eventName=eventName;
        this.description=eventDescription;
        this.event_image=picImageUrlString;
        this.venu=venuStr;
        this.city=cityStr;
        this.address=addressStr;
        this.timeStamp=timeStamp;
        this.startDate=startDate;
        this.endDate=endDate;
        this.startTime=startTime;
        this.endTime=endTime;
        this.eventType=event_type;
        this.payable=payable;
        this.totalFee=totalFee;
        this.category=category;
        this.creater_id=creater_id;
    }

    public Event(String eventKey, String eventName, String eventDescription, String category, String picImageUrlString, String event_type, String addressStr, Long timeStamp, String startDate, String endDate, String startTime, String endTime, boolean payable, int totalFee, String creater_id) {

        this.eventId=eventKey;
        this.eventName=eventName;
        this.description=eventDescription;
        this.event_image=picImageUrlString;
        this.address=addressStr;
        this.timeStamp=timeStamp;
        this.startDate=startDate;
        this.endDate=endDate;
        this.startTime=startTime;
        this.endTime=endTime;
        this.eventType=event_type;
        this.payable=payable;
        this.totalFee=totalFee;
        this.category=category;
        this.creater_id=creater_id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId=eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName=eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description=description;
    }

    public String getEvent_image() {
        return event_image;
    }

    public void setEvent_image(String event_image) {
        this.event_image=event_image;
    }

    public String getVenu() {
        return venu;
    }

    public void setVenu(String venu) {
        this.venu=venu;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city=city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address=address;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp=timeStamp;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate=startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate=endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime=startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime=endTime;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType=eventType;
    }

    public boolean isPayable() {
        return payable;
    }

    public void setPayable(boolean payable) {
        this.payable=payable;
    }

    public int getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(int totalFee) {
        this.totalFee=totalFee;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category=category;
    }

    public String getCreater_id() {
        return creater_id;
    }

    public void setCreater_id(String creater_id) {
        this.creater_id=creater_id;
    }

}

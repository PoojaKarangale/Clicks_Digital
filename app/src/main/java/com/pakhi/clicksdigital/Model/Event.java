package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

public class Event implements Serializable {


    String eventId;
    String name;
    String description;
    String event_image;
    String city;
    String location;
    String time;
    String date;
    String duration;
    String event_type;
    String payable;
    String cost;
    String category;
    String creater_id;

    public Event() {
    }


    public Event(String eventId, String name, String description, String event_image, String city, String location, String time, String date, String duration, String event_type, String payable, String cost, String category, String creater_id) {
        this.eventId = eventId;
        this.name = name;
        this.description = description;
        this.event_image = event_image;
        this.city = city;
        this.location = location;
        this.time = time;
        this.date = date;
        this.duration = duration;
        this.event_type = event_type;
        this.payable = payable;
        this.cost = cost;
        this.category = category;
        this.creater_id = creater_id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvent_image() {
        return event_image;
    }

    public void setEvent_image(String event_image) {
        this.event_image = event_image;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getCreater_id() {
        return creater_id;
    }

    public void setCreater_id(String creater_id) {
        this.creater_id = creater_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getEvent_type() {
        return event_type;
    }

    public void setEvent_type(String event_type) {
        this.event_type = event_type;
    }

    public String getPayable() {
        return payable;
    }

    public void setPayable(String payable) {
        this.payable = payable;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}

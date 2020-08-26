package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

public class Contact implements Serializable {


    private String  uid;
    private String  user_name;
    private String  number;
    private String  notificationKey;
    private String  image;
    private String  bio;
    private String  request_type;
    private Boolean selected=false;

    public Contact() {
    }

    public Contact(String uid, String user_name, String number, String image, String bio, String request_type) {
        this.uid=uid;
        this.user_name=user_name;
        this.number=number;
        this.image=image;
        this.bio=bio;
        this.request_type=request_type;
    }

    public Contact(String name, String number) {
        this.user_name=user_name;
        this.number=number;
    }

    public Contact(String uid, String user_name, String image, String bio) {
        this.uid=uid;
        this.user_name=user_name;
        this.image=image;
        this.bio=bio;
    }

    public Contact(String uid) {
        this.uid=uid;
    }

    public Contact(String uid, String user_name, String number) {
        this.uid=uid;
        this.user_name=user_name;
        this.number=number;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name=user_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image=image;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio=bio;
    }


    public String getUid() {
        return uid;
    }

    public String getNumber() {
        return number;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey=notificationKey;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected=selected;
    }
}

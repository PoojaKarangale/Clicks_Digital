package com.pakhi.clicksdigital.Model;

public class User_request {
    String group_name, group_id, date, requesting_user, request_status;
    String request_id, time;

    public User_request() {
    }

    public User_request(String group_name, String group_id, String date, String requesting_user, String request_status, String request_id, String time) {
        this.group_name=group_name;
        this.group_id=group_id;
        this.date=date;
        this.requesting_user=requesting_user;
        this.request_status=request_status;
        this.request_id=request_id;
        this.time=time;
    }

    public String getRequest_id() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id=request_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id=group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name=group_name;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date=date;
    }

    public String getRequesting_user() {
        return requesting_user;
    }

    public void setRequesting_user(String requesting_user) {
        this.requesting_user=requesting_user;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status=request_status;
    }
}

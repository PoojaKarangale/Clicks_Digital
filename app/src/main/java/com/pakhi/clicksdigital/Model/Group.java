package com.pakhi.clicksdigital.Model;

import java.util.HashMap;

public class Group {

    String group_name;
    String description;
    String groupid;
    String uid_creater;
    String date;
    String time;
    String image_url;

    String status;
    String requesting_userid;

    private boolean isSelected;


    HashMap<String, String> Users;

    //here status is joined or requested
    //info is created at date time and created by group description


    public Group() {
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected=selected;
    }

    public HashMap<String, String> getUsers() {
        return Users;
    }

    public void setUsers(HashMap<String, String> users) {
        Users=users;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name=group_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description=description;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid=groupid;
    }

    public String getUid_creater() {
        return uid_creater;
    }

    public void setUid_creater(String uid_creater) {
        this.uid_creater=uid_creater;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date=date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time=time;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url=image_url;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status=status;
    }

    public String getRequesting_userid() {
        return requesting_userid;
    }

    public void setRequesting_userid(String requesting_userid) {
        this.requesting_userid=requesting_userid;
    }


}

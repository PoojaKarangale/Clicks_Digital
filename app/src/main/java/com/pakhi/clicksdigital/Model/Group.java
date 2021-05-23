package com.pakhi.clicksdigital.Model;

import java.util.HashMap;

public class Group {


    String groupid;
    String group_name;
    String description;
    String uid_creater;
    String image_url;
    Long timestamp;
    Long timestamp_creation;

    private boolean isSelected;

    HashMap<String, String> Users;

    public Group() {
    }

    public Group(String groupid, String group_name, String description, String uid_creater, String image_url, Long timestamp_creation) {
        this.groupid = groupid;
        this.group_name = group_name;
        this.description = description;
        this.uid_creater = uid_creater;
        this.image_url = image_url;
        this.timestamp_creation = timestamp_creation;
    }


    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public HashMap<String, String> getUsers() {
        return Users;
    }

    public void setUsers(HashMap<String, String> users) {
        Users = users;
    }

    public String getGroup_name() {
        return group_name;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }

    public String getUid_creater() {
        return uid_creater;
    }

    public void setUid_creater(String uid_creater) {
        this.uid_creater = uid_creater;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }


}

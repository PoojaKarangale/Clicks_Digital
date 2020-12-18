package com.pakhi.clicksdigital.JoinGroup;

public class ModelJoinGroup {
    String group_name, image_url;

    ModelJoinGroup(){

    }

    public ModelJoinGroup(String grpName, String imgURL) {
        this.group_name = grpName;
        this.image_url = imgURL;
    }

    public String getGrpName() {
        return group_name;
    }

    public void setGrpName(String grpName) {
        this.group_name = grpName;
    }

    public String getImgURL() {
        return image_url;
    }

    public void setImgURL(String imgURL) {
        this.image_url = imgURL;
    }
}

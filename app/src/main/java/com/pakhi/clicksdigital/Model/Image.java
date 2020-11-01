package com.pakhi.clicksdigital.Model;

public class Image {
    String image_title, image_url;

    public Image(String image_title, String image_url) {
        this.image_title=image_title;
        this.image_url=image_url;
    }

    public String getImage_title() {
        return image_title;
    }

    public void setImage_title(String image_title) {
        this.image_title=image_title;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url=image_url;
    }
}

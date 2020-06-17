package com.pakhi.clicksdigital.Model;

import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class User implements Serializable {

    private String image_url,user_id;

    private String expectations_from_us, experiences, gender,  number, offer_to_community,
            speaker_experience,  user_bio, user_email, user_name, user_type, weblink, work_profession;

    public User() {
    }

    public User(String expectations_from_us, String experiences, String gender, String number, String offer_to_community, String speaker_experience, String bio_str, String email_str, String full_name_str, String user_type, String weblink_str, String working) {
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public User(String user_id,  String expectations_from_us, String experiences, String gender, String number, String offer_to_community, String speaker_experience, String user_bio, String user_email, String user_name, String user_type, String weblink, String work_profession,String image_url) {
        this.image_url = image_url;
        this.user_id = user_id;
        this.expectations_from_us = expectations_from_us;
        this.experiences = experiences;
        this.gender = gender;
        this.number = number;
        this.offer_to_community = offer_to_community;
        this.speaker_experience = speaker_experience;
        this.user_bio = user_bio;
        this.user_email = user_email;
        this.user_name = user_name;
        this.user_type = user_type;
        this.weblink = weblink;
        this.work_profession = work_profession;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getExpectations_from_us() {
        return expectations_from_us;
    }

    public void setExpectations_from_us(String expectations_from_us) {
        this.expectations_from_us = expectations_from_us;
    }

    public String getExperiences() {
        return experiences;
    }

    public void setExperiences(String experiences) {
        this.experiences = experiences;
    }



    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getOffer_to_community() {
        return offer_to_community;
    }

    public void setOffer_to_community(String offer_to_community) {
        this.offer_to_community = offer_to_community;
    }

    public String getSpeaker_experience() {
        return speaker_experience;
    }

    public void setSpeaker_experience(String speaker_experience) {
        this.speaker_experience = speaker_experience;
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio = user_bio;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink = weblink;
    }

    public String getWork_profession() {
        return work_profession;
    }

    public void setWork_profession(String work_profession) {
        this.work_profession = work_profession;
    }
}

package com.pakhi.clicksdigital.Model;
import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
public class User implements Serializable {

    private String expectations_from_us, experiences, facebook_link, gender, insta_link, number, offer_to_community,
            speaker_experience, twiter_link, user_bio, user_email, user_name, user_type, weblink, work_profession;

    HashMap<String,Object> groups;

    public User(String expectations_from_us, String experiences, String facebook_link, String gender, String insta_link, String number, String offer_to_community, String speaker_experience, String twiter_link, String user_bio, String user_email, String user_name, String user_type, String weblink, String work_profession) {
        this.expectations_from_us = expectations_from_us;
        this.experiences = experiences;
        this.facebook_link = facebook_link;
        this.gender = gender;
        this.insta_link = insta_link;
        this.number = number;
        this.offer_to_community = offer_to_community;
        this.speaker_experience = speaker_experience;
        this.twiter_link = twiter_link;
        this.user_bio = user_bio;
        this.user_email = user_email;
        this.user_name = user_name;
        this.user_type = user_type;
        this.weblink = weblink;
        this.work_profession = work_profession;
    }

    public User() {
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

    public String getFacebook_link() {
        return facebook_link;
    }

    public void setFacebook_link(String facebook_link) {
        this.facebook_link = facebook_link;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getInsta_link() {
        return insta_link;
    }

    public void setInsta_link(String insta_link) {
        this.insta_link = insta_link;
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

    public String getTwiter_link() {
        return twiter_link;
    }

    public void setTwiter_link(String twiter_link) {
        this.twiter_link = twiter_link;
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

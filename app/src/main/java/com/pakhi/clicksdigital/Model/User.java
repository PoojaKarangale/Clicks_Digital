package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

@SuppressWarnings("serial")
public class User implements Serializable {

    private String image_url;
    private String user_id;
    private String city;


    private String  last_name;
    private String  company;
    private boolean isSelected;
    private String  expectations_from_us, experiences, gender, number, offer_to_community,
            speaker_experience, user_bio, user_email, user_name, user_type, weblink, work_profession;

    public User() {
    }


    public User(String image_url, String user_id, String user_bio, String user_name, String user_type) {
        this.image_url=image_url;
        this.user_id=user_id;
        this.user_bio=user_bio;
        this.user_name=user_name;
        this.user_type=user_type;
    }

    public User(String userid, String full_name_str, String bio_str, String image_url, String user_type, String city, String expectations_from_us, String experiences, String gender, String number, String offer_to_community, String speaker_experience, String user_email, String weblink, String work_profession, String last_name, String company) {
        this.image_url=image_url;
        this.user_id=userid;
        this.city=city;
        this.expectations_from_us=expectations_from_us;
        this.experiences=experiences;
        this.gender=gender;
        this.number=number;
        this.offer_to_community=offer_to_community;
        this.speaker_experience=speaker_experience;
        this.user_bio=bio_str;
        this.user_email=user_email;
        this.user_name=full_name_str;
        this.user_type=user_type;
        this.weblink=weblink;
        this.work_profession=work_profession;
        this.last_name=last_name;
        this.company=company;
    }

    public User(String userid, String full_name_str, String bio_str, String image_url, String user_type, String city, String expectations_from_us, String experiences, String gender, String number, String offer_to_community, String speaker_experience, String user_email, String weblink, String work_profession) {
        this.image_url=image_url;
        this.user_id=userid;
        this.city=city;
        this.expectations_from_us=expectations_from_us;
        this.experiences=experiences;
        this.gender=gender;
        this.number=number;
        this.offer_to_community=offer_to_community;
        this.speaker_experience=speaker_experience;
        this.user_bio=bio_str;
        this.user_email=user_email;
        this.user_name=full_name_str;
        this.user_type=user_type;
        this.weblink=weblink;
        this.work_profession=work_profession;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name=last_name;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company=company;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city=city;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id=user_id;
    }

    public boolean getSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected=selected;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url=image_url;
    }

    public String getExpectations_from_us() {
        return expectations_from_us;
    }

    public void setExpectations_from_us(String expectations_from_us) {
        this.expectations_from_us=expectations_from_us;
    }

    public String getExperiences() {
        return experiences;
    }

    public void setExperiences(String experiences) {
        this.experiences=experiences;
    }


    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender=gender;
    }


    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number=number;
    }

    public String getOffer_to_community() {
        return offer_to_community;
    }

    public void setOffer_to_community(String offer_to_community) {
        this.offer_to_community=offer_to_community;
    }

    public String getSpeaker_experience() {
        return speaker_experience;
    }

    public void setSpeaker_experience(String speaker_experience) {
        this.speaker_experience=speaker_experience;
    }

    public String getUser_bio() {
        return user_bio;
    }

    public void setUser_bio(String user_bio) {
        this.user_bio=user_bio;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email=user_email;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name=user_name;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type=user_type;
    }

    public String getWeblink() {
        return weblink;
    }

    public void setWeblink(String weblink) {
        this.weblink=weblink;
    }

    public String getWork_profession() {
        return work_profession;
    }

    public void setWork_profession(String work_profession) {
        this.work_profession=work_profession;
    }
}

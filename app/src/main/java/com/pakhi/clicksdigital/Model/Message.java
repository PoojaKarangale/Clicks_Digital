package com.pakhi.clicksdigital.Model;

public class Message {

    private String from, message, type, to, messageID, time, date;


    boolean isSeen;

    public Message(String from, String message, String type, String to, String messageID, String time, String date, boolean isSeen) {
        this.from=from;
        this.message=message;
        this.type=type;
        this.to=to;
        this.messageID=messageID;
        this.time=time;
        this.date=date;
        this.isSeen=isSeen;
    }

    public Message() {
    }

    public Message(String from, String message, String type, String to, String messageID, String time, String date) {
        this.from=from;
        this.message=message;
        this.type=type;
        this.to=to;
        this.messageID=messageID;
        this.time=time;
        this.date=date;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen=seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from=from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message=message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type=type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to=to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID=messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time=time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date=date;
    }

}

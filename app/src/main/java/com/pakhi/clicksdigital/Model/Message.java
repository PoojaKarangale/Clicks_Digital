package com.pakhi.clicksdigital.Model;

import java.io.Serializable;

public class Message implements Serializable, Comparable<Message> {

    private String typeOfSelectedMessage;
    private String selectedMessageId;
    Long timestamp;
    boolean isSeen;
    private String from, message, type, to, messageID, time, date;
    //change
    private String extra;

   /* public boolean isNewGroup() {
        return isNewGroup;
    }

    public void setNewGroup(boolean newGroup) {
        isNewGroup = newGroup;
    }

    private  boolean isNewGroup;
   public Message(String from, String message, String type, String to, String messageID, String time, String date, Long timestamp, String extra,String selectedMessageId, String typeOfSelectedMessage,boolean isNewGroup) {
        this.from=from;
        this.message=message;
        this.type=type;
        this.to=to;
        this.messageID=messageID;
        this.time=time;
        this.date=date;
         this.timestamp=timestamp;
        this.extra=extra;
        this.selectedMessageId = selectedMessageId;
        this.typeOfSelectedMessage = typeOfSelectedMessage;
        this.isNewGroup = isNewGroup;
    }*/


    // for reply
    public Message(String from, String message, String type, String to, String messageID, String time, String date,Long timestamp, boolean isSeen, /*change*/String extra, String selectedMessageId, String typeOfSelectedMessage) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.extra = extra;
        this.selectedMessageId = selectedMessageId;
        this.typeOfSelectedMessage = typeOfSelectedMessage;
    }

    public Message() {
    }

/*    //for reply
    public Message(String from, String message, String type, String to, String messageID, String time, String date, Long timestamp,boolean isSeen, String extra, String selectedMessageId, String typeOfSelectedMessage) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.to = to;
        this.messageID = messageID;
        this.time = time;
        this.date = date;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
        this.extra = extra;
        this.selectedMessageId = selectedMessageId;
        this.typeOfSelectedMessage = typeOfSelectedMessage;
    }*/


    public String getSelectedMessageId() {
        return selectedMessageId;
    }

    public String getTypeOfSelectedMessage() {
        return typeOfSelectedMessage;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    /* public String getExtra(String extra){return extra;}*/

    @Override
    public int compareTo(Message o) {
        if (getTimestamp() == null || o.getTimestamp() == null)
            return 0;
        return getTimestamp().compareTo(o.getTimestamp());
    }

    public String getExtra() {
        return extra;
    }
}

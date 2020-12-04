package com.pakhi.clicksdigital.Model;

import java.util.ArrayList;

public class GroupTopic {
    public ArrayList<String> getTopicIDs() {
        return topicIDs;
    }

    public void setTopicIDs(ArrayList<String> topicIDs) {
        this.topicIDs=topicIDs;
    }

    public GroupTopic(ArrayList<String> topicIDs) {
        this.topicIDs=topicIDs;
    }

    public GroupTopic() {
    }

    ArrayList<String> topicIDs= new ArrayList<>();

}

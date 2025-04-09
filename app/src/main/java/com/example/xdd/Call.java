package com.example.xdd;

public class Call {
    private String callName;
    private String callStatus;
    private String channelId;

    public Call(String callName, String callStatus, String channelId) {
        this.callName = callName;
        this.callStatus = callStatus;
        this.channelId = channelId;
    }

    public String getCallName() {
        return callName;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public String getChannelId() {
        return channelId;
    }
}
package com.example.xdd;

public class Call {
    private String callName;
    private String callStatus;

    public Call(String callName, String callStatus) {
        this.callName = callName;
        this.callStatus = callStatus;
    }

    public String getCallName() {
        return callName;
    }

    public String getCallStatus() {
        return callStatus;
    }
}
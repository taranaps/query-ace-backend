package com.queryapplication.dto;

import java.util.Date;


public class ErrorDetails {

    private Date timeStamp;
    private String messsage;
    private String description;

    public ErrorDetails() {
    }

    public ErrorDetails(Date timeStamp, String messsage, String description) {
        this.timeStamp = timeStamp;
        this.messsage = messsage;
        this.description = description;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMesssage() {
        return messsage;
    }

    public void setMesssage(String messsage) {
        this.messsage = messsage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


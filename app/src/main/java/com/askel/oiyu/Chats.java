package com.askel.oiyu;
//Getters and Setters used in ChatActivity
public class Chats {
    private String status;

    public Chats(){

    }

    public Chats(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

package com.askel.oiyu;

public class Messages {

    private String message, type,from;
    private long time;
    private boolean seen,SMSSent;

    public Messages(){

    }

    public Messages(String message, String type, long time, boolean seen, String from,boolean SMSSent) {
        this.message = message;
        this.type = type;
        this.time = time;
        this.seen = seen;
        this.from=from;
        this.SMSSent=SMSSent;
    }
    public String getFrom(){
        return from;
    }
    public void setFrom(String from){
        this.from=from;
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

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public boolean isSMSSent() {
        return SMSSent;
    }

    public void setSMSSent(boolean SMSSent) {
        this.SMSSent = SMSSent;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }
}

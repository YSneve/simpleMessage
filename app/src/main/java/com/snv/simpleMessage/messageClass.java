package com.snv.simpleMessage;

public class messageClass {
    int id;
    String messageText;
    String userName;

    public messageClass(int id, String messageText, String userName) {
        this.id = id;
        this.messageText = messageText;
        this.userName = userName;
    }

    public messageClass(int id, String messageText) {
        this.id = id;
        this.messageText = messageText;
    }


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }
}

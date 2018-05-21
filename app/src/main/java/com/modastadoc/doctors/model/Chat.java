package com.modastadoc.doctors.model;

/**
 * Created by kunasi on 13/08/17.
 */

public class Chat {
    private int type;
    private String message;

    public Chat(int type, String message) {
        this.type = type;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}

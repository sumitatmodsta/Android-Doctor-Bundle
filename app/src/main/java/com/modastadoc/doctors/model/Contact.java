package com.modastadoc.doctors.model;

/**
 * Created by kunasi on 14/08/17.
 */

public class Contact {
    private String id, name, email;
    public Contact(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}

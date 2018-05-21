package com.modastadoc.doctors.model;

/**
 * Created by kunasi on 16/10/17.
 */

public class Language {
    public String name;
    public boolean isSelected;

    public Language(String name, boolean isSelected) {
        this.name = name;
        this.isSelected = isSelected;
    }

    public Language(String name) {
        this(name, false);
    }
}

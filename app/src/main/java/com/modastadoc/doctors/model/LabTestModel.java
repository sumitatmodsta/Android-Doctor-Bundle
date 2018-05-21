package com.modastadoc.doctors.model;

/**
 * Created by vijay.hiremath on 05/10/16.
 */
public class LabTestModel
{
    String id;
    String lab;
    boolean checked;

    public LabTestModel(String id, String lab, boolean checked) {
        this.id = id;
        this.lab = lab;
        this.checked = checked;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLab() {
        return lab;
    }

    public void setLab(String lab) {
        this.lab = lab;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}

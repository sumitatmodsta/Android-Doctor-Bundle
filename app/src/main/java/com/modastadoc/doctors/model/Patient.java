package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kunasi on 02/09/17.
 */

public class Patient implements Parcelable {
    public String name, pic, age, gender, weight, height, address, city, medication, diagnosis;

    public Patient(String name, String pic, String age, String gender, String weight, String height,
                   String address, String city, String medication, String diagnosis) {
        this.name = name;
        this.pic = pic;
        this.age = age;
        this.gender = gender;
        this.weight = weight;
        this.height = height;
        this.address = address;
        this.city = city;
        this.medication = medication;
        this.diagnosis = diagnosis;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Storing the Patient data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(pic);
        dest.writeString(age);
        dest.writeString(gender);
        dest.writeString(weight);
        dest.writeString(height);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(medication);
        dest.writeString(diagnosis);
    }

    /**
     * Retrieving Patient data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Patient(Parcel in) {
        this.name = in.readString();
        this.pic = in.readString();
        this.age = in.readString();
        this.gender = in.readString();
        this.weight = in.readString();
        this.height = in.readString();
        this.address = in.readString();
        this.city = in.readString();
        this.medication = in.readString();
        this.diagnosis = in.readString();
    }

    public static final Parcelable.Creator<Patient> CREATOR = new Parcelable.Creator<Patient>() {

        @Override
        public Patient createFromParcel(Parcel source) {
            return new Patient(source);
        }

        @Override
        public Patient[] newArray(int size) {
            return new Patient[size];
        }
    };
}

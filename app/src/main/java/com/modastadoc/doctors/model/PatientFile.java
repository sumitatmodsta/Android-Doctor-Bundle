package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kunasi on 14/08/17.
 */

public class PatientFile implements Parcelable {
    public String id, name, type;

    public PatientFile(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
    }

    /**
     * Retrieving Patient data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private PatientFile(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.type = in.readString();
    }

    public static final Parcelable.Creator<PatientFile> CREATOR = new Parcelable.Creator<PatientFile>() {

        @Override
        public PatientFile createFromParcel(Parcel source) {
            return new PatientFile(source);
        }

        @Override
        public PatientFile[] newArray(int size) {
            return new PatientFile[size];
        }
    };
}

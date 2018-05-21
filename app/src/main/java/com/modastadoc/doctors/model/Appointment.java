package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.modastadoc.doctors.App;

/**
 * Created by kunasi on 17/08/17.
 */

public class Appointment implements Parcelable {
    public String orderID, name, date, time, picUrl, status, delayedBy;

    public Appointment(String orderID, String name, String date, String time, String picUrl,
                       String status, String delayedBy) {
        this.orderID = orderID;
        this.name = name;
        this.date = date;
        this.time = time;
        this.picUrl = picUrl;
        this.status = status;
        this.delayedBy = delayedBy;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Storing the Student data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderID);
        dest.writeString(name);
        dest.writeString(date);
        dest.writeString(time);
        dest.writeString(picUrl);
        dest.writeString(status);
        dest.writeString(delayedBy);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Appointment(Parcel in){
        this.orderID = in.readString();
        this.name = in.readString();
        this.date = in.readString();
        this.time = in.readString();
        this.picUrl = in.readString();
        this.status = in.readString();
        this.delayedBy = in.readString();
    }

    public static final Parcelable.Creator<Appointment> CREATOR = new Parcelable.Creator<Appointment>() {

        @Override
        public Appointment createFromParcel(Parcel source) {
            return new Appointment(source);
        }

        @Override
        public Appointment[] newArray(int size) {
            return new Appointment[size];
        }
    };
}

package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kunasi on 02/09/17.
 */

public class Booking implements Parcelable {
    public String orderID, bookingStatus, docStatus, patStatus, sessionID, token, diagnosis;
    public int duration;

    public Booking(String orderID, String bookingStatus, String docStatus, String patStatus,
                    String sessionID, String token, int duration, String diagnosis) {
        this.orderID = orderID;
        this.bookingStatus = bookingStatus;
        this.docStatus = docStatus;
        this.patStatus = patStatus;
        this.sessionID = sessionID;
        this.token = token;
        this.duration = duration;
        this.diagnosis = diagnosis;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Storing the Booking data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(orderID);
        dest.writeString(bookingStatus);
        dest.writeString(docStatus);
        dest.writeString(patStatus);
        dest.writeString(sessionID);
        dest.writeString(token);
        dest.writeInt(duration);
        dest.writeString(diagnosis);
    }

    /**
     * Retrieving Booking data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Booking(Parcel in) {
        this.orderID = in.readString();
        this.bookingStatus = in.readString();
        this.docStatus = in.readString();
        this.patStatus = in.readString();
        this.sessionID = in.readString();
        this.token = in.readString();
        this.duration = in.readInt();
        this.diagnosis = in.readString();
    }

    public static final Parcelable.Creator<Booking> CREATOR = new Parcelable.Creator<Booking>() {

        @Override
        public Booking createFromParcel(Parcel source) {
            return new Booking(source);
        }

        @Override
        public Booking[] newArray(int size) {
            return new Booking[size];
        }
    };
}

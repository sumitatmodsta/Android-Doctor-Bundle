package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kunasi on 22/08/17.
 */

public class Live implements Parcelable {
    public String apiKey = "45595052";
    public String sessionID, token, status;
    public int duration;

    public Live(String sessionID, String token, int duration, String status) {
        this.sessionID = sessionID;
        this.token = token;
        this.duration = duration;
        this.status = status;
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
        dest.writeString(apiKey);
        dest.writeString(sessionID);
        dest.writeString(token);
        dest.writeInt(duration);
        dest.writeString(status);
    }

    /**
     * Retrieving Student data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Live(Parcel in){
        this.apiKey = in.readString();
        this.sessionID = in.readString();
        this.token = in.readString();
        this.duration = in.readInt();
        this.status = in.readString();
    }

    public static final Parcelable.Creator<Live> CREATOR = new Parcelable.Creator<Live>() {

        @Override
        public Live createFromParcel(Parcel source) {
            return new Live(source);
        }

        @Override
        public Live[] newArray(int size) {
            return new Live[size];
        }
    };
}

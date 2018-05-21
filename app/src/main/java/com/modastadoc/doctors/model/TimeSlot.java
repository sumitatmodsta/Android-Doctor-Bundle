package com.modastadoc.doctors.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by kunasi on 07/09/17.
 */

public class TimeSlot implements Parcelable {
    public boolean status;
    public long currentTimeStamp, activeTimeStamp;

    public TimeSlot(boolean status, long currentTimeStamp, long activeTimeStamp) {
        this.status = status;
        this.currentTimeStamp = currentTimeStamp;
        this.activeTimeStamp = activeTimeStamp;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    /**
     * Storing the TimeSlot data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(status?1:0);
        dest.writeLong(currentTimeStamp);
        dest.writeLong(activeTimeStamp);
    }

    /**
     * Retrieving TimeSlot data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private TimeSlot(Parcel in){
        this.status = in.readInt()==1;
        this.currentTimeStamp = in.readLong();
        this.activeTimeStamp = in.readLong();
    }

    public static final Parcelable.Creator<TimeSlot> CREATOR = new Parcelable.Creator<TimeSlot>() {

        @Override
        public TimeSlot createFromParcel(Parcel source) {
            return new TimeSlot(source);
        }

        @Override
        public TimeSlot[] newArray(int size) {
            return new TimeSlot[size];
        }
    };
}

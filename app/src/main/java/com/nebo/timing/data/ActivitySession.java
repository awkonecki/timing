package com.nebo.timing.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;

public class ActivitySession implements Parcelable {
    private String name = null;
    private long sessionStartDate = 0L;
    private long totalTime = 0L;
    private long [] sessionLapTimes;
    private HashSet<String> labels = new HashSet<String>();

    private ActivitySession(Parcel parcel) {
        if (parcel != null) {
            name = parcel.readString();
            sessionStartDate = parcel.readLong();
            totalTime = parcel.readLong();
            parcel.readLongArray(sessionLapTimes);
            // TODO @awkonecki figure out if or need labels.
            // String [] tempLabels = new String [parcel.dataSize()];
            // parcel.readStringArray(tempLabels);
        }
    }

    public ActivitySession() {}

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setSessionStartDate(long startDate) {
        this.sessionStartDate = startDate;
    }

    public long getSessionStartDate() {
        return this.sessionStartDate;
    }

    public void setSessionLapTimes(long [] lapTimes) {
        if (lapTimes != null && lapTimes.length > 0) {
            this.sessionLapTimes = new long [lapTimes.length];
            System.arraycopy(lapTimes, 0, this.sessionLapTimes, 0, lapTimes.length);
        }
    }

    public long [] getSessionLapTimes() {
        return this.sessionLapTimes;
    }

    public void setLabels(String [] labels) {
        if (labels != null) {
            for (String label : labels) {
                this.labels.add(label);
            }
        }
    }

    public String [] getLabels() {
        String [] result = new String [this.labels.size()];
        result = this.labels.toArray(result);
        return result;
    }

    public static final Creator<ActivitySession> CREATOR = new Creator<ActivitySession>() {
        @Override
        public ActivitySession createFromParcel(Parcel source) {
            return new ActivitySession(source);
        }

        @Override
        public ActivitySession[] newArray(int size) {
            if (size > 0) {
                return new ActivitySession[size];
            }

            return new ActivitySession[0];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (dest != null) {
            dest.writeString(name);
            dest.writeLong(sessionStartDate);
            dest.writeLong(totalTime);
            dest.writeLongArray(sessionLapTimes);
        }
    }
}

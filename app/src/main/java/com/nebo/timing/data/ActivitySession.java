package com.nebo.timing.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ActivitySession implements Parcelable {
    private String name = null;
    private long totalTime = 0L;
    private List<Long> sessionLapTimes = new ArrayList<>();

    private ActivitySession(Parcel parcel) {
        if (parcel != null) {
            name = parcel.readString();
            totalTime = parcel.readLong();
            parcel.readList(sessionLapTimes, null);

            // TODO @awkonecki figure out if or need labels.
        }
    }

    public ActivitySession() {}

    public long getTotalTime() {
        return this.totalTime;
    }

    public ActivitySession(String name) {
        this.name = name;
    }

    public void addSessionLapTime(long time) {
        sessionLapTimes.add(time);
        totalTime += time;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public List<Long> getSessionLapTimes() {
        return new ArrayList<>(this.sessionLapTimes);
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
            dest.writeLong(totalTime);
            dest.writeList(sessionLapTimes);
        }
    }
}

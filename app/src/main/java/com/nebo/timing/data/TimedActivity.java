package com.nebo.timing.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TimedActivity implements Parcelable {
    private String name = null;
    private String category = null;
    private long totalElapsedTime = 0L;
    private ArrayList<ActivitySession> activitySessions = new ArrayList<>();

    public TimedActivity() {}

    public List<ActivitySession> getActivitySessions() {
        return new ArrayList<>(activitySessions);
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public String getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public static TimedActivity getTimedActivity() {
        TimedActivity timedActivity = new TimedActivity();
        timedActivity.totalElapsedTime = 1234567L;
        timedActivity.category = "Study";
        timedActivity.name = "CTCI";

        ActivitySession session1 = new ActivitySession();
        ActivitySession session2 = new ActivitySession();

        session1.setSessionLapTimes(new long [] {1541, 2131, 1227, 39582});
        session2.setSessionLapTimes(new long [] {21784, 13002, 1412, 12964});

        timedActivity.activitySessions.add(session1);
        timedActivity.activitySessions.add(session2);

        return timedActivity;
    }

    private TimedActivity(Parcel source) {
        if (source != null) {
            name = source.readString();
            category = source.readString();
            totalElapsedTime = source.readLong();
            activitySessions = source.createTypedArrayList(ActivitySession.CREATOR);
        }
    }

    public static final Creator<TimedActivity> CREATOR = new Creator<TimedActivity>() {
        @Override
        public TimedActivity[] newArray(int size) {
            if (size <= 0) {
                return new TimedActivity[0];
            }
            else {
                return new TimedActivity[size];
            }
        }

        @Override
        public TimedActivity createFromParcel(Parcel source) {
            return new TimedActivity(source);
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
            dest.writeString(category);
            dest.writeLong(totalElapsedTime);
            dest.writeTypedList(activitySessions);
        }
    }
}

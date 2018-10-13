package com.nebo.timing.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TimedActivity {

    private long totalElapsedTime = 0L;
    private List<ActivitySession> activitySessions = new ArrayList<>();
    private HashSet<String> categories = new HashSet<>();

    public TimedActivity() {}

    public List<ActivitySession> getActivitySessions() {
        return new ArrayList<>(activitySessions);
    }

    public long getTotalElapsedTime() {
        return totalElapsedTime;
    }

    public static TimedActivity getTimedActivity() {
        TimedActivity timedActivity = new TimedActivity();
        timedActivity.totalElapsedTime = 1234567L;

        ActivitySession session1 = new ActivitySession();
        ActivitySession session2 = new ActivitySession();

        session1.setSessionLapTimes(new long [] {1541, 2131, 1227, 39582});
        session2.setSessionLapTimes(new long [] {21784, 13002, 1412, 12964});

        timedActivity.activitySessions.add(session1);
        timedActivity.activitySessions.add(session2);

        return timedActivity;
    }
}

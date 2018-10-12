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
}

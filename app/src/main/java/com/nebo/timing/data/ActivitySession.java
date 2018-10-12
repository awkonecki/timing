package com.nebo.timing.data;

import java.util.HashSet;

public class ActivitySession {
    private String name = null;
    private long sessionStartDate = 0L;
    private long [] sessionLapTimes;
    private HashSet<String> labels = new HashSet<String>();

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
}

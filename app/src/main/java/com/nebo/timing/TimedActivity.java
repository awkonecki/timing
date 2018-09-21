package com.nebo.timing;

public class TimedActivity {
    private String mActivityName;

    public TimedActivity() {}

    public TimedActivity(String name) {
        this.mActivityName = name;
    }

    public void setActivityName(String name) {
        this.mActivityName = name;
    }

    public String getActivityName() {
        return this.mActivityName;
    }
}

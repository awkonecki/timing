package com.nebo.timing.util;

import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ActivityTimerUtils {

    private static final String [] CATEGORIES = new String [] {
            "Study",
            "Exercise",
            "Work",
            "Cook",
            "Play",
            "Shop",
            "Travel"
    };

    public static List<TimedActivity> generateActivities(String [] activityNames) {
        List<TimedActivity> activities = new ArrayList<>();
        Random random = new Random();

        for (int index = 0; index < activityNames.length; index++) {
            // Create the new activity with a random category.
            TimedActivity timedActivity = new TimedActivity(
                    activityNames[index], CATEGORIES[random.nextInt(CATEGORIES.length)]);

            // Now create a random set of sessions.
            int totalSessions = random.nextInt(50) + 1;

            for (int sessionIndex = 0; sessionIndex < totalSessions; sessionIndex++) {
                ActivitySession session = new ActivitySession(
                        createRandomString(random.nextInt(15) + 1));

                // Add a random number of laps for the session.
                int totalLaps = random.nextInt(8) + 1;
                for (int lapIndex = 0; lapIndex < totalLaps; lapIndex++) {
                    session.addSessionLapTime((long)(random.nextInt(10000000) + 10000));
                }

                timedActivity.addActivitySession(session);
            }

            activities.add(timedActivity);
        }

        return activities;
    }

    private static String createRandomString(int stringLength) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        while (stringLength > 0) {
            sb.append((char) (random.nextInt(27) + 'a'));
            stringLength--;
        }

        return sb.toString();
    }
}

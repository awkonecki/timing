package com.nebo.timing.async;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.nebo.timing.R;
import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PushTimedActivity extends AsyncTaskLoader<Void> implements ChildEventListener {
    private static final String TAG = "PushTimedActivity";
    private final long [] mSessionLapTimes;
    private final String mTimedActivityKey;
    private TimedActivity mTimedActivity = null;
    private final String mUserUid;
    private final String mSessionName;
    private final String mActivityName;
    private final String mCategoryName;
    private boolean mWait = true;
    private final HashSet<String> mSetKeys = new HashSet<>();

    public PushTimedActivity(@NonNull Context context, @Nullable Bundle args) {
        super(context);

        if (args != null) {
            mSessionLapTimes = args.getLongArray(getContext().getString(R.string.key_lap_times));
            mTimedActivityKey = args.getString(getContext().getString(
                    R.string.key_timed_activity_key),
                    null);
            mTimedActivity = args.getParcelable(getContext().getString(R.string.key_selected_activity));
            mUserUid = args.getString(getContext().getString(R.string.key_user_uid), null);
            mSessionName = args.getString(
                    getContext().getString(R.string.key_session_name),
                    getContext().getString(R.string.default_session_name));
            mActivityName = args.getString(
                    getContext().getString(R.string.key_new_name_string),
                    getContext().getString(R.string.edit_activity_name_default));
            mCategoryName = args.getString(
                    getContext().getString(R.string.key_new_category_string),
                    getContext().getString(R.string.edit_activity_category_default));
            ArrayList<String> keys = args.getStringArrayList(
                    getContext().getString(R.string.key_timed_activity_keys));

            if (keys != null) {
                mSetKeys.addAll(keys);
            }
        }
        else {
            mSessionLapTimes = null;
            mTimedActivityKey = null;
            mTimedActivity = null;
            mSessionName = null;
            mActivityName = null;
            mCategoryName = null;
            mUserUid = null;
        }
    }

    @Nullable
    @Override
    public Void loadInBackground() {
        // Make sure that the current auth user id is equal to the target user id that logged in.
        if (mUserUid != null && mUserUid.equals(FirebaseAuth.getInstance().getUid())) {

            // Firebase DB Reference Setup.
            DatabaseReference dbref = FirebaseDatabase.getInstance().getReference()
                    .child(getContext().getString(R.string.firebase_database_timed_activities));
            Query query = null;

            // create a new session
            ActivitySession session = new ActivitySession(mSessionName);
            if (mSessionLapTimes != null) {
                for (long sessionLap : mSessionLapTimes) {
                    session.addSessionLapTime(sessionLap);
                }
            }

            // now need to see if this will be a new entry or an update to an already existing timed
            // activity.
            if (mTimedActivity == null) {
                query = dbref
                        .orderByChild(getContext().getString(R.string.firebase_database_activity_user))
                        .equalTo(FirebaseAuth.getInstance().getUid());
                query.addChildEventListener(this);

                // new entry.
                mTimedActivity = new TimedActivity(
                        mActivityName,
                        mCategoryName,
                        FirebaseAuth.getInstance().getUid());
                mTimedActivity.addActivitySession(session);
                dbref.push().setValue(mTimedActivity);
            }
            else {
                // update existing.
                // 1. need to go into the location into the firebase database within the
                // timed-activities location.
                dbref = dbref.child(mTimedActivityKey);
                dbref.addChildEventListener(this);

                // 2. add the new session to the activity.
                mTimedActivity.addActivitySession(session);
                // 3. get the list and update with a map.
                Map<String, Object> updateOfSessions = new HashMap<>();
                updateOfSessions.put(
                        getContext().getString(R.string.firebase_database_activity_sessions),
                        mTimedActivity.getActivitySessions());
                updateOfSessions.put(
                        getContext().getString(R.string.firebase_database_activity_total_elapsed_time),
                        mTimedActivity.getTotalElapsedTime());
                // 4. update
                dbref.updateChildren(updateOfSessions);
            }

            // Block until know for sure firebase database can reflect the data
            while (mWait) {}

            // Cleanup
            if (query != null) {
                query.removeEventListener(this);
            }
            else {
                dbref.removeEventListener(this);
            }
        }

        return null;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        if (dataSnapshot.getKey() != null && (!mSetKeys.contains(dataSnapshot.getKey()) ||
                dataSnapshot.getKey().equals(getContext().getString(
                        R.string.firebase_database_activity_total_elapsed_time))))
        {
            mWait = false;
        }
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
        if (dataSnapshot.getKey() != null && dataSnapshot.getKey().equals(mTimedActivityKey)) {
            mWait = false;
        }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

    @Override
    public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {
        Log.e (TAG, "onCancelled called with error of " +
                databaseError.getMessage() +
                "\n" +
                databaseError.getDetails());
        mWait = false;
    }
}

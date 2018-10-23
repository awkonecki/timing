package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.auth.AuthUI;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimerActivityBinding;
import com.nebo.timing.util.ActivityTimerUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ActivityTimerActivity extends AppCompatActivity implements
    TimedActivityAdapter.OnTimedActivityClick {

    private String TAG = "ActivityTimerActivity-DEBUG";

    private ActivityTimerActivityBinding mBinding = null;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();
    private Map<String, TimedActivity> mKeyToTimedActivities = new TreeMap<>();
    private Map<String, String> mActivityNameToActivityKey = new HashMap<>();
    private Map<String, Integer> mActivityKeyToIndex = new HashMap<>();

    public static final int STOPWATCH_ACTIVITY = 1;
    public static final int SELECT_ACTIVITY = 2;
    public static final int RC_SIGN_IN = 3;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mTimedActivitiesDBRef;

    private long [] sessionLapTimes = null;
    private long sessionTotalTime = 0L;
    private TimedActivity mSelectedActivity = null;

    private String mCurrentUser = null;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ChildEventListener mDBChildEventListner = null;

    private boolean isClosing = false;

    private void onStopWatchClick() {
        Intent intent = new Intent(this, StopWatchActivity.class);
        startActivityForResult(intent, STOPWATCH_ACTIVITY);
    }

    private void selectActivity() {
        Intent intent = new Intent(this, SelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(
                getString(R.string.key_timed_activities),
                (ArrayList<TimedActivity>) mTimedActivities);
        intent.putExtras(bundle);
        startActivityForResult(intent, SELECT_ACTIVITY);
    }

    private void saveFirebaseEntry() {
        // create a new session
        ActivitySession session = new ActivitySession("Session");
        for (long sessionLap : sessionLapTimes) {
            session.addSessionLapTime(sessionLap);
        }

        TimedActivity timedActivity = null;

        if (mActivityNameToActivityKey.get(mSelectedActivity.getName()) != null) {
            timedActivity = mKeyToTimedActivities.get(
                    mActivityNameToActivityKey.get(mSelectedActivity.getName()));
        }

        // now need to see if this will be a new entry or an update to an already existing timed
        // activity.
        if (timedActivity == null) {
            // new entry.
            timedActivity = new TimedActivity(
                    mSelectedActivity.getName(),
                    mSelectedActivity.getCategory(),
                    mCurrentUser);

            timedActivity.addActivitySession(session);
            mTimedActivitiesDBRef.push().setValue(timedActivity);
        }
        else {
            // update existing.

            // 1. need to go into the location into the firebase database within the
            // timed-activities location.
            DatabaseReference dbref = mTimedActivitiesDBRef
                    .child(mActivityNameToActivityKey.get(timedActivity.getName()));

            // 2. add the new session to the activity.
            timedActivity.addActivitySession(session);

            // 3. get the list and update with a map.
            Map<String, Object> updateOfSessions = new HashMap<>();
            updateOfSessions.put(getString(
                    R.string.firebase_database_activity_sessions),
                    timedActivity.getActivitySessions());
            updateOfSessions.put(
                    getString(R.string.firebase_database_activity_total_elapsed_time),
                    timedActivity.getTotalElapsedTime());

            // 4. update
            dbref.updateChildren(updateOfSessions);
        }
    }

    private void attachDBListener() {
        if (mDBChildEventListner == null) {
            mDBChildEventListner = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    TimedActivity timedActivity = dataSnapshot.getValue(TimedActivity.class);
                    if (timedActivity != null) {
                        mKeyToTimedActivities.put(dataSnapshot.getKey(), timedActivity);
                        mActivityNameToActivityKey.put(timedActivity.getName(), dataSnapshot.getKey());

                        if (mBinding.rvTimedActivities.getAdapter() != null) {
                            ((TimedActivityAdapter) mBinding.rvTimedActivities.getAdapter())
                                    .addNewTimedActivity(timedActivity);
                            mTimedActivities.add(timedActivity);
                            mActivityKeyToIndex.put(dataSnapshot.getKey(), mTimedActivities.size() - 1);
                        }
                        hideEmtpy();
                        buildGraph();
                    }
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    TimedActivity timedActivity = dataSnapshot.getValue(TimedActivity.class);

                    if (timedActivity != null && mBinding.rvTimedActivities.getAdapter() != null) {
                        ((TimedActivityAdapter) mBinding.rvTimedActivities.getAdapter())
                                .updateAtIndex(
                                        mActivityKeyToIndex.get(dataSnapshot.getKey()).intValue(),
                                        timedActivity);
                    }
                    hideEmtpy();
                    buildGraph();
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            };

            mTimedActivitiesDBRef.addChildEventListener(mDBChildEventListner);
        }
    }

    private void detachDBListener() {
        if (mDBChildEventListner != null) {
            mTimedActivitiesDBRef.removeEventListener(mDBChildEventListner);
            mDBChildEventListner = null;
        }
    }

    private void onSignedInInitialize(String user) {
        mCurrentUser = user;
        attachDBListener();
    }

    private void onSignedOutCleanup() {
        mCurrentUser = null;
        mTimedActivities.clear();
        mKeyToTimedActivities.clear();
        mActivityNameToActivityKey.clear();
        mActivityKeyToIndex.clear();
        detachDBListener();
    }

    private void createAuthStateListener() {
        if (mAuthStateListener == null) {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (!isClosing) {
                        if (firebaseAuth.getCurrentUser() == null) {
                            // No one is signed-in
                            startActivityForResult(
                                    AuthUI.getInstance()
                                            .createSignInIntentBuilder()
                                            .setIsSmartLockEnabled(false)
                                            .setAvailableProviders(Arrays.asList(
                                                    new AuthUI.IdpConfig.GoogleBuilder().build(),
                                                    new AuthUI.IdpConfig.EmailBuilder().build()))
                                            .build(),
                                    RC_SIGN_IN);
                        } else {
                            // someone is already sign-in
                            onSignedInInitialize(firebaseAuth.getCurrentUser().getDisplayName());
                        }
                    }
                }
            };
        }
    }

    private void showEmpty() {
        mBinding.rvTimedActivities.setVisibility(View.GONE);
        mBinding.pcActivitiesByCategory.setVisibility(View.GONE);
        mBinding.tvGraphTimerActivitiesLabel.setVisibility(View.GONE);
        mBinding.tvRvTimerActivitiesLabel.setVisibility(View.GONE);
        mBinding.tvEmpty.setVisibility(View.VISIBLE);
    }

    private void hideEmtpy() {
        mBinding.tvEmpty.setVisibility(View.GONE);
        mBinding.rvTimedActivities.setVisibility(View.VISIBLE);
        mBinding.pcActivitiesByCategory.setVisibility(View.VISIBLE);
        mBinding.tvGraphTimerActivitiesLabel.setVisibility(View.VISIBLE);
        mBinding.tvRvTimerActivitiesLabel.setVisibility(View.VISIBLE);
    }

    private void buildGraph() {
        // 1. will need to organzie the current set of timed activities based off of category.
        HashMap<String, Long> categoryElapsedTimeTotals = new HashMap<>();
        long totalTime = 0L;

        for (TimedActivity timedActivity : mTimedActivities) {
            Long currentElapsedTime = categoryElapsedTimeTotals
                    .getOrDefault(timedActivity.getCategory(), 0L) +
                    timedActivity.getTotalElapsedTime();

            // Update total
            totalTime += timedActivity.getTotalElapsedTime();

            // Write back total time for the category.
            categoryElapsedTimeTotals.put(timedActivity.getCategory(), currentElapsedTime);
        }

        // 2. the mapping of all the categories with their respective total elapsed time is now
        //    compounded.  Populate the chart data.
        List<PieEntry> categoryEntries = new ArrayList<>();
        for (String key : categoryElapsedTimeTotals.keySet()) {
            long time = categoryElapsedTimeTotals.get(key);
            categoryEntries.add(new PieEntry(((float) time) / totalTime * 100, key));
        }

        PieDataSet pieDataSet = new PieDataSet(categoryEntries, "Category Times %");
        pieDataSet.setColors(ActivityTimerUtils.getColors(categoryElapsedTimeTotals.size()));

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);

        // 3. Adding the data to the chart.
        mBinding.pcActivitiesByCategory.setData(pieData);

        // setting the bar chart properties.
        mBinding.pcActivitiesByCategory.getDescription().setEnabled(false);

        // Now invalidate the chart to redraw.
        mBinding.pcActivitiesByCategory.invalidate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ActivityTimerActivity", "onActivityResult " + Integer.toString(requestCode) + " " + Integer.toString(resultCode));

        switch (requestCode) {
            case STOPWATCH_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    // Get the data that is returned via the intent.
                    Bundle bundle = new Bundle();
                    if (data != null) {
                        bundle = data.getExtras();

                        if (bundle != null) {
                            sessionTotalTime = bundle.getLong(
                                    getString(R.string.key_total_time),
                                    0L);
                            sessionLapTimes = bundle.getLongArray(
                                    getString(R.string.key_lap_times));

                            if (sessionTotalTime != 0L) {
                                // Handle the data for the for the individual laps.
                                // need to handle assigning it to an activity (new / old).
                                selectActivity(); }
                            else {
                                // Clear otherwise.
                                sessionTotalTime = 0L;
                                sessionLapTimes = null;
                            }
                        }
                    }
                }
                break;
            case SELECT_ACTIVITY:
                Log.d("ActivityTimerActivity", "onACtivityResult SelectActivity returned.");
                if (resultCode == RESULT_OK) {
                    Bundle bundle = new Bundle();
                    if (data != null) {
                        bundle = data.getExtras();

                        if (bundle != null) {
                            mSelectedActivity = bundle.getParcelable(
                                    getString(R.string.key_selected_activity));

                            if (mSelectedActivity != null) {
                                saveFirebaseEntry();
                            }
                            else {
                                // Clear otherwise, not allowing re-selection of activity with the
                                // previous recorded time.
                                sessionTotalTime = 0L;
                                sessionLapTimes = null;
                            }
                        }
                    }


                }
                break;
            case RC_SIGN_IN:
                if (resultCode == RESULT_CANCELED) {
                    finish();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item != null) {
            switch (item.getItemId()) {
                case R.id.mi_stopwatch:
                    onStopWatchClick();
                    break;
                case R.id.mi_sign_out:
                    isClosing = true;
                    onSignedOutCleanup();
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                public void onComplete(@NonNull Task<Void> task) {
                                    finish();
                                }
                            });
                    break;
            }
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer_activity);

        // 1. get the firebase instance
        mFirebaseDatabase = FirebaseDatabase.getInstance();

        // 2. get the firebase reference to the desired child
        mTimedActivitiesDBRef = mFirebaseDatabase
                .getReference()
                .child(getString(R.string.firebase_database_timed_activities));

        // 1. setup the firebase auth instance.
        mFirebaseAuth = FirebaseAuth.getInstance();

        attachDBListener();

        // Setup of the UI recyclerview widget
        mBinding.rvTimedActivities.setAdapter(new TimedActivityAdapter(
                this,
                this,
                mTimedActivities));
        mBinding.rvTimedActivities.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false));
        mBinding.rvTimedActivities.setHasFixedSize(true);

        if (mTimedActivities.isEmpty()) {
            showEmpty();
        }
        else {
            // Population of the Pie chart based on previously stored data.
            buildGraph();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onClick(TimedActivity timedActivity) {
        // Support the launching of the intent to get the timeActivity details.
        Intent intent = new Intent(getApplicationContext(), TimedActivityDetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.key_timed_activity), timedActivity);

        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        Log.d(TAG, "onResume");
        if (mAuthStateListener == null) {
            createAuthStateListener();
        }
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        */
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
        Log.d(TAG, "onPause");
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
        onSignedOutCleanup();
        */
    }
}

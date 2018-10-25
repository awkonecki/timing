package com.nebo.timing;

import android.app.ActivityOptions;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.util.SparseArray;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
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
    TimedActivityAdapter.OnTimedActivityClick, ValueEventListener {

    private String TAG = "ActivityTimerActivity-DEBUG";

    private ActivityTimerActivityBinding mBinding = null;

    public static final int STOPWATCH_ACTIVITY = 1;
    public static final int RC_SIGN_IN = 3;

    private Query mQuery;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();

    private long [] sessionLapTimes = null;
    private long sessionTotalTime = 0L;

    private String mCurrentUser = null;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private boolean isClosing = false;

    private void onStopWatchClick() {
        Bundle bundle = new Bundle();
        Intent intent = new Intent(getApplicationContext(), StopWatchActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, STOPWATCH_ACTIVITY);
    }

    private void selectActivity() {
        Intent intent = new Intent(getApplicationContext(), SelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putLongArray(getString(R.string.key_lap_times), sessionLapTimes);
        Log.d(TAG, "Current user is " + mCurrentUser);
        bundle.putString(getString(R.string.key_user_uid), mCurrentUser);
        intent.putExtras(bundle);

        // clear prior to starting the activity to prevent resume from calling selectActivity again.
        sessionLapTimes = null;
        sessionTotalTime = 0L;

        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    private void attachDBListener() {
        if (mQuery == null
                && mCurrentUser != null
                && mCurrentUser.equals(FirebaseAuth.getInstance().getUid()))
        {
            DatabaseReference dbref = FirebaseDatabase
                    .getInstance()
                    .getReference()
                    .child(getString(R.string.firebase_database_timed_activities));
            mQuery = dbref.orderByChild(getString(R.string.firebase_database_activity_user))
                    .equalTo(FirebaseAuth.getInstance().getUid());
            mQuery.addValueEventListener(this);
        }
    }

    private void detachDBListener() {
        if (mQuery != null) {
            mQuery.removeEventListener(this);
            mQuery = null;
        }
    }

    private void onSignedInInitialize(String user) {
        Log.d(TAG, "onSignedInInitialize " + user + " " + FirebaseAuth.getInstance().getUid());
        if (mAuthStateListener != null) {
            Log.d(TAG, "onSignedInInitialize true " + FirebaseAuth.getInstance().getUid());
            mCurrentUser = user;
            attachDBListener();

            if (sessionLapTimes != null) {
                selectActivity();
            }
        }
    }

    private void onSignedOutCleanup() {
        mCurrentUser = null;
        detachDBListener();
        mTimedActivities.clear();
        if (mBinding.rvTimedActivities.getAdapter() != null) {
            ((TimedActivityAdapter)mBinding.rvTimedActivities.getAdapter()).clearActivities();
        }
    }

    private void createAuthStateListener() {
        if (mAuthStateListener == null) {
            mAuthStateListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if (!isClosing) {
                        if (firebaseAuth.getCurrentUser() == null) {
                            Log.d(TAG, "Create AuthStateListener new");
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
                            Log.d(TAG, "Create AuthStateListener else " + FirebaseAuth.getInstance().getUid());
                            onSignedInInitialize(FirebaseAuth.getInstance().getUid());
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
            PieEntry pieEntry = new PieEntry(((float) time) / totalTime * 100, key);
            categoryEntries.add(pieEntry);
        }

        PieDataSet pieDataSet = new PieDataSet(categoryEntries, getString(R.string.pie_graph_label));
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

        switch (requestCode) {
            case STOPWATCH_ACTIVITY:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    // Get the data that is returned via the intent.
                    Bundle bundle = data.getExtras();

                    sessionTotalTime = bundle.getLong(
                            getString(R.string.key_total_time),
                            0L);
                    sessionLapTimes = bundle.getLongArray(getString(R.string.key_lap_times));

                    if (sessionTotalTime == 0L) {
                        // Clear otherwise.
                        sessionTotalTime = 0L;
                        sessionLapTimes = null;
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
        setSupportActionBar(mBinding.tbActivityTimerActivityToolbar);

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
    public void onClick(TimedActivity timedActivity) {
        // Support the launching of the intent to get the timeActivity details.
        Intent intent = new Intent(getApplicationContext(), TimedActivityDetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.key_timed_activity), timedActivity);

        intent.putExtras(bundle);
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mAuthStateListener == null) {
            createAuthStateListener();
            FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener);
            mAuthStateListener = null;
        }
        onSignedOutCleanup();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            TimedActivity timedActivity = snapshot.getValue(TimedActivity.class);

            if (timedActivity != null) {
                if (mBinding.rvTimedActivities.getAdapter() != null) {
                    ((TimedActivityAdapter) mBinding.rvTimedActivities.getAdapter())
                            .addNewTimedActivity(timedActivity);
                    mTimedActivities.add(timedActivity);
                }
            }
        }

        hideEmtpy();
        buildGraph();
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}
}

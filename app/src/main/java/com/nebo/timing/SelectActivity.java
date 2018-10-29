package com.nebo.timing;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectActivity extends AppCompatActivity implements ValueEventListener,
        SelectActivityAdapter.OnActivitySelection {

    private static final int ASYNC_PUSH_DATA = 1;
    private ActivitySelectActivityBinding mBinding = null;
    private int mSelectedIndex = -1;
    private String mSelectedKey = null;
    private TimedActivity mSelectedActivity = null;
    private Query mQuery = null;
    private ArrayList<String> mKeys = new ArrayList<>();
    private String mUserUid = null;

    private long mSessionTotalTime = 0L;
    private long [] mSessionLapTimes = null;
    private boolean mSaving = false;

    private static Context sContext = null;

    private static class SaveTimedActivity extends AsyncTask<Bundle, Void, Void> {

        @Override
        protected Void doInBackground(Bundle... bundles) {
            long [] sessionLapTimes;
            String timedActivityKey;
            TimedActivity timedActivity = null;
            String userUid;
            String sessionName;
            String activityName;
            String categoryName;

            if (bundles != null && bundles.length > 0) {
                for (Bundle args : bundles) {
                    sessionLapTimes = args.getLongArray(sContext.getString(R.string.key_lap_times));
                    timedActivityKey = args.getString(
                            sContext.getString(R.string.key_timed_activity_key),
                            null);
                    timedActivity = args.getParcelable(sContext
                            .getString(R.string.key_selected_activity));
                    userUid = args.getString(sContext
                            .getString(R.string.key_user_uid), null);
                    sessionName = args.getString(
                            sContext.getString(R.string.key_session_name),
                            sContext.getString(R.string.default_session_name));
                    activityName = args.getString(
                            sContext.getString(R.string.key_new_name_string),
                            sContext.getString(R.string.edit_activity_name_default));
                    categoryName = args.getString(
                            sContext.getString(R.string.key_new_category_string),
                            sContext.getString(R.string.edit_activity_category_default));
                    ArrayList<String> keys = args.getStringArrayList(
                            sContext.getString(R.string.key_timed_activity_keys));

                    // Make sure that the current auth user id is equal to the target user id that
                    // logged in.
                    if (userUid != null && userUid.equals(FirebaseAuth.getInstance().getUid())) {

                        // Firebase DB Reference Setup.
                        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference()
                                .child(sContext
                                        .getString(R.string.firebase_database_timed_activities));

                        // create a new session
                        ActivitySession session = new ActivitySession(sessionName);
                        if (sessionLapTimes != null) {
                            for (long sessionLap : sessionLapTimes) {
                                session.addSessionLapTime(sessionLap);
                            }
                        }

                        // now need to see if this will be a new entry or an update to an already
                        // existing timed activity.
                        if (timedActivity == null) {
                            // new entry.
                            timedActivity = new TimedActivity(
                                    activityName,
                                    categoryName,
                                    FirebaseAuth.getInstance().getUid());
                            timedActivity.addActivitySession(session);
                            dbref.push().setValue(timedActivity);
                        }
                        else {
                            // update existing.
                            // 1. need to go into the location into the firebase database within the
                            // timed-activities location.
                            dbref = dbref.child(timedActivityKey);

                            // 2. add the new session to the activity.
                            timedActivity.addActivitySession(session);
                            // 3. get the list and update with a map.
                            Map<String, Object> updateOfSessions = new HashMap<>();
                            updateOfSessions.put(
                                    sContext
                                            .getString(R.string.firebase_database_activity_sessions),
                                    timedActivity.getActivitySessions());
                            updateOfSessions.put(
                                    sContext.getString(R.string.firebase_database_activity_total_elapsed_time),
                                    timedActivity.getTotalElapsedTime());
                            // 4. update
                            dbref.updateChildren(updateOfSessions);
                        }
                    }
                }
            }

            return null;
        }
    }

    private void saveSelectedAndFinish() {
        // Create the bundle for the async task
        Bundle args = new Bundle();

        args.putLongArray(getString(R.string.key_lap_times), mSessionLapTimes);
        args.putString(getString(R.string.key_user_uid), mUserUid);
        // TODO @awkonecki let user specify session name other than default.
        args.putString(getString(R.string.key_session_name), getString(R.string.default_session_name));
        args.putStringArrayList(getString(R.string.key_timed_activity_keys), mKeys);

        if (mBinding.tbUseNewActivityToggle.isChecked()) {
            // use the user specified name & category in creation of a new.
            args.putString(getString(R.string.key_new_name_string),
                    mBinding.etNewActivityName.getText().toString());
            args.putString(getString(R.string.key_new_category_string),
                    mBinding.etNewActivityCategory.getText().toString());
        }
        else {
            args.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
            args.putString(getString(R.string.key_timed_activity_key), mSelectedKey);

        }

        // for handling for when new data comes back.
        mSaving = true;
        SelectActivity.sContext = getApplicationContext();

        // Launch the aysnc task
        (new SaveTimedActivity()).execute(args);
    }

    private void attachQueryListener() {
        // Build the query on the Firebase data reference only if the uid is valid.
        if (FirebaseAuth.getInstance().getUid() != null && mQuery == null) {
            mQuery = FirebaseDatabase.getInstance().getReference()
                    .child(getString(R.string.firebase_database_timed_activities))
                    .orderByChild(getString(R.string.firebase_database_activity_user))
                    .equalTo(FirebaseAuth.getInstance().getUid());
            mQuery.addValueEventListener(this);
        }
    }

    private void detachQueryListener() {
        if (mQuery != null) {
            mQuery.removeEventListener(this);
            mQuery = null;
        }
    }

    private void initializeView() {
        // Recycler view setup
        mBinding.rvSaveTimeActivities.setAdapter(new SelectActivityAdapter(
                this,
                this,
                mSelectedIndex));
        mBinding.rvSaveTimeActivities.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false));
        mBinding.rvSaveTimeActivities.setHasFixedSize(true);

        // setup of the toggle button behavior.
        mBinding.tbUseNewActivityToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.tbUseNewActivityToggle.isChecked()) {
                    ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter())
                            .unSelect(
                                    mBinding.rvSaveTimeActivities
                                            .getLayoutManager()
                                            .findViewByPosition(mSelectedIndex));
                    mSelectedIndex = -1;
                    mSelectedActivity = null;
                    mSelectedKey = null;
                }
            }
        });
    }

    private void loadInstanceData(@Nullable Bundle instanceState) {
        if (instanceState != null) {
            mSelectedActivity = instanceState.getParcelable(
                    getString(R.string.key_selected_activity));
            mSelectedKey = instanceState.getString(
                    getString(R.string.key_timed_activity_key), null);
            mSelectedIndex = instanceState.getInt(
                    getString(R.string.key_selected_activity_index), -1);
            mBinding.tbUseNewActivityToggle.setChecked(instanceState.getBoolean(
                    getString(R.string.key_tb_status),false));
            mBinding.etNewActivityName.setText(
                    instanceState.getString(
                            getString(R.string.key_new_name_string),
                            getString(R.string.edit_activity_name_default)));
            mBinding.etNewActivityCategory.setText(
                    instanceState.getString(
                            getString(R.string.key_new_category_string),
                            getString(R.string.edit_activity_category_default)));

            mSessionTotalTime = instanceState.getLong(
                    getString(R.string.key_total_time),
                    0L);
            mSessionLapTimes = instanceState.getLongArray(
                    getString(R.string.key_lap_times));

            mUserUid = instanceState.getString(getString(R.string.key_user_uid), null);
        }
        else {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                mSessionTotalTime = bundle.getLong(
                        getString(R.string.key_total_time),
                        0L);
                mSessionLapTimes = bundle.getLongArray(
                        getString(R.string.key_lap_times));
                mUserUid = bundle.getString(getString(R.string.key_user_uid), null);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_select_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item != null) {
            switch (item.getItemId()) {
                case R.id.mi_save_selected_activity:
                    if (mBinding.tbUseNewActivityToggle.isChecked() || mSelectedIndex != -1) {
                        // perform the async task that is responsible of pushing the data.
                        saveSelectedAndFinish();
                    }
                    else {
                        finish();
                    }
                    break;
            }
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handling of animations for the the activity
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Slide());

        // Setup of the view
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_activity);
        setSupportActionBar(mBinding.tbSelectActivityToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load the data.
        loadInstanceData(savedInstanceState);

        // Initialize the view
        initializeView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mSelectedActivity != null) {
            outState.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
        }
        if (mSelectedKey != null) {
            outState.putString(getString(R.string.key_timed_activity_key), mSelectedKey);
        }
        if (mSelectedIndex != -1) {
            outState.putInt(getString(R.string.key_selected_activity_index), mSelectedIndex);
        }
        if (mBinding.tbUseNewActivityToggle.isChecked()) {
            outState.putBoolean(getString(R.string.key_tb_status), true);
        }

        if (mUserUid != null) {
            outState.putString(getString(R.string.key_user_uid), mUserUid);
        }

        outState.putString(
                getString(R.string.key_new_name_string),
                mBinding.etNewActivityName.getText().toString());
        outState.putString(
                getString(R.string.key_new_category_string),
                mBinding.etNewActivityCategory.getText().toString());

        outState.putLong(getString(R.string.key_total_time), mSessionTotalTime);
        outState.putLongArray(getString(R.string.key_lap_times), mSessionLapTimes);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        attachQueryListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachQueryListener();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            TimedActivity timedActivity = snapshot.getValue(TimedActivity.class);
            ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter())
                    .addActivity(snapshot.getKey(), timedActivity);
            mKeys.add(snapshot.getKey());

            if (mSaving) {
                finish();
            }
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}

    @Override
    public void selectedActivity(int index, String key, TimedActivity timedActivity) {
        if (index == mSelectedIndex) {
            ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter())
                    .unSelect(
                            mBinding.rvSaveTimeActivities
                                    .getLayoutManager()
                                    .findViewByPosition(mSelectedIndex));
            mSelectedIndex = -1;
            mSelectedActivity = null;
            mSelectedKey = null;
        }
        else {
            mSelectedActivity = timedActivity;
            mSelectedIndex = index;
            mSelectedKey = key;
        }

        // Toggle button default state
        mBinding.tbUseNewActivityToggle.setChecked(false);
    }
}

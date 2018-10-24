package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nebo.timing.async.PushTimedActivity;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;

import java.util.ArrayList;

public class SelectActivity extends AppCompatActivity implements ValueEventListener,
        SelectActivityAdapter.OnActivitySelection,
        LoaderManager.LoaderCallbacks<Void> {

    private static final int ASYNC_PUSH_DATA = 1;
    private ActivitySelectActivityBinding mBinding = null;
    private int mSelectedIndex = -1;
    private String mSelectedKey = null;
    private TimedActivity mSelectedActivity = null;
    private Query mQuery = null;
    private ArrayList<String> mKeys = new ArrayList<>();
    // TODO @awkonecki pass in logged in user uid from auth state change.
    private String mUserUid = FirebaseAuth.getInstance().getUid();

    private long mSessionTotalTime = 0L;
    private long [] mSessionLapTimes = null;

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

        detachQueryListener();

        // Launch of the async task.
        if (getSupportLoaderManager().getLoader(ASYNC_PUSH_DATA) == null) {
            getSupportLoaderManager().initLoader(ASYNC_PUSH_DATA, args, this).forceLoad();
        }
        else {
            getSupportLoaderManager().restartLoader(ASYNC_PUSH_DATA, args, this).forceLoad();
        }
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
        }
        else {
            Bundle bundle = getIntent().getExtras();

            if (bundle != null) {
                mSessionTotalTime = bundle.getLong(
                        getString(R.string.key_total_time),
                        0L);
                mSessionLapTimes = bundle.getLongArray(
                        getString(R.string.key_lap_times));
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

    @NonNull
    @Override
    public Loader<Void> onCreateLoader(int id, @Nullable Bundle args) {
        Loader<Void> loader = null;

        switch (id) {
            case ASYNC_PUSH_DATA:
                loader = new PushTimedActivity(this, args);
                break;
            default:
                loader = new Loader<>(this);
                break;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Void> loader, Void data) {
        finish();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Void> loader) {}
}

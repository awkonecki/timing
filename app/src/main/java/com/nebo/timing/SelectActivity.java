package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectActivity extends AppCompatActivity implements ValueEventListener {

    private ActivitySelectActivityBinding mBinding = null;
    private List<TimedActivity> mActivities = new ArrayList<>();
    private TimedActivity mSelectedActivity = null;
    private Query mQuery = null;

    private void saveSelectedAndFinish() {
        Bundle bundle = new Bundle();
        if (mSelectedActivity != null) {
            if (mBinding.tbUseNewActivityToggle.isChecked()) {
                mSelectedActivity = new TimedActivity(
                        mBinding.etNewActivityName.getText().toString(),
                        mBinding.etNewActivityCategory.getText().toString(),
                        null);
            }

            bundle.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
        }

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
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
        // Toggle button default state
        mBinding.tbUseNewActivityToggle.setChecked(false);

        // Recycler view setup
        mBinding.rvSaveTimeActivities.setAdapter(new SelectActivityAdapter(this));
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
                    ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter()).unSelect();
                    String name = mBinding.etNewActivityName.getText().toString();
                    String category = mBinding.etNewActivityCategory.getText().toString();

                    // mSelectedActivity = mMapOfActivities.get(name);

                    if (mSelectedActivity == null) {
                        mSelectedActivity = new TimedActivity(name, category, null);
                    }
                }
            }
        });
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
                    saveSelectedAndFinish();
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

        // Initialize the view
        initializeView();

        if (savedInstanceState != null) {
            mActivities = savedInstanceState.getParcelableArrayList(
                    getString(R.string.key_timed_activities));
            mBinding.etNewActivityName.setText(
                    savedInstanceState.getString(getString(R.string.key_new_name_string)));
            mBinding.etNewActivityCategory.setText(
                    savedInstanceState.getString(getString(R.string.key_new_category_string)));
        }
        else {
            // process intent data.
            Bundle intentBundle = getIntent().getExtras();

            if (intentBundle != null) {
                mActivities = intentBundle.getParcelableArrayList(
                        getString(R.string.key_timed_activities));
            }
        }

        Log.d("onCreate", FirebaseAuth.getInstance().getUid());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(
                getString(R.string.key_timed_activities), (ArrayList<TimedActivity>) mActivities);
        outState.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
        outState.putString(
                getString(R.string.key_new_name_string),
                mBinding.etNewActivityName.getText().toString());
        outState.putString(
                getString(R.string.key_new_category_string),
                mBinding.etNewActivityCategory.getText().toString());

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
            Log.d("onValueChanged", snapshot.toString());
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {}
}

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.google.firebase.auth.FirebaseAuth;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectActivity extends AppCompatActivity {

    private ActivitySelectActivityBinding mBinding = null;
    private List<TimedActivity> mActivities = new ArrayList<>();
    private TimedActivity mSelectedActivity = null;
    private HashMap<String, TimedActivity> mMapOfActivities = new HashMap<>();

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

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Slide());

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_activity);
        setSupportActionBar(mBinding.tbSelectActivityToolbar);
        
        mBinding.tbUseNewActivityToggle.setChecked(false);

        // if the auth state is not valid then no work to do, just return.
        if (FirebaseAuth.getInstance().getUid() == null) {
            finish();
        }

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

        // Setup the hash set to aid in making sure unique activities by name.
        for (TimedActivity activity : mActivities) {
            mMapOfActivities.put(activity.getName(), activity);
        }

        // setup the view's recyclerview widget
        mBinding.rvSaveTimeActivities.setAdapter(new SelectActivityAdapter());
        mBinding.rvSaveTimeActivities.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false));
        mBinding.rvSaveTimeActivities.setHasFixedSize(true);

        mBinding.tbUseNewActivityToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.tbUseNewActivityToggle.isChecked()) {
                    ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter()).unSelect();
                    String name = mBinding.etNewActivityName.getText().toString();
                    String category = mBinding.etNewActivityCategory.getText().toString();

                    mSelectedActivity = mMapOfActivities.get(name);

                    if (mSelectedActivity == null) {
                        mSelectedActivity = new TimedActivity(name, category, null);
                    }
                }
            }
        });
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
}

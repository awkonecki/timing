package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimerActivityBinding;

import java.util.ArrayList;
import java.util.List;

public class ActivityTimerActivity extends AppCompatActivity {
    private ActivityTimerActivityBinding mBinding = null;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer_activity);

        // Static data
        // TODO @awkonecki remove later
        mTimedActivities.clear();
        mTimedActivities.add(TimedActivity.getTimedActivity());
        mTimedActivities.add(TimedActivity.getTimedActivity());
        mTimedActivities.add(TimedActivity.getTimedActivity());

        if (savedInstanceState != null) {

        }
        else {
            // This is the base activity (main activity) thus do not expect to process data from
            // intent.
        }

        // Setup of the UI recyclerview widget
        mBinding.rvTimedActivities.setAdapter(new TimedActivityAdapter(
                this,
                mTimedActivities));
        mBinding.rvTimedActivities.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false));
        mBinding.rvTimedActivities.setHasFixedSize(true);

        // Population of the Pie chart based on previously stored data.
    }
}

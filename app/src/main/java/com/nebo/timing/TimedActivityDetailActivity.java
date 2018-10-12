package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimedActivityDetailBinding;

public class TimedActivityDetailActivity extends AppCompatActivity {
    private ActivityTimedActivityDetailBinding mBinding = null;
    private TimedActivity mTimedActivity = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the databinding for referencing widgets within the UI.
        mBinding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_timed_activity_detail);

        if (savedInstanceState != null) {
            // use the instance data
            mTimedActivity = savedInstanceState.getParcelable(
                    getString(R.string.key_timed_activity));
        }
        else {
            // use the intent passed data.
            mTimedActivity = getIntent().getParcelableExtra(getString(R.string.key_timed_activity));
        }

        // will likely want the activity to perform the query on the data based on the activity /
        // user key combination from persistent storage.

        // assume for now the data is populated somewhere.
    }
}

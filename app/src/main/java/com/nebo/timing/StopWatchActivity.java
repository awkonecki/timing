package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.nebo.timing.databinding.ActivityStopwatchBinding;
import com.nebo.timing.ui.ElapsedTimeFragment;
import com.nebo.timing.ui.LapTimesFragment;
import com.nebo.timing.ui.StopWatchActionsFragment;

public class StopWatchActivity extends AppCompatActivity {
    ActivityStopwatchBinding mBinding = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_stopwatch);

        // Population of the activity fragments.
        LapTimesFragment lapTimesFragment = new LapTimesFragment();
        ElapsedTimeFragment elapsedTimeFragment = new ElapsedTimeFragment();
        StopWatchActionsFragment stopWatchActionsFragment = new StopWatchActionsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(mBinding.rlElapsedTime.getId(), elapsedTimeFragment)
                .add(mBinding.rlLapTimes.getId(), lapTimesFragment)
                .add(mBinding.rlStopwatchActions.getId(), stopWatchActionsFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_stopwatch, menu);
        return super.onCreateOptionsMenu(menu);
    }
}

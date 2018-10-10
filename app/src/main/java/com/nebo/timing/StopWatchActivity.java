package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import com.nebo.timing.data.StopWatch;
import com.nebo.timing.databinding.ActivityStopwatchBinding;
import com.nebo.timing.ui.ElapsedTimeFragment;
import com.nebo.timing.ui.LapTimesFragment;
import com.nebo.timing.ui.StopWatchActionsFragment;
import com.nebo.timing.ui.StopWatchActionsFragment.ACTIONS;

import java.util.LinkedList;
import java.util.List;

public class StopWatchActivity extends AppCompatActivity implements
        StopWatchActionsFragment.StopWatchActions,
        StopWatch.StopWatchTickEvents {

    private ActivityStopwatchBinding mBinding = null;
    private StopWatch mStopWatch = null;
    private List<Long> mLaps = null;

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

        // Create the stopwatch object.
        mStopWatch = new StopWatch(this);

        // Create the list of laps.
        mLaps = new LinkedList<Long>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_stopwatch, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void handleStopWatchAction(ACTIONS action) {
        // Allows the StopWatchActionsFragment to inform the StopWatchActivity class about a user
        // action on the supported StopWatchActions.
        switch (action) {
            case Start:
                mStopWatch.play();
                break;
            case Stop:
                mStopWatch.pause();
                break;
            case Lap:
                break;
            case Reset:
                mStopWatch.stop();
                mLaps.clear();
                tickEvent(0);
                break;
        }
    }

    @Override
    public void tickEvent(long milliSecondsElapsed) {
        Long lastLapTime = 0L;
        String totalTimeDisplay = null, lapTimeDisplay = null;

        if (mLaps.size() > 1) {
            lastLapTime = mLaps.get(1);
        }
        else if (mLaps.size() == 0) {
            mLaps.add(0L);
        }

        totalTimeDisplay = StopWatch.buildTimeStamp(milliSecondsElapsed);
        lapTimeDisplay = StopWatch.buildTimeStamp(
                milliSecondsElapsed - lastLapTime);
        mLaps.set(0, milliSecondsElapsed);

        // Inform the fragments to update their times.
        ElapsedTimeFragment elapsedTimeFragment = (ElapsedTimeFragment) getSupportFragmentManager()
                .findFragmentById(mBinding.rlElapsedTime.getId());

        LapTimesFragment lapTimesFragment = (LapTimesFragment) getSupportFragmentManager()
                .findFragmentById(mBinding.rlLapTimes.getId());

        if (elapsedTimeFragment != null) {
            elapsedTimeFragment.updateElapsedTime(totalTimeDisplay);
        }
        else {
            throw new java.lang.UnsupportedOperationException(
                    "The elapsed time fragment does not exist and recieved a tick event."
            );
        }

        if (lapTimesFragment != null) {
            if (milliSecondsElapsed == 0) {
                lapTimesFragment.resetLapTimes();
            }
            else {
                lapTimesFragment.updateLapTime(lapTimeDisplay);
            }
        }
        else {
            throw new java.lang.UnsupportedOperationException(
                    "The lap time fragment does not exist and recieved a tick event."
            );
        }
    }
}

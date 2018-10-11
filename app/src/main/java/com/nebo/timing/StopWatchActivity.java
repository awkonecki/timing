package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

    private void createLap() {
        LapTimesFragment lapTimesFragment = (LapTimesFragment) getSupportFragmentManager()
                .findFragmentById(mBinding.rlLapTimes.getId());

        // Add a new lap that starts at 0 to the list of laps.
        mLaps.add(0, 0L);

        if (lapTimesFragment != null) {
            if (!mLaps.isEmpty()) {
                lapTimesFragment.newLapTime(StopWatch.buildTimeStamp(mLaps.get(0)));
            }
        }
        else {
            throw new java.lang.UnsupportedOperationException(
                    "The lap time fragment does not exist and recieved a create lap event."
            );
        }
    }

    private void initializeFragments(long baseTime, List<String> times) {
        // Inform the fragments to update their times.
        ElapsedTimeFragment elapsedTimeFragment = (ElapsedTimeFragment) getSupportFragmentManager()
                .findFragmentById(mBinding.rlElapsedTime.getId());

        LapTimesFragment lapTimesFragment = (LapTimesFragment) getSupportFragmentManager()
                .findFragmentById(mBinding.rlLapTimes.getId());

        if (elapsedTimeFragment != null) {
            Log.d("initializeFragments", "should be updating");
            elapsedTimeFragment.updateElapsedTime(StopWatch.buildTimeStamp(baseTime));
        }
        else {
            throw new java.lang.UnsupportedOperationException(
                    "The elapsed time fragment does not exist and trying to initialize."
            );
        }

        if (lapTimesFragment != null) {
            lapTimesFragment.initializeLaps(times);
        }
        else {
            throw new java.lang.UnsupportedOperationException(
                    "The lap time fragment does not exist and trying to initialize."
            );
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        long [] times = new long [mLaps.size()];

        int index = 0;
        for (long time : mLaps) {
            times[index] = time;
            index++;
        }

        if (mStopWatch != null) {
            outState.putInt(
                    getString(R.string.key_stopwatch_state),
                    mStopWatch.getState().getStateValue());
            outState.putLongArray(getString(R.string.key_lap_times), times);
            mStopWatch.stop();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_stopwatch);
        long baseTime = 0L;
        List<String> timeStrings = new LinkedList<String>();
        int stopWatchState = StopWatch.sSTOP_STATE_VALUE;

        // Create the list of laps.
        mLaps = new LinkedList<Long>();

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            // Obtain instance state data
            long [] times = savedInstanceState.getLongArray(getString(R.string.key_lap_times));

            if (times != null && times.length > 0) {
                for (long time : times) {
                    baseTime += time;
                    mLaps.add(time);
                    timeStrings.add(StopWatch.buildTimeStamp(time));
                }
            }

            stopWatchState = savedInstanceState.getInt(getString(R.string.key_stopwatch_state),
                    StopWatch.sSTOP_STATE_VALUE);
        }
        else {
            // Check intent data
        }

        // Create the stopwatch object.
        mStopWatch = new StopWatch(this, StopWatch.sDEFAULT_TIME_INTERVAL, baseTime);

        // Population of the activity fragments.
        if (savedInstanceState == null) {
            LapTimesFragment lapTimesFragment = new LapTimesFragment();
            ElapsedTimeFragment elapsedTimeFragment = new ElapsedTimeFragment();
            StopWatchActionsFragment stopWatchActionsFragment = new StopWatchActionsFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(mBinding.rlElapsedTime.getId(), elapsedTimeFragment)
                    .add(mBinding.rlLapTimes.getId(), lapTimesFragment)
                    .add(mBinding.rlStopwatchActions.getId(), stopWatchActionsFragment)
                    .commit();
        }
        else {
            // Populate the fragments associated with the StopWatchActivity.
            initializeFragments(baseTime, timeStrings);

            // Check the state
            if (stopWatchState == StopWatch.sPLAY_STATE_VALUE) {
                mStopWatch.play();
            }
        }
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
                createLap();
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

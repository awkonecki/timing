package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import com.nebo.timing.data.StopWatch;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityStopwatchBinding;
import com.nebo.timing.ui.ElapsedTimeFragment;
import com.nebo.timing.ui.LapTimesFragment;
import com.nebo.timing.ui.StopWatchActionsFragment;
import com.nebo.timing.ui.StopWatchActionsFragment.ACTIONS;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class StopWatchActivity extends AppCompatActivity implements
        StopWatchActionsFragment.StopWatchActions,
        StopWatch.StopWatchTickEvents {

    private ActivityStopwatchBinding mBinding = null;
    private StopWatch mStopWatch = null;
    private List<Long> mLaps = null;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();

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

    private void saveAndReturn() {
        Bundle bundle = new Bundle();
        long [] lapsToSave = new long [mLaps.size()];
        long prevLap = 0L;
        long totalTime = 0L;

        // populate the array that will be passed back as a bundle.
        for (int index = lapsToSave.length - 1; index >= 0; index--) {
            lapsToSave[index] = mLaps.get(index) - prevLap;
            prevLap = mLaps.get(index);
        }

        if (mLaps.size() > 0) {
            totalTime = mLaps.get(0);
        }

        bundle.putLong(getString(R.string.key_total_time), totalTime);
        bundle.putLongArray(getString(R.string.key_lap_times), lapsToSave);
        bundle.putParcelableArrayList(
                getString(R.string.key_timed_activities),
                (ArrayList<TimedActivity>) mTimedActivities);

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item != null) {
            switch (item.getItemId()) {
                case R.id.mi_save:
                    mStopWatch.pause();
                    saveAndReturn();
                    break;
            }
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mStopWatch != null) {
            mStopWatch.unRegisterCallback();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mStopWatch != null) {
            mStopWatch.registerCallback(this);
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
        }

        outState.putParcelableArrayList(
                getString(R.string.key_timed_activities),
                (ArrayList<TimedActivity>) mTimedActivities);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Slide());

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_stopwatch);
        setSupportActionBar(mBinding.tbActivityStopwatchToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long baseTime = 0L;
        List<String> timeStrings = new LinkedList<String>();
        int stopWatchState = StopWatch.sSTOP_STATE_VALUE;

        // Create the list of laps.
        mLaps = new LinkedList<Long>();

        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            // Obtain instance state data
            long [] times = savedInstanceState.getLongArray(getString(R.string.key_lap_times));

            if (times != null && times.length > 0) {
                baseTime = times[0];
                for (long time : times) {
                    mLaps.add(time);
                    timeStrings.add(StopWatch.buildTimeStamp(time));
                    // Log.d("onCreate", "time found is " + Long.toString(time));
                }

                // need to account for the first lap time.
                if (mLaps.size() > 1) {
                    timeStrings.set(0,
                            StopWatch.buildTimeStamp(baseTime - mLaps.get(1)));
                }
            }

            stopWatchState = savedInstanceState.getInt(getString(R.string.key_stopwatch_state),
                    StopWatch.sSTOP_STATE_VALUE);

            mTimedActivities = savedInstanceState.
                    getParcelableArrayList(getString(R.string.key_timed_activities));
        }
        else {
            // Check intent data
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                mTimedActivities = bundle.
                        getParcelableArrayList(getString(R.string.key_timed_activities));
            }
        }

        if (mTimedActivities == null) {
            mTimedActivities = new ArrayList<>();
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

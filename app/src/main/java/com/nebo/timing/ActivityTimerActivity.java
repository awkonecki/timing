package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimerActivityBinding;
import com.nebo.timing.util.ActivityTimerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityTimerActivity extends AppCompatActivity implements
    TimedActivityAdapter.OnTimedActivityClick {

    private ActivityTimerActivityBinding mBinding = null;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();

    public static final int STOPWATCH_ACTIVITY = 1;
    public static final int SAVETIME_ACTIVITY = 2;

    private void onStopWatchClick() {
        Intent intent = new Intent(this, StopWatchActivity.class);
        startActivityForResult(intent, STOPWATCH_ACTIVITY);
    }

    private void onSaveTime() {
        Intent intent = new Intent(this, SelectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(
                getString(R.string.key_timed_activities),
                (ArrayList<TimedActivity>) mTimedActivities);
        intent.putExtras(bundle);
        startActivityForResult(intent, SAVETIME_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("ActivityTimerActivity", "onActivityResult " + Integer.toString(requestCode) + " " + Integer.toString(resultCode));

        switch (requestCode) {
            case STOPWATCH_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    // Get the data that is returned via the intent.
                    Bundle bundle = new Bundle();
                    if (data != null) {
                        bundle = data.getExtras();
                    }

                    // Handle the data for the for the individual laps.
                    // need to handle assigning it to an activity (new / old).
                    onSaveTime();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_activity_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item != null) {
            switch (item.getItemId()) {
                case R.id.mi_stopwatch:
                    onStopWatchClick();
                    break;
            }
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer_activity);

        // Static data
        // TODO @awkonecki remove later
        mTimedActivities.clear();
        mTimedActivities = ActivityTimerUtils.generateActivities(new String [] {
                "Chatper 4",
                "Chapter 5",
                "Chapter 6",
                "Chapter 7",
                "Chapter 8",
                "Going to Work"
        });

        if (savedInstanceState != null) {

        }
        else {
            // This is the base activity (main activity) thus do not expect to process data from
            // intent.
        }

        // Setup of the UI recyclerview widget
        mBinding.rvTimedActivities.setAdapter(new TimedActivityAdapter(
                this,
                this,
                mTimedActivities));
        mBinding.rvTimedActivities.setLayoutManager(new LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false));
        mBinding.rvTimedActivities.setHasFixedSize(true);

        // Population of the Pie chart based on previously stored data.
        buildGraph();
    }

    private void buildGraph() {
        // 1. will need to organzie the current set of timed activities based off of category.
        HashMap<String, Long> categoryElapsedTimeTotals = new HashMap<>();
        long totalTime = 0L;

        for (TimedActivity timedActivity : mTimedActivities) {
            Long currentElapsedTime = categoryElapsedTimeTotals
                    .getOrDefault(timedActivity.getCategory(), 0L) +
                    timedActivity.getTotalElapsedTime();

            // Update total
            totalTime += timedActivity.getTotalElapsedTime();

            // Write back total time for the category.
            categoryElapsedTimeTotals.put(timedActivity.getCategory(), currentElapsedTime);
        }

        // 2. the mapping of all the categories with their respective total elapsed time is now
        //    compounded.  Populate the chart data.
        List<PieEntry> categoryEntries = new ArrayList<>();
        for (String key : categoryElapsedTimeTotals.keySet()) {
            long time = categoryElapsedTimeTotals.get(key);
            categoryEntries.add(new PieEntry(((float) time) / totalTime * 100, key));
        }

        PieDataSet pieDataSet = new PieDataSet(categoryEntries, "Category Times %");
        pieDataSet.setColors(ActivityTimerUtils.getColors(categoryElapsedTimeTotals.size()));

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);

        // 3. Adding the data to the chart.
        mBinding.pcActivitiesByCategory.setData(pieData);

        // setting the bar chart properties.
        mBinding.pcActivitiesByCategory.getDescription().setEnabled(false);

        // Now invalidate the chart to redraw.
        mBinding.pcActivitiesByCategory.invalidate();
    }

    @Override
    public void onClick(TimedActivity timedActivity) {
        // Support the launching of the intent to get the timeActivity details.
        Intent intent = new Intent(getApplicationContext(), TimedActivityDetailActivity.class);

        Bundle bundle = new Bundle();
        bundle.putParcelable(getString(R.string.key_timed_activity), timedActivity);

        intent.putExtras(bundle);
        startActivity(intent);
    }
}

package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimerActivityBinding;
import com.nebo.timing.util.TimerActivityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ActivityTimerActivity extends AppCompatActivity implements
    TimedActivityAdapter.OnTimedActivityClick
    {
    private ActivityTimerActivityBinding mBinding = null;
    private List<TimedActivity> mTimedActivities = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_timer_activity);

        // Static data
        // TODO @awkonecki remove later
        mTimedActivities.clear();
        mTimedActivities = TimerActivityUtils.generateActivities(new String [] {
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
        pieDataSet.setColors(getColors(categoryElapsedTimeTotals.size()));

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

    private int[] getColors(int count) {

        // have as many colors as stack-values per entry
        int[] colors = new int[count];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

        return colors;
    }

        @Override
        public void onClick(TimedActivity timeActivity) {
            // Support the launching of the intent to get the timeActivity details.
        }
    }

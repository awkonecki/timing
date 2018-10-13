package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.common.base.Stopwatch;
import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.StopWatch;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimedActivityDetailBinding;
import com.nebo.timing.databinding.ActivitySessionElementBinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimedActivityDetailActivity extends AppCompatActivity {
    private ActivityTimedActivityDetailBinding mBinding = null;
    private TimedActivity mTimedActivity = null;

    private class ActivitySessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ActivitySession> sessionList = new ArrayList<>();

        public ActivitySessionsAdapter(List<ActivitySession> sessions) {
            sessionList = new ArrayList<>(sessions);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivitySessionElementBinding binding = DataBindingUtil.inflate(
                    getLayoutInflater(),
                    R.layout.activity_session_element,
                    parent,
                    false);
            return new ActivitySessionViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position >= 0) {
                ((ActivitySessionViewHolder) (holder)).bind(sessionList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return sessionList.size();
        }

        private class ActivitySessionViewHolder extends RecyclerView.ViewHolder {

            private ActivitySessionElementBinding mBinding = null;

            public ActivitySessionViewHolder(ActivitySessionElementBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void bind(ActivitySession session) {
                mBinding.tvActivitySessionDate.setText("Date");
                mBinding.tvActivitySessionName.setText(session.getName());
                mBinding.tvLabels.setText("Labels");
            }
        }
    }

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
            // use the intent passed data, assume for now the data is passed in via the intent.
            mTimedActivity = getIntent().getParcelableExtra(getString(R.string.key_timed_activity));
            mTimedActivity = TimedActivity.getTimedActivity();
        }

        // setup the activity's recyclerview widget
        mBinding.rvRecordedSessions.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false));
        mBinding.rvRecordedSessions.setHasFixedSize(true);
        mBinding.rvRecordedSessions.setAdapter(new ActivitySessionsAdapter(
                mTimedActivity.getActivitySessions()));

        // set the activity name
        mBinding.tvTotalTimeDisplay.setText(
                StopWatch.buildTimeStamp(mTimedActivity.getTotalElapsedTime()));

        // Build the graph
        buildGraph();
    }

    private void buildGraph() {
        LineChart chart = (LineChart) findViewById(R.id.chart);

        // will nedd to invert instead of padding since display of 0.0f for an empty leads to
        // incorrect representation within the graph itself.
        // this means that lap 1 will be at the top while lap N will be at the bottom.
        BarEntry stackEntry1 = new BarEntry(0f, new float [] {2.31f,4.51f,34.5f});
        BarEntry stackEntry2 = new BarEntry(1f, new float [] {4.41f,13.27f,22.5f,13.6f});

        List<BarEntry> entries = new ArrayList<>();
        entries.add(stackEntry1);
        entries.add(stackEntry2);

        BarDataSet dataSet = new BarDataSet(entries,"data");
        dataSet.setColors(getColors());

        //dataSet.setColors(R.color.colorAccent, R.color.colorPrimary);
        //dataSet.addColor(R.color.colorAccent);
        //dataSet.addColor(R.color.colorPrimary);
        //dataSet.color

        ArrayList<IBarDataSet> dataSets = new ArrayList<IBarDataSet>();
        dataSets.add(dataSet);

        BarData data = new BarData(dataSets);

        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.9f; // x2 dataset
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        data.setBarWidth(barWidth);

        // Set formatting
        final String [] values = new String [] {"1", "2"};

        mBinding.bcChart.setFitBars(true);
        mBinding.bcChart.setData(data);
        // Dont really want the grid, adds to much visual noise.
        mBinding.bcChart.setDrawGridBackground(false);
        mBinding.bcChart.setDrawBarShadow(false);

        mBinding.bcChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mBinding.bcChart.getXAxis().setDrawGridLines(false);
        mBinding.bcChart.getXAxis().setDrawAxisLine(false);

        mBinding.bcChart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return values[(int) value];
            }
        });
        mBinding.bcChart.getXAxis().setGranularity(1f);


        mBinding.bcChart.getAxisLeft().setDrawAxisLine(true);
        mBinding.bcChart.getAxisLeft().setDrawGridLines(false);
        mBinding.bcChart.getAxisLeft().setDrawLabels(false);

        mBinding.bcChart.getAxisRight().setDrawAxisLine(false);
        mBinding.bcChart.getAxisRight().setDrawGridLines(false);
        mBinding.bcChart.getAxisRight().setDrawLabels(false);


        // Dont want values above the bar
        mBinding.bcChart.setDrawValueAboveBar(false);
        mBinding.bcChart.setTouchEnabled(false);
        mBinding.bcChart.setPinchZoom(false);
        mBinding.bcChart.setDoubleTapToZoomEnabled(false);

        mBinding.bcChart.invalidate(); // refresh
    }

    private int[] getColors() {

        // will need to be the largest number of laps across all the sessions.
        int stacksize = 4;

        // have as many colors as stack-values per entry
        int[] colors = new int[stacksize];

        for (int i = 0; i < colors.length; i++) {
            colors[i] = ColorTemplate.MATERIAL_COLORS[i];
        }

        return colors;
    }
}

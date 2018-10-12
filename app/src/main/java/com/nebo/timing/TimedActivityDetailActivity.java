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
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
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

        BarEntry stackEntry1 = new BarEntry(0f, new float [] {2.31f,4.51f,34.5f});
        BarEntry stackEntry2 = new BarEntry(1f, new float [] {4.41f,13.27f,22.5f,13.6f});


        List<BarEntry> entries = new ArrayList<>();
        entries.add(stackEntry1);
        entries.add(stackEntry2);

        BarDataSet dataSet = new BarDataSet(entries,"data");

        //dataSet.setColors(R.color.colorAccent, R.color.colorPrimary);
        //dataSet.addColor(R.color.colorAccent);
        //dataSet.addColor(R.color.colorPrimary);
        //dataSet.color

        BarData data = new BarData(dataSet);


        float groupSpace = 0.06f;
        float barSpace = 0.02f; // x2 dataset
        float barWidth = 0.9f; // x2 dataset
        // (0.02 + 0.45) * 2 + 0.06 = 1.00 -> interval per "group"

        data.setBarWidth(barWidth);

        mBinding.bcChart.setFitBars(true);
        mBinding.bcChart.setData(data);
        mBinding.bcChart.invalidate(); // refresh
    }
}

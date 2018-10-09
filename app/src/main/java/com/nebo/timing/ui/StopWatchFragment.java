package com.nebo.timing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.R;

import com.nebo.timing.data.StopWatch;
import com.nebo.timing.databinding.FragmentStopWatchBinding;
import com.nebo.timing.databinding.LapElementBinding;


import java.util.LinkedList;
import java.util.List;

public class StopWatchFragment extends Fragment {
    private static final String TAG = "StopWatchFragment";
    private FragmentStopWatchBinding mBinding = null;
    private StopWatch mStopWatch = new StopWatch(new TimeIntervalTick());

    private class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapView> {
        private LapElementBinding mBinding = null;

        List<String> mDisplayTimes = new LinkedList<String>();

        @NonNull
        @Override
        public LapView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.lap_element, parent, false);

            mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.lap_element, parent, false);

            return new LapView(mBinding.getRoot());
        }

        @Override
        public void onBindViewHolder(@NonNull LapView holder, int position) {

        }

        @Override
        public int getItemCount() {
            return mDisplayTimes.size();
        }

        public void updateLapTime(String displayTime) {
            if (mDisplayTimes.isEmpty()) {
                return;
            }
            else {
                mDisplayTimes.set(0, displayTime);
                notifyItemChanged(0);
            }
        }

        public void addNewLapTime(String displayTime) {
            mDisplayTimes.add(0, displayTime);
            notifyDataSetChanged();
        }

        public class LapView extends RecyclerView.ViewHolder {

            public LapView(LapElementBinding lapElementBinding) {
                super(lapElementBinding);
            }

            public LapView(View itemView) {
                super(itemView);
            }
        }
    }

    private class TimeIntervalTick implements StopWatch.StopWatchTickEvents {
        @Override
        public void tickEvent(long milliSecondsElapsed) {
            mBinding.tvTime.setText(buildTimeStamp(milliSecondsElapsed));
        }

        private String buildTimeStamp(long milliSecondsElapsed) {
            long totalMilli = milliSecondsElapsed % 1000;
            long totalSeconds = milliSecondsElapsed / 1000L;

            long totalHours = totalSeconds / 3600L;
            long totalMinutes = (totalSeconds - (totalHours * 3600L)) / 60L;
            totalSeconds = (totalSeconds - (totalHours * 3600L) - (totalMinutes * 60L));

            StringBuffer sb = new StringBuffer(getString(R.string.default_time).length());

            if (totalHours == 0) {
                sb.append("00:");
            }
            else if (totalHours < 10) {
                sb.append("0").append(Long.toString(totalHours)).append(':');
            }
            else {
                sb.append(Long.toString(totalHours)).append(':');
            }

            if (totalMinutes > 59) {
                throw new java.lang.IllegalArgumentException(
                        "Total Number of minutes must be less than 60 " + Long.toString(totalMinutes)
                );
            }
            else if (totalMinutes < 10) {
                sb.append('0').append(Long.toString(totalMinutes)).append(':');
            }
            else {
                sb.append(Long.toString(totalMinutes)).append(':');
            }

            if (totalSeconds > 59) {
                throw new java.lang.IllegalArgumentException(
                        "Total Number of seconds must be less than 60 " + Long.toString(totalSeconds)
                );
            }
            else if (totalSeconds < 10) {
                sb.append('0').append(Long.toString(totalSeconds));
            }
            else {
                sb.append(Long.toString(totalSeconds));
            }

            sb.append('.').append(Long.toString(totalMilli));

            return sb.toString();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stop_watch, container, false);

        mBinding.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopWatch.play();
            }
        });

        mBinding.ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopWatch.pause();
            }
        });

        mBinding.ibReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopWatch.play();
            }
        });

        mBinding.ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopWatch.stop();
            }
        });

        mBinding.ibLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        return mBinding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        mStopWatch.stop();
    }
}

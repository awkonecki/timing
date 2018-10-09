package com.nebo.timing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
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
    private List<Long> mLapTimes = new LinkedList<Long>();
    // private long mLastLapTime = 0L;

    private class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapView> {
        private LapElementBinding mBinding = null;

        List<String> mDisplayTimes = new LinkedList<String>();

        @NonNull
        @Override
        public LapView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.lap_element, parent, false);

            mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.lap_element, parent, false);

            return new LapView(mBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull LapView holder, int position) {
            if (position >= 0 && position < mDisplayTimes.size()) {
                holder.bind(mDisplayTimes.size() - position, mDisplayTimes.get(position));
            }
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

        public void clearLapTimes() {
            mDisplayTimes.clear();
            notifyDataSetChanged();
        }

        public class LapView extends RecyclerView.ViewHolder {

            LapElementBinding mBinding = null;

            private LapView(LapElementBinding lapElementBinding) {
                super(lapElementBinding.getRoot());
                mBinding = lapElementBinding;
            }

            public void bind(int lapPosition, String lapData) {
                mBinding.tvLapid.setText("Lap " + Integer.toString(lapPosition));
                mBinding.tvLaptime.setText(lapData);
            }
        }
    }

    private class TimeIntervalTick implements StopWatch.StopWatchTickEvents {
        @Override
        public void tickEvent(long milliSecondsElapsed) {
            Long lastLapTime = 0L;
            String totalTimeDisplay = null, lapTimeDisplay = null;

            if (mLapTimes.size() > 1) {
                lastLapTime = mLapTimes.get(1);
            }

            totalTimeDisplay = buildTimeStamp(milliSecondsElapsed);
            Log.d(TAG, Long.toString(milliSecondsElapsed ) + " " + Long.toString(lastLapTime));
            lapTimeDisplay = buildTimeStamp(milliSecondsElapsed - lastLapTime);
            mBinding.tvTime.setText(totalTimeDisplay);
            ((LapAdapter)(mBinding.rvLaps.getAdapter())).updateLapTime(lapTimeDisplay);
            mLapTimes.set(0, milliSecondsElapsed);
        }

        private String buildTimeStamp(long milliSecondsElapsed) {
            long totalMilli = milliSecondsElapsed % 1000;
            long totalSeconds = milliSecondsElapsed / 1000L;

            long totalHours = totalSeconds / 3600L;
            long totalMinutes = (totalSeconds - (totalHours * 3600L)) / 60L;
            totalSeconds = (totalSeconds - (totalHours * 3600L) - (totalMinutes * 60L));

            StringBuilder sb = new StringBuilder(getString(R.string.default_time).length());

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
                if (mBinding.rvLaps.getAdapter().getItemCount() == 0) {
                    ((LapAdapter) mBinding.rvLaps.getAdapter()).addNewLapTime(getString(R.string.default_time));
                    mLapTimes.add(0, 0L);
                }
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
                mStopWatch.stop();
                mLapTimes.clear();
                ((LapAdapter)mBinding.rvLaps.getAdapter()).clearLapTimes();
                ((LapAdapter) mBinding.rvLaps.getAdapter()).addNewLapTime(getString(R.string.default_time));
                mLapTimes.add(0, 0L);
                mStopWatch.play();
            }
        });

        mBinding.ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStopWatch.stop();
                mLapTimes.clear();
                ((LapAdapter)mBinding.rvLaps.getAdapter()).clearLapTimes();
            }
        });

        mBinding.ibLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LapAdapter)mBinding.rvLaps.getAdapter()).addNewLapTime(getString(R.string.default_time));
                mLapTimes.add(0, 0L);
                // mLapTimes.add(mLastLapTime);
            }
        });

        mBinding.rvLaps.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mBinding.rvLaps.setHasFixedSize(true);
        mBinding.rvLaps.setAdapter(new LapAdapter());
        return mBinding.getRoot();
    }

    @Override
    public void onStop() {
        super.onStop();
        mStopWatch.stop();
    }
}

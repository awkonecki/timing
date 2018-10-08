package com.nebo.timing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.R;

import com.nebo.timing.data.CountUpTimer;
import com.nebo.timing.data.StopWatch;
import com.nebo.timing.databinding.FragmentStopWatchBinding;

public class StopWatchFragment extends Fragment {
    private static final String TAG = "StopWatchFragment";
    private FragmentStopWatchBinding mBinding = null;
    private StopWatch mStopWatch = new StopWatch(5000, 2000, new TimeIntervalTick());

    private class TimeIntervalTick implements StopWatch.StopWatchTickEvents {
        @Override
        public void tickEvent(long milliSecondsElapsed) {
            mBinding.tvTime.setText(buildTimeStamp(milliSecondsElapsed));
        }

        private String buildTimeStamp(long milliSecondsElapsed) {
            return "00:00:00";
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

        mBinding.ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mBinding.getRoot();
    }
}

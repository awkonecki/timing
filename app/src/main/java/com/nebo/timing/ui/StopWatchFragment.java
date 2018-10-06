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
import com.nebo.timing.databinding.FragmentStopWatchBinding;

public class StopWatchFragment extends Fragment implements CountUpTimer.OnTimerEvents {
    private static final String TAG = "StopWatchFragment";
    FragmentStopWatchBinding mBinding = null;
    CountUpTimer mCountUpTimer = null;
    int count = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_stop_watch, container, false);

        mBinding.ibPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBinding.ibPlay.setEnabled(false);
                mCountUpTimer = new CountUpTimer(1000L, 1000L, StopWatchFragment.this);
            }
        });

        mBinding.ibPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBinding.ibReplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBinding.ibStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        mBinding.ibSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mBinding.getRoot();
    }

    @Override
    public void onTick(long millisUntilFinished) {}

    @Override
    public void onFinish() {
        count++;
        Log.d(TAG, "onFinish Callback " + Integer.toString(count));
        mBinding.tvTime.setText(Integer.toString(count));
    }
}

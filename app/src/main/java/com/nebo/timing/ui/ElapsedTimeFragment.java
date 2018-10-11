package com.nebo.timing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.R;
import com.nebo.timing.data.StopWatch;
import com.nebo.timing.databinding.FragmentElapsedTimeBinding;

public class ElapsedTimeFragment extends Fragment {
    private FragmentElapsedTimeBinding mBinding = null;
    private String mStartingTime = StopWatch.buildTimeStamp(0L);

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_elapsed_time,
                container,
                false);

        // Set the tv to the default
        mBinding.tvElapsedTime.setText(mStartingTime);

        return mBinding.getRoot();
    }

    public void updateElapsedTime(String time) {
        if (mBinding != null) {
            mBinding.tvElapsedTime.setText(time);
        }
        else {
            mStartingTime = time;
        }
    }
}

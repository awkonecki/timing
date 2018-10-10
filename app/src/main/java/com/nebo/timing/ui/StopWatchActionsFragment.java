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
import com.nebo.timing.databinding.FragmentStopwatchActionsBinding;

public class StopWatchActionsFragment extends Fragment {
    private FragmentStopwatchActionsBinding mBinding = null;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_stopwatch_actions,
                container,
                false);
        return mBinding.getRoot();
    }
}

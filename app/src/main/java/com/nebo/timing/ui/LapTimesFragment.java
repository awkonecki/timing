package com.nebo.timing.ui;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.R;
import com.nebo.timing.databinding.FragmentLapTimesBinding;
import com.nebo.timing.databinding.LapElementBinding;

import java.util.LinkedList;
import java.util.List;

public class LapTimesFragment extends Fragment {
    private FragmentLapTimesBinding mBinding = null;
    private LapAdapter mLapAdapter = new LapAdapter();

    private class LapAdapter extends RecyclerView.Adapter<LapTimesFragment.LapAdapter.LapView> {
        private LapElementBinding mBinding = null;

        List<String> mDisplayTimes = new LinkedList<String>();

        public LapAdapter() {}

        public LapAdapter(List<String> displayTimes) {
            for (String time : displayTimes) {
                mDisplayTimes.add(time);
            }
        }

        @NonNull
        @Override
        public LapTimesFragment.LapAdapter.LapView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.lap_element, parent, false);

            mBinding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.lap_element, parent, false);

            return new LapTimesFragment.LapAdapter.LapView(mBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull LapTimesFragment.LapAdapter.LapView holder, int position) {
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
                mDisplayTimes.add(displayTime);
                notifyDataSetChanged();
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
                mBinding.tvLapid.setText(String.format(getString(R.string.lap_format),
                        getString(R.string.label_lap),
                        Integer.toString(lapPosition)));
                mBinding.tvLaptime.setText(lapData);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        mBinding = DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_lap_times,
                container,
                false);

        // Create a linear layout manager for the recyclerview widget
        mBinding.rvLapTimes.setLayoutManager(
                new LinearLayoutManager(
                        getContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
        mBinding.rvLapTimes.setHasFixedSize(true);
        mBinding.rvLapTimes.setAdapter(mLapAdapter);

        return mBinding.getRoot();
    }

    public void updateLapTime(String lapTimeDisplay) {
        if (mBinding != null) {
            ((LapAdapter) mBinding.rvLapTimes.getAdapter()).updateLapTime(lapTimeDisplay);
        }
    }

    public void resetLapTimes() {
        if (mBinding != null) {
            ((LapAdapter) mBinding.rvLapTimes.getAdapter()).clearLapTimes();
        }
    }

    public void newLapTime(String lapTimeDisplay) {
        if (mBinding != null) {
            ((LapAdapter) mBinding.rvLapTimes.getAdapter()).addNewLapTime(lapTimeDisplay);
        }
    }

    public void initializeLaps(List<String> times) {
        if (mBinding == null) {
            mLapAdapter = new LapAdapter(times);
        }
        else {
            ((LapAdapter) mBinding.rvLapTimes.getAdapter()).clearLapTimes();
            for (String time : times) {
                updateLapTime(time);
            }
        }
    }
}

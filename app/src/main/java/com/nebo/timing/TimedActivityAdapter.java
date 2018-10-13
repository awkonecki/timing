package com.nebo.timing;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.TimedActivityElementBinding;

import java.util.ArrayList;
import java.util.List;

public class TimedActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<TimedActivity> timedActivities = new ArrayList<>();
    private Context mContext = null;

    public TimedActivityAdapter(Context context, List<TimedActivity> timedActivities) {
        timedActivities = new ArrayList<>(timedActivities);
        mContext = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TimedActivityElementBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.timed_activity_element,
                parent,
                false);

        return new TimedActivityViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < timedActivities.size()) {
            ((TimedActivityViewHolder)holder).bind(timedActivities.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return timedActivities.size();
    }

    private class TimedActivityViewHolder extends RecyclerView.ViewHolder {
        private TimedActivityElementBinding mBinding = null;

        public TimedActivityViewHolder(@NonNull TimedActivityElementBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(TimedActivity timedActivity) {

        }
    }
}

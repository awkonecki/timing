package com.nebo.timing;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.TimedActivityElementBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    //**********************************************************************************************
    // PRIVATE CLASS MEMBERS
    //**********************************************************************************************
    private final Context mContext;
    private List<TimedActivity> mActivities = new ArrayList<>();
    private SparseArray<String> mActivityKeyIndex = new SparseArray<>();

    private final OnActivitySelection mCallback;

    private class SelectActivityView extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TimedActivityElementBinding elementBinding;

        public SelectActivityView(TimedActivityElementBinding binding) {
            super(binding.getRoot());
            elementBinding = binding;
            elementBinding.tvTimedActivityTime.setVisibility(View.GONE);
            itemView.setOnClickListener(this);
        }

        public void bind(TimedActivity timedActivity) {
            elementBinding.tvTimedActivityCategory.setText(timedActivity.getCategory());
            elementBinding.tvTimedActivityName.setText(timedActivity.getName());
        }

        @Override
        public void onClick(View v) {
            v.setBackgroundColor(mContext.getColor(R.color.colorAccent));
            mCallback.selectedActivity(
                    getAdapterPosition(),
                    mActivityKeyIndex.get(getAdapterPosition(), null),
                    mActivities.get(getAdapterPosition()));
        }
    }

    //**********************************************************************************************
    // PUBLIC CLASS MEMBERS
    //**********************************************************************************************
    public interface OnActivitySelection {
        void selectedActivity(int position, String key, TimedActivity timedActivity);
    }

    public SelectActivityAdapter(Context context, @NonNull OnActivitySelection interfaceCallback) {
        mContext = context;
        mCallback = interfaceCallback;
    }

    public void addActivity(String key, TimedActivity timedActivity) {
        mActivities.add(timedActivity);
        mActivityKeyIndex.put(mActivities.size() - 1, key);
    }

    public void unSelect(View view) {
        if (view != null) {
            view.setBackgroundColor(mContext.getColor(R.color.colorWhite));
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TimedActivityElementBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(mContext),
                R.layout.timed_activity_element,
                parent,
                false);

        return new SelectActivityView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= 0 && position < mActivities.size()) {
            ((SelectActivityView)holder).bind(mActivities.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return mActivities.size();
    }
}
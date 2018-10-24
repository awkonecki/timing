package com.nebo.timing;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.TimedActivityElementBinding;

public class SelectActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private View mSelectedView = null;
    private final Context mContext;

    public SelectActivityAdapter(Context context) {
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

        return new SelectActivityView(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        /*
        if (position >= 0 && position < mActivities.size()) {
            ((SelectActivityView)holder).bind(mActivities.get(position));
        }
        */
    }

    @Override
    public int getItemCount() {
        return 0;
        //return mActivities.size();
    }

    public void unSelect() {
        if (mSelectedView != null) {
            mSelectedView.setBackgroundColor(0xFFFFFF);
            // mSelectedActivity = null;
            mSelectedView = null;
        }
    }

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
            int index = getAdapterPosition();

            if (v == mSelectedView) {
                mSelectedView.setBackgroundColor(0xFFFFFF);
                // mSelectedActivity = null;
                mSelectedView = null;
            }
            else {
                /*
                if (index >= 0 && index < mActivities.size()) {
                    mSelectedActivity = mActivities.get(index);
                }
                */
                if (mSelectedView != null) {
                    mSelectedView.setBackgroundColor(0xFFFFFF);
                }

                v.setBackgroundColor(mContext.getColor(R.color.colorAccent));
                mSelectedView = v;

                // mBinding.tbUseNewActivityToggle.setChecked(false);
            }
        }
    }
}
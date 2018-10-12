package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.data.ActivitySession;
import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivityTimedActivityDetailBinding;
import com.nebo.timing.databinding.ActivitySessionElementBinding;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TimedActivityDetailActivity extends AppCompatActivity {
    private ActivityTimedActivityDetailBinding mBinding = null;
    private TimedActivity mTimedActivity = null;

    private class ActivitySessionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        List<ActivitySession> sessionList = new ArrayList<>();

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ActivitySessionElementBinding binding = DataBindingUtil.inflate(
                    getLayoutInflater(),
                    R.layout.activity_session_element,
                    parent,
                    false);
            return new ActivitySessionViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (position >= 0) {
                ((ActivitySessionViewHolder) (holder)).bind(sessionList.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return sessionList.size();
        }

        private class ActivitySessionViewHolder extends RecyclerView.ViewHolder {

            private ActivitySessionElementBinding mBinding = null;

            public ActivitySessionViewHolder(ActivitySessionElementBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void bind(ActivitySession session) {
                mBinding.tvActivitySessionDate.setText("Date");
                mBinding.tvActivitySessionName.setText(session.getName());
                mBinding.tvLabels.setText("Labels");
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the databinding for referencing widgets within the UI.
        mBinding = DataBindingUtil.setContentView(
                this,
                R.layout.activity_timed_activity_detail);

        if (savedInstanceState != null) {
            // use the instance data
            mTimedActivity = savedInstanceState.getParcelable(
                    getString(R.string.key_timed_activity));
        }
        else {
            // use the intent passed data, assume for now the data is passed in via the intent.
            mTimedActivity = getIntent().getParcelableExtra(getString(R.string.key_timed_activity));
        }


    }
}

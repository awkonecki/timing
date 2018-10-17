package com.nebo.timing;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;
import com.nebo.timing.databinding.TimedActivityElementBinding;

import java.util.ArrayList;
import java.util.List;

public class SelectActivity extends AppCompatActivity {

    private ActivitySelectActivityBinding mBinding = null;
    private List<TimedActivity> mActivities = new ArrayList<>();
    private TimedActivity mSelectedActivity = null;

    private class SelectActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TimedActivityElementBinding binding = DataBindingUtil.inflate(
                    getLayoutInflater(),
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

        private class SelectActivityView extends RecyclerView.ViewHolder {
            private final TimedActivityElementBinding elementBinding;

            public SelectActivityView(TimedActivityElementBinding binding) {
                super(binding.getRoot());
                elementBinding = binding;
                elementBinding.tvTimedActivityTime.setVisibility(View.GONE);
            }

            public void bind(TimedActivity timedActivity) {
                elementBinding.tvTimedActivityCategory.setText(timedActivity.getCategory());
                elementBinding.tvTimedActivityName.setText(timedActivity.getName());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_select_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item != null) {
            switch (item.getItemId()) {
                case R.id.mi_save_selected_activity:
                    // TODO @awkonecki return status and selected activity.
                    finish();
                    break;
            }
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_activity);

        if (savedInstanceState != null) {
            mActivities = savedInstanceState.getParcelableArrayList(
                    getString(R.string.key_timed_activities));
            mSelectedActivity = savedInstanceState.getParcelable(
                    getString(R.string.key_timed_activity));
        }
        else {
            // process intent data.
            Bundle intentBundle = getIntent().getExtras();

            if (intentBundle != null) {
                mActivities = intentBundle.getParcelableArrayList(
                        getString(R.string.key_timed_activities));
            }
        }

        // setup the view's recyclerview widget
        mBinding.rvSaveTimeActivities.setAdapter(new SelectActivityAdapter());
        mBinding.rvSaveTimeActivities.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false));
        mBinding.rvSaveTimeActivities.setHasFixedSize(true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(
                getString(R.string.key_timed_activities), (ArrayList<TimedActivity>) mActivities);
    }
}

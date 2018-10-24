package com.nebo.timing;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Slide;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.nebo.timing.data.TimedActivity;
import com.nebo.timing.databinding.ActivitySelectActivityBinding;
import com.nebo.timing.databinding.TimedActivityElementBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectActivity extends AppCompatActivity {

    private ActivitySelectActivityBinding mBinding = null;
    private List<TimedActivity> mActivities = new ArrayList<>();
    private TimedActivity mSelectedActivity = null;
    private HashMap<String, TimedActivity> mMapOfActivities = new HashMap<>();

    private class SelectActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private View mSelectedView = null;

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

        public void unSelect() {
            if (mSelectedView != null) {
                mSelectedView.setBackgroundColor(0xFFFFFF);
                mSelectedActivity = null;
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
                    mSelectedActivity = null;
                    mSelectedView = null;
                }
                else {
                    if (index >= 0 && index < mActivities.size()) {
                        mSelectedActivity = mActivities.get(index);
                    }

                    if (mSelectedView != null) {
                        mSelectedView.setBackgroundColor(0xFFFFFF);
                    }

                    v.setBackgroundColor(getColor(R.color.colorAccent));
                    mSelectedView = v;

                    mBinding.tbUseNewActivityToggle.setChecked(false);
                }
            }
        }
    }

    private void saveSelectedAndFinish() {
        Bundle bundle = new Bundle();
        if (mSelectedActivity != null) {
            if (mBinding.tbUseNewActivityToggle.isChecked()) {
                mSelectedActivity = new TimedActivity(
                        mBinding.etNewActivityName.getText().toString(),
                        mBinding.etNewActivityCategory.getText().toString(),
                        null);
            }

            bundle.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
        }

        Intent intent = new Intent();
        intent.putExtras(bundle);
        setResult(RESULT_OK, intent);
        finish();
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
                    saveSelectedAndFinish();
                    break;
            }
        }

        return true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setEnterTransition(new Slide());
        getWindow().setExitTransition(new Slide());

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_select_activity);
        setSupportActionBar(mBinding.tbSelectActivityToolbar);
        
        mBinding.tbUseNewActivityToggle.setChecked(false);

        if (savedInstanceState != null) {
            mActivities = savedInstanceState.getParcelableArrayList(
                    getString(R.string.key_timed_activities));
            mBinding.etNewActivityName.setText(
                    savedInstanceState.getString(getString(R.string.key_new_name_string)));
            mBinding.etNewActivityCategory.setText(
                    savedInstanceState.getString(getString(R.string.key_new_category_string)));
        }
        else {
            // process intent data.
            Bundle intentBundle = getIntent().getExtras();

            if (intentBundle != null) {
                mActivities = intentBundle.getParcelableArrayList(
                        getString(R.string.key_timed_activities));
            }
        }

        // Setup the hash set to aid in making sure unique activities by name.
        for (TimedActivity activity : mActivities) {
            mMapOfActivities.put(activity.getName(), activity);
        }

        // setup the view's recyclerview widget
        mBinding.rvSaveTimeActivities.setAdapter(new SelectActivityAdapter());
        mBinding.rvSaveTimeActivities.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.VERTICAL,
                        false));
        mBinding.rvSaveTimeActivities.setHasFixedSize(true);

        mBinding.tbUseNewActivityToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBinding.tbUseNewActivityToggle.isChecked()) {
                    ((SelectActivityAdapter) mBinding.rvSaveTimeActivities.getAdapter()).unSelect();
                    String name = mBinding.etNewActivityName.getText().toString();
                    String category = mBinding.etNewActivityCategory.getText().toString();

                    mSelectedActivity = mMapOfActivities.get(name);

                    if (mSelectedActivity == null) {
                        mSelectedActivity = new TimedActivity(name, category, null);
                    }
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(
                getString(R.string.key_timed_activities), (ArrayList<TimedActivity>) mActivities);
        outState.putParcelable(getString(R.string.key_selected_activity), mSelectedActivity);
        outState.putString(
                getString(R.string.key_new_name_string),
                mBinding.etNewActivityName.getText().toString());
        outState.putString(
                getString(R.string.key_new_category_string),
                mBinding.etNewActivityCategory.getText().toString());

    }
}

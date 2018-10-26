package com.nebo.timing.async;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nebo.timing.R;
import com.nebo.timing.data.StopWatch;
import com.nebo.timing.data.TimedActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class WidgetServiceListView extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TimedActivityRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    private class TimedActivityRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory,
        ValueEventListener {
        private Context mContext;
        private int mAppWidgetId;
        private Query mQuery = null;
        private List<TimedActivity> mTimedActivities = new ArrayList<>();
        private HashMap<String, Integer> mActivityKeysToIndex = new HashMap<>();

        public TimedActivityRemoteViewsFactory(Context context, Intent intent) {
            mContext = context;
            mAppWidgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            String userId = null;

            if (getSharedPreferences(getString(R.string.sp_timer), MODE_PRIVATE) != null) {
                userId = getSharedPreferences(getString(R.string.sp_timer), MODE_PRIVATE)
                        .getString(getString(R.string.key_user_uid), null);
            }

            if (userId != null) {
                mQuery = FirebaseDatabase.getInstance().getReference()
                        .child(getString(R.string.firebase_database_timed_activities))
                        .orderByChild(getString(R.string.firebase_database_activity_user))
                        .equalTo(userId);
                mQuery.addValueEventListener(this);
            }
        }

        @Override
        public void onDataSetChanged() {}

        @Override
        public void onDestroy() {
            if (mQuery != null) {
                mQuery.removeEventListener(this);
                mQuery = null;
            }
        }

        @Override
        public int getCount() {
            return mTimedActivities.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.timed_activity_element);
            rv.setTextViewText(R.id.tv_timed_activity_name, mTimedActivities.get(position).getName());
            rv.setTextViewText(R.id.tv_timed_activity_category, mTimedActivities.get(position).getCategory());
            rv.setTextViewText(R.id.tv_timed_activity_time, StopWatch.buildTimeStamp(mTimedActivities.get(position).getTotalElapsedTime()));
            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                TimedActivity timedActivity = snapshot.getValue(TimedActivity.class);
                if (timedActivity != null) {
                    if (mActivityKeysToIndex.containsKey(snapshot.getKey())) {
                        mTimedActivities.set(mActivityKeysToIndex.get(snapshot.getKey()), timedActivity);
                    }
                    else {
                        mTimedActivities.add(timedActivity);
                        mActivityKeysToIndex.put(snapshot.getKey(), mTimedActivities.size() - 1);
                    }
                    AppWidgetManager
                            .getInstance(mContext)
                            .notifyAppWidgetViewDataChanged(mAppWidgetId, 0);
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {}
    }
}

package com.nebo.timing;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.nebo.timing.async.WidgetServiceListView;

/**
 * Implementation of App Widget functionality.
 */
public class TimerWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_widget);
        // views.setTextViewText(R.id.appwidget_text, widgetText);

        // Setup list of TimedActivities
        Intent listViewWidgetService = new Intent(context, WidgetServiceListView.class);
        views.setRemoteAdapter(R.id.lv_widget_timed_activities, listViewWidgetService);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
                // intent is an update intent for the appwidget manager.
                int [] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                        new ComponentName(context, TimerWidget.class));

                for (int appWidgetId : widgetIds) {
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.timer_widget);
                    AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
                }

                AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(
                        widgetIds,
                        R.id.lv_widget_timed_activities);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {}

    @Override
    public void onDisabled(Context context) {}

    // Allow the app to inform the widget to update.
    public static void sendRefreshBoardcast(Context context) {
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.setComponent(new ComponentName(context, TimerWidget.class));
        context.sendBroadcast(intent);
    }
}


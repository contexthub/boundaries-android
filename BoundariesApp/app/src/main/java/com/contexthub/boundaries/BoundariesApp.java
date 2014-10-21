package com.contexthub.boundaries;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.SensorPipelineEvent;
import com.chaione.contexthub.sdk.SensorPipelineListener;

/**
 * Created by andy on 10/20/14.
 */
public class BoundariesApp extends Application implements SensorPipelineListener {

    private static Context instance;

    public BoundariesApp() {
        instance = this;
    }

    public static Context getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ContextHub.init(this, "YOUR-APP-ID-HERE");
        ContextHub.getInstance().addSensorPipelineListener(this);
    }

    @Override
    public void onEventReceived(SensorPipelineEvent event) {
        Log.d(getClass().getName(), event.getEventDetails().toString());
    }

    @Override
    public boolean shouldPostEvent(SensorPipelineEvent sensorPipelineEvent) {
        return true;
    }

    @Override
    public void onBeforeEventPosted(SensorPipelineEvent sensorPipelineEvent) {

    }

    @Override
    public void onEventPosted(SensorPipelineEvent sensorPipelineEvent) {

    }
}

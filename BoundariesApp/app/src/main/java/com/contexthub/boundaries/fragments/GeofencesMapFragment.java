package com.contexthub.boundaries.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.GeofenceProxy;
import com.chaione.contexthub.sdk.SensorPipelineEvent;
import com.chaione.contexthub.sdk.SensorPipelineListener;
import com.chaione.contexthub.sdk.callbacks.Callback;
import com.chaione.contexthub.sdk.dev.MockLocationProvider;
import com.chaione.contexthub.sdk.model.Geofence;
import com.contexthub.boundaries.BoundariesApp;
import com.contexthub.boundaries.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

/**
 * Created by andy on 10/24/14.
 */
public class GeofencesMapFragment extends SupportMapFragment implements SensorPipelineListener, Callback<List<Geofence>>, GoogleMap.OnMapClickListener {

    private static final int ZOOM_LEVEL = 13;
    private static final LatLng LOCATION_CHAIONE_WOODWAY = new LatLng(29.763553,-95.461784);

    private MockLocationProvider mockLocationProvider;
    private Marker currentLocation;
    private GeofenceProxy proxy = new GeofenceProxy();
    private HashMap<Long, Circle> circlesMap = new HashMap<Long, Circle>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mockLocationProvider = new MockLocationProvider(getActivity());
        setCurrentLocation(LOCATION_CHAIONE_WOODWAY);

        getMap().setOnMapClickListener(this);

        proxy.listGeofences(this);
    }

    @Override
    public void onSuccess(List<Geofence> result) {
        for(Geofence geofence: result) {
            drawGeofence(geofence);
        }
    }

    private void drawGeofence(Geofence geofence) {
        LatLng coordinates = new LatLng(geofence.getLatitude(), geofence.getLongitude());
        Circle circle = getMap().addCircle(new CircleOptions().center(coordinates)
                .radius(geofence.getRadius()).strokeWidth(0)
                .fillColor(getResources().getColor(R.color.circle_fill)));
        circlesMap.put(geofence.getId(), circle);
    }

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        ContextHub.getInstance().addSensorPipelineListener(this);
        ContextHub.getInstance().getBus().register(this); // register to use ContextHub's instance of Otto event bus
    }

    @Override
    public void onPause() {
        super.onPause();
        ContextHub.getInstance().removeSensorPipelineListener(this);
        ContextHub.getInstance().getBus().unregister(this);
    }
    @Override
    public void onEventReceived(final SensorPipelineEvent event) {
        if(event.getName().equals("location_changed")) {
            handleLocationChange(event);
        }

        if(event.getName().equals("geofence_in")) {
            handleGeofenceIn(event);
        }

        /* Since some context events may be received on a background thread, use a handler to ensure
           the toast message is shown on the UI thread */
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BoundariesApp.getInstance(), event.getEventDetails().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean shouldPostEvent(SensorPipelineEvent event) {
        return true;
    }

    @Override
    public void onBeforeEventPosted(SensorPipelineEvent event) {

    }

    @Override
    public void onEventPosted(SensorPipelineEvent event) {

    }

    /**
     * Extract the coordinates from the context event and update the current location pin on the map
     * @param event the sensor pipeline event
     */
    private void handleLocationChange(SensorPipelineEvent event) {
        try {
            JSONObject data = event.getEventDetails().getJSONObject("data");
            double latitude = data.getDouble("latitude");
            double longitude = data.getDouble("longitude");
            setCurrentLocation(new LatLng(latitude, longitude));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract the name from the context event and update the current location pin on the map
     * @param event the sensor pipeline event
     */
    private void handleGeofenceIn(SensorPipelineEvent event) {
        try {
            JSONObject fence = event.getEventDetails().getJSONObject("data").getJSONObject("fence");
            final String name = fence.getString("name");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (currentLocation != null) {
                        currentLocation.setTitle(name);
                    }
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentLocation(LatLng coordinates) {
        if(currentLocation != null) currentLocation.remove();
        currentLocation = getMap().addMarker(new MarkerOptions().position(coordinates).title("Current Location"));
        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(coordinates, ZOOM_LEVEL));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        setCurrentLocation(latLng);
        mockLocationProvider.setMockLocation(latLng);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_geofences_map, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_geofence:
                GeofenceCreateDialogFragment dialogFragment =
                        GeofenceCreateDialogFragment.newInstance(currentLocation.getPosition());
                dialogFragment.show(getFragmentManager(), "create_geofence_dialog");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Draws the created geofence on the map and sets the name of the location pin
     * @param event
     * @see {@link GeofenceCreateDialogFragment.GeofenceCreatedEvent}
     */
    @Subscribe
    public void onGeofenceCreatedEvent(GeofenceCreateDialogFragment.GeofenceCreatedEvent event) {
        drawGeofence(event.getGeofence());
        currentLocation.setTitle(event.getGeofence().getName());
    }

    /**
     * Removes the existing geofence from the map and redraws it
     * @param event
     * @see {@link GeofenceUpdateDialogFragment.GeofenceUpdatedEvent}
     */
    @Subscribe
    public void onGeofenceUpdatedEvent(GeofenceUpdateDialogFragment.GeofenceUpdatedEvent event) {
        Geofence geofence = event.getGeofence();
        Circle existingCircle = circlesMap.get(geofence.getId());
        existingCircle.remove();
        circlesMap.remove(existingCircle);
        drawGeofence(geofence);
    }

    /**
     * Removes the deleted geofence from the map
     * @param event
     * @see {@link GeofenceListFragment.GeofenceDeletedEvent}
     */
    @Subscribe
    public void onGeofenceDeletedEvent(GeofenceListFragment.GeofenceDeletedEvent event){
        long id = event.getId();
        Circle existingCircle = circlesMap.get(id);
        existingCircle.remove();
        circlesMap.remove(id);
        if(currentLocation != null) {
            currentLocation.setTitle("Current Location");
        }
    }
}

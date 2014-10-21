package com.contexthub.boundaries.fragments;

import android.os.Bundle;
import android.view.View;

import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.model.Geofence;
import com.contexthub.boundaries.R;
import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;

/**
 * Dialog fragment for creating a geofence
 */
public class GeofenceCreateDialogFragment extends GeofenceEditDialogFragment {

    private static final int GEOFENCE_RADIUS = 500;

    private static final String ARG_COORDINATES = "coordinates";

    public static GeofenceCreateDialogFragment newInstance(LatLng coordinates) {
        GeofenceCreateDialogFragment fragment = new GeofenceCreateDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_COORDINATES, coordinates);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onClick(View v) {
        if(!validGeofence()) return;
        showProgressDialog(R.string.creating_geofence);
        LatLng coordinates = (LatLng) getArguments().getParcelable(ARG_COORDINATES);
        proxy.createGeofence(geofenceName.getText().toString(), coordinates.latitude, coordinates.longitude,
                GEOFENCE_RADIUS, Arrays.asList("geofence-tag"), this);
    }

    @Override
    protected void postGeofenceChangedEvent(Geofence geofence) {
        ContextHub.getInstance().getBus().post(new GeofenceCreatedEvent(geofence));
    }

    public class GeofenceCreatedEvent {
        private Geofence geofence;

        public GeofenceCreatedEvent(Geofence geofence) {
            this.geofence = geofence;
        }

        public Geofence getGeofence() {
            return geofence;
        }
    }
}

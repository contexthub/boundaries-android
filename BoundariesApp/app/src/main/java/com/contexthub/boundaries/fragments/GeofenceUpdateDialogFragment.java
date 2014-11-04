package com.contexthub.boundaries.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;

import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.model.Geofence;
import com.contexthub.boundaries.R;

/**
 * Dialog fragment for updating a geofence
 */
public class GeofenceUpdateDialogFragment extends GeofenceEditDialogFragment {

    private static final String ARG_GEOFENCE = "geofence";

    private Geofence geofence;

    public static GeofenceUpdateDialogFragment newInstance(Geofence geofence) {
        GeofenceUpdateDialogFragment fragment = new GeofenceUpdateDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_GEOFENCE, geofence);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        geofence = getArguments().getParcelable(ARG_GEOFENCE);
        geofenceName.setText(geofence.getName());
        return dialog;
    }

    @Override
    public void onClick(View v) {
        if(!validGeofence()) return;
        showProgressDialog(R.string.updating_geofence);
        geofence.setName(geofenceName.getText().toString());
        proxy.updateGeofence(geofence.getId(), geofence, this);
    }

    @Override
    protected void postGeofenceChangedEvent(Geofence geofence) {
        ContextHub.getInstance().getBus().post(new GeofenceUpdatedEvent(geofence));
    }

    public class GeofenceUpdatedEvent {
        private Geofence geofence;

        public GeofenceUpdatedEvent(Geofence geofence) {
            this.geofence = geofence;
        }

        public Geofence getGeofence() {
            return geofence;
        }
    }
}

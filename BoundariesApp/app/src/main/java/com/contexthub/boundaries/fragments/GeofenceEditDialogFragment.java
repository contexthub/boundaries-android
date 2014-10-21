package com.contexthub.boundaries.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.chaione.contexthub.sdk.GeofenceProxy;
import com.chaione.contexthub.sdk.LocationService;
import com.chaione.contexthub.sdk.callbacks.Callback;
import com.chaione.contexthub.sdk.model.Geofence;
import com.contexthub.boundaries.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Abstract base class for dialog fragment to create and update geofences
 */
public abstract class GeofenceEditDialogFragment extends DialogFragment implements Callback<Geofence>, View.OnClickListener {

    @InjectView(R.id.geofence_name) protected EditText geofenceName;

    protected GeofenceProxy proxy = new GeofenceProxy();

    private ProgressDialog progressDialog;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_geofence_edit, null);
        ButterKnife.inject(this, view);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_geofence)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null) // overridden below to avoid default behavior of dismissing on click
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

        // Override positive button click listener so we can validate input
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(GeofenceEditDialogFragment.this);
            }
        });
        return dialog;
    }

    protected boolean validGeofence() {
        geofenceName.setError(null);

        if(geofenceName.getText().toString().isEmpty()) {
            geofenceName.setError(getString(R.string.geofence_name_required));
            return false;
        }

        return true;
    }

    protected void showProgressDialog(int messageResource) {
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(messageResource));
        progressDialog.setIndeterminate(true);
        progressDialog.show();
    }

    @Override
    public void onSuccess(Geofence geofence) {
        if(progressDialog != null) progressDialog.dismiss();

        /* If you do not have push properly set up, you need to explicitly call synchronize on
           LocationServices so it will generate events for this geofence */
        LocationService.getInstance().synchronize();

        postGeofenceChangedEvent(geofence);
        dismiss();
    }

    protected abstract void postGeofenceChangedEvent(Geofence geofence);

    @Override
    public void onFailure(Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }
}

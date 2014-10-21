package com.contexthub.boundaries.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaione.contexthub.sdk.ContextHub;
import com.chaione.contexthub.sdk.GeofenceProxy;
import com.chaione.contexthub.sdk.LocationService;
import com.chaione.contexthub.sdk.callbacks.Callback;
import com.chaione.contexthub.sdk.model.Geofence;
import com.contexthub.boundaries.R;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Fragment for displaying a list of geofences
 */
public class GeofenceListFragment extends Fragment implements Callback<List<Geofence>>, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    @InjectView(android.R.id.list) ListView list;
    @InjectView(android.R.id.empty) TextView empty;

    GeofenceAdapter adapter;
    GeofenceProxy proxy = new GeofenceProxy();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        ContextHub.getInstance().getBus().register(this); // register to use ContextHub's instance of Otto event bus
    }

    @Override
    public void onPause() {
        super.onPause();
        ContextHub.getInstance().getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_geofence_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        adapter = new GeofenceAdapter(getActivity(), new ArrayList<Geofence>());
        list.setAdapter(adapter);
        list.setEmptyView(empty);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        loadItems();
    }

    private void loadItems() {
        proxy.listGeofences(this);
    }

    /**
     * Called after successfully fetching geofences from ContextHub
     * @param geofences the resulting geofences
     */
    @Override
    public void onSuccess(List<Geofence> geofences) {
        adapter.clear();
        adapter.addAll(geofences);
        adapter.sort();
    }

    /**
     * Called when an error occurs fetching geofences from ContextHub
     * @param e the exception details
     */
    @Override
    public void onFailure(Exception e) {
        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Geofence geofence = (Geofence) adapterView.getAdapter().getItem(i);
        GeofenceUpdateDialogFragment dialogFragment = GeofenceUpdateDialogFragment.newInstance(geofence);
        dialogFragment.show(getFragmentManager(), "update_geofence_dialog");
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        Geofence geofence = (Geofence) adapterView.getAdapter().getItem(i);
        showDeleteConfirmDialog(geofence);
        return true;
    }

    private void showDeleteConfirmDialog(final Geofence geofence) {
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.confirm_delete_title)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        deleteGeofence(geofence);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        dialog.show();
    }

    private void deleteGeofence(final Geofence geofence) {
        getActivity().setProgressBarIndeterminateVisibility(true);

        // Submit a request to ContextHub to delete the specified geofence
        proxy.deleteGeofence(geofence.getId(), new Callback<Object>() {
            /**
             * Called after successfully deleting a geofence from ContextHub
             * @param o
             */
            @Override
            public void onSuccess(Object o) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                ContextHub.getInstance().getBus().post(new GeofenceDeletedEvent(geofence.getId()));

                /* If you do not have push properly set up, you need to explicitly call synchronize on
                   LocationServices so it will stop generating events for this geofence */
                LocationService.getInstance().synchronize();

                Toast.makeText(getActivity(), R.string.geofence_deleted, Toast.LENGTH_SHORT).show();
                loadItems();
            }

            /**
             * Called when an error occurs deleting a geofence from ContextHub
             * @param e the exception details
             */
            @Override
            public void onFailure(Exception e) {
                getActivity().setProgressBarIndeterminateVisibility(false);
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class GeofenceAdapter extends ArrayAdapter<Geofence> {

        public GeofenceAdapter(Context context, List<Geofence> objects) {
            super(context, -1, objects);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if(convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.geofence_item, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            Geofence geofence = getItem(position);
            holder.name.setText(geofence.getName());
            holder.latitudeLongitudeRadius.setText(getString(R.string.latitude_longitude_radius,
                    String.valueOf(geofence.getLatitude()), String.valueOf(geofence.getLongitude()),
                    String.valueOf(geofence.getRadius())));

            return convertView;
        }

        public void sort() {
            sort(new Comparator<Geofence>() {
                @Override
                public int compare(Geofence geofence1, Geofence geofence2) {
                    return geofence1.getName().toLowerCase().compareTo(geofence2.getName().toLowerCase());
                }
            });
            notifyDataSetChanged();
        }
    }

    class ViewHolder {
        @InjectView(R.id.geofence_name) TextView name;
        @InjectView(R.id.geofence_latitude_longitude_radius) TextView latitudeLongitudeRadius;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    /**
     * Adds the created geofence to the list
     * @param event
     * @see {@link GeofenceCreateDialogFragment.GeofenceCreatedEvent}
     */
    @Subscribe
    public void onGeofenceCreatedEvent(GeofenceCreateDialogFragment.GeofenceCreatedEvent event) {
        adapter.add(event.getGeofence());
        adapter.sort();
    }

    /**
     * Updates the geofence in the list
     * @param event
     * @see {@link GeofenceUpdateDialogFragment.GeofenceUpdatedEvent}
     */
    @Subscribe
    public void onGeofenceUpdatedEvent(GeofenceUpdateDialogFragment.GeofenceUpdatedEvent event) {
        Geofence updatedGeofence = event.getGeofence();
        for (int i = 0; i < adapter.getCount(); i++) {
            Geofence geofence = adapter.getItem(i);
            if(updatedGeofence.getId() == geofence.getId()) {
                adapter.remove(geofence);
                break;
            }
        }
        adapter.add(updatedGeofence);
        adapter.sort();
    }

    public class GeofenceDeletedEvent {
        private long id;

        public GeofenceDeletedEvent(long id) {
            this.id = id;
        }

        public long getId() {
            return id;
        }
    }
}

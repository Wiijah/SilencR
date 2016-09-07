package Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Pierre Rivierre on 8/31/2016.
 */
public class Geofencing implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {
    GoogleApiClient googleApiClient;
    Context context;
    List<Geofence> addList;
    List<Intent> intentList;
    List<Long> removeList;

    public Geofencing(Context context) {
        this.context = context;
        buildGoogleApiClient();

        addList = new ArrayList<Geofence>();
        removeList = new ArrayList<Long>();
        intentList = new ArrayList<Intent>();
    }

    public void addGeofence(Geofence geo, Intent intent) {

//        LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(geo),
//                getGeofencePendingIntent()).setResultCallback(this);

        addList.add(geo);
        intentList.add(intent);
        addAndRemove();
    }

    private PendingIntent getGeofencePendingIntent(Intent intent) {
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest(Geofence geo) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(geo);
        return builder.build();
    }

    public void disconnect() {
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    public void removeGeofence(long geofenceId) {
//        return LocationServices.GeofencingApi.removeGeofences(googleApiClient,
//                        Arrays.asList(new String[]{Long.toString(geofenceId)}));

        removeList.add(geofenceId);
        addAndRemove();
    }

    private void addAndRemove() {
        if (!googleApiClient.isConnected()) {
            googleApiClient.connect();
            return;
        }

        int count = 0;
        for (Geofence geo : addList) {
            LocationServices.GeofencingApi.addGeofences(googleApiClient, getGeofencingRequest(geo),
                getGeofencePendingIntent(intentList.get(count))).setResultCallback(this);
            count++;
        }

        for (long id : removeList) {
            LocationServices.GeofencingApi.removeGeofences(googleApiClient,
                        Arrays.asList(new String[]{Long.toString(id)}));
        }
    }

    private void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // what what ?
        addAndRemove();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i("Geofencing"," connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        googleApiClient.connect();
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i("onResult", "called");
        if (status.isSuccess()) {
            Log.i("onResult", "Success! ");
        }
    }
}

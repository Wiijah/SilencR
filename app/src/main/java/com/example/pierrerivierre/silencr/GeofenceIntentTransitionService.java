package com.example.pierrerivierre.silencr;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Pierre Rivierre on 8/29/2016.
 */
public class GeofenceIntentTransitionService extends IntentService {
    public GeofenceIntentTransitionService() {
        this("No name");
    }

    public GeofenceIntentTransitionService(String name) {
        super(name);
        Log.i("Service", "called");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            // error
            Log.i("ERROR", "");
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            Log.i("Enter", "");
            AudioManager e = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (e.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                e.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i("Exit", "");
            AudioManager e = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (e.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                e.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            }
        } else {
            Log.i("WHAT", "WHAT?");
            // Uh oh. Problem.
        }
    }
}

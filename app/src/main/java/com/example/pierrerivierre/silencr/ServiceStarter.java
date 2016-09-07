package com.example.pierrerivierre.silencr;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.SystemClock;

import com.google.android.gms.location.Geofence;

import Helpers.Contract;
import Helpers.DBHelp;
import Helpers.Geofencing;

public class ServiceStarter extends BroadcastReceiver {
    public ServiceStarter() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // recreate geofences
        DBHelp dbh = new DBHelp(context);
        Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                new String[] {Contract.EntrySettings.COLUMN_NAME_ENTRY_ID,
                        Contract.EntrySettings.COLUMN_NAME_LATITUDE,
                        Contract.EntrySettings.COLUMN_NAME_LONGITUDE,
                        Contract.EntrySettings.COLUMN_NAME_RADIUS},
                null, null, null);

        Geofencing geofencing = new Geofencing(context);
        while (c.moveToNext()) {
            if (!c.isNull(1)) {
                int id =  Integer.parseInt(
                        c.getString(
                                c.getColumnIndexOrThrow(
                                        Contract.EntrySettings.COLUMN_NAME_ENTRY_ID)));
                double lat = Double.parseDouble
                        (c.getString(
                                c.getColumnIndexOrThrow(
                                        Contract.EntrySettings.COLUMN_NAME_LATITUDE)));
                double lng = Double.parseDouble(
                        c.getString(
                                c.getColumnIndexOrThrow(
                                        Contract.EntrySettings.COLUMN_NAME_LONGITUDE)));
                int rad = Integer.parseInt(
                        c.getString(
                                c.getColumnIndexOrThrow(
                                        Contract.EntrySettings.COLUMN_NAME_RADIUS)));

                Geofence geo = new Geofence.Builder().setRequestId(String.valueOf(id))
                        .setCircularRegion(lat,
                                lng,
                                rad).setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                                | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build();
                geofencing.addGeofence(geo, new Intent(context,
                        GeofenceIntentTransitionService.class));
            }
        }

        geofencing.disconnect();
        dbh.close();
        // start location service
        // context.startService(new Intent(context, LocationService.class));

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60 * 1000,
                600 * 1000, PendingIntent.getBroadcast(context, 0
                        , new Intent(context, AlarmReceiver.class),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }
}

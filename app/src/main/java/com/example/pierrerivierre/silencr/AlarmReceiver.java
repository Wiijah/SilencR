package com.example.pierrerivierre.silencr;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {
    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final LocationManager locmgr =
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10000,
                new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locmgr.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }
}

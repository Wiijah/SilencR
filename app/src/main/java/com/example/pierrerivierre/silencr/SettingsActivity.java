package com.example.pierrerivierre.silencr;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;

import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import Helpers.Contract;
import Helpers.DBHelp;
import Helpers.Geofencing;

public class SettingsActivity extends AppCompatActivity implements ResultCallback {


    private static final int REQUEST_LOCATION = 1;
    List<TaggedListElem> elemList;
    long id;
    String radius;

    ArrayAdapter<TaggedListElem> adapter;
    protected Location lastLocation;
    protected Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        id = intent.getLongExtra(ListActivity.EXTRA_ID, -2);

        DBHelp dbh = new DBHelp(this);
        Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                Contract.EntrySettings.BASE_COLUMNS,
                Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + id,
                null,
                null);

        final TaggedListElem[] elems = new TaggedListElem[
                Contract.EntrySettings.BASE_COLUMNS.length];
        c.moveToFirst();
        for (int i = 0; i < elems.length; i++) {
            elems[i] = new TaggedListElem(i, c.getString(i));
            elems[i].rep = elems[i].elem.toString();
        }

        c.close();

        c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                new String[] {Contract.EntrySettings.COLUMN_NAME_LATITUDE,
                        Contract.EntrySettings.COLUMN_NAME_LONGITUDE,
                        Contract.EntrySettings.COLUMN_NAME_RADIUS},
                Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + id, null, null);

        if (c.moveToFirst()) {
            if (!c.isNull(0)) {
                location = new Location("");
                location.setLatitude(
                        Double.parseDouble(
                                c.getString(
                                        c.getColumnIndexOrThrow(
                                                Contract.EntrySettings.COLUMN_NAME_LATITUDE))));

                location.setLongitude(
                        Double.parseDouble(
                                c.getString(
                                        c.getColumnIndexOrThrow(
                                                Contract.EntrySettings.COLUMN_NAME_LONGITUDE))));

                radius = c.getString(
                        c.getColumnIndexOrThrow(Contract.EntrySettings.COLUMN_NAME_RADIUS));
            }
        }
        c.close();
        dbh.close();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        elemList = new ArrayList<TaggedListElem>();
        Collections.addAll(elemList, elems);

        adapter = new ArrayAdapter<TaggedListElem>(this,
                android.R.layout.simple_list_item_1, elemList);

        ListView listView = (ListView) findViewById(R.id.settings_list);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                // final TextView textView = (TextView) findViewById(R.id.text_settings);
                //textView.animate().alpha(1).setDuration(1000);

                if (elemList.get(position).tag == 1) {
                    Intent intent = new Intent(getApplicationContext(), MapActivity.class);

                    boolean locationSet = location != null;

                    if (!locationSet && lastLocation == null) {
                        Toast.makeText(getApplicationContext(), "Can't find location",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Location saveLocation;
                        if(locationSet) {
                            saveLocation = location;
                        } else {
                            saveLocation = lastLocation;
                        }
                        intent.putExtra("lat", saveLocation.getLatitude());
                        intent.putExtra("long", saveLocation.getLongitude());
                        startActivityForResult(intent, 1);
                    }
                } else if (elemList.get(position).tag == 2
                        || elemList.get(position).tag == 3) {

                    Toast.makeText(getApplicationContext(),
                            "Work In Progress", Toast.LENGTH_SHORT).show();
                    return;
                    //crateDefaultTimeDialog(position, adapter);
                } else {
                    createDefaultAlertDialog(position, adapter);
                }
            }
        });

        requestUpdate();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                location = new Location("");
                location.setLatitude(data.getDoubleExtra("lat", -1));
                location.setLongitude(data.getDoubleExtra("long", -1));

                elemList.get(1).elem = location;
                elemList.get(1).rep = data.getStringExtra("title");
                adapter.notifyDataSetChanged();

                radius = data.getStringExtra("rad");
            }
        }
    }

    @Override
    protected void onStart() {
        Log.i("onStart", "called");
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
     }


    @Override
    protected void onStop() {
        super.onStop();
    }

    private void crateDefaultTimeDialog(final int position,
                                        final ArrayAdapter<TaggedListElem> adapter) {
        Calendar c = Calendar.getInstance();
        final TimePickerDialog dialog = new TimePickerDialog(
                SettingsActivity.this,
                android.R.style.Theme_Material_Light_Dialog_Alert, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                TaggedListElem elem = elemList.get(position);
                elem.elem = timePicker;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    elem.rep = timePicker.getHour() + ":" + timePicker.getMinute();
                }
                adapter.notifyDataSetChanged();
            }
        }, c.get(Calendar.HOUR), c.get(Calendar.MINUTE), true);
        dialog.show();
    }

    private void createDefaultAlertDialog(final int position,
                                          final ArrayAdapter<TaggedListElem> adapter) {
        final AlertDialog dialog = new AlertDialog.Builder(SettingsActivity.this).create();
        LayoutInflater inflater = getLayoutInflater();
        dialog.setView(inflater.inflate(R.layout.editable_item, null));
        dialog.setTitle("TEST");
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialog.dismiss();
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Save",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText editText = (EditText) dialog.findViewById(R.id.text);
                        String message = editText.getText().toString();
                        TaggedListElem elem = elemList.get(position);
                        elem.elem = message;
                        elem.rep = message;
                        adapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    public void closeText(View view) {
        if (view.getAlpha() != 1) {
            return;
        }

        view.animate().alpha(0).setDuration(1000);
    }

    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    @Override
    public void onBackPressed() {
        goBack(null);
    }


    public void goBack(View view) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(ListActivity.EXTRA_ID, id);
        setResult(Activity.RESULT_CANCELED, returnIntent);
        finish();
    }

    public void saveAndGoBack(View view) {
        DBHelp dbh = new DBHelp(this);
        String[] elems = new String[elemList.size()];


        final Geofencing fence = new Geofencing(this);

        Cursor c = dbh.read(Contract.EntrySettings.TABLE_NAME,
                new String[] {Contract.EntrySettings.COLUMN_NAME_GEO_ID},
                Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + id, null, null);

        if (c.moveToFirst()) {
            final long id =
                    c.getLong(c.getColumnIndexOrThrow(Contract.EntrySettings.COLUMN_NAME_GEO_ID));
            fence.removeGeofence(id);
        }

        for (int i = 0; i < elems.length; i++) {
            elems[i] = elemList.get(i).rep;
        }

        if (location == null) {
            Toast.makeText(
                    getApplicationContext(), "You haven't chosen a location",
                    Toast.LENGTH_SHORT)
                    .show();
            fence.disconnect();
            dbh.close();
            return;
        }

        Geofence geo = new Geofence.Builder().setRequestId(String.valueOf(id))
                .setCircularRegion(location.getLatitude(),
                        location.getLongitude(),
                        Integer.parseInt(radius)).setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.i("Not", "Fine Location");
            return;
        }

        fence.addGeofence(geo, new Intent(this, GeofenceIntentTransitionService.class));
        fence.disconnect();

        dbh.update(Contract.EntrySettings.TABLE_NAME, Contract.EntrySettings.BASE_COLUMNS,
                elems, Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + id,
                null);
        dbh.update(Contract.EntrySettings.TABLE_NAME,
                new String[] {Contract.EntrySettings.COLUMN_NAME_GEO_ID,
                        Contract.EntrySettings.COLUMN_NAME_LATITUDE,
                        Contract.EntrySettings.COLUMN_NAME_LONGITUDE,
                        Contract.EntrySettings.COLUMN_NAME_RADIUS},
                new String[] {geo.getRequestId(), "" + location.getLatitude(),
                        "" + location.getLongitude(), radius},
                Contract.EntrySettings.COLUMN_NAME_ENTRY_ID + " = " + id, null);


        Intent returnIntent = new Intent();
        returnIntent.putExtra(ListActivity.EXTRA_ID, id);
        setResult(RESULT_OK, returnIntent);
        Log.i("save and go back", "finished");
        finish();
    }

    private void requestUpdate() {
        final LocationManager locmgr = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locmgr == null) {
            Log.i("FUCK", "YOU");
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(
                            this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        } else {
            Log.i("GOT", "PERMiSSION");
            if (locmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.i("GPS", "ENABLED");
                locmgr.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, 1000, 10000, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location newLocation) {
                        Log.i("found", newLocation.toString());
                        lastLocation = newLocation;
                        if (lastLocation != null) {
                            locmgr.removeUpdates(this);
                        }
                    }

                    @Override
                    public void onStatusChanged(String s, int i, Bundle bundle) {
                        Log.i("Status", "changed");
                    }

                    @Override
                    public void onProviderEnabled(String s) {
                        Log.i("Provider", "enabled");
                    }

                    @Override
                    public void onProviderDisabled(String s) {
                        Log.i("Provider", "disabled");
                    }
                });
            } else if (locmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locmgr.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 1000, 10000, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location newLocation) {
                        lastLocation = newLocation;
                        if (lastLocation != null) {
                            locmgr.removeUpdates(this);
                        }
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length == 1 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                // fine
            } else {
                // display error
            }
        } else if (requestCode == 2) {
            if (grantResults.length == 1 && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {
                requestUpdate();
            }
        }
    }

    @Override
    public void onResult(@NonNull Result result) {
        // stub
    }

    protected void switchView(View view) {
        Toast.makeText(this, "Not available yet", Toast.LENGTH_SHORT).show();
    }

    private class TaggedListElem {
        public int tag;
        public Object elem;
        public String rep = "";

        public TaggedListElem(int tag, Object elem) {
            this.tag = tag;
            this.elem = elem;
        }

        @Override
        public String toString() {
            return rep;
        }
    }
}

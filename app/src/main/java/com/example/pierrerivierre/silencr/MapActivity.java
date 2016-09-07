package com.example.pierrerivierre.silencr;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {


    double lat;
    double lng;
    GoogleMap map;
    LatLng location;
    Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();

        SupportMapFragment supportMapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (supportMapFragment != null) {
            supportMapFragment.getMapAsync(this);
        }

        location = new LatLng(intent.getDoubleExtra("lat", -1),
                intent.getDoubleExtra("long", -1));
        lat = location.latitude;
        lng = location.longitude;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        map = googleMap;
        googleMap.addMarker(new MarkerOptions().position(location).title("Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                googleMap.clear();
                lat = latLng.latitude;
                lng = latLng.longitude;
                googleMap.addMarker(new MarkerOptions().position(latLng).title("Location"));
            }
        });
    }

    protected void display(View view) {
        if (map == null) {
            return;
        }

        String strRadius = ((EditText) findViewById(R.id.radius)).getText().toString();
        if (strRadius == null || strRadius.equals("")) {
            Toast.makeText(this, "Please select a radius", Toast.LENGTH_SHORT).show();
            return;
        }

        double radius = Double.parseDouble(strRadius);
        if (circle != null) {
            circle.remove();
        }

        circle = map.addCircle(new CircleOptions().center(new LatLng(lat, lng)).radius(radius)
                .strokeColor(Color.BLACK).fillColor(Color.TRANSPARENT).strokeWidth(3));
    }

    protected void save(View view) {
        Intent intent = new Intent();

        String message = ((EditText) findViewById(R.id.title)).getText().toString();
        if (message.equals("")) {
            message = "Your location";
        }

        intent.putExtra("title", message);
        intent.putExtra("lat", lat);
        intent.putExtra("long", lng);

        String radStr = ((EditText) (findViewById(R.id.radius))).getText().toString();
        if(radStr == null || radStr.equals("")) {
            Toast.makeText(this, "Please choose a radius", Toast.LENGTH_SHORT).show();
            return;
        }
        int rad = Integer.parseInt(radStr);

        if (rad < 100) {
            rad = 100;
        } else if (rad > 1000) {
            rad = 1000;
        }

        intent.putExtra("rad", "" + rad);
        setResult(RESULT_OK, intent);
        finish();
    }
}

package com.sravan.demoapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends ActionBarActivity {

    private String latLong;

    private LatLng projectName;
    private GoogleMap googleMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latLong = getIntent().getExtras().getString("pLoc");
        String[] latlongSplit = latLong.split("/");
        projectName = new LatLng(Double.parseDouble(latlongSplit[0]) , Double.parseDouble(latlongSplit[1]));

        try {
            if (googleMap == null) {
                googleMap = ((MapFragment) getFragmentManager().
                        findFragmentById(R.id.map)).getMap();
            }
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            googleMap.addMarker(new MarkerOptions().
                    position(projectName).title(getIntent().getExtras().getString("pName"))).showInfoWindow();

            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    Intent projectDetailsIntent = new Intent(MainActivity.this, ProjectDetails.class);
                    projectDetailsIntent.putExtra("pName", getIntent().getExtras().getString("pName"));
                    projectDetailsIntent.putExtra("pID", getIntent().getExtras().getString("pID"));
                    startActivity(projectDetailsIntent);
                    return true;
                }
            });
            // Move the camera instantly to hamburg with a zoom of 15.
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(projectName, 15));

            // Zoom in, animating the camera.
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

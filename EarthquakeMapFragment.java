package com.example.earthquake_service_alarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EarthquakeMapFragment extends Fragment implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {
    private GoogleMap mMap = null;
    Map<String, Marker> mMarkers = new HashMap<>();
    Cursor mEarthquakes;
    float pref_min_magnitude = 3;

    private Cursor mLocalCursor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Obtain the SupportMapFragment and request the Google Map object.
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_earthquake_map,
                container, false);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setEarthquakeMarkers(mLocalCursor);
        LoaderManager.getInstance(this).initLoader(0, null, this);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        String[] projection = new String[] {
                EarthquakeProvider.KEY_ID,
                EarthquakeProvider.KEY_DATE,
                EarthquakeProvider.KEY_DETAILS,
                EarthquakeProvider.KEY_SUMMARY,
                EarthquakeProvider.KEY_LOCATION_LAT,
                EarthquakeProvider.KEY_LOCATION_LNG,
                EarthquakeProvider.KEY_MAGNITUDE,
                EarthquakeProvider.KEY_LINK
        };
        //Earthquake earthquakeActivity =  getActivity();
        Context context = getActivity().getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        pref_min_magnitude = Float.parseFloat(prefs.getString("PREF_MAGNITUDE", "3.0"));
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + pref_min_magnitude;
        CursorLoader loader = new CursorLoader(getActivity(), EarthquakeProvider.CONTENT_URI, projection, where, null, null);
        return loader;
    }

    //data is ready
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        //mAdapter.setCursor(cursor);
        mLocalCursor = data;
        setEarthquakeMarkers(mLocalCursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        //mAdapter.setCursor(null);
        mLocalCursor = null;
    }


    public void setEarthquakeMarkers(Cursor earthquakes) {

        mEarthquakes = earthquakes;
        if (mMap == null || earthquakes == null) return;
        Map<Integer, Location> newEarthquakes = new HashMap<>();
        // Add Markers for each earthquake above the user threshold.
        earthquakes.moveToFirst();
        do {
            float magnitude = earthquakes.getFloat(earthquakes.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
            if (magnitude >= pref_min_magnitude) {
                Integer id = earthquakes.getInt(earthquakes.getColumnIndex(EarthquakeProvider.KEY_ID));

                int latIndex = earthquakes.getColumnIndexOrThrow(EarthquakeProvider.KEY_LOCATION_LAT);
                int lngIndex = earthquakes.getColumnIndexOrThrow(EarthquakeProvider.KEY_LOCATION_LNG);
                Double lat = earthquakes.getDouble(latIndex);
                Double lng = earthquakes.getDouble(lngIndex);
                Location location = new Location("Me");
                location.setLongitude(lng);
                location.setLatitude(lat);
                newEarthquakes.put(id, location);

                if (!mMarkers.containsKey(id)) {
                    //Point newLocation = newEarthquakes.getLocation();
                    double tmp = location.getLatitude();
                    Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),location.getLongitude())).title("M:" + magnitude));
                    mMarkers.put(id.toString(), marker);
                }
            }
        }while (earthquakes.moveToNext());
        earthquakes.close();
    }
// Remove any Markers representing earthquakes that should no longer  // be displayed.  for (Iterator<String> iterator = mMarkers.keySet().iterator();       iterator.hasNext();) {    String earthquakeID = iterator.next();    if (!newEarthquakes.containsKey(earthquakeID)) {      mMarkers.get(earthquakeID).remove();      iterator.remove();    }  } }


//    @Override
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        // Obtain the SupportMapFragment and request the Google Map object.
//        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
//        mapFragment.getMapAsync(this);
//    }

}

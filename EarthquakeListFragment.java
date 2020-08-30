package com.example.earthquake_service_alarm;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
//import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class EarthquakeListFragment extends Fragment implements  LoaderManager.LoaderCallbacks<Cursor>{
    //private ArrayList<Earthquake> mEarthquakes = new ArrayList<>();
    private RecyclerView mRecyclerView;
    //private EarthquakeRecyclerViewAdapter mEarthquakeAdapter = new EarthquakeRecyclerViewAdapter(mEarthquakes);
    //double minimumMagnitude = 3;

    //SimpleCursorAdapter adapter;
    EarthquakeCursorAdapter mAdapter;


    public EarthquakeListFragment() {  }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_earthquake_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        mAdapter = new EarthquakeCursorAdapter(null);

        return view;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set the Recycler View adapter
        Context context = view.getContext();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecyclerView.setAdapter(mAdapter);
        LoaderManager.getInstance(this).initLoader(0, null, this);
//        Thread t = new Thread(new Runnable() {
//            public void run() {
//                startService();
//            }
//        });
        startService();
        //t.start();
    }

//    public void setEarthquakes(List<Earthquake> earthquakes) {
//        for (Earthquake earthquake: earthquakes) {
//            if (!mEarthquakes.contains(earthquake)) {
//                mEarthquakes.add(earthquake);
//                mEarthquakeAdapter.notifyItemInserted(mEarthquakes.indexOf(earthquake));
//            }
//        }
//    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
        Float pref_min_magnitude = Float.parseFloat(prefs.getString("PREF_MAGNITUDE", "3.0"));
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + pref_min_magnitude;
        CursorLoader loader = new CursorLoader(getActivity(), EarthquakeProvider.CONTENT_URI, projection, where, null, null);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //adapter.swapCursor(cursor);
        mAdapter.setCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //adapter.swapCursor(null);
        mAdapter.setCursor(null);
    }

    Handler handler = new Handler();
    public void startService(){
        handler.post(new Runnable() {    public void run() {      LoaderManager.getInstance(EarthquakeListFragment.this).restartLoader(0, null, EarthquakeListFragment.this);     }  });
        //getActivity().startService(new Intent(getActivity(), EarthquakeUpdateService.class));
        EarthquakeUpdateJobService.scheduleUpdateJob(getActivity().getApplication());

    }

    private void addNewQuake(Earthquake quake) {
        ContentResolver cr = getActivity().getContentResolver();
        String w = EarthquakeProvider.KEY_DATE + " = " + quake.getDate().getTime();
        Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);
        if (query.getCount()==0) {
            ContentValues values = new ContentValues();
            values.put(EarthquakeProvider.KEY_DATE, quake.getDate().getTime());
            values.put(EarthquakeProvider.KEY_DETAILS, quake.getDetails());
            values.put(EarthquakeProvider.KEY_SUMMARY, quake.toString());
            double lat = quake.getLocation().getLatitude();
            double lng = quake.getLocation().getLongitude();
            values.put(EarthquakeProvider.KEY_LOCATION_LAT, lat);
            values.put(EarthquakeProvider.KEY_LOCATION_LNG, lng);
            values.put(EarthquakeProvider.KEY_LINK, quake.getLink());
            values.put(EarthquakeProvider.KEY_MAGNITUDE, quake.getMagnitude());
            cr.insert(EarthquakeProvider.CONTENT_URI, values);
        }
        query.close();

    }

}

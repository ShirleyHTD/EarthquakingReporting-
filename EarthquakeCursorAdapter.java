package com.example.earthquake_service_alarm;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class EarthquakeCursorAdapter extends RecyclerView.Adapter<EarthquakeCursorAdapter.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View parentView;
        public Earthquake earthquake;
        public final TextView date;
        public final TextView details;
        public final TextView magnitude;

        public ViewHolder(View view) {
            super(view);
            parentView = view;
            date = (TextView) view.findViewById(R.id.date);
            details = (TextView) view.findViewById(R.id.details);
            magnitude = (TextView) view.findViewById(R.id.magnitude);

        }

    }








    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final NumberFormat MAGNITUDE_FORMAT = new DecimalFormat("0.0");
    private Cursor mEarthquakes;
    private int mDateIndex;
    private int mDetailsIndex;
    private int mMagnitudeIndex;


    public EarthquakeCursorAdapter(Cursor c){
        mEarthquakes = c;
    }

    @NonNull
    @Override
    public EarthquakeCursorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_earthquake, parent, false);
        return new EarthquakeCursorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mEarthquakes != null){
            mEarthquakes.moveToPosition(position);

            holder.details.setText(mEarthquakes.getString(mDetailsIndex));
            holder.magnitude.setText(mEarthquakes.getString(mMagnitudeIndex));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH.mm");
            long tmp_long = Long.parseLong(mEarthquakes.getString(mDateIndex));
            String tmp = sdf.format(tmp_long);
            holder.date.setText(tmp);


        }
    }


    @Override
    public int getItemCount() {
        if(mEarthquakes==null){
            return 0;
        }
        return mEarthquakes.getCount();
    }

    public void setCursor(Cursor cursor) {
        mEarthquakes = cursor;
        if (cursor != null) {
            mDateIndex = mEarthquakes.getColumnIndex(EarthquakeProvider.KEY_DATE);
            mDetailsIndex = mEarthquakes.getColumnIndex(EarthquakeProvider.KEY_DETAILS);
            mMagnitudeIndex = mEarthquakes.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE);
        }
        notifyDataSetChanged();
    }


}

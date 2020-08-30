package com.example.earthquake_service_alarm;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EarthquakeRecyclerViewAdapter extends RecyclerView.Adapter<EarthquakeRecyclerViewAdapter.ViewHolder> {

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
//            detailsView = (TextView)
//                    view.findViewById(R.id.list_item_earthquake_details);
        }
//        @Override
//        public String toString() {
//            return super.toString() + " '" + detailsView.getText() + "'";
//        }
    }

    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm", Locale.US);
    private static final NumberFormat MAGNITUDE_FORMAT = new DecimalFormat("0.0");

    private final List<Earthquake> mEarthquakes;


    public EarthquakeRecyclerViewAdapter(List<Earthquake> earthquakes ) {
        mEarthquakes = earthquakes;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_earthquake, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Earthquake earthquake = mEarthquakes.get(position);
        //holder.earthquake = mEarthquakes.get(position);
        //holder.detailsView.setText(mEarthquakes.get(position).toString());
        holder.date.setText(TIME_FORMAT.format(earthquake.getDate()));
        holder.details.setText(earthquake.getDetails());
        holder.magnitude.setText(MAGNITUDE_FORMAT.format(earthquake.getMagnitude()));
    }


    @Override
    public int getItemCount() {
        return mEarthquakes.size();
    }



}

package com.example.earthquake_service_alarm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
//import androidx.preference.PreferenceManager;

import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.SimpleJobService;
import com.firebase.jobdispatcher.Trigger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class EarthquakeUpdateJobService extends SimpleJobService {
    private static final String TAG = "EarthquakeUpdateJob ";
    private static final String UPDATE_JOB_TAG = "update_job";
    private static final String PERIODIC_JOB_TAG = "periodic_job";
    private static final String NOTIFICATION_CHANNEL = "earthquake";
    public static final int NOTIFICATION_ID = 1;

    public static void scheduleUpdateJob(Context context) {
        FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(context));
        jobDispatcher.mustSchedule(jobDispatcher.newJobBuilder()
                .setTag(UPDATE_JOB_TAG)
                .setService(EarthquakeUpdateJobService.class)
                .setConstraints(Constraint.ON_ANY_NETWORK)
                .setTrigger(Trigger.executionWindow(0, 0))
                .build());
    }




    private void scheduleNextUpdate(JobParameters job) {
        if (job.getTag().equals(UPDATE_JOB_TAG)) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            int updateFreq = Integer.parseInt(
                    prefs.getString("PREF_REFRESH_FREQ", "15"));
            boolean autoUpdateChecked = prefs.getBoolean("AUTO_REFRESH", true);
            if (autoUpdateChecked) {
                FirebaseJobDispatcher jobDispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
                jobDispatcher.mustSchedule(jobDispatcher.newJobBuilder()
                        .setTag(PERIODIC_JOB_TAG)
                        .setService(EarthquakeUpdateJobService.class)
                        .setConstraints(Constraint.ON_ANY_NETWORK)
                        .setReplaceCurrent(true)
                        .setRecurring(true)
                        .setTrigger(Trigger.executionWindow(updateFreq*60 / 2,updateFreq*60))
                        .setLifetime(Lifetime.FOREVER)
                        .build());
            }
        }
    }

    private void addNewQuake(Earthquake quake) {
        ContentResolver cr = getContentResolver();
        String w = EarthquakeProvider.KEY_DATE + " = " + quake.getDate().getTime();
        Cursor query = cr.query(EarthquakeProvider.CONTENT_URI, null, w, null, null);
        broadcastNotification(quake);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.earthquake_channel_name);
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL, name, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.enableLights(true);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void broadcastNotification(Earthquake earthquake){
        createNotificationChannel();
        Intent startActivityIntent = new Intent(this, MainActivity.class);
        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        final NotificationCompat.Builder earthquakeNotificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL);
        earthquakeNotificationBuilder
                .setSmallIcon(R.drawable.notification_icon)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(launchIntent)
                .setAutoCancel(true)
                .setShowWhen(true);
        earthquakeNotificationBuilder
                .setWhen(earthquake.getDate().getTime())
                .setContentTitle("M:" + earthquake.getMagnitude())
                .setContentText(earthquake.getDetails())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(earthquake.getDetails()));
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(NOTIFICATION_ID, earthquakeNotificationBuilder.build());
    }


    @Override
    public int onRunJob(JobParameters job) {
        URL url;
        //handler.post(new Runnable() {    public void run() {      getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);     }  });

        try{
            String quakeData = ("https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_day.atom");
            url = new URL(quakeData);

            URLConnection connection;
            connection = url.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            int responseCode = httpURLConnection.getResponseCode();

            if(responseCode == HttpURLConnection.HTTP_OK){
                //downloaded the doc
                InputStream gotIn = httpURLConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();

                //read
                Document dom = db.parse(gotIn);
                Element docEle = dom.getDocumentElement();

                //earthquakes.clear();

                //store into nodeList
                NodeList nl = docEle.getElementsByTagName("entry");
                if(nl != null && nl.getLength() > 0 ){
                    for(int i = 0; i < nl.getLength(); i++){
                        Element entry = (Element) nl.item(i);

                        Element id = (Element) entry.getElementsByTagName("id").item(0);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = hostname + link.getAttribute("href");
                        String id_name = id.getFirstChild().getNodeValue();


                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();
                        //SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'hh:mm:ss'Z'");
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
                        Date qdate = new GregorianCalendar(0,0,0).getTime();

                        try{
                            qdate = sdf.parse(dt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        //int end = magnitudeString.length() - 1; magnitudeString.substring(0, end)
                        double magnitude = Double.parseDouble(magnitudeString);

                        details = details.split(",")[1].trim();

                        //String id, Date date, String details, Location location, double magnitude, String link
                        final Earthquake quake = new Earthquake(id_name,qdate,details, l, magnitude, linkString);
                        addNewQuake(quake);

                    }
                }
            }
            scheduleNextUpdate(job);
            return RESULT_SUCCESS;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return RESULT_FAIL_NORETRY;
        } catch (IOException e) {
            e.printStackTrace();
            return RESULT_FAIL_RETRY;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return RESULT_FAIL_NORETRY;
        } catch (SAXException e) {
            e.printStackTrace();
            return RESULT_FAIL_NORETRY;
        }finally {

        }
    }
}

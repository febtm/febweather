package fmt.febuweather.helper;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import fmt.febuweather.MyLocation;
import fmt.febuweather.R;

import static android.content.Context.VIBRATOR_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {

    BasicFunctions basicFunctions;

    Context mContext;

    String LOCATION, LATITUDE, LONGITUDE;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.mContext = context;

        if(intent.getStringExtra("For").equals("Notification")) {

            basicFunctions = new BasicFunctions(mContext);

            if (!(ActivityCompat.checkSelfPermission(mContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                if (basicFunctions.isConnectingToInternet())
                    getLocation();

                else {

                    String message = "No Internet Connection | " +
                            "Connect to the Internet to receive forecast updates of your Location !";

                    setUpNotification(message);

                }

            }

            else {

                String message = "Location Permission Not Granted | " +
                        "Grant Location permission to receive forecast updates of your Location !";

                setUpNotification(message);

            }

        }

        else if(intent.getStringExtra("For").equals("Alarm")){

            Intent alarmIntent = new Intent(mContext, AlarmProvider.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(alarmIntent);

        }

    }


    @SuppressLint("MissingPermission")
    private void getLocation(){

        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        assert locationManager != null;

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, new Listener());

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new Listener());

        android.location.Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        setDetails(location);

        new GetCurrentForecastTask().execute();

    }


    private void setDetails(android.location.Location location){

        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());

        LATITUDE = String.valueOf(location.getLatitude());

        LONGITUDE = String.valueOf(location.getLongitude());

        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException e) {

            e.printStackTrace();

        }

        assert addresses != null;

        LOCATION = addresses.get(0).getLocality();

    }


    private class Listener implements LocationListener {

        public void onLocationChanged(android.location.Location location) {}
        public void onProviderDisabled(String provider){}
        public void onProviderEnabled(String provider){}
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }


    private void setUpNotification(String message){

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, message)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_logo_main))
                        .setSmallIcon(R.drawable.me_my_location)
                        .setContentTitle("FebWeather")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message)
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(mContext, MyLocation.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 99997, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.notify(0, builder.build());

        try {

            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification_sound");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();

            Vibrator v = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
            assert v != null;
            v.vibrate(500);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @SuppressLint("StaticFieldLeak")
    private class GetCurrentForecastTask extends AsyncTask<String, Void, JSONObject> {

        private GetCurrentForecastTask() {}

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            try {

                jsonWeather = basicFunctions.getWeatherJSON(basicFunctions.OWM_CURRENT_FORECAST_URL,
                        LATITUDE, LONGITUDE);

            } catch (Exception e) {

                Log.d("Error", "Cannot process JSON results", e);

            }

            return jsonWeather;
        }

        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                if(json != null){

                    JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");

                    String description = weather.getString("main") + ", " + weather.getString("description");

                    String temperature = main.getInt("temp") + " °C";

                    String message = temperature + " | " + description + " in " + LOCATION;

                    setUpNotification(message);

                }

            } catch (JSONException e) {

                Toast.makeText(mContext, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }
        }
    }
}
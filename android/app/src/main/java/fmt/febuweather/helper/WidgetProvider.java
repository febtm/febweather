package fmt.febuweather.helper;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fmt.febuweather.MyLocation;
import fmt.febuweather.R;


public class WidgetProvider extends AppWidgetProvider {


    private BasicFunctions basicFunctions;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;

    private Context mContext;

    Integer UNIT_TEMPERATURE, UNIT_WIND_SPEED;

    String MY_LOCATION, MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE;

    private RemoteViews remoteViews;

    private AppWidgetManager mAppWidgetManager;

    private int mWidgetId;


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        mContext = context;

        mAppWidgetManager = appWidgetManager;

        basicFunctions = new BasicFunctions(mContext);

        mOpenHelper = new BasicFunctions.DatabaseHelper(mContext);

        remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.activity_widget_provider);

        for (int widgetId : appWidgetIds) {

            mWidgetId = widgetId;

            SQL_DB = mOpenHelper.getWritableDatabase();

            String SQL_UNIT_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_UNITS_TABLE + "' ( '"
                    + basicFunctions.UNIT_NAME + "' TEXT NOT NULL, '"
                    + basicFunctions.UNIT_VALUE + "' INT(11) NOT NULL ) ;";

            SQL_DB.execSQL(SQL_UNIT_CREATE);

            String SQL_UNIT_SELECT = "SELECT * FROM " + basicFunctions.MY_UNITS_TABLE;

            DB_CURSOR = SQL_DB.rawQuery(SQL_UNIT_SELECT, null);

            if(DB_CURSOR.getCount() > 0) {

                DB_CURSOR.moveToFirst();


                if(DB_CURSOR.getInt(1) == 0)
                    UNIT_TEMPERATURE = 0;

                else
                    UNIT_TEMPERATURE = 1;

                DB_CURSOR.moveToNext();


                if(DB_CURSOR.getInt(1) == 0)
                    UNIT_WIND_SPEED = 0;

                else
                    UNIT_WIND_SPEED = 1;
            }

            else {

                String SQL_UNIT_INSERT = "INSERT INTO '"
                        + basicFunctions.MY_UNITS_TABLE + "' ( '" + basicFunctions.UNIT_NAME + "', '"
                        + basicFunctions.UNIT_VALUE + "' ) VALUES ( 'Temperature', '0' ), ( 'Wind_speed', '0' ) ;";

                SQL_DB.execSQL(SQL_UNIT_INSERT);

                UNIT_TEMPERATURE = 0;

                UNIT_WIND_SPEED = 0;

            }

            DB_CURSOR.close();

            SQL_DB.close();


            if (!(ActivityCompat.checkSelfPermission(mContext,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(mContext,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

                if (basicFunctions.isConnectingToInternet()) {

                    remoteViews.setViewVisibility(R.id.wp_no_network, View.GONE);

                    remoteViews.setViewVisibility(R.id.wp_no_location_permission, View.GONE);

                    remoteViews.setViewVisibility(R.id.wp_location, View.VISIBLE);

                    remoteViews.setViewVisibility(R.id.wp_forecast_details, View.VISIBLE);

                    getLocation();

                }

                else {

                    remoteViews.setViewVisibility(R.id.wp_no_network, View.VISIBLE);

                    remoteViews.setViewVisibility(R.id.wp_no_location_permission, View.GONE);

                    remoteViews.setViewVisibility(R.id.wp_forecast_details, View.GONE);

                }

            }

            else {

                remoteViews.setViewVisibility(R.id.wp_no_network, View.GONE);

                remoteViews.setViewVisibility(R.id.wp_no_location_permission, View.VISIBLE);

                remoteViews.setViewVisibility(R.id.wp_forecast_details, View.GONE);

            }

            showTime();

            Intent openIntent = new Intent(mContext, MyLocation.class);
            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 99998, openIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.wp_no_location_permission_button, openPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wp_forecast_details, openPendingIntent);

            Intent refreshIntent = new Intent(mContext, WidgetProvider.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 99999, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.wp_no_network_button, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wp_no_network_refresh, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wp_no_location_permission_refresh, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wp_location_details_refresh, refreshPendingIntent);

            mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

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

        MY_LOCATION_LATITUDE = String.valueOf(location.getLatitude());

        MY_LOCATION_LONGITUDE = String.valueOf(location.getLongitude());

        List<Address> addresses = null;

        try {

            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

        } catch (IOException e) {

            e.printStackTrace();

        }

        assert addresses != null;

        MY_LOCATION = addresses.get(0).getLocality();

        remoteViews.setTextViewText(R.id.wp_location, MY_LOCATION);

    }


    private class Listener implements LocationListener {

        public void onLocationChanged(android.location.Location location) {}
        public void onProviderDisabled(String provider){}
        public void onProviderEnabled(String provider){}
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }


    private void showTime(){

        CountDownTimer newtimer = new CountDownTimer(1000000000, 5000) {

            @SuppressLint("SetTextI18n")
            public void onTick(long millisUntilFinished) {

                Calendar c = Calendar.getInstance();

                SimpleDateFormat timeFormat = new SimpleDateFormat("HH : mm");

                SimpleDateFormat dateFormat = new SimpleDateFormat("EE, d | M");

                remoteViews.setTextViewText(R.id.wp_time, timeFormat.format(c.getTime()));

                remoteViews.setTextViewText(R.id.wp_date, dateFormat.format(c.getTime()));

                mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

            }

            public void onFinish() {}
        };

        newtimer.start();

    }


    @SuppressLint("StaticFieldLeak")
    private class GetCurrentForecastTask extends AsyncTask<String, Void, JSONObject> {

        private GetCurrentForecastTask() {}

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            try {

                jsonWeather = basicFunctions.getWeatherJSON(basicFunctions.OWM_CURRENT_FORECAST_URL,
                        MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE);

            } catch (Exception e) {

                Log.d("Error", "Cannot process JSON results", e);

            }

            return jsonWeather;
        }

        @SuppressLint("DefaultLocale")
        @SuppressWarnings("deprecation")
        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                if(json != null){

                    JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    JSONObject wind = json.getJSONObject("wind");

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = String.format("%.2f", main.getDouble("temp")) + " °C";

                    else
                        temperature = String.format("%.2f", basicFunctions.celsiusToFahrenheit(main.getDouble("temp"))) + " °F";

                    String description = weather.getString("main") + ", " + weather.getString("description");

                    String humidity = main.getDouble("humidity") + " %";

                    String pressure = main.getDouble("pressure") + " hPa";

                    String max_min_temp;

                    if(UNIT_TEMPERATURE == 0)
                        max_min_temp = main.getInt("temp_min") + " / " + main.getInt("temp_max") + " °C";

                    else
                        max_min_temp = basicFunctions.celsiusToFahrenheit(main.getInt("temp_min")) + " / "
                                + basicFunctions.celsiusToFahrenheit(main.getInt("temp_max")) + " °F";

                    String wind_speed;

                    if(UNIT_WIND_SPEED == 0)
                        wind_speed = wind.getDouble("speed") + " m / s";

                    else
                        wind_speed = basicFunctions.metresPerSecondToKilometresPerHour(wind.getDouble("speed")) + " km / hr";

                    String wind_angle = wind.getDouble("deg") + " degrees";


                    remoteViews.setTextViewText(R.id.wp_temperature, temperature);

                    remoteViews.setTextViewText(R.id.wp_description, description);


                    remoteViews.setTextViewText(R.id.wp_pressure, pressure);

                    remoteViews.setTextViewText(R.id.wp_humidity, humidity);

                    remoteViews.setTextViewText(R.id.wp_maxmintemp, max_min_temp);


                    remoteViews.setTextViewText(R.id.wp_wind_speed, wind_speed);

                    remoteViews.setTextViewText(R.id.wp_wind_angle, wind_angle);


                    mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

                }

            } catch (JSONException e) {

                Toast.makeText(mContext, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

        }
    }

}
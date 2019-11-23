package fmt.febuweather.helper;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fmt.febuweather.R;


public class AlarmProvider extends AppCompatActivity {


    TextView AP_TIME, AP_LOCATION, AP_DATE, AP_TEMPERATURE, AP_ICON,
            AP_DESCRIPTION, AP_HUMIDITY, AP_PRESSURE, AP_MINMAXTEMP,
            AP_WIND_SPEED, AP_WIND_ANGLE, AP_LABEL;

    Button AP_DISMISS, AP_SNOOZE;

    LinearLayout AP_FORECAST_DETAILS;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;

    String MY_LOCATION, MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE, LABEL;

    Integer UNIT_TEMPERATURE, UNIT_WIND_SPEED, ALA_DURATION, SNO_DURATION, VIBRATION;

    private BasicFunctions basicFunctions;

    MediaPlayer mediaPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_provider);

        AP_TIME = findViewById(R.id.ap_time);
        AP_LOCATION = findViewById(R.id.ap_location);
        AP_DATE = findViewById(R.id.ap_date);
        AP_TEMPERATURE = findViewById(R.id.ap_temperature);
        AP_ICON = findViewById(R.id.ap_icon);
        AP_DESCRIPTION = findViewById(R.id.ap_description);
        AP_HUMIDITY = findViewById(R.id.ap_humidity);
        AP_PRESSURE = findViewById(R.id.ap_pressure);
        AP_MINMAXTEMP = findViewById(R.id.ap_minmaxtemp);
        AP_WIND_SPEED = findViewById(R.id.ap_wind_speed);
        AP_WIND_ANGLE = findViewById(R.id.ap_wind_angle);
        AP_LABEL = findViewById(R.id.ap_label);

        AP_FORECAST_DETAILS = findViewById(R.id.ap_forecast_details);

        AP_SNOOZE = findViewById(R.id.ap_snooze);
        AP_DISMISS = findViewById(R.id.ap_dismiss);

        basicFunctions = new BasicFunctions(AlarmProvider.this);

        mOpenHelper = new BasicFunctions.DatabaseHelper(this);


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


        SQL_DB = mOpenHelper.getWritableDatabase();

        String SQL_ALA_SET_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_ALARM_SETTINGS_TABLE + "' ( '"
                + basicFunctions.ALARM_SETTING_NAME + "' TEXT NOT NULL, '"
                + basicFunctions.ALARM_SETTING_VALUE + "' TEXT NOT NULL ) ;";

        SQL_DB.execSQL(SQL_ALA_SET_CREATE);

        String SQL_ALA_SET_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARM_SETTINGS_TABLE;

        DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_SET_SELECT, null);

        if(DB_CURSOR.getCount() > 0) {

            DB_CURSOR.moveToFirst();

            ALA_DURATION = Integer.parseInt(DB_CURSOR.getString(1)) * 60 * 1000;

            DB_CURSOR.moveToNext();

            LABEL = DB_CURSOR.getString(1);

            DB_CURSOR.moveToNext();

            SNO_DURATION = Integer.parseInt(DB_CURSOR.getString(1)) * 60 * 1000;

            DB_CURSOR.moveToNext();

            VIBRATION = Integer.parseInt(DB_CURSOR.getString(1));

        }

        else {

            String SQL_ALA_SET_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_ALARM_SETTINGS_TABLE + "' ( '" + basicFunctions.ALARM_SETTING_NAME + "', '"
                    + basicFunctions.ALARM_SETTING_VALUE + "' ) VALUES ( 'Alarm_duration', '15' ), ( 'Label', '' )," +
                    " ( 'Snooze_duration', '10' ), ( 'Vibration', '1' ) ;";

            SQL_DB.execSQL(SQL_ALA_SET_INSERT);

            ALA_DURATION = 15 * 60 * 1000;

            LABEL = "";

            SNO_DURATION = 10 * 60 * 1000;

            VIBRATION = 1;

        }

        DB_CURSOR.close();

        SQL_DB.close();


        if (!(ActivityCompat.checkSelfPermission(AlarmProvider.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(AlarmProvider.this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if (basicFunctions.isConnectingToInternet())
                getLocation();

            else
                setUpAlarm();

        }

        else
            setUpAlarm();


        AP_SNOOZE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= 21) {
                    finishAndRemoveTask();
                }
                else {
                    finish();
                }

                mediaPlayer.stop();


                AlarmManager alarmManager = (AlarmManager) AlarmProvider.this.getSystemService(ALARM_SERVICE);

                Calendar calendar = Calendar.getInstance();
                calendar.add(Calendar.MINUTE, (SNO_DURATION / 60000));

                Intent intent = new Intent(AlarmProvider.this, AlarmReceiver.class);
                intent.putExtra("For", "Alarm");
                PendingIntent pendingIntent = PendingIntent.getBroadcast(AlarmProvider.this, 99993, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                long time = calendar.getTimeInMillis();

                assert alarmManager != null;
                alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

            }
        });


        AP_DISMISS.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                if(Build.VERSION.SDK_INT >= 21)
                {
                    finishAndRemoveTask();
                }
                else
                {
                    finish();
                }

                mediaPlayer.stop();

                AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notIntent = new Intent(AlarmProvider.this, AlarmReceiver.class);
                PendingIntent notPendingIntent = PendingIntent.getBroadcast(AlarmProvider.this, 99993, notIntent, 0);
                assert notAlarmManager != null;
                notAlarmManager.cancel(notPendingIntent);
                notPendingIntent.cancel();

            }
        });

    }


    @SuppressLint("MissingPermission")
    private void getLocation(){

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        assert locationManager != null;

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, new Listener());

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, new Listener());

        android.location.Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if (location == null)
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        setDetails(location);

        new GetCurrentForecastTask().execute();

        setUpAlarm();

    }


    private void setDetails(android.location.Location location){

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

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

        AP_LOCATION.setVisibility(View.VISIBLE);
        AP_LOCATION.setText(MY_LOCATION);

        AP_FORECAST_DETAILS.setVisibility(View.VISIBLE);

    }


    private class Listener implements LocationListener {

        public void onLocationChanged(android.location.Location location) {}
        public void onProviderDisabled(String provider){}
        public void onProviderEnabled(String provider){}
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }


    private void setUpAlarm() {

        try {

            Handler mHandler = new Handler();
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {

                    if(Build.VERSION.SDK_INT >= 21)
                    {
                        finishAndRemoveTask();
                    }
                    else
                    {
                        finish();

                    }

                    mediaPlayer.stop();

                    AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent notIntent = new Intent(AlarmProvider.this, AlarmReceiver.class);
                    PendingIntent notPendingIntent = PendingIntent.getBroadcast(AlarmProvider.this, 99993, notIntent, 0);
                    assert notAlarmManager != null;
                    notAlarmManager.cancel(notPendingIntent);
                    notPendingIntent.cancel();

                }

            }, ALA_DURATION);

            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
            window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);

            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + AlarmProvider.this.getPackageName() + "/raw/alarm_sound");

            mediaPlayer = MediaPlayer.create(this, alarmSound);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();

            long[] pattern = { 0, 1000, 250, 500, 250, 500, 250, 500, 250, 500};

            if(VIBRATION == 1) {

                Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

                assert vibrator != null;
                vibrator.vibrate(pattern, -1);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        Calendar c = Calendar.getInstance();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd | MM");

        AP_TIME.setText(timeFormat.format(c.getTime()));

        AP_DATE.setText(dateFormat.format(c.getTime()));

        if(LABEL.equals(""))
            AP_LABEL.setVisibility(View.GONE);

        else
            AP_LABEL.setText(LABEL);

        String sno_dur_text;

        if(SNO_DURATION == 60000)
            sno_dur_text = "SNOOZE 1 MINUTE";

        else
            sno_dur_text = "SNOOZE " + (SNO_DURATION / 60000) + " MINUTES";

        AP_SNOOZE.setText(sno_dur_text);

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
                    JSONObject sys = json.getJSONObject("sys");
                    JSONObject main = json.getJSONObject("main");
                    JSONObject wind = json.getJSONObject("wind");

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = String.format("%.2f", main.getDouble("temp")) + " °C";

                    else
                        temperature = String.format("%.2f", basicFunctions.celsiusToFahrenheit(main.getDouble("temp"))) + " °F";

                    String iconText = basicFunctions.setWeatherIcon(weather.getInt("id"),
                            sys.getLong("sunrise") * 1000,
                            sys.getLong("sunset") * 1000);

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


                    AP_TEMPERATURE.setText(temperature);

                    AP_ICON.setTypeface(BasicFunctions.weatherFont);
                    AP_ICON.setText(Html.fromHtml(iconText));

                    AP_DESCRIPTION.setText(description);


                    AP_PRESSURE.setText(pressure);

                    AP_HUMIDITY.setText(humidity);

                    AP_MINMAXTEMP.setText(max_min_temp);


                    AP_WIND_SPEED.setText(wind_speed);

                    AP_WIND_ANGLE.setText(wind_angle);

                }

            } catch (JSONException e) {

                Toast.makeText(AlarmProvider.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

        }
    }

}
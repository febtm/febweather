package fmt.febuweather;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fmt.febuweather.helper.BasicFunctions;
import fmt.febuweather.helper.Menu;
import io.fabric.sdk.android.Fabric;


public class MyLocation extends AppCompatActivity {


    TextView ML_LOCATION, ML_ICON, ML_DESCRIPTION,
             ML_TEMPERATURE, ML_HUMIDITY, ML_PRESSURE,
             ML_WIND_SPEED, ML_WIND_ANGLE, ML_SUNRISE_TIME, ML_SUNSET_TIME;

    ImageButton MENU_BUTTON;

    String MY_LOCATION, MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE;

    Integer UNIT_TEMPERATURE, UNIT_WIND_SPEED;

    Integer NOT_MOR_TIME, NOT_AFT_TIME, NOT_EVE_TIME;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;


    ArrayList<LocationDailyForecastValues> ML_DAILY_LOCATION_LIST;
    RecyclerView mDailyRecyclerView;
    RecyclerView.Adapter mDailyAdapter;
    RecyclerView.LayoutManager mDailyLayoutManager;

    ArrayList<LocationHourlyForecastValues> ML_HOURLY_LOCATION_LIST;
    RecyclerView mHourlyRecyclerView;
    RecyclerView.Adapter mHourlyAdapter;
    RecyclerView.LayoutManager mHourlyLayoutManager;

    int COUNT;

    private ProgressDialog progressDialog;

    private BasicFunctions basicFunctions;

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_location);

        Fabric.with(this, new Crashlytics());

        AppEventsLogger.activateApp(getApplication());

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6196885651315287~3113095854");

        AdView mAdView = findViewById(R.id.ml_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        basicFunctions = new BasicFunctions(MyLocation.this);

        menu = new Menu(MyLocation.this);

        ML_LOCATION = findViewById(R.id.ml_location);
        ML_ICON = findViewById(R.id.ml_icon);
        ML_DESCRIPTION = findViewById(R.id.ml_description);
        ML_TEMPERATURE = findViewById(R.id.ml_temperature);
        ML_HUMIDITY = findViewById(R.id.ml_humidity);
        ML_PRESSURE = findViewById(R.id.ml_pressure);
        ML_WIND_SPEED = findViewById(R.id.ml_wind_speed);
        ML_WIND_ANGLE = findViewById(R.id.ml_wind_angle);
        ML_SUNRISE_TIME = findViewById(R.id.ml_sunrise_time);
        ML_SUNSET_TIME = findViewById(R.id.ml_sunset_time);
        mDailyRecyclerView = findViewById(R.id.ml_daily_forecast);
        mHourlyRecyclerView = findViewById(R.id.ml_hourly_forecast);
        MENU_BUTTON = findViewById(R.id.ml_menu);

        MENU_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                menu.LeftDrawer.toggleLeftDrawer();

            }
        });

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

        String SQL_NOT_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_NOTIFICATIONS_TABLE + "' ( '"
                + basicFunctions.NOTIFICATION_NAME + "' TEXT NOT NULL, '"
                + basicFunctions.NOTIFICATION_STATUS + "' INT(11) NOT NULL, '"
                + basicFunctions.NOTIFICATION_TIME + "' INT(11) NOT NULL ) ;";

        SQL_DB.execSQL(SQL_NOT_CREATE);

        String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

        DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

        if(DB_CURSOR.getCount() <= 0) {

            String SQL_NOT_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_NOTIFICATIONS_TABLE + "' ( '" + basicFunctions.NOTIFICATION_NAME + "', '"
                    + basicFunctions.NOTIFICATION_STATUS + "', '" + basicFunctions.NOTIFICATION_TIME
                    + "' ) VALUES ( 'Morning', '1', '10' ), ( 'Afternoon', '1', '16' ), ( 'Evening', '1', '22' ) ;";

            SQL_DB.execSQL(SQL_NOT_INSERT);

            NOT_MOR_TIME = 10;

            NOT_AFT_TIME = 16;

            NOT_EVE_TIME = 22;

            basicFunctions.setUpNotification(99994, NOT_MOR_TIME);

            basicFunctions.setUpNotification(99995, NOT_AFT_TIME);

            basicFunctions.setUpNotification(99996, NOT_EVE_TIME);

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

        if(DB_CURSOR.getCount() <= 0) {

            String SQL_ALA_SET_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_ALARM_SETTINGS_TABLE + "' ( '" + basicFunctions.ALARM_SETTING_NAME + "', '"
                    + basicFunctions.ALARM_SETTING_VALUE + "' ) VALUES ( 'Alarm_duration', '10' ), ( 'Label', '' )," +
                    " ( 'Snooze_duration', '5' ), ( 'Vibration', '1' ) ;";

            SQL_DB.execSQL(SQL_ALA_SET_INSERT);

        }

        DB_CURSOR.close();

        SQL_DB.close();


        SQL_DB = mOpenHelper.getWritableDatabase();

        String SQL_ALA_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_ALARMS_TABLE + "' ( '"
                + basicFunctions.ALARM_ID + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_STATUS + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_HOUR + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_MINUTE + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_SUNDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_MONDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_TUESDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_WEDNESDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_THURSDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_FRIDAY + "' INT(11) NOT NULL, '"
                + basicFunctions.ALARM_SATURDAY + "' INT(11) NOT NULL ) ;";

        SQL_DB.execSQL(SQL_ALA_CREATE);

        String SQL_ALA_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARMS_TABLE;

        DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_SELECT, null);

        if(DB_CURSOR.getCount() <= 0) {

            String SQL_ALA_SET_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_ALARMS_TABLE + "' ( '" + basicFunctions.ALARM_ID + "', '"
                    + basicFunctions.ALARM_STATUS + "', '" + basicFunctions.ALARM_HOUR + "', '"
                    + basicFunctions.ALARM_MINUTE + "', '" + basicFunctions.ALARM_SUNDAY + "', '"
                    + basicFunctions.ALARM_MONDAY + "', '" + basicFunctions.ALARM_TUESDAY + "', '"
                    + basicFunctions.ALARM_WEDNESDAY + "', '" + basicFunctions.ALARM_THURSDAY + "', '"
                    + basicFunctions.ALARM_FRIDAY + "', '" + basicFunctions.ALARM_SATURDAY
                    + "' ) VALUES ( 0, 1, " + 6 + ", " + 0 + ", "
                    + 1 + ", " + 1 + ", " + 1 + ", " + 1 + ", " + 1 + ", "
                    + 1 + ", " + 1 + " ) ;";

            SQL_DB.execSQL(SQL_ALA_SET_INSERT);

            basicFunctions.setUpAlarm(0, 6, 0, 1);
            basicFunctions.setUpAlarm(0, 6, 0, 2);
            basicFunctions.setUpAlarm(0, 6, 0, 3);
            basicFunctions.setUpAlarm(0, 6, 0, 4);
            basicFunctions.setUpAlarm(0, 6, 0, 5);
            basicFunctions.setUpAlarm(0, 6, 0, 6);
            basicFunctions.setUpAlarm(0, 6, 0, 7);

        }

        DB_CURSOR.close();

        SQL_DB.close();


        mDailyRecyclerView.setHasFixedSize(true);
        mDailyLayoutManager = new LinearLayoutManager(this);
        mDailyRecyclerView.setLayoutManager(mDailyLayoutManager);
        ML_DAILY_LOCATION_LIST = new ArrayList<>();
        mDailyAdapter = new DailyForecastAdapter(ML_DAILY_LOCATION_LIST);
        mDailyRecyclerView.setAdapter(mDailyAdapter);

        mHourlyRecyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mHourlyLayoutManager = linearLayoutManager;
        mHourlyRecyclerView.setLayoutManager(mHourlyLayoutManager);
        ML_HOURLY_LOCATION_LIST = new ArrayList<>();
        mHourlyAdapter = new HourlyForecastAdapter(ML_HOURLY_LOCATION_LIST);
        mHourlyRecyclerView.setAdapter(mHourlyAdapter);

        Intent previousIntent = getIntent();
        MY_LOCATION = previousIntent.getStringExtra(basicFunctions.LOCATION_NAME);
        MY_LOCATION_LATITUDE = previousIntent.getStringExtra(basicFunctions.LOCATION_LATITUDE);
        MY_LOCATION_LONGITUDE = previousIntent.getStringExtra(basicFunctions.LOCATION_LONGITUDE);

        if(MY_LOCATION == null && MY_LOCATION_LATITUDE == null && MY_LOCATION_LONGITUDE == null){

            if (ActivityCompat.checkSelfPermission(MyLocation.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MyLocation.this,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(MyLocation.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 0);

            }

            else
                if (basicFunctions.isConnectingToInternet())
                    getLocation();

                else {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {

                                case DialogInterface.BUTTON_POSITIVE:

                                    if(basicFunctions.isConnectingToInternet())
                                        getLocation();

                                    else
                                        Toast.makeText(MyLocation.this,
                                                "No Internet Connection. Try again later !",
                                                Toast.LENGTH_LONG).show();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:

                                    Toast.makeText(MyLocation.this,
                                            "No Internet Connection. Try again later !",
                                            Toast.LENGTH_LONG).show();

                                    dialog.dismiss();

                                    break;

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(MyLocation.this);
                    builder.setMessage("No Internet Connection. Try again ?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }

        }

        else {

            if (basicFunctions.isConnectingToInternet())
                fetchForecast();

            else {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {

                            case DialogInterface.BUTTON_POSITIVE:

                                if(basicFunctions.isConnectingToInternet())
                                    fetchForecast();

                                else
                                    Toast.makeText(MyLocation.this,
                                            "No Internet Connection. Try again later !",
                                            Toast.LENGTH_LONG).show();

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:

                                Toast.makeText(MyLocation.this,
                                        "No Internet Connection. Try again later !",
                                        Toast.LENGTH_LONG).show();

                                dialog.dismiss();

                                break;

                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(MyLocation.this);
                builder.setMessage("No Internet Connection. Try again ?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

            }

        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        switch (requestCode) {

            case 0: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    getLocation();

                else
                    Toast.makeText(MyLocation.this, "Kindly grant Location permission to continue !", Toast.LENGTH_LONG).show();

                break;
            }

            default:
                break;

        }
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

        fetchForecast();

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

    }


    private class Listener implements LocationListener {

        public void onLocationChanged(android.location.Location location) {}
        public void onProviderDisabled(String provider){}
        public void onProviderEnabled(String provider){}
        public void onStatusChanged(String provider, int status, Bundle extras){}

    }


    private void fetchForecast(){

        progressDialog = new ProgressDialog(MyLocation.this);
        progressDialog.setMessage("Fetching Forecast ... ");
        progressDialog.show();

        new GetCurrentForecastTask().execute();

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

                    String iconText = basicFunctions.setWeatherIcon(weather.getInt("id"),
                            sys.getLong("sunrise") * 1000,
                            sys.getLong("sunset") * 1000);

                    String description = weather.getString("main") + ", " + weather.getString("description");

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = String.format("%.2f", main.getDouble("temp")) + " °C";

                    else
                        temperature = String.format("%.2f", basicFunctions.celsiusToFahrenheit(main.getDouble("temp"))) + " °F";

                    String humidity = main.getDouble("humidity") + " %";

                    String pressure = main.getDouble("pressure") + " hPa";

                    String sunrise_time, sunset_time;

                    long sunrise_millis = sys.getLong("sunrise") * 1000L;
                    long sunset_millis = sys.getLong("sunset") * 1000L;
                    @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    sdf.setTimeZone(TimeZone.getDefault());

                    Date d = new Date(sunrise_millis);
                    sunrise_time = sdf.format(d);

                    d = new Date(sunset_millis);
                    sunset_time = sdf.format(d);

                    String wind_speed;

                    if(UNIT_WIND_SPEED == 0)
                        wind_speed = wind.getDouble("speed") + " m / s";

                    else
                        wind_speed = basicFunctions.metresPerSecondToKilometresPerHour(wind.getDouble("speed")) + " km / hr";

                    String wind_angle = wind.getDouble("deg") + " degrees";

                    ML_LOCATION.setText(MY_LOCATION);

                    ML_ICON.setTypeface(BasicFunctions.weatherFont);
                    ML_ICON.setText(Html.fromHtml(iconText));

                    ML_DESCRIPTION.setText(description);
                    ML_TEMPERATURE.setText(temperature);
                    ML_HUMIDITY.setText(humidity);
                    ML_PRESSURE.setText(pressure);

                    ML_WIND_SPEED.setText(wind_speed);
                    ML_WIND_ANGLE.setText(wind_angle);
                    ML_SUNRISE_TIME.setText(sunrise_time);
                    ML_SUNSET_TIME.setText(sunset_time);

                    COUNT = 0;

                    new GetHourlyForecastTask().execute();

                }

            } catch (JSONException e) {

                Toast.makeText(MyLocation.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

        }
    }


    @SuppressLint("StaticFieldLeak")
    private class GetHourlyForecastTask extends AsyncTask<String, Void, JSONObject> {

        private GetHourlyForecastTask() {}

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            try {

                jsonWeather = basicFunctions.getWeatherJSON(basicFunctions.OWM_HOURLY_FORECAST_URL,
                        MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE);

            } catch (Exception e) {

                Toast.makeText(MyLocation.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

            return jsonWeather;
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                if(json != null){

                    JSONObject list = json.getJSONArray("list").getJSONObject(COUNT);
                    JSONObject weather = list.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = list.getJSONObject("main");


                    long millis = list.getLong("dt")*1000L;

                    Date d = new Date(millis);
                    Calendar original = Calendar.getInstance();
                    original.setTimeInMillis(millis);

                    Calendar today = Calendar.getInstance();

                    String time;

                    if(today.get(Calendar.DATE) == original.get(Calendar.DATE)){

                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        sdf.setTimeZone(TimeZone.getDefault());
                        time = "Today, " + sdf.format(d);

                    } else{

                        SimpleDateFormat sdf = new SimpleDateFormat("EEE, HH:mm");
                        sdf.setTimeZone(TimeZone.getDefault());
                        time = sdf.format(d);

                    }

                    Location location = new Location(MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE);

                    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

                    String sunriseForDate = calculator.getOfficialSunriseForDate(original) + ":00";
                    String sunsetForDate = calculator.getOfficialSunsetForDate(original) + ":00";

                    Time sunRise = Time.valueOf(sunriseForDate);
                    Time sunSet = Time.valueOf(sunsetForDate);

                    String iconText = basicFunctions.setWeatherIcon(weather.getInt("id"),
                            sunRise.getTime() * 1000, sunSet.getTime() * 1000);

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = main.getInt("temp") + " °C";

                    else
                        temperature = basicFunctions.celsiusToFahrenheit(main.getInt("temp")) + " °F";

                    String pressure = main.getInt("pressure") + " hPa";

                    String humidity = main.getInt("humidity") + " %";

                    LocationHourlyForecastValues obj = new LocationHourlyForecastValues(time, iconText, temperature, humidity, pressure);

                    ML_HOURLY_LOCATION_LIST.add(obj);

                    mHourlyAdapter.notifyDataSetChanged();

                    COUNT++;

                    if(COUNT == 16){

                        COUNT = 0;

                        new GetDailyForecastTask().execute();

                    }

                    else
                        new GetHourlyForecastTask().execute();

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MyLocation.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }
        }
    }


    private class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.DataHolder> {

        private ArrayList<LocationHourlyForecastValues> mDataset;

        class DataHolder extends RecyclerView.ViewHolder {

            TextView ML_H_TIME, ML_H_ICON, ML_H_TEMPERATURE, ML_H_HUMIDITY, ML_H_PRESSURE;

            DataHolder(final View itemView) {
                super(itemView);

                ML_H_TIME = itemView.findViewById(R.id.ml_li_h_time);
                ML_H_ICON = itemView.findViewById(R.id.ml_li_h_icon);
                ML_H_TEMPERATURE = itemView.findViewById(R.id.ml_li_h_temperature);
                ML_H_HUMIDITY = itemView.findViewById(R.id.ml_li_h_humidity);
                ML_H_PRESSURE = itemView.findViewById(R.id.ml_li_h_pressure);

                ML_H_ICON.setTypeface(BasicFunctions.weatherFont);

            }

        }

        HourlyForecastAdapter(ArrayList<LocationHourlyForecastValues> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_ml_li_hourly, parent, false);

            mHourlyRecyclerView.setMinimumHeight(mHourlyRecyclerView.getHeight() + view.getHeight());

            return new DataHolder(view);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onBindViewHolder(@NonNull DataHolder holder, int position) {

            holder.ML_H_TIME.setText(mDataset.get(position).getML_H_TIME());
            holder.ML_H_ICON.setText(Html.fromHtml(mDataset.get(position).getML_H_ICON()));
            holder.ML_H_TEMPERATURE.setText(mDataset.get(position).getML_H_TEMPERATURE());
            holder.ML_H_HUMIDITY.setText(mDataset.get(position).getML_H_HUMIDITY());
            holder.ML_H_PRESSURE.setText(mDataset.get(position).getML_H_PRESSURE());

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }


    private class LocationHourlyForecastValues {

        private String ML_H_TIME;
        private String ML_H_ICON;
        private String ML_H_TEMPERATURE;
        private String ML_H_HUMIDITY;
        private String ML_H_PRESSURE;

        LocationHourlyForecastValues(String time, String icon, String temperature,String humidity, String pressure){
            ML_H_TIME = time;
            ML_H_ICON = icon;
            ML_H_TEMPERATURE = temperature;
            ML_H_HUMIDITY = humidity;
            ML_H_PRESSURE = pressure;
        }

        String getML_H_TIME() {
            return ML_H_TIME;
        }

        String getML_H_ICON() {
            return ML_H_ICON;
        }

        String getML_H_TEMPERATURE() {
            return ML_H_TEMPERATURE;
        }

        String getML_H_HUMIDITY() {
            return ML_H_HUMIDITY;
        }

        String getML_H_PRESSURE() {
            return ML_H_PRESSURE;
        }

    }


    @SuppressLint("StaticFieldLeak")
    private class GetDailyForecastTask extends AsyncTask<String, Void, JSONObject> {

        private GetDailyForecastTask() {}

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            try {

                jsonWeather = basicFunctions.getWeatherJSON(basicFunctions.OWM_DAILY_FORECAST_URL,
                        MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE);

            } catch (Exception e) {

                Toast.makeText(MyLocation.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

            return jsonWeather;
        }

        @SuppressLint("SimpleDateFormat")
        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                if(json != null){

                    JSONObject list = json.getJSONArray("list").getJSONObject(COUNT);
                    JSONObject weather = list.getJSONArray("weather").getJSONObject(0);
                    JSONObject temp = list.getJSONObject("temp");


                    long millis = list.getLong("dt")*1000L;

                    Date d = new Date(millis);
                    Calendar original = Calendar.getInstance();
                    original.setTimeInMillis(millis);

                    Calendar today = Calendar.getInstance();

                    String date;

                    if(today.get(Calendar.DATE) == original.get(Calendar.DATE)){

                        SimpleDateFormat sdf = new SimpleDateFormat("d | M");
                        sdf.setTimeZone(TimeZone.getDefault());
                        date = "Today, " + sdf.format(d);

                    } else{

                        SimpleDateFormat sdf = new SimpleDateFormat("EE, d | M");
                        sdf.setTimeZone(TimeZone.getDefault());
                        date = sdf.format(d);

                    }

                    Location location = new Location(MY_LOCATION_LATITUDE, MY_LOCATION_LONGITUDE);

                    SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(location, TimeZone.getDefault());

                    String sunriseForDate = calculator.getOfficialSunriseForDate(original) + ":00";
                    String sunsetForDate = calculator.getOfficialSunsetForDate(original) + ":00";

                    Time sunRise = Time.valueOf(sunriseForDate);
                    Time sunSet = Time.valueOf(sunsetForDate);

                    String iconText = basicFunctions.setWeatherIcon(weather.getInt("id"),
                            sunRise.getTime() * 1000, sunSet.getTime() * 1000);

                    String description = weather.getString("main") + ", " + weather.getString("description");

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = temp.getInt("min") + " / " + temp.getInt("max") + " °C";

                    else
                        temperature = basicFunctions.celsiusToFahrenheit(temp.getInt("min"))
                                + " / " + basicFunctions.celsiusToFahrenheit(temp.getInt("max")) + " °F";

                    LocationDailyForecastValues obj = new LocationDailyForecastValues(date, iconText, description, temperature);

                    ML_DAILY_LOCATION_LIST.add(obj);

                    mDailyAdapter.notifyDataSetChanged();

                    COUNT++;

                    if(COUNT == 10)
                        progressDialog.dismiss();

                    else
                        new GetDailyForecastTask().execute();

                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MyLocation.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }
        }
    }


    private class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DataHolder> {

        private ArrayList<LocationDailyForecastValues> mDataset;

        class DataHolder extends RecyclerView.ViewHolder {

            TextView ML_D_DATE, ML_D_ICON, ML_D_DESCRIPTION, ML_D_TEMPERATURE;

            DataHolder(final View itemView) {
                super(itemView);

                ML_D_DATE = itemView.findViewById(R.id.ml_li_d_date);
                ML_D_ICON = itemView.findViewById(R.id.ml_li_d_icon);
                ML_D_DESCRIPTION = itemView.findViewById(R.id.ml_li_d_description);
                ML_D_TEMPERATURE = itemView.findViewById(R.id.ml_li_d_temperature);

                ML_D_ICON.setTypeface(BasicFunctions.weatherFont);

            }

        }

        DailyForecastAdapter(ArrayList<LocationDailyForecastValues> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_ml_li_daily, parent, false);

            mDailyRecyclerView.setMinimumHeight(mDailyRecyclerView.getHeight() + view.getHeight());

            return new DataHolder(view);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onBindViewHolder(@NonNull DataHolder holder, int position) {

            holder.ML_D_DATE.setText(mDataset.get(position).getML_D_DATE());
            holder.ML_D_DESCRIPTION.setText(mDataset.get(position).getML_D_DESCRIPTION());
            holder.ML_D_ICON.setText(Html.fromHtml(mDataset.get(position).getML_D_ICON()));
            holder.ML_D_TEMPERATURE.setText(mDataset.get(position).getML_D_TEMPERATURE());

        }

        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

    }


    private class LocationDailyForecastValues {

        private String ML_D_DATE;
        private String ML_D_ICON;
        private String ML_D_DESCRIPTION;
        private String ML_D_TEMPERATURE;

        LocationDailyForecastValues(String date, String icon, String description, String temperature){
            ML_D_DATE = date;
            ML_D_DESCRIPTION = description;
            ML_D_ICON = icon;
            ML_D_TEMPERATURE = temperature;
        }

        String getML_D_DATE() {
            return ML_D_DATE;
        }

        String getML_D_DESCRIPTION() {
            return ML_D_DESCRIPTION;
        }

        String getML_D_ICON() {
            return ML_D_ICON;
        }

        String getML_D_TEMPERATURE() {
            return ML_D_TEMPERATURE;
        }

    }

}
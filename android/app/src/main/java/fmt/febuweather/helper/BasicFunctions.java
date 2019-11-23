package fmt.febuweather.helper;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

//Created by Febin M Thomas on 04-May-17.
//Created by Febin M Thomas on 21-May-18.


public class BasicFunctions {


    private Context mContext;


    public final String OWM_CURRENT_FORECAST_URL =
            "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";

    public final String OWM_HOURLY_FORECAST_URL =
            "http://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric&cnt=16";

    public final String OWM_DAILY_FORECAST_URL =
            "http://api.openweathermap.org/data/2.5/forecast/daily?lat=%s&lon=%s&units=metric&cnt=10";

    public static Typeface weatherFont;


    public static final String SEND_EMAIL = "https://febtech.000webhostapp.com/android_febweather/sendEmail.php";


    private static final String DATABASE_NAME = "febweather.db";
    private static final int DATABASE_VERSION = 1;

    public String MY_LOCATIONS_TABLE = "my_locations";
    public String LOCATION_NAME = "location_name";
    public String LOCATION_LATITUDE = "location_latitude";
    public String LOCATION_LONGITUDE = "location_longitude";

    public String MY_UNITS_TABLE = "my_units";
    public String UNIT_NAME = "unit_name";
    public String UNIT_VALUE = "unit_value";

    public String MY_NOTIFICATIONS_TABLE = "my_notifications";
    public String NOTIFICATION_NAME = "notification_name";
    public String NOTIFICATION_STATUS = "notification_status";
    public String NOTIFICATION_TIME = "notification_time";

    public String MY_ALARM_SETTINGS_TABLE = "my_alarm_settings";
    public String ALARM_SETTING_NAME = "alarm_setting_name";
    public String ALARM_SETTING_VALUE = "alarm_setting_value";

    public String MY_ALARMS_TABLE = "my_alarms";
    public String ALARM_ID = "alarm_id";
    public String ALARM_STATUS = "alarm_status";
    public String ALARM_HOUR = "alarm_hour";
    public String ALARM_MINUTE = "alarm_minute";
    public String ALARM_SUNDAY = "alarm_sunday";
    public String ALARM_MONDAY = "alarm_monday";
    public String ALARM_TUESDAY = "alarm_tuesday";
    public String ALARM_WEDNESDAY = "alarm_wednesday";
    public String ALARM_THURSDAY = "alarm_thursday";
    public String ALARM_FRIDAY = "alarm_friday";
    public String ALARM_SATURDAY = "alarm_saturday";


    public BasicFunctions(Context context){

        mContext = context;

        weatherFont = Typeface.createFromAsset(mContext.getAssets(), "fonts/weathericons-regular-webfont.ttf");

    }


    public double celsiusToFahrenheit(double celsius)
    {
        return (celsius * 9) / 5 + 32;
    }


    public double metresPerSecondToKilometresPerHour(double metresPerSecond)
    {
        return round((metresPerSecond * 3.6));
    }


    private double round(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();

    }


    public void setUpNotification(int id, int hour){

        Calendar calendar = Calendar.getInstance();

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        intent.putExtra("For", "Notification");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long time = (calendar.getTimeInMillis()-(calendar.getTimeInMillis()%60000));

        if(System.currentTimeMillis()>time) {
            time = time + (1000*60*60*24);
        }

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time, AlarmManager.INTERVAL_DAY, pendingIntent);

    }


    public void setUpAlarm(int id, int hour, int minute, int day){

        Calendar calendar = Calendar.getInstance();

        if(day == 1)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);

        else if(day == 2)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

        else if(day == 3)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);

        else if(day == 4)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);

        else if(day == 5)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);

        else if(day == 6)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);

        else
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        if(calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
        }

        int identifier = Integer.parseInt(String.valueOf(day) + "" + String.valueOf(id));

        Intent intent = new Intent(mContext, AlarmReceiver.class);
        intent.putExtra("For", "Alarm");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, identifier, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long time = calendar.getTimeInMillis();

        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, time,AlarmManager.INTERVAL_DAY, pendingIntent);

    }


    public Address getLatLong(String location) {

        List<Address> addressList = null;

        Address address;

        assert location != null;

        Geocoder geocoder = new Geocoder(mContext);

        try {

            addressList = geocoder.getFromLocationName(location, 100);

        } catch (IOException e) {

            e.printStackTrace();

        }

        assert addressList != null;
        address = addressList.get(0);

        return address;
    }


    public JSONObject getWeatherJSON(String URL, String lat, String lon){

        try {

            java.net.URL url = new URL(String.format(URL, lat, lon));

            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            String OPEN_WEATHER_MAP_API = "228a0735d5437d1e6fe3078844cebafc";

            connection.addRequestProperty("x-api-key", OPEN_WEATHER_MAP_API);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;
            while((tmp=reader.readLine())!=null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }


    public String setWeatherIcon(int actualId, long sunrise, long sunset){

        int id = actualId / 100;
        String icon = "";

        if(actualId == 800){

            long currentTime = new Date().getTime();

            if(currentTime>=sunrise && currentTime<sunset) {
                icon = "&#xf00d;";
            } else {
                icon = "&#xf02e;";
            }

        } else {

            switch(id) {

                case 2 : icon = "&#xf01e;";
                    break;
                case 3 : icon = "&#xf01c;";
                    break;
                case 7 : icon = "&#xf014;";
                    break;
                case 8 : icon = "&#xf013;";
                    break;
                case 6 : icon = "&#xf01b;";
                    break;
                case 5 : icon = "&#xf019;";
                    break;

            }
        }
        return icon;
    }


    public static class DatabaseHelper extends SQLiteOpenHelper {

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    }


    public boolean isConnectingToInternet() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            assert connectivityManager != null;
            Network[] networks = connectivityManager.getAllNetworks();
            NetworkInfo networkInfo;

            for (Network mNetwork : networks) {

                networkInfo = connectivityManager.getNetworkInfo(mNetwork);

                if (networkInfo.getState().equals(NetworkInfo.State.CONNECTED)) {
                    return true;
                }
            }

        } else {

            if (connectivityManager != null) {

                NetworkInfo[] info = connectivityManager.getAllNetworkInfo();

                if (info != null) {

                    for (NetworkInfo anInfo : info) {

                        if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

}
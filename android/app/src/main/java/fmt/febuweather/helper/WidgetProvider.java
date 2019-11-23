package fmt.febuweather.helper;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

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

    private Runnable mTimeStatusChecker;

    private int mTimeDetailsInterval = 5 * 1000;

    private Handler mTimeHandler;


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


            mTimeHandler = new Handler();

            mTimeStatusChecker = new Runnable() {
                @Override
                public void run() {
                    try {

                        Calendar c = Calendar.getInstance();

                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("EE, d / M");

                        remoteViews.setTextViewText(R.id.wi_time, timeFormat.format(c.getTime()));

                        remoteViews.setTextViewText(R.id.wi_date, dateFormat.format(c.getTime()));

                        mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

                    } finally {
                        mTimeHandler.postDelayed(mTimeStatusChecker, mTimeDetailsInterval);
                    }
                }
            };

            startRepeatingTask();


            SQL_DB = mOpenHelper.getReadableDatabase();

            String SQL_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_LOCATIONS_TABLE + "' ( '"
                    + basicFunctions.LOCATION_NAME + "' TEXT NOT NULL, '"
                    + basicFunctions.LOCATION_LATITUDE + "' TEXT NOT NULL, '"
                    + basicFunctions.LOCATION_LONGITUDE + "' TEXT NOT NULL );";

            SQL_DB.execSQL(SQL_CREATE);

            String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE + " LIMIT 1";

            DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, new String[] {});

            if(DB_CURSOR.moveToFirst()) {

                MY_LOCATION = DB_CURSOR.getString(0);
                MY_LOCATION_LATITUDE = DB_CURSOR.getString(1);
                MY_LOCATION_LONGITUDE = DB_CURSOR.getString(2);

                remoteViews.setViewVisibility(R.id.wi_location, View.VISIBLE);

                remoteViews.setTextViewText(R.id.wi_location, MY_LOCATION);

                if(basicFunctions.isConnectingToInternet())
                    fetchForecast();

                else {

                    remoteViews.setViewVisibility(R.id.wi_no_network, View.VISIBLE);

                    remoteViews.setViewVisibility(R.id.wi_no_location, View.GONE);

                    remoteViews.setViewVisibility(R.id.wi_forecast_details, View.GONE);

                }

            }

            else {

                remoteViews.setViewVisibility(R.id.wi_location, View.GONE);

                remoteViews.setViewVisibility(R.id.wi_no_network, View.GONE);

                remoteViews.setViewVisibility(R.id.wi_no_location, View.VISIBLE);

                remoteViews.setViewVisibility(R.id.wi_forecast_details, View.GONE);

            }

            Intent openIntent = new Intent(mContext, MyLocation.class);
            PendingIntent openPendingIntent = PendingIntent.getActivity(context, 99998, openIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.wi_no_location_button, openPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wi_forecast_details, openPendingIntent);

            Intent refreshIntent = new Intent(mContext, WidgetProvider.class);
            refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 99999, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.wi_no_network_button, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wi_no_network_refresh, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wi_no_location_refresh, refreshPendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.wi_location_details_refresh, refreshPendingIntent);

            mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

            DB_CURSOR.close();

            SQL_DB.close();

        }
    }


    private void fetchForecast(){

        remoteViews.setViewVisibility(R.id.wi_no_network, View.GONE);

        remoteViews.setViewVisibility(R.id.wi_no_location, View.GONE);

        remoteViews.setViewVisibility(R.id.wi_forecast_details, View.VISIBLE);

        new GetCurrentForecastTask().execute();

    }


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


                    remoteViews.setTextViewText(R.id.wi_temperature, temperature);

                    remoteViews.setTextViewText(R.id.wi_description, description);


                    remoteViews.setTextViewText(R.id.wi_pressure, pressure);

                    remoteViews.setTextViewText(R.id.wi_humidity, humidity);

                    remoteViews.setTextViewText(R.id.wi_maxmintemp, max_min_temp);


                    remoteViews.setTextViewText(R.id.wi_wind_speed, wind_speed);

                    remoteViews.setTextViewText(R.id.wi_wind_angle, wind_angle);


                    mAppWidgetManager.updateAppWidget(mWidgetId, remoteViews);

                }

            } catch (JSONException e) {

                Toast.makeText(mContext, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

        }
    }

    void startRepeatingTask() {
        mTimeStatusChecker.run();
    }

}
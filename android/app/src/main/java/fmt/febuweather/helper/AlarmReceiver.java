package fmt.febuweather.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fmt.febuweather.MyLocation;
import fmt.febuweather.R;

import static android.content.Context.VIBRATOR_SERVICE;


public class AlarmReceiver extends BroadcastReceiver {

    BasicFunctions basicFunctions;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;

    Context mContext;

    String LOCATION, LATITUDE, LONGITUDE;


    @Override
    public void onReceive(Context context, Intent intent)
    {

        this.mContext = context;

        if(intent.getStringExtra("For").equals("Notification")) {

            basicFunctions = new BasicFunctions(mContext);

            mOpenHelper = new BasicFunctions.DatabaseHelper(mContext);

            SQL_DB = mOpenHelper.getReadableDatabase();

            String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE + " LIMIT 1";

            DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, new String[]{});

            if (DB_CURSOR.moveToFirst()) {

                LOCATION = DB_CURSOR.getString(0);
                LATITUDE = DB_CURSOR.getString(1);
                LONGITUDE = DB_CURSOR.getString(2);

                if(basicFunctions.isConnectingToInternet())
                    new GetCurrentForecastTask().execute();

                else {

                    String message = "No Internet Connection | " +
                            "Connect to the Internet to receive Forecast Updates of " + LOCATION + " !";

                    setUpNotification(message);

                }

            }

            else {

                String message = "Your Location List is empty | Click here to add a location !";

                setUpNotification(message);

            }

        }

        else if(intent.getStringExtra("For").equals("Alarm")){

            Intent alarmIntent = new Intent(mContext, AlarmProvider.class);
            alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            mContext.startActivity(alarmIntent);

        }

    }


    private void setUpNotification(String message){

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(mContext)
                        .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.app_logo_main))
                        .setSmallIcon(R.drawable.me_my_location)
                        .setContentTitle("FebuWeather")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setContentText(message)
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(mContext, MyLocation.class);
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 99997, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        NotificationManager manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());

        try {

            Uri alarmSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + mContext.getPackageName() + "/raw/notification_sound");
            Ringtone r = RingtoneManager.getRingtone(mContext, alarmSound);
            r.play();

            Vibrator v = (Vibrator) mContext.getSystemService(VIBRATOR_SERVICE);
            v.vibrate(500);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


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

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            } catch (JSONException e) {

                Toast.makeText(mContext, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }
        }
    }
}
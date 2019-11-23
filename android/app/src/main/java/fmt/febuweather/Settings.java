package fmt.febuweather;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import fmt.febuweather.helper.AlarmReceiver;
import fmt.febuweather.helper.BasicFunctions;
import fmt.febuweather.helper.Menu;


public class Settings extends AppCompatActivity {


    Switch UNIT_TEMP_SWITCH, UNIT_WIND_SWITCH, NOT_MOR_SWITCH, NOT_AFT_SWITCH, NOT_EVE_SWITCH, ALA_VIB_SWITCH;

    ImageButton MENU_BUTTON, SE_AL_LABEL_CANCEL, NOT_MOR_MINUS, NOT_MOR_PLUS, NOT_AFT_MINUS, NOT_AFT_PLUS,
                NOT_EVE_MINUS, NOT_EVE_PLUS, ALA_ADD, ALA_LAB_SET, ALA_DUR_MINUS, ALA_DUR_PLUS,
                ALA_SNO_MINUS, ALA_SNO_PLUS, ALA_SET, ALA_CANCEL;

    EditText ALA_LAB_TEXT;

    TimePicker ALA_TIMEPICKER;

    TextView NOT_MOR_TEXT, NOT_AFT_TEXT, NOT_EVE_TEXT, ALA_DUR_TEXT, ALA_SNO_TEXT;

    CheckBox ALA_SUNDAY, ALA_MONDAY, ALA_TUESDAY, ALA_WEDNESDAY, ALA_THURSDAY, ALA_FRIDAY, ALA_SATURDAY;

    LinearLayout LL_ALL_SETTINGS, LL_AL_SETTINGS;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;

    ArrayList<AlarmListValues> SE_AL_ALARM_LIST;
    RecyclerView mAlarmRecyclerView;
    RecyclerView.Adapter mAlarmAdapter;
    RecyclerView.LayoutManager mAlarmLayoutManager;


    BasicFunctions basicFunctions;

    private Menu menu;


    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MENU_BUTTON = findViewById(R.id.se_menu);
        UNIT_TEMP_SWITCH = findViewById(R.id.se_unit_temp_switch);
        UNIT_WIND_SWITCH = findViewById(R.id.se_unit_wind_speed_switch);

        NOT_MOR_SWITCH = findViewById(R.id.se_not_mor_switch);
        NOT_AFT_SWITCH = findViewById(R.id.se_not_aft_switch);
        NOT_EVE_SWITCH = findViewById(R.id.se_not_eve_switch);

        NOT_MOR_MINUS = findViewById(R.id.se_not_mor_minus);
        NOT_MOR_PLUS = findViewById(R.id.se_not_mor_plus);

        NOT_AFT_MINUS = findViewById(R.id.se_not_aft_minus);
        NOT_AFT_PLUS = findViewById(R.id.se_not_aft_plus);

        NOT_EVE_MINUS = findViewById(R.id.se_not_eve_minus);
        NOT_EVE_PLUS = findViewById(R.id.se_not_eve_plus);

        NOT_MOR_TEXT = findViewById(R.id.se_not_mor_time);
        NOT_AFT_TEXT = findViewById(R.id.se_not_aft_time);
        NOT_EVE_TEXT = findViewById(R.id.se_not_eve_time);

        ALA_ADD = findViewById(R.id.se_al_add_alarm);

        ALA_DUR_MINUS = findViewById(R.id.se_al_dur_minus);
        ALA_DUR_PLUS = findViewById(R.id.se_al_dur_plus);

        ALA_LAB_SET = findViewById(R.id.se_al_set_label);
        ALA_LAB_TEXT = findViewById(R.id.se_al_label);
        SE_AL_LABEL_CANCEL = findViewById(R.id.se_al_label_cancel);

        SE_AL_LABEL_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ALA_LAB_TEXT.setText("");

            }
        });

        ALA_SNO_MINUS = findViewById(R.id.se_al_sno_minus);
        ALA_SNO_PLUS = findViewById(R.id.se_al_sno_plus);

        ALA_VIB_SWITCH = findViewById(R.id.se_al_vibration_switch);

        ALA_DUR_TEXT = findViewById(R.id.se_al_dur_time);
        ALA_SNO_TEXT = findViewById(R.id.se_al_sno_time);

        LL_AL_SETTINGS = findViewById(R.id.se_al_settings);
        LL_ALL_SETTINGS = findViewById(R.id.se_all_settings);

        ALA_SET = findViewById(R.id.se_al_set_alarm);

        ALA_CANCEL = findViewById(R.id.se_al_cancel_alarm);

        ALA_SUNDAY = findViewById(R.id.se_al_set_sunday);
        ALA_MONDAY = findViewById(R.id.se_al_set_monday);
        ALA_TUESDAY = findViewById(R.id.se_al_set_tuesday);
        ALA_WEDNESDAY = findViewById(R.id.se_al_set_wednesday);
        ALA_THURSDAY = findViewById(R.id.se_al_set_thursday);
        ALA_FRIDAY = findViewById(R.id.se_al_set_friday);
        ALA_SATURDAY = findViewById(R.id.se_al_set_saturday);

        ALA_TIMEPICKER = findViewById(R.id.se_al_set_timepicker);

        ALA_TIMEPICKER.setIs24HourView(true);

        basicFunctions = new BasicFunctions(Settings.this);

        mOpenHelper = new BasicFunctions.DatabaseHelper(this);

        menu = new Menu(Settings.this);

        MENU_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.LeftDrawer.toggleLeftDrawer();

            }
        });

        mAlarmRecyclerView = findViewById(R.id.se_al_alarm_list);

        mAlarmRecyclerView.setHasFixedSize(true);
        mAlarmLayoutManager = new LinearLayoutManager(this);
        mAlarmRecyclerView.setLayoutManager(mAlarmLayoutManager);
        SE_AL_ALARM_LIST = new ArrayList<>();
        mAlarmAdapter = new DailyForecastAdapter(SE_AL_ALARM_LIST);
        mAlarmRecyclerView.setAdapter(mAlarmAdapter);


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
                UNIT_TEMP_SWITCH.setChecked(false);

            else
                UNIT_TEMP_SWITCH.setChecked(true);

            DB_CURSOR.moveToNext();


            if(DB_CURSOR.getInt(1) == 0)
                UNIT_WIND_SWITCH.setChecked(false);

            else
                UNIT_WIND_SWITCH.setChecked(true);
        }

        else {

            String SQL_UNIT_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_UNITS_TABLE + "' ( '" + basicFunctions.UNIT_NAME + "', '"
                    + basicFunctions.UNIT_VALUE + "' ) VALUES ( 'Temperature', '0' ), ( 'Wind_speed', '0' ) ;";

            SQL_DB.execSQL(SQL_UNIT_INSERT);

            UNIT_TEMP_SWITCH.setChecked(false);

            UNIT_WIND_SWITCH.setChecked(false);

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

        if(DB_CURSOR.getCount() > 0) {

            DB_CURSOR.moveToFirst();


            if(DB_CURSOR.getInt(1) == 0)
                NOT_MOR_SWITCH.setChecked(false);

            else
                NOT_MOR_SWITCH.setChecked(true);

            String time = String.format("%02d", DB_CURSOR.getInt(2)) + " : 00";

            NOT_MOR_TEXT.setText(time);

            DB_CURSOR.moveToNext();


            if(DB_CURSOR.getInt(1) == 0)
                NOT_AFT_SWITCH.setChecked(false);

            else
                NOT_AFT_SWITCH.setChecked(true);

            time = String.format("%02d", DB_CURSOR.getInt(2)) + " : 00";

            NOT_AFT_TEXT.setText(time);

            DB_CURSOR.moveToNext();


            if(DB_CURSOR.getInt(1) == 0)
                NOT_EVE_SWITCH.setChecked(false);

            else
                NOT_EVE_SWITCH.setChecked(true);

            time = String.format("%02d", DB_CURSOR.getInt(2)) + " : 00";

            NOT_EVE_TEXT.setText(time);

        }

        else {

            String SQL_NOT_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_NOTIFICATIONS_TABLE + "' ( '" + basicFunctions.NOTIFICATION_NAME + "', '"
                    + basicFunctions.NOTIFICATION_STATUS + "', '" + basicFunctions.NOTIFICATION_TIME
                    + "' ) VALUES ( 'Morning', '1', '10' ), ( 'Afternoon', '1', '16' ), ( 'Evening', '1', '22' ) ;";

            SQL_DB.execSQL(SQL_NOT_INSERT);

            NOT_MOR_SWITCH.setChecked(true);
            String time = "10 : 00";
            NOT_MOR_TEXT.setText(time);
            basicFunctions.setUpNotification(99994, 10);

            NOT_AFT_SWITCH.setChecked(true);
            time = "16 : 00";
            NOT_AFT_TEXT.setText(time);
            basicFunctions.setUpNotification(99995, 16);

            NOT_EVE_SWITCH.setChecked(true);
            time = "22 : 00";
            NOT_EVE_TEXT.setText(time);
            basicFunctions.setUpNotification(99996, 22);

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

            String time;

            if(Integer.parseInt(DB_CURSOR.getString(1)) == 1)
                time = DB_CURSOR.getString(1) + " minute";

            else
                time = DB_CURSOR.getString(1) + " minutes";

            ALA_DUR_TEXT.setText(time);

            DB_CURSOR.moveToNext();

            ALA_LAB_TEXT.setText(DB_CURSOR.getString(1));

            DB_CURSOR.moveToNext();

            if(Integer.parseInt(DB_CURSOR.getString(1)) == 1)
                time = DB_CURSOR.getString(1) + " minute";

            else
                time = DB_CURSOR.getString(1) + " minutes";

            ALA_SNO_TEXT.setText(time);

            DB_CURSOR.moveToNext();

            if(Integer.parseInt(DB_CURSOR.getString(1)) == 0)
                ALA_VIB_SWITCH.setChecked(false);

            else
                ALA_VIB_SWITCH.setChecked(true);

        }

        else {

            String SQL_ALA_SET_INSERT = "INSERT INTO '"
                    + basicFunctions.MY_ALARM_SETTINGS_TABLE + "' ( '" + basicFunctions.ALARM_SETTING_NAME + "', '"
                    + basicFunctions.ALARM_SETTING_VALUE + "' ) VALUES ( 'Alarm_duration', '10' ), ( 'Label', '' )," +
                    " ( 'Snooze_duration', '5' ), ( 'Vibration', '1' ) ;";

            SQL_DB.execSQL(SQL_ALA_SET_INSERT);

            String time = "10 minutes";
            ALA_DUR_TEXT.setText(time);

            time = "5 minutes";
            ALA_SNO_TEXT.setText(time);

            ALA_VIB_SWITCH.setChecked(true);

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

        if(DB_CURSOR.getCount() > 0) {

            DB_CURSOR.moveToFirst();

            do {

                AlarmListValues obj = new AlarmListValues(DB_CURSOR.getInt(0), DB_CURSOR.getInt(1), DB_CURSOR.getInt(2)
                        , DB_CURSOR.getInt(3), DB_CURSOR.getInt(4), DB_CURSOR.getInt(5), DB_CURSOR.getInt(6)
                        , DB_CURSOR.getInt(7), DB_CURSOR.getInt(8), DB_CURSOR.getInt(9), DB_CURSOR.getInt(10));

                SE_AL_ALARM_LIST.add(obj);

                mAlarmAdapter.notifyDataSetChanged();

            }while(DB_CURSOR.moveToNext());

        }

        else {

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


        AdView mAdView = findViewById(R.id.se_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        UNIT_TEMP_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_UNIT_UPDATE;

                if(isChecked)
                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_UNITS_TABLE
                            + " SET " + basicFunctions.UNIT_VALUE + " = 1 WHERE "
                            + basicFunctions.UNIT_NAME + " = 'Temperature' ;";

                else
                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_UNITS_TABLE
                            + " SET " + basicFunctions.UNIT_VALUE + " = 0 WHERE "
                            + basicFunctions.UNIT_NAME + " = 'Temperature' ;";

                SQL_DB.execSQL(SQL_UNIT_UPDATE);

                SQL_DB.close();


            }
        });


        UNIT_WIND_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_UNIT_UPDATE;

                if(isChecked)
                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_UNITS_TABLE
                            + " SET " + basicFunctions.UNIT_VALUE + " = 1 WHERE "
                            + basicFunctions.UNIT_NAME + " = 'Wind_speed' ;";

                else
                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_UNITS_TABLE
                            + " SET " + basicFunctions.UNIT_VALUE + " = 0  WHERE "
                            + basicFunctions.UNIT_NAME + " = 'Wind_speed' ;";

                SQL_DB.execSQL(SQL_UNIT_UPDATE);

                SQL_DB.close();

            }
        });


        NOT_MOR_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_UNIT_UPDATE;

                if(isChecked) {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Morning' ;";

                    String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                    DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                    if(DB_CURSOR.getCount() > 0) {

                        DB_CURSOR.moveToFirst();

                        basicFunctions.setUpNotification(99994, DB_CURSOR.getInt(2));

                    }

                }

                else {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 0 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Morning' ;";

                    AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                    PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, 99994, notIntent, 0);
                    assert notAlarmManager != null;
                    notAlarmManager.cancel(notPendingIntent);
                    notPendingIntent.cancel();

                }

                SQL_DB.execSQL(SQL_UNIT_UPDATE);

                SQL_DB.close();


            }
        });

        NOT_MOR_MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_MOR_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    Integer morning = DB_CURSOR.getInt(2);

                    if(morning != 1){

                        morning--;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + morning
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Morning' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String morning_text = String.format("%02d", morning) + " : 00";

                        NOT_MOR_TEXT.setText(morning_text);

                        basicFunctions.setUpNotification(99994, morning);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Morning range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        NOT_MOR_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_MOR_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    Integer morning = DB_CURSOR.getInt(2);

                    if(morning != 11){

                        morning++;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + morning
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Morning' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String morning_text = String.format("%02d", morning) + " : 00";

                        NOT_MOR_TEXT.setText(morning_text);

                        basicFunctions.setUpNotification(99994, morning);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Morning range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        NOT_AFT_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_UNIT_UPDATE;

                if(isChecked) {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Afternoon' ;";

                    String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                    DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                    if(DB_CURSOR.getCount() > 0) {

                        DB_CURSOR.moveToFirst();

                        DB_CURSOR.moveToNext();

                        basicFunctions.setUpNotification(99995, DB_CURSOR.getInt(2));

                    }

                }

                else {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 0 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Afternoon' ;";

                    AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                    PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, 99995, notIntent, 0);
                    assert notAlarmManager != null;
                    notAlarmManager.cancel(notPendingIntent);
                    notPendingIntent.cancel();

                }

                SQL_DB.execSQL(SQL_UNIT_UPDATE);

                SQL_DB.close();


            }
        });


        NOT_AFT_MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_AFT_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    DB_CURSOR.moveToNext();

                    Integer afternoon = DB_CURSOR.getInt(2);

                    if(afternoon != 12){

                        afternoon--;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + afternoon
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Afternoon' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String afternoon_text = String.format("%02d", afternoon) + " : 00";

                        NOT_AFT_TEXT.setText(afternoon_text);

                        basicFunctions.setUpNotification(99995, afternoon);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Afternoon range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        NOT_AFT_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_AFT_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    DB_CURSOR.moveToNext();

                    Integer afternoon = DB_CURSOR.getInt(2);

                    if(afternoon != 17){

                        afternoon++;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + afternoon
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Afternoon' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String afternoon_text = String.format("%02d", afternoon) + " : 00";

                        NOT_AFT_TEXT.setText(afternoon_text);

                        basicFunctions.setUpNotification(99995, afternoon);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Afternoon range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        NOT_EVE_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_UNIT_UPDATE;

                if(isChecked) {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Evening' ;";

                    String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                    DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                    if(DB_CURSOR.getCount() > 0) {

                        DB_CURSOR.moveToFirst();

                        DB_CURSOR.moveToNext();

                        DB_CURSOR.moveToNext();

                        basicFunctions.setUpNotification(99996, DB_CURSOR.getInt(2));

                    }

                }

                else {

                    SQL_UNIT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                            + " SET " + basicFunctions.NOTIFICATION_STATUS + " = 0 WHERE "
                            + basicFunctions.NOTIFICATION_NAME + " = 'Evening' ;";

                    AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                    PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, 99996, notIntent, 0);
                    assert notAlarmManager != null;
                    notAlarmManager.cancel(notPendingIntent);
                    notPendingIntent.cancel();

                }

                SQL_DB.execSQL(SQL_UNIT_UPDATE);

                SQL_DB.close();


            }
        });


        NOT_EVE_MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_EVE_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToLast();

                    Integer evening = DB_CURSOR.getInt(2);

                    if(evening != 18){

                        if(evening != 0)
                            evening--;

                        else
                            evening = 23;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + String.valueOf(evening)
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Evening' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String evening_text = String.format("%02d", evening) + " : 00";

                        NOT_EVE_TEXT.setText(evening_text);

                        basicFunctions.setUpNotification(99996, evening);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Evening range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        NOT_EVE_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NOT_EVE_SWITCH.setChecked(true);

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_NOT_SELECT = "SELECT * FROM " + basicFunctions.MY_NOTIFICATIONS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_NOT_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToLast();

                    Integer evening = DB_CURSOR.getInt(2);

                    if(evening != 0){

                        if(evening != 23)
                            evening++;

                        else
                            evening = 0;

                        String SQL_NOT_UPDATE = "UPDATE " + basicFunctions.MY_NOTIFICATIONS_TABLE
                                + " SET " + basicFunctions.NOTIFICATION_TIME + " = " + String.valueOf(evening)
                                + ", " + basicFunctions.NOTIFICATION_STATUS + " = 1 WHERE "
                                + basicFunctions.NOTIFICATION_NAME + " = 'Evening' ;";

                        SQL_DB.execSQL(SQL_NOT_UPDATE);

                        String evening_text = String.format("%02d", evening) + " : 00";

                        NOT_EVE_TEXT.setText(evening_text);

                        basicFunctions.setUpNotification(99996, evening);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Evening range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        ALA_DUR_MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_DUR_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARM_SETTINGS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_DUR_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    Integer time = Integer.parseInt(DB_CURSOR.getString(1));

                    if(time != 1){

                        if(time == 5)
                            time = 1;

                        else
                            time -= 5;

                        String SQL_ALA_DUR_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                                + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = " + time
                                + " WHERE " + basicFunctions.ALARM_SETTING_NAME + " = 'Alarm_duration' ;";

                        SQL_DB.execSQL(SQL_ALA_DUR_UPDATE);

                        String ala_dur_text;

                        if(time == 1)
                            ala_dur_text = time + " minute";

                        else
                            ala_dur_text = time + " minutes";

                        ALA_DUR_TEXT.setText(ala_dur_text);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Alarm range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        ALA_DUR_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_DUR_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARM_SETTINGS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_DUR_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    Integer time = Integer.parseInt(DB_CURSOR.getString(1));

                    if(time != 30){

                        if(time == 1)
                            time = 5;

                        else
                            time += 5;

                        String SQL_ALA_DUR_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                                + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = " + time
                                + " WHERE " + basicFunctions.ALARM_SETTING_NAME + " = 'Alarm_duration' ;";

                        SQL_DB.execSQL(SQL_ALA_DUR_UPDATE);

                        String ala_dur_text = time + " minutes";

                        ALA_DUR_TEXT.setText(ala_dur_text);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Alarm range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        ALA_LAB_SET.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view){

                String alarm_label = ALA_LAB_TEXT.getText().toString();

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_DUR_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                        + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = '" + alarm_label
                        + "' WHERE " + basicFunctions.ALARM_SETTING_NAME + " = 'Label' ;";

                SQL_DB.execSQL(SQL_ALA_DUR_UPDATE);

                if(TextUtils.isEmpty(alarm_label))
                    Toast.makeText(Settings.this, "Your Label has been removed !", Toast.LENGTH_LONG).show();

                else
                    Toast.makeText(Settings.this, "Your Label has been set !", Toast.LENGTH_LONG).show();

                SQL_DB.close();

            }
        });


        ALA_SNO_MINUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_SNO_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARM_SETTINGS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_SNO_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    DB_CURSOR.moveToNext();

                    DB_CURSOR.moveToNext();

                    Integer time = Integer.parseInt(DB_CURSOR.getString(1));

                    if(time != 1){

                        if(time == 5)
                            time = 1;

                        else
                            time -= 5;

                        String SQL_ALA_SNO_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                                + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = " + time
                                + " WHERE " + basicFunctions.ALARM_SETTING_NAME + " = 'Snooze_duration' ;";

                        SQL_DB.execSQL(SQL_ALA_SNO_UPDATE);

                        String ala_sno_text;

                        if(time == 1)
                            ala_sno_text = time + " minute";

                        else
                            ala_sno_text = time + " minutes";

                        ALA_SNO_TEXT.setText(ala_sno_text);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Snooze range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        ALA_SNO_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_SNO_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARM_SETTINGS_TABLE;

                DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_SNO_SELECT, null);

                if(DB_CURSOR.getCount() > 0) {

                    DB_CURSOR.moveToFirst();

                    DB_CURSOR.moveToNext();

                    DB_CURSOR.moveToNext();

                    Integer time = Integer.parseInt(DB_CURSOR.getString(1));

                    if(time != 30){

                        if(time == 1)
                            time = 5;

                        else
                            time += 5;

                        String SQL_ALA_SNO_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                                + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = " + time
                                + " WHERE " + basicFunctions.ALARM_SETTING_NAME + " = 'Snooze_duration' ;";

                        SQL_DB.execSQL(SQL_ALA_SNO_UPDATE);

                        String ala_sno_text = time + " minutes";

                        ALA_SNO_TEXT.setText(ala_sno_text);

                    }

                    else
                        Toast.makeText(Settings.this, "Not within Snooze range !", Toast.LENGTH_LONG).show();

                    DB_CURSOR.close();

                    SQL_DB.close();

                }

            }
        });


        ALA_VIB_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                SQL_DB = mOpenHelper.getWritableDatabase();

                String SQL_ALA_VIB_UPDATE;

                if(isChecked)
                    SQL_ALA_VIB_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                            + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = 1 WHERE "
                            + basicFunctions.ALARM_SETTING_NAME + " = 'Vibration' ;";

                else
                    SQL_ALA_VIB_UPDATE = "UPDATE " + basicFunctions.MY_ALARM_SETTINGS_TABLE
                            + " SET " + basicFunctions.ALARM_SETTING_VALUE + " = 0 WHERE "
                            + basicFunctions.ALARM_SETTING_NAME + " = 'Vibration' ;";

                SQL_DB.execSQL(SQL_ALA_VIB_UPDATE);

                SQL_DB.close();

            }
        });


        ALA_ADD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LL_ALL_SETTINGS.setVisibility(View.GONE);

                ALA_SUNDAY.setChecked(true);
                ALA_MONDAY.setChecked(true);
                ALA_TUESDAY.setChecked(true);
                ALA_WEDNESDAY.setChecked(true);
                ALA_THURSDAY.setChecked(true);
                ALA_FRIDAY.setChecked(true);
                ALA_SATURDAY.setChecked(true);

                LL_AL_SETTINGS.setVisibility(View.VISIBLE);

            }
        });

        ALA_CANCEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LL_ALL_SETTINGS.setVisibility(View.VISIBLE);
                LL_AL_SETTINGS.setVisibility(View.GONE);

            }
        });

        ALA_SET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Integer sunday = 0, monday = 0, tuesday = 0, wednesday = 0,
                        thursday = 0, friday = 0, saturday = 0, hour, minute;

                if(ALA_SUNDAY.isChecked())
                    sunday = 1;

                if(ALA_MONDAY.isChecked())
                    monday = 1;

                if(ALA_TUESDAY.isChecked())
                    tuesday = 1;

                if(ALA_WEDNESDAY.isChecked())
                    wednesday = 1;

                if(ALA_THURSDAY.isChecked())
                    thursday = 1;

                if(ALA_FRIDAY.isChecked())
                    friday = 1;

                if(ALA_SATURDAY.isChecked())
                    saturday = 1;


                if(sunday == 0 && monday == 0 && tuesday == 0 && wednesday == 0
                        && thursday == 0 && friday == 0 && saturday == 0)
                    Toast.makeText(Settings.this, "Kindly choose the days !", Toast.LENGTH_LONG).show();

                else {

                    if (Build.VERSION.SDK_INT >= 23) {

                        hour = ALA_TIMEPICKER.getHour();

                        minute = ALA_TIMEPICKER.getMinute();

                    } else {

                        hour = ALA_TIMEPICKER.getCurrentHour();

                        minute = ALA_TIMEPICKER.getCurrentMinute();
                    }

                    SQL_DB = mOpenHelper.getWritableDatabase();

                    String SQL_ALA_COUNT_SELECT = "SELECT COUNT (*) FROM " + basicFunctions.MY_ALARMS_TABLE;

                    DB_CURSOR = SQL_DB.rawQuery(SQL_ALA_COUNT_SELECT, null);

                    int id = 0;

                    if(DB_CURSOR.getCount() > 0){

                        DB_CURSOR.moveToFirst();
                        id = DB_CURSOR.getInt(0);
                    }

                    DB_CURSOR.close();

                    String SQL_ALA_SET_INSERT = "INSERT INTO '"
                            + basicFunctions.MY_ALARMS_TABLE + "' ( '" + basicFunctions.ALARM_ID + "', '"
                            + basicFunctions.ALARM_STATUS + "', '" + basicFunctions.ALARM_HOUR + "', '"
                            + basicFunctions.ALARM_MINUTE + "', '" + basicFunctions.ALARM_SUNDAY + "', '"
                            + basicFunctions.ALARM_MONDAY + "', '" + basicFunctions.ALARM_TUESDAY + "', '"
                            + basicFunctions.ALARM_WEDNESDAY + "', '" + basicFunctions.ALARM_THURSDAY + "', '"
                            + basicFunctions.ALARM_FRIDAY + "', '" + basicFunctions.ALARM_SATURDAY
                            + "' ) VALUES ( " + id + ", 1, " + hour + ", " + minute + ", "
                            + sunday + ", " + monday + ", " + tuesday + ", " + wednesday + ", " + thursday + ", "
                            + friday + ", " + saturday + " ) ;";

                    SQL_DB.execSQL(SQL_ALA_SET_INSERT);

                    AlarmListValues obj = new AlarmListValues(id, 1, hour, minute, sunday,
                            monday, tuesday, wednesday, thursday, friday, saturday);

                    SE_AL_ALARM_LIST.add(obj);

                    Toast.makeText(Settings.this, "Your Alarm has been added !", Toast.LENGTH_LONG).show();

                    mAlarmAdapter.notifyDataSetChanged();

                    SQL_DB.close();

                    if(sunday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 1);

                    if(monday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 2);

                    if(tuesday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 3);

                    if(wednesday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 4);

                    if(thursday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 5);

                    if(friday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 6);

                    if(saturday == 1)
                        basicFunctions.setUpAlarm(id, hour, minute, 7);

                    LL_ALL_SETTINGS.setVisibility(View.VISIBLE);
                    LL_AL_SETTINGS.setVisibility(View.GONE);

                }
            }
        });
    }


    private class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DataHolder> {

        private ArrayList<AlarmListValues> mDataset;

        class DataHolder extends RecyclerView.ViewHolder {

            ImageView SE_AL_STATUS;
            TextView SE_AL_TIME, SE_AL_DAYS;
            Switch SE_AL_SWITCH;
            ImageButton SE_AL_DELETE;

            DataHolder(final View itemView) {
                super(itemView);

                SE_AL_STATUS = itemView.findViewById(R.id.se_al_li_status);
                SE_AL_TIME = itemView.findViewById(R.id.se_al_li_time);
                SE_AL_DAYS = itemView.findViewById(R.id.se_al_li_days);
                SE_AL_SWITCH = itemView.findViewById(R.id.se_al_li_switch);
                SE_AL_DELETE = itemView.findViewById(R.id.se_al_li_delete);

            }

        }

        DailyForecastAdapter(ArrayList<AlarmListValues> myDataset) {
            mDataset = myDataset;
        }

        @NonNull
        @Override
        public DataHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_se_al_list_item, parent, false);

            mAlarmRecyclerView.setMinimumHeight(mAlarmRecyclerView.getHeight() + view.getHeight());

            return new DataHolder(view);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onBindViewHolder(@NonNull final DataHolder holder, @SuppressLint("RecyclerView") final int position) {

            if(mDataset.get(holder.getAdapterPosition()).getSE_AL_STATUS() == 0){

                holder.SE_AL_STATUS.setBackgroundResource(R.drawable.se_alarm_off);
                holder.SE_AL_SWITCH.setChecked(false);

            }

            else {

                holder.SE_AL_STATUS.setBackgroundResource(R.drawable.se_alarm_on);
                holder.SE_AL_SWITCH.setChecked(true);

            }

            @SuppressLint("DefaultLocale")
            String time = String.format("%02d", mDataset.get(position).getSE_AL_HOUR()) +
                            " : " + String.format("%02d", mDataset.get(position).getSE_AL_MINUTE());

            holder.SE_AL_TIME.setText(time);

            String days = "";

            Integer sunday, monday, tuesday, wednesday, thursday, friday, saturday;

            sunday = mDataset.get(position).getSE_AL_SUNDAY();
            monday = mDataset.get(position).getSE_AL_MONDAY();
            tuesday = mDataset.get(position).getSE_AL_TUESDAY();
            wednesday = mDataset.get(position).getSE_AL_WEDNESDAY();
            thursday = mDataset.get(position).getSE_AL_THURSDAY();
            friday = mDataset.get(position).getSE_AL_FRIDAY();
            saturday = mDataset.get(position).getSE_AL_SATURDAY();

            if(sunday == 1 && monday == 1 && tuesday == 1 && wednesday == 1
                    && thursday == 1 && friday == 1 && saturday == 1)
                days = "Everyday";

            else {

                if (sunday == 1)
                    days += "Sun ";

                if (monday == 1)
                    days += "Mon ";

                if (tuesday == 1)
                    days += "Tue ";

                if (wednesday == 1)
                    days += "Wed ";

                if (thursday == 1)
                    days += "Thu ";

                if (friday == 1)
                    days += "Fri ";

                if (saturday == 1)
                    days += "Sat";

            }

            holder.SE_AL_DAYS.setText(days);

            holder.SE_AL_DELETE.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:

                                    Integer id, identifier, hour, minute, sunday, monday, tuesday, wednesday, thursday, friday, saturday;

                                    SQL_DB = mOpenHelper.getReadableDatabase();

                                    id = mDataset.get(position).getSE_AL_ID();
                                    hour = mDataset.get(position).getSE_AL_HOUR();
                                    minute = mDataset.get(position).getSE_AL_MINUTE();

                                    sunday = mDataset.get(position).getSE_AL_SUNDAY();
                                    monday = mDataset.get(position).getSE_AL_MONDAY();
                                    tuesday = mDataset.get(position).getSE_AL_TUESDAY();
                                    wednesday = mDataset.get(position).getSE_AL_WEDNESDAY();
                                    thursday = mDataset.get(position).getSE_AL_THURSDAY();
                                    friday = mDataset.get(position).getSE_AL_FRIDAY();
                                    saturday = mDataset.get(position).getSE_AL_SATURDAY();

                                    String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_ALARMS_TABLE + " WHERE "
                                            + basicFunctions.ALARM_ID + " = " + id;

                                    DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, null);

                                    DB_CURSOR.moveToFirst();

                                    SQLiteDatabase db = mOpenHelper.getWritableDatabase();

                                    db.delete(basicFunctions.MY_ALARMS_TABLE, " " + basicFunctions.ALARM_ID + " = '" + id + "' " +
                                            "AND " + basicFunctions.ALARM_HOUR + " = '" + hour + "' " +
                                            "AND " + basicFunctions.ALARM_MINUTE + " = '" + minute + "'", null);

                                    Toast.makeText(Settings.this, "Your Alarm has been removed !", Toast.LENGTH_LONG).show();

                                    SE_AL_ALARM_LIST.remove(position);

                                    mAlarmAdapter.notifyDataSetChanged();

                                    if(mAlarmAdapter.getItemCount() == 0)
                                        Toast.makeText(Settings.this, "Your Alarm List is empty !", Toast.LENGTH_LONG).show();

                                    if(sunday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(1) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(monday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(2) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(tuesday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(3) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(wednesday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(4) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(thursday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(5) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(friday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(6) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    if(saturday == 1) {

                                        identifier = Integer.parseInt(String.valueOf(7) + "" + String.valueOf(id));

                                        AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                                        Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                                        PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                                        assert notAlarmManager != null;
                                        notAlarmManager.cancel(notPendingIntent);
                                        notPendingIntent.cancel();

                                    }

                                    DB_CURSOR.close();

                                    SQL_DB.close();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:

                                    Toast.makeText(Settings.this, "Your Alarm has not been removed !", Toast.LENGTH_LONG).show();

                                    dialog.dismiss();

                                    break;

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
                    builder.setMessage("Are you sure you want to remove your Alarm ?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }
            });

            holder.SE_AL_SWITCH.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    SQL_DB = mOpenHelper.getWritableDatabase();

                    String SQL_ALA_LIST_UPDATE;

                    Integer id, identifier, hour, minute, sunday, monday, tuesday, wednesday, thursday, friday, saturday;

                    id = mDataset.get(position).getSE_AL_ID();
                    hour = mDataset.get(position).getSE_AL_HOUR();
                    minute = mDataset.get(position).getSE_AL_MINUTE();

                    sunday = mDataset.get(position).getSE_AL_SUNDAY();
                    monday = mDataset.get(position).getSE_AL_MONDAY();
                    tuesday = mDataset.get(position).getSE_AL_TUESDAY();
                    wednesday = mDataset.get(position).getSE_AL_WEDNESDAY();
                    thursday = mDataset.get(position).getSE_AL_THURSDAY();
                    friday = mDataset.get(position).getSE_AL_FRIDAY();
                    saturday = mDataset.get(position).getSE_AL_SATURDAY();

                    if(isChecked) {

                        SQL_ALA_LIST_UPDATE = "UPDATE " + basicFunctions.MY_ALARMS_TABLE
                                + " SET " + basicFunctions.ALARM_STATUS + " = 1 WHERE "
                                + basicFunctions.ALARM_ID + " = " + id + " ;";

                        holder.SE_AL_STATUS.setBackgroundResource(R.drawable.se_alarm_on);

                        if(sunday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 1);

                        if(monday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 2);

                        if(tuesday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 3);

                        if(wednesday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 4);

                        if(thursday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 5);

                        if(friday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 6);

                        if(saturday == 1)
                            basicFunctions.setUpAlarm(id, hour, minute, 7);

                    }

                    else {


                        SQL_ALA_LIST_UPDATE = "UPDATE " + basicFunctions.MY_ALARMS_TABLE
                                + " SET " + basicFunctions.ALARM_STATUS + " = 0 WHERE "
                                + basicFunctions.ALARM_ID + " = " + mDataset.get(position).getSE_AL_ID() + " ;";

                        holder.SE_AL_STATUS.setBackgroundResource(R.drawable.se_alarm_off);

                        if(sunday == 1) {

                            identifier = Integer.parseInt(String.valueOf(1) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(monday == 1) {

                            identifier = Integer.parseInt(String.valueOf(2) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(tuesday == 1) {

                            identifier = Integer.parseInt(String.valueOf(3) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(wednesday == 1) {

                            identifier = Integer.parseInt(String.valueOf(4) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(thursday == 1) {

                            identifier = Integer.parseInt(String.valueOf(5) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(friday == 1) {

                            identifier = Integer.parseInt(String.valueOf(6) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                        if(saturday == 1) {

                            identifier = Integer.parseInt(String.valueOf(7) + "" + String.valueOf(id));

                            AlarmManager notAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                            Intent notIntent = new Intent(Settings.this, AlarmReceiver.class);
                            PendingIntent notPendingIntent = PendingIntent.getBroadcast(Settings.this, identifier, notIntent, 0);
                            assert notAlarmManager != null;
                            notAlarmManager.cancel(notPendingIntent);
                            notPendingIntent.cancel();

                        }

                    }

                    SQL_DB.execSQL(SQL_ALA_LIST_UPDATE);

                    SQL_DB.close();


                }
            });

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


    private class AlarmListValues {

        private Integer SE_AL_ID;
        private Integer SE_AL_STATUS;
        private Integer SE_AL_HOUR;
        private Integer SE_AL_MINUTE;
        private Integer SE_AL_SUNDAY;
        private Integer SE_AL_MONDAY;
        private Integer SE_AL_TUESDAY;
        private Integer SE_AL_WEDNESDAY;
        private Integer SE_AL_THURSDAY;
        private Integer SE_AL_FRIDAY;
        private Integer SE_AL_SATURDAY;

        AlarmListValues(Integer id, Integer status, Integer hour, Integer minute, Integer sunday,
                        Integer monday, Integer tuesday, Integer wednesday, Integer thursday, Integer friday, Integer saturday){

            SE_AL_ID = id;
            SE_AL_STATUS = status;
            SE_AL_HOUR = hour;
            SE_AL_MINUTE = minute;
            SE_AL_SUNDAY = sunday;
            SE_AL_MONDAY = monday;
            SE_AL_TUESDAY = tuesday;
            SE_AL_WEDNESDAY = wednesday;
            SE_AL_THURSDAY = thursday;
            SE_AL_FRIDAY = friday;
            SE_AL_SATURDAY = saturday;

        }

        Integer getSE_AL_ID() {
            return SE_AL_ID;
        }

        Integer getSE_AL_STATUS() {
            return SE_AL_STATUS;
        }

        Integer getSE_AL_HOUR() {
            return SE_AL_HOUR;
        }

        Integer getSE_AL_MINUTE() {
            return SE_AL_MINUTE;
        }

        Integer getSE_AL_SUNDAY() {
            return SE_AL_SUNDAY;
        }

        Integer getSE_AL_MONDAY() {
            return SE_AL_MONDAY;
        }

        Integer getSE_AL_TUESDAY() {
            return SE_AL_TUESDAY;
        }

        Integer getSE_AL_WEDNESDAY() {
            return SE_AL_WEDNESDAY;
        }

        Integer getSE_AL_THURSDAY() {
            return SE_AL_THURSDAY;
        }

        Integer getSE_AL_FRIDAY() {
            return SE_AL_FRIDAY;
        }

        Integer getSE_AL_SATURDAY() {
            return SE_AL_SATURDAY;
        }

    }

}
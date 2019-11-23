package fmt.febuweather;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;

import fmt.febuweather.helper.BasicFunctions;
import fmt.febuweather.helper.Menu;


public class OtherLocations extends AppCompatActivity {


    EditText OL_ENTER_LOCATION;
    ImageButton MENU_BUTTON, OL_ADD_LOCATION;

    ArrayList<LocationCurrentForecastValues> OL_LOCATION_LIST;
    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;

    BasicFunctions.DatabaseHelper mOpenHelper;
    SQLiteDatabase SQL_DB;
    Cursor DB_CURSOR;

    Integer UNIT_TEMPERATURE, UNIT_WIND_SPEED;

    ProgressDialog progressDialog;

    private Menu menu;

    private BasicFunctions basicFunctions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_locations);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        }

        basicFunctions = new BasicFunctions(OtherLocations.this);

        menu = new Menu(OtherLocations.this);

        OL_ENTER_LOCATION = (EditText) findViewById(R.id.ol_enter_location);
        OL_ADD_LOCATION = (ImageButton) findViewById(R.id.ol_add_location);
        mRecyclerView = (RecyclerView) findViewById(R.id.ol_location_list);
        MENU_BUTTON = (ImageButton) findViewById(R.id.ol_menu);

        MENU_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                menu.LeftDrawer.toggleLeftDrawer();

            }
        });

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        OL_LOCATION_LIST = new ArrayList<>();
        mAdapter = new LocationCurrentForecastAdapter(OL_LOCATION_LIST);
        mRecyclerView.setAdapter(mAdapter);

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

        if(basicFunctions.isConnectingToInternet())
            fetchLocationList();

        else {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {

                        case DialogInterface.BUTTON_POSITIVE:

                            if(basicFunctions.isConnectingToInternet())
                                fetchLocationList();

                            else
                                Toast.makeText(OtherLocations.this,
                                        "No Internet Connection. Try again later !",
                                        Toast.LENGTH_LONG).show();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                            Toast.makeText(OtherLocations.this,
                                    "No Internet Connection. Try again later !",
                                    Toast.LENGTH_LONG).show();

                            dialog.dismiss();

                            break;

                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(OtherLocations.this);
            builder.setMessage("No Internet Connection. Try again ?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }


        OL_ADD_LOCATION.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String location = OL_ENTER_LOCATION.getText().toString();

                if (TextUtils.isEmpty(location))
                    Toast.makeText(OtherLocations.this, "Kindly enter a Location !", Toast.LENGTH_LONG).show();

                else {

                    if(basicFunctions.isConnectingToInternet())
                        addLocation(location);

                    else {

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {

                                    case DialogInterface.BUTTON_POSITIVE:

                                        if(basicFunctions.isConnectingToInternet())
                                            addLocation(location);

                                        else
                                            Toast.makeText(OtherLocations.this,
                                                    "No Internet Connection. Try again later !",
                                                    Toast.LENGTH_LONG).show();

                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:

                                        Toast.makeText(OtherLocations.this,
                                                "No Internet Connection. Try again later !",
                                                Toast.LENGTH_LONG).show();

                                        dialog.dismiss();

                                        break;

                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(OtherLocations.this);
                        builder.setMessage("No Internet Connection. Try again ?")
                                .setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();

                    }

                }
            }
        });

        AdView mAdView = (AdView) findViewById(R.id.ol_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

    }


    private void fetchLocationList() {

        progressDialog = new ProgressDialog(OtherLocations.this);
        progressDialog.setMessage("Fetching Location List ... ");
        progressDialog.show();

        SQL_DB = mOpenHelper.getReadableDatabase();

        String SQL_CREATE = "CREATE TABLE IF NOT EXISTS '" + basicFunctions.MY_LOCATIONS_TABLE + "' ( '"
                + basicFunctions.LOCATION_NAME + "' TEXT NOT NULL, '"
                + basicFunctions.LOCATION_LATITUDE + "' TEXT NOT NULL, '"
                + basicFunctions.LOCATION_LONGITUDE + "' TEXT NOT NULL );";

        SQL_DB.execSQL(SQL_CREATE);

        String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE;

        DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, null);

        if(DB_CURSOR.getCount() > 0) {

            DB_CURSOR.moveToFirst();

            do{

                new GetCurrentForecastTask().execute(String.valueOf(DB_CURSOR.getPosition()), DB_CURSOR.getString(0),
                        DB_CURSOR.getString(1), DB_CURSOR.getString(2));

            } while(DB_CURSOR.moveToNext());

        }

        else {
            Toast.makeText(OtherLocations.this, "Your Location List is empty !", Toast.LENGTH_LONG).show();

            DB_CURSOR.close();

            SQL_DB.close();

            progressDialog.dismiss();

        }
    }


    private class GetCurrentForecastTask extends AsyncTask<String, Void, JSONObject> {

        private GetCurrentForecastTask() {}

        String position, location, latitude, longitude;

        @Override
        protected JSONObject doInBackground(String... params) {

            JSONObject jsonWeather = null;

            position = params[0];
            location = params[1];
            latitude = params[2];
            longitude = params[3];

            try {

                jsonWeather = basicFunctions.getWeatherJSON(basicFunctions.OWM_CURRENT_FORECAST_URL, latitude, longitude);

            } catch (Exception e) {

                Toast.makeText(OtherLocations.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

            return jsonWeather;
        }

        @SuppressLint("DefaultLocale")
        @Override
        protected void onPostExecute(JSONObject json) {

            try {

                if(json != null){

                    JSONObject weather = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");

                    String description = weather.getString("main") + ", " + weather.getString("description");

                    String iconText = basicFunctions.setWeatherIcon(weather.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000);

                    String temperature;

                    if(UNIT_TEMPERATURE == 0)
                        temperature = String.format("%.2f", main.getDouble("temp")) + " °C";

                    else
                        temperature = String.format("%.2f", basicFunctions.celsiusToFahrenheit(main.getDouble("temp"))) + " °F";

                    String humidity = main.getDouble("humidity") + " %";

                    LocationCurrentForecastValues obj = new LocationCurrentForecastValues(location, description, iconText, temperature, humidity);

                    OL_LOCATION_LIST.add(obj);

                    mAdapter.notifyDataSetChanged();

                    if(position.equals("0")){

                        DB_CURSOR.close();

                        SQL_DB.close();

                        progressDialog.dismiss();
                    }

                }

            } catch (JSONException e) {

                Toast.makeText(OtherLocations.this, "Error, Cannot process JSON results", Toast.LENGTH_LONG).show();

            }

        }
    }


    private void addLocation(String location) {

        progressDialog = new ProgressDialog(OtherLocations.this);
        progressDialog.setMessage("Adding Location ... ");
        progressDialog.show();

        Address address = basicFunctions.getLatLong(location);

        String latitude = String.valueOf(address.getLatitude());

        String longitude = String.valueOf(address.getLongitude());

        SQL_DB = mOpenHelper.getWritableDatabase();

        String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE + " WHERE "
                + basicFunctions.LOCATION_LATITUDE + " = ? AND " + basicFunctions.LOCATION_LONGITUDE + " = ?";

        DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, new String[] { latitude, longitude });

        if (DB_CURSOR.moveToFirst())
            Toast.makeText(OtherLocations.this, location + " already exists !", Toast.LENGTH_LONG).show();

        else {

            String SQL_INSERT = "INSERT INTO '" + basicFunctions.MY_LOCATIONS_TABLE + "' ( '"
                    + basicFunctions.LOCATION_NAME + "', '" + basicFunctions.LOCATION_LATITUDE + "', '"
                    + basicFunctions.LOCATION_LONGITUDE + "' ) VALUES ( '" + location + "', '"
                    + latitude + "', '" + longitude + "' )";

            SQL_DB.execSQL(SQL_INSERT);

            new GetCurrentForecastTask().execute("0", location, latitude, longitude);

            OL_ENTER_LOCATION.setText("");

            Toast.makeText(OtherLocations.this, location + " has been added !", Toast.LENGTH_LONG).show();

        }

        DB_CURSOR.close();

        SQL_DB.close();

    }


    private void deleteLocation(int position, String location){

        progressDialog = new ProgressDialog(OtherLocations.this);
        progressDialog.setMessage("Removing Location ... ");
        progressDialog.show();

        SQL_DB = mOpenHelper.getReadableDatabase();

        String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE + " WHERE "
                + basicFunctions.LOCATION_NAME + " = ?";

        DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, new String[] { location });

        DB_CURSOR.moveToFirst();

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        db.delete(basicFunctions.MY_LOCATIONS_TABLE, " " + basicFunctions.LOCATION_NAME + " = '" + location + "' " +
                "AND " + basicFunctions.LOCATION_LATITUDE + " = '" + DB_CURSOR.getString(1) + "' " +
                "AND " + basicFunctions.LOCATION_LONGITUDE + " = '" + DB_CURSOR.getString(2) + "'", null);

        Toast.makeText(OtherLocations.this, location + " has been removed !", Toast.LENGTH_LONG).show();

        OL_LOCATION_LIST.remove(position);

        mAdapter.notifyDataSetChanged();

        progressDialog.dismiss();

        if(mAdapter.getItemCount() == 0)
            Toast.makeText(OtherLocations.this, "Your Location List is empty !", Toast.LENGTH_LONG).show();

        DB_CURSOR.close();

        SQL_DB.close();

    }


    private class LocationCurrentForecastAdapter extends RecyclerView.Adapter<LocationCurrentForecastAdapter.DataHolder> {

        private ArrayList<LocationCurrentForecastValues> mDataset;

        class DataHolder extends RecyclerView.ViewHolder {

            TextView OL_LOCATION, OL_DESCRIPTION, OL_ICON, OL_TEMPERATURE, OL_HUMIDITY;

            ImageView OL_MY_LOCATION;

            ImageButton OL_DELETE;

            DataHolder(final View itemView) {
                super(itemView);

                OL_LOCATION = (TextView) itemView.findViewById(R.id.ol_i_location);
                OL_DESCRIPTION = (TextView) itemView.findViewById(R.id.ol_i_description);
                OL_ICON = (TextView) itemView.findViewById(R.id.ol_i_icon);
                OL_TEMPERATURE = (TextView) itemView.findViewById(R.id.ol_i_temperature);
                OL_HUMIDITY = (TextView) itemView.findViewById(R.id.ol_i_humidity);
                OL_MY_LOCATION = (ImageView) itemView.findViewById(R.id.ol_i_my_location);
                OL_DELETE = (ImageButton) itemView.findViewById(R.id.ol_i_delete_location);

                OL_ICON.setTypeface(BasicFunctions.weatherFont);

                OL_DELETE.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String location = mDataset.get(getAdapterPosition()).getOL_LOCATION();

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:

                                        mRecyclerView.setMinimumHeight(mRecyclerView.getHeight() - (itemView.getHeight() + 40));

                                        deleteLocation(getAdapterPosition(), location);

                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:

                                        dialog.dismiss();

                                        Toast.makeText(OtherLocations.this, location + " has not been removed !", Toast.LENGTH_LONG).show();

                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(OtherLocations.this);
                        builder.setMessage("Are you sure you want to remove " + location + " ?")
                                .setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();


                    }
                });

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String location = mDataset.get(getAdapterPosition()).getOL_LOCATION();

                        SQL_DB = mOpenHelper.getReadableDatabase();

                        String SQL_SELECT = "SELECT * FROM " + basicFunctions.MY_LOCATIONS_TABLE + " WHERE "
                                + basicFunctions.LOCATION_NAME + " = ?";

                        DB_CURSOR = SQL_DB.rawQuery(SQL_SELECT, new String[] { location });

                        DB_CURSOR.moveToFirst();

                        Intent intent = new Intent(OtherLocations.this, MyLocation.class);
                        intent.putExtra(basicFunctions.LOCATION_NAME, location);
                        intent.putExtra(basicFunctions.LOCATION_LATITUDE, DB_CURSOR.getString(1));
                        intent.putExtra(basicFunctions.LOCATION_LONGITUDE, DB_CURSOR.getString(2));
                        startActivity(intent);

                        DB_CURSOR.close();

                        SQL_DB.close();

                    }
                });

            }

        }

        LocationCurrentForecastAdapter(ArrayList<LocationCurrentForecastValues> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_other_locations_item, parent, false);

            mRecyclerView.setMinimumHeight(mRecyclerView.getHeight() + view.getHeight());

            return new DataHolder(view);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onBindViewHolder(DataHolder holder, int position) {

            if(position == 0)
                holder.OL_MY_LOCATION.setVisibility(View.VISIBLE);

            holder.OL_LOCATION.setText(mDataset.get(position).getOL_LOCATION());
            holder.OL_DESCRIPTION.setText(mDataset.get(position).getOL_DESCRIPTION());
            holder.OL_ICON.setText(Html.fromHtml(mDataset.get(position).getOL_ICON()));
            holder.OL_TEMPERATURE.setText(mDataset.get(position).getOL_TEMPERATURE());
            holder.OL_HUMIDITY.setText(mDataset.get(position).getOL_HUMIDITY());
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


    private class LocationCurrentForecastValues {

        private String OL_LOCATION;
        private String OL_DESCRIPTION;
        private String OL_ICON;
        private String OL_TEMPERATURE;
        private String OL_HUMIDITY;

        LocationCurrentForecastValues(String location, String description, String icon, String temperature, String humidity){
            OL_LOCATION = location;
            OL_DESCRIPTION = description;
            OL_ICON = icon;
            OL_TEMPERATURE = temperature;
            OL_HUMIDITY = humidity;
        }

        String getOL_LOCATION() {
            return OL_LOCATION;
        }

        String getOL_DESCRIPTION() {
            return OL_DESCRIPTION;
        }

        String getOL_ICON() {
            return OL_ICON;
        }

        String getOL_TEMPERATURE() {
            return OL_TEMPERATURE;
        }

        String getOL_HUMIDITY() {
            return OL_HUMIDITY;
        }

    }

}
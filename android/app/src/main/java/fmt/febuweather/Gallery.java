package fmt.febuweather;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.PlacePhotoResult;
import com.google.android.gms.location.places.Places;

import fmt.febuweather.helper.BasicFunctions;
import fmt.febuweather.helper.Menu;


public class Gallery extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {


    GoogleApiClient mGoogleApiClient;

    PlacePhotoMetadataBuffer photoMetadataBuffer;


    ImageView GA_LOC_IMAGE;

    TextView GA_LOC_NAME;

    ImageButton MENU_BUTTON, GA_PREV_IMAGE, GA_NEXT_IMAGE;


    int LOCATION_INDEX, PICTURE_INDEX, PICTURE_COUNT;

    String placeId[];

    ProgressDialog pDialog;

    private Menu menu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = this.getWindow();

            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        }

        menu = new Menu(Gallery.this);

        final BasicFunctions basicFunctions = new BasicFunctions(Gallery.this);

        GA_LOC_IMAGE = (ImageView) findViewById(R.id.ga_loc_image);
        GA_PREV_IMAGE = (ImageButton) findViewById(R.id.ga_prev_image);
        GA_NEXT_IMAGE = (ImageButton) findViewById(R.id.ga_next_image);
        GA_LOC_NAME = (TextView) findViewById(R.id.ga_loc_name);
        MENU_BUTTON = (ImageButton) findViewById(R.id.ga_menu);

        MENU_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.LeftDrawer.toggleLeftDrawer();

            }
        });

        placeId = new String[]{ getString(R.string.ga_loc_id_usa),
                                getString(R.string.ga_loc_id_uk),
                                getString(R.string.ga_loc_id_aus),
                                getString(R.string.ga_loc_id_nz),
                                getString(R.string.ga_loc_id_india)};

        AdView mAdView = (AdView) findViewById(R.id.ga_adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        if(basicFunctions.isConnectingToInternet())
            fetchImages();

        else {

            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){

                        case DialogInterface.BUTTON_POSITIVE:

                            if(basicFunctions.isConnectingToInternet())
                                fetchImages();

                            else
                                Toast.makeText(Gallery.this,
                                        "No Internet Connection. Try again later !",
                                        Toast.LENGTH_LONG).show();

                            break;

                        case DialogInterface.BUTTON_NEGATIVE:

                            Toast.makeText(Gallery.this,
                                    "No Internet Connection. Try again later !",
                                    Toast.LENGTH_LONG).show();

                            dialog.dismiss();

                            break;

                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this);
            builder.setMessage("No Internet Connection. Try again ?")
                    .setPositiveButton("Yes", dialogClickListener)
                    .setNegativeButton("No", dialogClickListener).show();

        }


        GA_PREV_IMAGE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (basicFunctions.isConnectingToInternet())
                    fetchPreviousImage();

                else {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {

                                case DialogInterface.BUTTON_POSITIVE:

                                    if (basicFunctions.isConnectingToInternet())
                                        fetchPreviousImage();

                                    else
                                        Toast.makeText(Gallery.this,
                                                "No Internet Connection. Try again later !",
                                                Toast.LENGTH_LONG).show();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:

                                    Toast.makeText(Gallery.this,
                                            "No Internet Connection. Try again later !",
                                            Toast.LENGTH_LONG).show();

                                    dialog.dismiss();

                                    break;

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this);
                    builder.setMessage("No Internet Connection. Try again ?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }
            }

        });


        GA_NEXT_IMAGE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (basicFunctions.isConnectingToInternet())
                    fetchNextImage();

                else {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {

                                case DialogInterface.BUTTON_POSITIVE:

                                    if (basicFunctions.isConnectingToInternet())
                                        fetchNextImage();

                                    else
                                        Toast.makeText(Gallery.this,
                                                "No Internet Connection. Try again later !",
                                                Toast.LENGTH_LONG).show();

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:

                                    Toast.makeText(Gallery.this,
                                            "No Internet Connection. Try again later !",
                                            Toast.LENGTH_LONG).show();

                                    dialog.dismiss();

                                    break;

                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this);
                    builder.setMessage("No Internet Connection. Try again ?")
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }

            }
        });

    }


    private void fetchImages(){

        pDialog = new ProgressDialog(Gallery.this);
        pDialog.setMessage("Fetching Image !");
        pDialog.show();

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        LOCATION_INDEX = 0;
        PICTURE_INDEX = 0;

        GA_LOC_NAME.setText(getString(R.string.ga_loc_name_usa));

        placePhotosAsync();

    }


    private void fetchPreviousImage(){

        pDialog = new ProgressDialog(Gallery.this);
        pDialog.setMessage("Fetching Image !");

        pDialog.show();

        PICTURE_INDEX--;

        if (PICTURE_INDEX >= 0) {

            photoMetadataBuffer.get(PICTURE_INDEX)
                    .getScaledPhoto(mGoogleApiClient, GA_LOC_IMAGE.getWidth(),
                            GA_LOC_IMAGE.getHeight())
                    .setResultCallback(mDisplayPhotoResultCallback);

            pDialog.dismiss();

        } else {

            if(LOCATION_INDEX > 0)
                LOCATION_INDEX--;

            else
                LOCATION_INDEX = 4;

            PICTURE_INDEX = 0;

            placePhotosAsync();

        }

        switch (LOCATION_INDEX){

            case 0:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_usa));
                break;

            case 1:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_uk));
                break;

            case 2:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_aus));
                break;

            case 3:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_nz));
                break;

            case 4:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_india));
                break;

            default:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_usa));
                break;

        }

    }


    private void fetchNextImage(){

        pDialog = new ProgressDialog(Gallery.this);
        pDialog.setMessage("Fetching Image !");

        pDialog.show();

        PICTURE_INDEX++;

        if (PICTURE_INDEX < PICTURE_COUNT) {

            photoMetadataBuffer.get(PICTURE_INDEX)
                    .getScaledPhoto(mGoogleApiClient, GA_LOC_IMAGE.getWidth(),
                            GA_LOC_IMAGE.getHeight())
                    .setResultCallback(mDisplayPhotoResultCallback);

            pDialog.dismiss();

        }

        else {

            if(LOCATION_INDEX < 4)
                LOCATION_INDEX++;

            else
                LOCATION_INDEX = 0;

            PICTURE_INDEX = 0;

            placePhotosAsync();

        }

        switch (LOCATION_INDEX){

            case 0:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_usa));
                break;

            case 1:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_uk));
                break;

            case 2:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_aus));
                break;

            case 3:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_nz));
                break;

            case 4:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_india));
                break;

            default:
                GA_LOC_NAME.setText(getString(R.string.ga_loc_name_usa));
                break;

        }

    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}


    private ResultCallback<PlacePhotoResult> mDisplayPhotoResultCallback
            = new ResultCallback<PlacePhotoResult>() {
        @Override
        public void onResult(@NonNull PlacePhotoResult placePhotoResult) {
            if (!placePhotoResult.getStatus().isSuccess()) {
                return;
            }
            GA_LOC_IMAGE.setImageBitmap(placePhotoResult.getBitmap());
        }
    };


    private void placePhotosAsync() {

        Places.GeoDataApi.getPlacePhotos(mGoogleApiClient, placeId[LOCATION_INDEX])
                .setResultCallback(new ResultCallback<PlacePhotoMetadataResult>() {


                    @Override
                    public void onResult(@NonNull PlacePhotoMetadataResult photos) {
                        if (!photos.getStatus().isSuccess()) {
                            return;
                        }

                        photoMetadataBuffer = photos.getPhotoMetadata();
                        PICTURE_COUNT = photoMetadataBuffer.getCount();

                        if (PICTURE_COUNT > 0)
                            photoMetadataBuffer.get(PICTURE_INDEX)
                                    .getScaledPhoto(mGoogleApiClient, GA_LOC_IMAGE.getWidth(),
                                            GA_LOC_IMAGE.getHeight())
                                    .setResultCallback(mDisplayPhotoResultCallback);

                        else
                            Toast.makeText(Gallery.this, "No Images Available !", Toast.LENGTH_LONG).show();

                    }
                });

        pDialog.dismiss();

    }

}
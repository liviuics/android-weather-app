package com.sandu.weatherapp;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.androdocs.httprequest.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;


public class MainActivity extends AppCompatActivity {

    SwipeRefreshLayout refreshLayout;
    PlacesClient placesClient;

    String apiKeyG = "AIzaSyCeqOwf0qcbIVorqeXxTBTIWbczwleyJAM";


    String CITY = "Craiova";
    String API = "6c8a7ecfb132ee9c0fea6900301d33d1";

    double LAT = 33;
    double LON = 23.2;
    int PERMISSION_ID = 44;
    FusedLocationProviderClient mFusedLocationClient;

    TextView addressTxt, updated_atTxt, statusTxt, tempTxt, temp_minTxt, temp_maxTxt, sunriseTxt,
            sunsetTxt, windTxt, pressureTxt, humidityTxt, latTxt, lonTxt;
    ImageView condIcon;
    //Button search;
    EditText searchET;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        latTxt = findViewById(R.id.latTextView);
        lonTxt = findViewById(R.id.lonTextView);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        addressTxt = findViewById(R.id.address);
        updated_atTxt = findViewById(R.id.updated_at);
        statusTxt = findViewById(R.id.status);
        tempTxt = findViewById(R.id.temp);
        temp_minTxt = findViewById(R.id.temp_min);
        temp_maxTxt = findViewById(R.id.temp_max);
        sunriseTxt = findViewById(R.id.sunrise);
        sunsetTxt = findViewById(R.id.sunset);
        windTxt = findViewById(R.id.wind);
        pressureTxt = findViewById(R.id.pressure);
        humidityTxt = findViewById(R.id.humidity);
        searchET = findViewById(R.id.searchET);
        //search = findViewById(R.id.searchBTN);
        refreshLayout = findViewById(R.id.refreshLayout);
        condIcon = findViewById(R.id.condIcon);


        if(checkInternet()) {
            requestNewLocationData();
            new weatherTaskL().execute();
        }
        else {
            findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            Toast.makeText(this, "Please check internet and try again!", Toast.LENGTH_LONG).show();

            ContextThemeWrapper ctw = new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme);
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctw);
                    alertDialog.setCancelable(false);

                    alertDialog.setTitle("Info");
                    alertDialog.setMessage("Please check internet connection and try again..");
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    alertDialog.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { // Here I've been added intent to open up data settings
                            startActivity(new Intent(
                                    Settings.ACTION_SETTINGS));
                        }
                    });
                     /* alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             finish();
                             System.exit(0);
                         }
                     });*/
                     alertDialog.setNegativeButton(android.R.string.no, null);
                     alertDialog.setIcon(R.drawable.ic_warning);
                     alertDialog.show();
        }

        Places.initialize(getApplicationContext(), apiKeyG);
        // Setup Places Client
        if (!Places.isInitialized()) {
            Places.initialize(MainActivity.this, apiKeyG);
        }
        placesClient = Places.createClient(this);

/*
        final AutocompleteSupportFragment autocompleteSupportFragment =
                (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));


        autocompleteSupportFragment.setOnPlaceSelectedListener(
                new PlaceSelectionListener() {
                    @Override
                    public void onPlaceSelected(Place place) {

                        Toast.makeText(MainActivity.this, "Searched successfully!" , Toast.LENGTH_SHORT).show();
                        CITY = place.getName();
                        new weatherTask().execute();

                        requestNewLocationData();

                    }

                    @Override
                    public void onError(Status status) {
                        Toast.makeText(MainActivity.this, "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
*/

        searchET.setFocusable(false);
        searchET.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //initializare place field
                List<Place.Field> fieldList = (Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
                //creare intent
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MainActivity.this);
                //start rezultate activity
                startActivityForResult(intent, 100);

            }
        });
/*
        search.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {


                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm.isAcceptingText()) { // verify if the soft keyboard is open
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                if (TextUtils.isEmpty(searchET.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Empty field not allowed!",
                            Toast.LENGTH_SHORT).show();
                } else {

                    CITY = searchET.getText().toString();
                    new weatherTask().execute();

                    requestNewLocationData();

                }
            }
        });*/


        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(checkInternet()) {
                searchET.setText("");
                new weatherTaskL().execute();
                Toast.makeText(MainActivity.this, "Refreshed successfully!",
                        Toast.LENGTH_SHORT).show();
                refreshLayout.setRefreshing(false); //if True avem loop infinit refresh
            }
                else {
                    findViewById(R.id.errorText).setVisibility(View.VISIBLE);
                    //Toast.makeText(this, "Please check internet and try again!", Toast.LENGTH_LONG).show();

                    ContextThemeWrapper ctw = new ContextThemeWrapper(MainActivity.this, R.style.AlertDialogTheme);
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ctw);
                    alertDialog.setCancelable(false);

                    alertDialog.setTitle("Info");
                    alertDialog.setMessage("Please check internet connection and try again..");
                    // Specifying a listener allows you to take an action before dismissing the dialog.
                    // The dialog is automatically dismissed when a dialog button is clicked.
                    alertDialog.setPositiveButton("GO TO SETTINGS", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) { // Here I've been added intent to open up data settings
                            startActivity(new Intent(
                                    Settings.ACTION_SETTINGS));
                        }
                    });
                     alertDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                         @Override
                         public void onClick(DialogInterface dialog, int which) {
                             finish();
                             System.exit(0);
                         }
                     });
                    alertDialog.setIcon(R.drawable.ic_warning);
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            CITY = place.getName();
            new weatherTask().execute();

        }
    }

    public boolean checkInternet(){

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();
        if (netInfo == null) {
            findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            Toast.makeText(this, "Please turn on Internet ", Toast.LENGTH_SHORT).show();
            return false;
        }
            else
                return true;

    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                mFusedLocationClient.getLastLocation().addOnCompleteListener(
                        new OnCompleteListener<Location>() {
                            @Override
                            public void onComplete(@NonNull Task<Location> task) {
                                Location location = task.getResult();
                                if (location == null) {
                                    requestNewLocationData();

                                } else {
                                    latTxt.setText(location.getLatitude() + "");
                                    lonTxt.setText(location.getLongitude() + "");
                                    LAT = location.getLatitude();
                                    LON = location.getLongitude();

                                }
                            }

                        }
                );

            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        } else {
            requestPermissions();
        }
    }


    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(3000);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(
                mLocationRequest, mLocationCallback,
                Looper.myLooper()
        );


    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            latTxt.setText(mLastLocation.getLatitude() + "");
            lonTxt.setText(mLastLocation.getLongitude() + "");
            LAT = mLastLocation.getLatitude();
            LON = mLastLocation.getLongitude();
            new weatherTaskL().execute();

        }
    };

    private boolean checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_ID

        );
        new weatherTaskL().execute();

    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();

            }
        }
        new weatherTaskL().execute();

    }




    @Override
    public void onResume() {
        super.onResume();
        if (checkPermissions()) {
            getLastLocation();

        }

    }
    //when user hits the submit button
    /*String oras;
    public void onSubmit(View v){
        //get references to the objects in the layout
        TextView tv = (TextView) findViewById(R.id.tv);
        EditText et = (EditText) findViewById(R.id.searchET);
        //set the text view to the name and the phone number entered by the user
        if (TextUtils.isEmpty(et.getText()) ){
            tv.setText("Fill the city!\n");
        }else{
            tv.setVisibility(View.GONE);
            oras = et.getText().toString();

        }


        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if(imm.isAcceptingText()) { // verify if the soft keyboard is open
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }*/

    class weatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* Showing the ProgressBar, Making the main design GONE */
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?q=" + CITY + "&units=metric&appid=" + API);
            return response;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(String result) {


            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject coord = jsonObj.getJSONObject("coord");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getInt("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");
                String address = jsonObj.getString("name") + ", " + sys.getString("country");
                String lat = "Lat: " + coord.getString("lat");
                String lon = "Lon: " + coord.getString("lon");
                String icon = weather.getString("icon");
                int id = weather.getInt("id");
                //Thunderstorm
                if (id >= 200 && id <= 202)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id >= 210 && id <= 212)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id >= 230 && id <= 232)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id == 221)
                    condIcon.setImageResource(R.drawable.d11);
                    //-----
                    //Drizzle
                else if (id >= 300 && id <= 302)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id >= 310 && id <= 314)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id == 321)
                    condIcon.setImageResource(R.drawable.d09);
                    //-----
                    //Rain
                else if (id >= 500 && id <= 504)
                    condIcon.setImageResource(R.drawable.d10);
                else if (id == 511)
                    condIcon.setImageResource(R.drawable.d13);
                else if (id >= 520 && id <= 522)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id == 531)
                    condIcon.setImageResource(R.drawable.d09);
                    //----
                    //Snow
                else if (id >= 600 && id <= 622)
                    condIcon.setImageResource(R.drawable.d13);
                    //----
                    //Atmosphere
                else if (id >= 700 && id <= 782)
                    condIcon.setImageResource(R.drawable.d50);
                    //----
                else if (id == 800) //clear sky
                    condIcon.setImageResource(R.drawable.d01);
                    //Clouds
                else if (id == 801)
                    condIcon.setImageResource(R.drawable.d02);
                else if (id == 802)
                    condIcon.setImageResource(R.drawable.d03);
                else if (id == 803)
                    condIcon.setImageResource(R.drawable.d04);
                else if (id == 804)
                    condIcon.setImageResource(R.drawable.d04);
                //--------

                /*
                if (icon == "01d")
                    condIcon.setImageResource(R.drawable.d01);
                else if (icon == "02d")
                    condIcon.setImageResource(R.drawable.d02);
                else if (icon == "03d")
                    condIcon.setImageResource(R.drawable.d03);
                else if (icon == "04d")
                    condIcon.setImageResource(R.drawable.d04);
                else if (icon == "09d")
                    condIcon.setImageResource(R.drawable.d09);
                else if (icon == "10d")
                    condIcon.setImageResource(R.drawable.d10);
                else if (icon == "11d")
                    condIcon.setImageResource(R.drawable.d11);
                else if (icon == "13d")
                    condIcon.setImageResource(R.drawable.d13);
                else if (icon == "50d")
                    condIcon.setImageResource(R.drawable.d50);*/


                // Picasso.get().load("http://openweathermap.org/img/wn/"+icon+"@2x.png").into(condIcon);

                /* Populating extracted data into our views */
                latTxt.setText(lat);
                lonTxt.setText(lon);

                addressTxt.setText(address);
                updated_atTxt.setText(updatedAtText);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);


                /* Views populated, Hiding the loader, Showing the main design */
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);


            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }

        }
    }


    class weatherTaskL extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            getLastLocation();
            /* Showing the ProgressBar, Making the main design GONE */
            findViewById(R.id.loader).setVisibility(View.VISIBLE);
            findViewById(R.id.mainContainer).setVisibility(View.GONE);
            findViewById(R.id.errorText).setVisibility(View.GONE);
        }

        protected String doInBackground(String... args) {
            String response = HttpRequest.excuteGet("https://api.openweathermap.org/data/2.5/weather?lat=" + LAT + "&lon=" + LON + "&units=metric&appid=" + API);
            return response;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONObject main = jsonObj.getJSONObject("main");
                JSONObject sys = jsonObj.getJSONObject("sys");
                JSONObject wind = jsonObj.getJSONObject("wind");
                JSONObject coord = jsonObj.getJSONObject("coord");
                JSONObject weather = jsonObj.getJSONArray("weather").getJSONObject(0);

                Long updatedAt = jsonObj.getLong("dt");
                String updatedAtText = "Updated at: " + new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(new Date(updatedAt * 1000));
                String temp = main.getInt("temp") + "°C";
                String tempMin = "Min Temp: " + main.getString("temp_min") + "°C";
                String tempMax = "Max Temp: " + main.getString("temp_max") + "°C";
                String pressure = main.getString("pressure");
                String humidity = main.getString("humidity");
                Long sunrise = sys.getLong("sunrise");
                Long sunset = sys.getLong("sunset");
                String windSpeed = wind.getString("speed");
                String weatherDescription = weather.getString("description");
                String address = jsonObj.getString("name") + ", " + sys.getString("country");
                String lat = "Lat: " + coord.getString("lat");
                String lon = "Lon: " + coord.getString("lon");
                String icon = weather.getString("icon");
                int id = weather.getInt("id");
                Log.d("tagicon", "Value: " + icon);
                //Thunderstorm
                if (id >= 200 && id <= 202)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id >= 210 && id <= 212)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id >= 230 && id <= 232)
                    condIcon.setImageResource(R.drawable.d11);
                else if (id == 221)
                    condIcon.setImageResource(R.drawable.d11);
                    //-----
                    //Drizzle
                else if (id >= 300 && id <= 302)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id >= 310 && id <= 314)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id == 321)
                    condIcon.setImageResource(R.drawable.d09);
                    //-----
                    //Rain
                else if (id >= 500 && id <= 504)
                    condIcon.setImageResource(R.drawable.d10);
                else if (id == 511)
                    condIcon.setImageResource(R.drawable.d13);
                else if (id >= 520 && id <= 522)
                    condIcon.setImageResource(R.drawable.d09);
                else if (id == 531)
                    condIcon.setImageResource(R.drawable.d09);
                    //----
                    //Snow
                else if (id >= 600 && id <= 622)
                    condIcon.setImageResource(R.drawable.d13);
                    //----
                    //Atmosphere
                else if (id >= 700 && id <= 782)
                    condIcon.setImageResource(R.drawable.d50);
                    //----
                /*else if (id == 800) //clear sky
                    condIcon.setImageResource(R.drawable.d01);*/
                    //Clouds
               /* else if (id == 801)
                    condIcon.setImageResource(R.drawable.d02);*/
                else if (id == 802)
                    condIcon.setImageResource(R.drawable.d03);
                else if (id == 803)
                    condIcon.setImageResource(R.drawable.d04);
                else if (id == 804)
                    condIcon.setImageResource(R.drawable.d04);

                switch(icon) {
                    case "01d":
                        condIcon.setImageResource(R.drawable.d01);
                        break;
                    case "01n":
                    condIcon.setImageResource(R.drawable.n01);
                        break;
                    case "02d":
                    condIcon.setImageResource(R.drawable.d02);
                        break;
                    case "02n":
                    condIcon.setImageResource(R.drawable.n02);
                        break;

                }
                /* Populating extracted data into our views */
                latTxt.setText(lat);
                lonTxt.setText(lon);

                addressTxt.setText(address);
                updated_atTxt.setText(updatedAtText);
                statusTxt.setText(weatherDescription.toUpperCase());
                tempTxt.setText(temp);
                temp_minTxt.setText(tempMin);
                temp_maxTxt.setText(tempMax);
                sunriseTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunrise * 1000)));
                sunsetTxt.setText(new SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(new Date(sunset * 1000)));
                windTxt.setText(windSpeed);
                pressureTxt.setText(pressure);
                humidityTxt.setText(humidity);

                /* Views populated, Hiding the loader, Showing the main design */
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.mainContainer).setVisibility(View.VISIBLE);

            } catch (JSONException e) {
                findViewById(R.id.loader).setVisibility(View.GONE);
                findViewById(R.id.errorText).setVisibility(View.VISIBLE);
            }

        }
    }
}
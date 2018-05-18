package com.bkay_apps.applordweather;

import android.Manifest;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    //Declare our widgets and global variables
    TextView display, temp, humidity, something, label1, label2;
    ImageView myImage;
    ProgressBar mProgressBar;
    StringBuilder stringBuilder;
    double lon,lat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hook them to the activity
        display = findViewById(R.id.cityView);
        temp = findViewById(R.id.tempView);
        humidity = findViewById(R.id.humidityView);
        something = findViewById(R.id.someView);
        label1 = findViewById(R.id.textView5);
        label2 = findViewById(R.id.textView6);
        myImage = findViewById(R.id.iconView);
        mProgressBar = findViewById(R.id.progressBar);

        /**Check if internet is available and phone has access then
         *Call the AsyncTask method to run the Http request in the background
         **/
        if (networkIsAvailable()) {
            loadData();
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(R.string.errorTitle);
            dialog.setCancelable(true);
            dialog.setMessage(R.string.errorMsg);
            dialog.setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.exit(0);
                }
            });
            dialog.setPositiveButton(R.string.retry, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    System.exit(0);
                }
            });
            dialog.show();
        }

    }

    //this method is do that we don't keep typing out the execute code
    public void loadData() {

        new FetchDataFromServer().execute();
    }

    //Getting info about internet availability and user connectivity
    public boolean networkIsAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    public class FetchDataFromServer extends AsyncTask<Void, Void, Void> {

        String result;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //Start by showing the progressBar so that the user doesnt think that the app froze
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                /*Alternatively I could use LocationManager to get the user's co-ordinates but here
                I hardcoded the location since I was still developing*/
                URL url = new URL("http://api.openweathermap.org/data/2.5/" +
                        "weather?lat=-26.2&lon=28.04&appid=2d85c3da1ac08ee612dd16f23db3128a");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream is = httpURLConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                stringBuilder = new StringBuilder();

                String line = "";

                while ((line = br.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            //Dismiss the progressBar and show the result
            mProgressBar.setVisibility(View.INVISIBLE);

            try {
                JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                JSONObject weatherElement = new JSONObject(jsonObject.getString("main"));
                Double value = Double.parseDouble(weatherElement.getString("temp"));
                int adjustedValue = (int) (value - 273.15);
                display.setText(jsonObject.getString("name"));
                myImage.setVisibility(View.VISIBLE);
                label1.setVisibility(View.VISIBLE);
                label2.setVisibility(View.VISIBLE);
                temp.setText(adjustedValue + "°C");
                humidity.setText(weatherElement.getString("humidity"));
                something.setText(weatherElement.getString("pressure"));

                JSONArray array = jsonObject.getJSONArray("weather");
                String iconCode = array.getJSONObject(0).getString("icon");
                String iconURL = "http://openweathermap.org/img/w/" + iconCode + ".png";;
                Picasso.with(MainActivity.this).load(iconURL).into(myImage);




            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Add action if the refresh button is pressed
        if (item.getItemId() == R.id.action_refresh) {
            display.setText("");
            humidity.setText("");
            temp.setText("");
            something.setText("");
            label2.setVisibility(View.INVISIBLE);
            label1.setVisibility(View.INVISIBLE);
            myImage.setVisibility(View.INVISIBLE);
            loadData();
        }
        return super.onOptionsItemSelected(item);

    }

}

package com.bkay_apps.applordweather;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    //Declare our widgets and global variables
    TextView display;
    ProgressBar mProgressBar;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Hook them to the activity
        display = findViewById(R.id.textView);
        mProgressBar = findViewById(R.id.progressBar);

        /**Check if internet is available and phone has access then
          *Call the AsyncTask method to run the Http request in the background
         **/
        if (networkIsAvailable()){
            loadData();
        }else{
            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
            dialog.setTitle(R.string.errorTitle);
            dialog.setCancelable(true);
            dialog.setMessage(R.string.errorMsg);
            dialog.setNegativeButton("Exit", null);
            dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    loadData();
                }
            });
        }

    }

    //this method is do that we don't keep typing out the execute code
    public void loadData(){

        new FetchDataFromServer().execute();
    }

    //Getting info about internet availability and user connectivity
    public boolean networkIsAvailable(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        boolean isAvailable = false;
        if (networkInfo.isConnected() && networkInfo != null){
            isAvailable = true;
        }
        return isAvailable;
    }

    public class FetchDataFromServer extends AsyncTask<Void, Void, Void>{

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
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=-26.2&lon=28.04&appid=2d85c3da1ac08ee612dd16f23db3128a");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream is = httpURLConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line = "";

                while (line != null){
                    line = br.readLine();
                    result += line;
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
            //The JSONObject class is giving me a hard time so here I show the JSON formatted data
            display.setText(result);
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
        if (item.getItemId() == R.id.action_refresh){
            display.setText("");
            loadData();
        }
        return super.onOptionsItemSelected(item);
    }
}

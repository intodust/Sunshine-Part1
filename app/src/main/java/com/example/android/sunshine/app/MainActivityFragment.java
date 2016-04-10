package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Result;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    public ArrayAdapter<String> mforecast;
    public ListView listView;
    public MainActivityFragment() {
    }
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.forecastfragment, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("fragment called", "onOptionsItemSelected-start");
        int id = item.getItemId();
        if(id== R.id.action_refresh){
            updateWeather();
            return true;
        }
        Log.d("fragment called", "onOptionsItemSelected-end");
        return super.onOptionsItemSelected(item);
    }
    private void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location= sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        fetchWeatherTask.execute(location);
    }
    @Override
    public void onStart(){
        super.onStart();
        updateWeather();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("MainActivityFragment", "--Deepak Calling");
        View rootView = inflater.inflate(R.layout.fragment_main, container);
        String[] forecastArray = {"Today - Sunny", "Tomorrow - Rainy", "Tuesday - Cloudy", "Wednesday - Sunny", "Thursday - Rainy"};

        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArray));
        mforecast = new ArrayAdapter(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>() /*weekForecast*/);
        listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mforecast);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d("listener called", "listview");
                    String forecast = mforecast.getItem(position);
                    Log.d("forecast", forecast);
                    Intent intent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, forecast);
                    startActivity(intent);
                    //intent.setData(forecast);
                    // Toast.makeText(getContext(),forecast,Toast.LENGTH_LONG).show();
                }
            });
        return rootView;
    }
     public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
         private final String LOG_FETCH = FetchWeatherTask.class.getSimpleName();

         @Override
         protected String[] doInBackground(String... params) {
             if (params.length == 0)
                 return null;
             HttpURLConnection urlConnection = null;
             BufferedReader reader = null;
             String jsonString = null;
             String formart = "json";
             String unit = "metric";
             int num_days = 7;
             String apiKey = "e395d3159373e72863fe16771d52a1bd";
             final String BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
             final String QUERY_PARAM = "q";
             final String FORMAT_PARAM = "mode";
             final String UNIT_PARAM = "units";
             final String DAYS_PARAM = "cnt";
             final String APP_ID = "APPID";
             String[] weatherForecast = null;
             try {
                 //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7&APPID=e395d3159373e72863fe16771d52a1bd");

                 Uri urlBuilder = Uri.parse(BASE_URL).buildUpon().appendQueryParameter(QUERY_PARAM, params[0]).appendQueryParameter(FORMAT_PARAM, formart).appendQueryParameter(UNIT_PARAM, unit).appendQueryParameter(DAYS_PARAM, Integer.toString(num_days)).appendQueryParameter(APP_ID, apiKey).build();
                 URL url = new URL(urlBuilder.toString());
                 urlConnection = (HttpURLConnection) url.openConnection();
                 urlConnection.setRequestMethod("GET");
                 urlConnection.connect();



              /*  Log.v(LOG_FETCH, "URL Created---" + urlBuilder.toString());*/
                 InputStream inputStream = urlConnection.getInputStream();
                 if (inputStream == null) {
                     return null;
                 }
                 StringBuffer stringBuffer = new StringBuffer();
                 String line = "";
                 reader = new BufferedReader(new InputStreamReader(inputStream));
                 while ((line = reader.readLine()) != null) {
                     stringBuffer.append(line + "\n");
                 }
                 if (stringBuffer.length() == 0) {
                     return null;
                 }
                 jsonString = stringBuffer.toString();
               /* Log.v(LOG_FETCH, "Forecast Data: " + jsonString);*/
                 //parseJson(jsonString, 1);

             } catch (IOException e) {
                 Log.e(LOG_FETCH, "Error " + e);
                 return null;
             } finally {
                 if (urlConnection != null) {
                     urlConnection.disconnect();
                 }
                 if (reader != null) {
                     try {
                         reader.close();
                     } catch (final IOException e) {
                         Log.e(LOG_FETCH, "Error " + e);
                     }
                 }
             }
             try {
                 return getWeatherDataFromJson(jsonString, num_days);
                 // mforecast.clear();
                 //mforecast.addAll(returnData);

             } catch (JSONException e) {
                 Log.e(LOG_FETCH, "JSON Exception " + e);
                 return null;
             }
         }

         @Override
         protected void onPostExecute(String[] result) {
             if (result != null) {
                 mforecast.clear();
                 for (String dailyforecast : result) {
                     mforecast.add(dailyforecast);
                 }
             }
         }
         private void parseJson(String jsonData, int index) {
             String PARAM_TEMP = "temp";
             try {
                 JSONObject jsonObject = new JSONObject(jsonData);
                 JSONArray geoData = jsonObject.getJSONArray("list");
                 if (index > 0) {
                     JSONObject geoTemperature = geoData.getJSONObject(index);
                     JSONObject temperature = geoTemperature.getJSONObject(PARAM_TEMP);
                     double maxTemp = temperature.getDouble("max");
                     double minTemp = temperature.getDouble("min");
                     Log.v("temperature-", "min" + minTemp + " --Max--" + maxTemp);
                     Log.v("Temp", "value= " + new JSONObject(jsonData).getJSONArray("list").getJSONObject(index).getJSONObject("temp"));
                 }
             } catch (JSONException ex) {
                 Log.e("Parsing Exception", ex + "JSON Error");
             }
         }

         private String getReadableDateString(long time) {
             // Because the API returns a unix timestamp (measured in seconds),
             // it must be converted to milliseconds in order to be converted to valid date.
             SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
             return shortenedDateFormat.format(time);
         }

         /**
          * Prepare the weather high/lows for presentation.
          */
         private String formatHighLows(double high, double low) {
             // For presentation, assume the user doesn't care about tenths of a degree.

             SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
             String unitType = sharedPreferences.getString(getString(R.string.pref_units_key), getString(R.string.pref_units_metric));
             if(unitType.equals(getString(R.string.pref_units_imperial))){
                 high= (high*1.8) + 32;
                 low= (low*1.8) + 32;
             }
             else if(!unitType.equals(getString(R.string.pref_units_metric))){
                 Log.d(LOG_FETCH, "Unit type not found: " + unitType);
             }
             long roundedHigh = Math.round(high);
             long roundedLow = Math.round(low);

             String highLowStr = roundedHigh + "/" + roundedLow;
             return highLowStr;
         }


         /**
          * Take the String representing the complete forecast in JSON Format and
          * pull out the data we need to construct the Strings needed for the wireframes.
          * <p/>
          * Fortunately parsing is easy:  constructor takes the JSON string and converts it
          * into an Object hierarchy for us.
          */
         private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                 throws JSONException {

             // These are the names of the JSON objects that need to be extracted.
             final String OWM_LIST = "list";
             final String OWM_WEATHER = "weather";
             final String OWM_TEMPERATURE = "temp";
             final String OWM_MAX = "max";
             final String OWM_MIN = "min";
             final String OWM_DESCRIPTION = "main";

             JSONObject forecastJson = new JSONObject(forecastJsonStr);
             JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);


             Time dayTime = new Time();
             dayTime.setToNow();

             // we start at the day returned by local time. Otherwise this is a mess.
             int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

             // now we work exclusively in UTC
             dayTime = new Time();

             String[] resultStrs = new String[numDays];
             for (int i = 0; i < weatherArray.length(); i++) {
                 // For now, using the format "Day, description, hi/low"
                 String day;
                 String description;
                 String highAndLow;

                 // Get the JSON object representing the day
                 JSONObject dayForecast = weatherArray.getJSONObject(i);

                 // The date/time is returned as a long.  We need to convert that
                 // into something human-readable, since most people won't read "1400356800" as
                 // "this saturday".
                 long dateTime;
                 // Cheating to convert this to UTC time, which is what we want anyhow
                 dateTime = dayTime.setJulianDay(julianStartDay + i);
                 day = getReadableDateString(dateTime);

                 // description is in a child array called "weather", which is 1 element long.
                 JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                 description = weatherObject.getString(OWM_DESCRIPTION);

                 // Temperatures are in a child object called "temp".  Try not to name variables
                 // "temp" when working with temperature.  It confuses everybody.
                 JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                 double high = temperatureObject.getDouble(OWM_MAX);
                 double low = temperatureObject.getDouble(OWM_MIN);

                 highAndLow = formatHighLows(high, low);
                 resultStrs[i] = day + " - " + description + " - " + highAndLow;
             }

     /*   for (String s : resultStrs) {
            Log.v("getWeatherDataFromJson", "Forecast entry: " + s);
        }*/
             return resultStrs;

         }
     }
}

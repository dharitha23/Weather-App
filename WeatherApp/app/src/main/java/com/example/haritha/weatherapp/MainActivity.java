package com.example.haritha.weatherapp;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private TextView tv1;
    private TextView tv2;
    private TextView city_name_tv;
    private Button get_weather_bt;
    private TextView out_city_name;
    private TextView out_temp;
    private TextView out_min_max_temp;
    private TextView out_main_weather;
    private TextView out_desc;
    private TextView humidity_clouds_tv;
    private Runnable runnable;
    private String city_name;
    private TextView weather_icon;
    private RelativeLayout relativeLayout;

    private void setDefault() //Halifax weather is set as default
    {
        runnable = new Runnable() {
            @Override
            public void run() {
                getWeather("halifax");
            }
        };

        //retrieve data on separate thread
        Thread thread = new Thread(null, runnable, "background");
        thread.start();
    }

    private void setWeatherIconAndBg (String day_night, int real_id)  //sets Icon and bg according to the weather in a city
    {

        int id = real_id / 100; //removing zeroes for convenience
        if (real_id == 800 && day_night.contains("d")) {
            weather_icon.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.sunny, 0, 0);
            relativeLayout.setBackgroundResource(R.drawable.clearskyday);
        }
        else if (real_id == 800 && day_night.contains("n")) {
            weather_icon.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.moon, 0, 0);
            relativeLayout.setBackgroundResource(R.drawable.clearskynight);
        }
        else {
            switch(id) {
                case 2 : weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.storm,0,0);
                            relativeLayout.setBackgroundResource(R.drawable.stormbg);
                            break;
                case 3 : weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.drizzle,0,0);
                            relativeLayout.setBackgroundResource(R.drawable.drizzlebg);
                            break;
                case 5 : weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.rain,0,0);
                            relativeLayout.setBackgroundResource(R.drawable.rainbg);
                             break;
                case 6 : weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.snow,0,0);
                            relativeLayout.setBackgroundResource(R.drawable.snowbg);
                            break;
                case 7 : weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.fog,0,0);
                            relativeLayout.setBackgroundResource(R.drawable.fogbg);
                            break;
                case 8 :
                    weather_icon.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.clouds, 0, 0);
                    if (day_night.contains("d"))
                        relativeLayout.setBackgroundResource(R.drawable.cloudsday);
                    else
                        relativeLayout.setBackgroundResource(R.drawable.cloudsnight);
                    break;
            }
        }

    }

    private void getWeather(String city_name)  //fetches weather details from URL by city name

    {

        if(city_name!= null && !city_name.trim().equals(""))
        {

            final String open_weather_map_url = "http://api.openweathermap.org/data/2.5/weather" + "?q=" +
                    city_name + "&appid=d11d7dfb234b9d98e8f015b19331c12e" + "&units=metric"; //gets temperature in degrees

            //build the request
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, open_weather_map_url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject weather_response) {

                    try {

                        String city_name = weather_response.optString("name");
                        String country_name = weather_response.getJSONObject("sys").optString("country");
                        String city_country = city_name + "," + country_name;
                        out_city_name.setText(city_country);


                        //accessing main object from JSON to get temp, humidity, min and max temp of the day
                        JSONObject main = weather_response.getJSONObject("main");
                        String deg = "°C";
                        String min = "Min";
                        String max = "Max";
                        Double t = main.getDouble("temp");
                        String tem = String.valueOf(Math.round(t));
                        String temp = tem + deg;
                        out_temp.setText(temp);
                        String humidity = main.getInt("humidity") + "%";
                        String temp_min = min + " " + main.getInt("temp_min") + deg;
                        String temp_max = max + " " + main.getInt("temp_max") + deg;
                        String temp_min_max = temp_min + " " + temp_max;
                        out_min_max_temp.setText(temp_min_max);


                        //accessing weather array from JSON to get details about main weather, icon and description

                        JSONArray array = weather_response.getJSONArray("weather");
                        JSONObject weather_obj = array.getJSONObject(0);
                        out_main_weather.setText(weather_obj.optString("main"));
                        out_desc.setText(weather_obj.optString("description"));
                        setWeatherIconAndBg( weather_obj.optString("icon"),weather_obj.getInt("id"));


                        //accessing clouds object from JSON to get percentage of cloud coverage
                        weather_response = weather_response.getJSONObject("clouds");
                        String clouds = weather_response.getInt("all") + "%" ;
                        String hc_tv = "Humidity" + " " + humidity + " " + "Clouds" + " " + clouds;
                        humidity_clouds_tv.setText(hc_tv);

                    } catch (JSONException e) {

                        e.printStackTrace();

                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();

                    Toast.makeText(getApplicationContext(), "Enter a valid city name", Toast.LENGTH_SHORT).show();
                    tv2.setText(null);
                    out_city_name.setText(null);
                    out_desc.setText(null);
                    out_main_weather.setText(null);
                    out_min_max_temp.setText(null);
                    out_temp.setText(null);
                    humidity_clouds_tv.setText(null);
                    weather_icon.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
                }
            });

            //add the request to queue
            RequestQueueSingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
        }
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv1 = findViewById(R.id.tv1);
        city_name_tv =  findViewById(R.id.city_name_tv);
        out_city_name =  findViewById(R.id.out_city_name);
        out_temp =  findViewById(R.id.out_temp);
        out_min_max_temp =  findViewById(R.id.out_min_max_temp);
        out_main_weather =  findViewById(R.id.out_main_weather);
        out_desc =  findViewById(R.id.out_desc);
        humidity_clouds_tv = findViewById(R.id.humidity_clouds_tv);
        get_weather_bt = findViewById(R.id.get_weather_bt);
        tv2 = findViewById(R.id.tv2);
        weather_icon = findViewById(R.id.weather_icon);
        relativeLayout = findViewById(R.id.layout);

        setDefault();

        get_weather_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                city_name = tv2.getText().toString();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        getWeather(city_name);
                    }
                };

                //retrieve data on separate thread
                Thread thread = new Thread(null, runnable, "background");
                thread.start();

                //close the soft keyboard
                InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }
}

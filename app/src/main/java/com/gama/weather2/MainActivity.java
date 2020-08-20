package com.gama.weather2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.gama.weather2.R.drawable.bg_gradient;
import static com.gama.weather2.R.drawable.bg_gradient_morning;
import static com.gama.weather2.R.drawable.bg_gradient_night;

public class MainActivity extends Activity {

    long unixSecond,sunrise,sunset;
    ImageView search;
    TextView tempText, city, humidityText, windText, pressureText, minTempText, maxTempText, descriptionText, sunriseText, sunsetText;
    EditText textField;
    FusedLocationProviderClient fusedLocationProviderClient;
    LinearLayout ll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        search = findViewById(R.id.searchImg);
        tempText = findViewById(R.id.temp);
        maxTempText = findViewById(R.id.temp_max);
        minTempText = findViewById(R.id.temp_min);
        windText = findViewById(R.id.wind);
        pressureText = findViewById(R.id.pressure);
        humidityText = findViewById(R.id.humidity);
        textField = findViewById(R.id.city);
        descriptionText = findViewById(R.id.status);
        city = findViewById(R.id.cityText);
        sunriseText = findViewById(R.id.sunrise);
        sunsetText = findViewById(R.id.sunset);
        ll =findViewById(R.id.Layout);

        setBackground();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Here we will call API
                Log.e("city", textField.getText().toString());
                getWeatherData(textField.getText().toString().trim());


            }
        });

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location != null) {
                    try {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        double lat = addresses.get(0).getLatitude();
                        double lon = addresses.get(0).getLongitude();
                        String name = addresses.get(0).getLocality();
                        String latitude = String.valueOf(lat);
                        String longitude = String.valueOf(lon);
                        city.setText(name);
                        String appId = "957b2b5e5ae2f3d67d26fc4af1618784";
                        getCurrentData(latitude, longitude, appId);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }



    String dateConverter(long unix){
        Date date = new java.util.Date(unix*1000L);
        SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm");
        String formattedDate = sdf.format(date);
        Log.e("Time",formattedDate);
        return  formattedDate;
    }

    private void getWeatherData(final String name){

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);


        Call<Weather> call = apiInterface.getWeatherData(name);

        call.enqueue(new Callback<Weather>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Weather> call, @NonNull Response<Weather> response) {

                assert response.body() != null;
                double rndm = Math.random()*5;
                double max =(response.body().getMain().getTempMax()+rndm);
                double min =(response.body().getMain().getTempMin()-(Math.random()*2));
                tempText.setText((response.body().getMain().getTemp())+"°C");
                minTempText.setText("Min Temp: "+Math.ceil(min) +"°");
                maxTempText.setText("Max Temp: "+Math.ceil(max) +"°");
                pressureText.setText(response.body().getMain().getPressure().toString());
                humidityText.setText(response.body().getMain().getHumidity().toString());
                descriptionText.setText(response.body().getWeather().get(0).getDescription());
                windText.setText(response.body().getWind().getSpeed()+" Km/h");
                city.setText(name);
                sunrise = response.body().getSys().getSunrise();
                sunriseText.setText(dateConverter(sunrise)+"AM");
                sunset = response.body().getSys().getSunset();
                sunsetText.setText(dateConverter(sunset)+"PM");

            }

            @Override
            public void onFailure(@NonNull Call<Weather> call,@NonNull Throwable t) {

                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();

            }
        });


    }

    private void getCurrentData(final String lat,final String lon,final String appId){

        ApiFace apiInterface = ApiClient.getClient().create(ApiFace.class);

        Call<Weather> call = apiInterface.getCurrentData(lat,lon,appId);

        call.enqueue(new Callback<Weather>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<Weather> call,@NonNull Response<Weather> response) {

                assert response.body() != null;
                double rndm = Math.random()*5;
                double max =(response.body().getMain().getTempMax()+rndm);
                double min =(response.body().getMain().getTempMin()-(Math.random()*2));
                tempText.setText((response.body().getMain().getTemp())+"°C");
                minTempText.setText("Min Temp: "+Math.ceil(min)+"°");
                maxTempText.setText("Max Temp: "+Math.ceil(max)+"°");
                pressureText.setText(response.body().getMain().getPressure().toString());
                humidityText.setText(response.body().getMain().getHumidity().toString());
                descriptionText.setText(response.body().getWeather().get(0).getMain());
                windText.setText(response.body().getWind().getSpeed()+" Km/h");
                sunrise = response.body().getSys().getSunrise();
                sunriseText.setText(dateConverter(sunrise)+"AM");
                unixSecond = response.body().getSys().getSunset();
                sunsetText.setText(dateConverter(sunset)+"PM");


            }

            @Override
            public void onFailure(@NonNull Call<Weather> call,@NonNull Throwable t) {

                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();
            }
        });


    }

    private void setBackground(){

        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("HH:mm");
        String localTime = date.format(currentLocalTime);
        String[] splitTime = localTime.split(":");
        String hours = splitTime[0];
        int mhour = Integer.parseInt(hours);
        Log.e("Hour", String.valueOf(mhour));
        Log.e("Local Time",localTime);

        int sunrise = 5;
        int sunset = 18;
        int evening = 12;
        if (mhour >= sunrise && mhour < evening){
            ll.setBackgroundResource(bg_gradient_morning);
        } else if (mhour >= evening && mhour < sunset){
            ll.setBackgroundResource(bg_gradient);
        }else {
            ll.setBackgroundResource(bg_gradient_night);
        }

    }
}
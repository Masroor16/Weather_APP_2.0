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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends Activity {

    long unixSecond;
    ImageView search;
    TextView tempText, city, humidityText, windText, pressureText, minTempText, maxTempText, descriptionText, sunriseText, sunsetText;
    EditText textField;
    FusedLocationProviderClient fusedLocationProviderClient;

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
                        String ad = addresses.get(0).getLocality();
                        getWeatherData(ad);
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
                tempText.setText((response.body().getMain().getTemp())+"°C");
                minTempText.setText("Min Temp: "+response.body().getMain().getTempMin()+"°");
                maxTempText.setText("Max Temp: "+response.body().getMain().getTempMax()+"°");
                pressureText.setText(response.body().getMain().getPressure().toString());
                humidityText.setText(response.body().getMain().getHumidity().toString());
                descriptionText.setText(response.body().getWeather().get(0).getDescription());
                windText.setText(response.body().getWind().getSpeed()+" Km/h");
                city.setText(name);
                unixSecond = response.body().getSys().getSunrise();
                sunriseText.setText(dateConverter(unixSecond)+"AM");
                unixSecond = response.body().getSys().getSunset();
                sunsetText.setText(dateConverter(unixSecond)+"PM");


            }

            @Override
            public void onFailure(@NonNull Call<Weather> call,@NonNull Throwable t) {

                Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_LONG).show();

            }
        });


    }
}
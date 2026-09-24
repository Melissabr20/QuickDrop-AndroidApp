package com.example.QuickDrop.ui.auth;

import com.example.QuickDrop.ui.admin.AdminHomeActivity;
import com.example.QuickDrop.ui.client.ClientHomeActivity;
import com.example.QuickDrop.ui.fournisseur.FournisseurHomeActivity;
import com.example.QuickDrop.ui.livreur.LivreurHomeActivity;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginRequest;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonRequest;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Path;


public class LoginActivity extends AppCompatActivity  {
    Button btnsigin;
    EditText username,password;
    String name,pass;

    private LocationManager locationManager;
    private static final int REQUEST_LOCATION_PERMISSION = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        btnsigin=findViewById(R.id.btnsignin);
        username=findViewById(R.id.username);
        password=findViewById(R.id.password);

        // Check location permission and request if not granted
        if ((ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(LoginActivity.this, "pas de permissions", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }

        // Button click listener
        btnsigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = username.getText().toString();
                pass = password.getText().toString();
                if (name.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
                } else {


                    startLocationUpdates();
                }
            }
        });
    }

    // Method to start location updates
    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_LOCATION_PERMISSION
            );

            return;
        }

        Log.d("LOCATION", "Permission OK");

        Log.d("LOCATION", "GPS enabled = "
                + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));

        Log.d("LOCATION", "NETWORK enabled = "
                + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));

        requestSingleLocationUpdate();
    }

    @SuppressWarnings("MissingPermission")
    private void requestSingleLocationUpdate() {
        String provider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                ? LocationManager.GPS_PROVIDER
                : LocationManager.NETWORK_PROVIDER;
        locationManager.requestLocationUpdates(provider, 500, 0, locationListener, Looper.getMainLooper());
    }

    // Location listener (replaces Google Play Services' LocationCallback)
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            handleLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, android.os.Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    };

    // Method to handle location updates
    private void handleLocation(Location location) {
        Log.d(
                "LOCATION",
                "Position reçue : "
                        + location.getLatitude()
                        + ", "
                        + location.getLongitude()
        );

        locationManager.removeUpdates(locationListener);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(name);
        loginRequest.setPassword(pass);

        loginUser(loginRequest, location);

    }

    public void loginUser(LoginRequest loginRequest, Location location){
        Call<LoginResponse> loginresponseCall=ApiClient.getService().loginUser(loginRequest);
        loginresponseCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if(response.isSuccessful()){
                    LoginResponse loginresponse = response.body();

                    if (loginresponse != null) {
                        LoginResponse.Client client = loginresponse.getClient();
                        LoginResponse.Livreur livreur = loginresponse.getLivreur();
                        LoginResponse.Fournisseur fournisseur = loginresponse.getFournisseur();
                        LoginResponse.User superuser=loginresponse.getSuperuser();
                        LoginResponse.Token token = loginresponse.getToken();
                        String access =token.getAccess();
                        if(superuser!=null){
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("accessToken", access);
                            Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                            startActivity(intent);

                        }
                        if(client!=null) {
                            LoginResponse.User user = client.getUser();
                            String usernameres = user.getUsername();
                            String emailres = user.getEmail();
                            int idclient = client.getId();
                            String lastname = user.getLast_name();
                            String firstname = user.getFirst_name();

                            // Récupérer des références à SharedPreferences
                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("accessToken", access);
                            editor.putString("username", usernameres);
                            editor.putString("lastname", lastname);
                            editor.putString("firstname", firstname);
                            editor.putString("email", emailres);
                            editor.putInt("idclient", idclient);
                            editor.apply();

                            // Récupérer l'adresse et les coordonnées GPS du client
                            if (location != null) {

                                Geocoder geocoder = new Geocoder(LoginActivity.this, Locale.getDefault());

                                try {
                                    List<Address> addresses = geocoder.getFromLocation(
                                            location.getLatitude(),
                                            location.getLongitude(),
                                            1
                                    );

                                    if (addresses != null && !addresses.isEmpty()) {

                                        Address addr = addresses.get(0);

                                        PutAdresseLivraisonRequest putAdresseLivraisonRequest =
                                                new PutAdresseLivraisonRequest();

                                        putAdresseLivraisonRequest.setCountry(addr.getCountryName());
                                        putAdresseLivraisonRequest.setCity(addr.getLocality());
                                        putAdresseLivraisonRequest.setAddress(addr.getAddressLine(0));
                                        putAdresseLivraisonRequest.setWilaya(addr.getAdminArea());

                                        // IMPORTANT : envoyer les coordonnées GPS
                                        putAdresseLivraisonRequest.setLat(
                                                String.valueOf(location.getLatitude())
                                        );

                                        putAdresseLivraisonRequest.setLng(
                                                String.valueOf(location.getLongitude())
                                        );

                                        Log.d("LOCATION_CLIENT",
                                                "LAT = " + location.getLatitude()
                                                + " LNG = " + location.getLongitude());

                                        Log.d("ADDRESS_CLIENT",
                                                "Address = " + addr.getAddressLine(0));

                                        PutLocationClient(
                                                putAdresseLivraisonRequest,
                                                access
                                        );

                                    } else {
                                        Log.e("GEOCODER", "Aucune adresse trouvée");
                                    }

                                } catch (IOException e) {
                                    Log.e("GEOCODER", "Erreur Geocoder", e);
                                }

                            } else {
                                Log.e("LOCATION_CLIENT", "Location = null");

                                // Si aucune position n'est disponible, ouvrir quand même l'application
                                Intent intent = new Intent(
                                        LoginActivity.this,
                                        ClientHomeActivity.class
                                );
                                startActivity(intent);
                            }

                            Intent intent = new Intent(LoginActivity.this,ClientHomeActivity.class);
                            startActivity(intent);
                        }else if (livreur!=null){

                            LoginResponse.User user = livreur.getUser();
                            String usernameres = user.getUsername();
                            String emailres = user.getEmail();
                            int idlivreur = livreur.getId();
                            String lastname = user.getLast_name();
                            String firstname = user.getFirst_name();

                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("accessToken", access);
                            editor.putString("username", usernameres);
                            editor.putString("lastname", lastname);
                            editor.putString("firstname", firstname);
                            editor.putString("email", emailres);
                            editor.putInt("idlivreur", idlivreur);
                            editor.putInt("fourOrliv",1);
                            editor.apply();

                            // NOTE: on the first loginUser() call fired from startLocationUpdates(),
                            // "location" is intentionally null (the GPS fix hasn't arrived yet).
                            // This branch previously called location.getLatitude()/getLongitude()
                            // unconditionally, which crashed with a NullPointerException on that
                            // first call. Guard it the same way the client branch above already does:
                            // geocode when we have a location, otherwise just proceed to the livreur
                            // home screen and let the second loginUser() call (from handleLocation(),
                            // once a real fix arrives) update the address.
                            if (location != null) {
                                Geocoder geocoder = new Geocoder(LoginActivity.this, Locale.getDefault());

                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && !addresses.isEmpty()) {
                                        PutAdresseLivraisonRequest putAdresseLivraisonRequest = new PutAdresseLivraisonRequest();
                                        putAdresseLivraisonRequest.setCity(addresses.get(0).getLocality());
                                        putAdresseLivraisonRequest.setAddress(addresses.get(0).getAddressLine(0));
                                        putAdresseLivraisonRequest.setWilaya(addresses.get(0).getAdminArea());

                                        // IMPORTANT : envoyer les coordonnées GPS
                                        putAdresseLivraisonRequest.setLat(
                                                String.valueOf(location.getLatitude())
                                        );

                                        putAdresseLivraisonRequest.setLng(
                                                String.valueOf(location.getLongitude())
                                        );

                                        PutAddressLivreur (idlivreur,putAdresseLivraisonRequest);
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    Log.e("Geocoder", "Error while getting address from latitude and longitude");
                                }
                            } else {
                                Log.e("LOCATION_LIVREUR", "Location = null");
                                Intent intent = new Intent(LoginActivity.this, LivreurHomeActivity.class);
                                startActivity(intent);
                            }

                        }
                        else if (fournisseur!=null){
                            String message ="Fournisseur"  ;
                            Toast.makeText(LoginActivity.this,message ,Toast.LENGTH_LONG).show();
                            LoginResponse.User user = fournisseur.getUser();
                            String usernameres = user.getUsername();
                            String emailres = user.getEmail();
                            int idfournisseur = fournisseur.getId();
                            String lastname = user.getLast_name();
                            String firstname = user.getFirst_name();

                            SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("accessToken", access);
                            editor.putString("username", usernameres);
                            editor.putString("lastname", lastname);
                            editor.putString("firstname", firstname);
                            editor.putString("email", emailres);
                            editor.putInt("idfournisseur", idfournisseur);
                            editor.putInt("fourOrliv",0);
                            editor.apply();

                            Intent intent = new Intent(LoginActivity.this, FournisseurHomeActivity.class);
                            startActivity(intent);
                        }
                    }
                }else{
                    String message ="error try later";
                    Toast.makeText(LoginActivity.this,message,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                String message=t.getLocalizedMessage();
                Toast.makeText(LoginActivity.this,message,Toast.LENGTH_LONG).show();
                Log.e("API_ERROR", "Failure: " + t.getMessage());


            }
        });
    }

    private void  PutLocationClient(PutAdresseLivraisonRequest putAdresseLivraisonRequest ,String token ){
        Call<PutAdresseLivraisonResponse> putAdresseLivraisonResponseCall=ApiClient.getService().putadresseinitial("Bearer "+ token ,putAdresseLivraisonRequest );
        putAdresseLivraisonResponseCall.enqueue(new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()){

                    String message ="successful"  ;
                    Toast.makeText(LoginActivity.this,message ,Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(LoginActivity.this,ClientHomeActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        });

    }
    public void PutAddressLivreur (int id_livreur, PutAdresseLivraisonRequest putAdresseLivraisonRequest){
        Call<PutAdresseLivraisonRequest> putAdresseLivraisonResponseCall=ApiClient.getService().PutAddressLivreur(id_livreur,putAdresseLivraisonRequest);
        putAdresseLivraisonResponseCall.enqueue(new Callback<PutAdresseLivraisonRequest>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonRequest> call, Response<PutAdresseLivraisonRequest> response) {
                if (response.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, LivreurHomeActivity.class);
                    startActivity(intent);

                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonRequest> call, Throwable t) {

            }
        });


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                startLocationUpdates();
            } else {
                // Show dialog to enable location settings
                showLocationDialog();
            }
        }
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Activate Location");
        builder.setMessage("To access this feature, please enable location.");
        builder.setPositiveButton("Enable", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
}
package com.example.QuickDrop.ui.client;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonRequest;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import com.example.QuickDrop.core.map.OsmMapConfig;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;


import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lets the user search for and confirm a delivery address on a map.
 *
 * Uses osmdroid (OpenStreetMap) instead of the Google Maps SDK. Address search still
 * relies on Android's built-in {@link Geocoder}, which is part of the platform, works
 * without any Maps API key, and needs no extra dependency.
 */
public class DeliveryAddressActivity extends FragmentActivity {

    private MapView map;
    private SearchView searchView;
    private Button btnchangeadd;
    private Address address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_address);

        map = findViewById(R.id.map);
        map.setMultiTouchControls(true);
        map.setTileSource(OsmMapConfig.OPEN_TOPO_MAP);   // <-- this line
        map.getController().setZoom(10.0);

        searchView = findViewById(R.id.reSearch);
        addEvents();

        btnchangeadd = findViewById(R.id.setaddr);
        btnchangeadd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PutAdresseLivraisonRequest putAdresseLivraisonRequest = new PutAdresseLivraisonRequest();
                putAdresseLivraisonRequest.setCountry(address.getCountryName());
                putAdresseLivraisonRequest.setCity(address.getLocality());
                putAdresseLivraisonRequest.setAddress(address.getAddressLine(0));
                putAdresseLivraisonRequest.setWilaya(address.getAdminArea());

                putAdresseLivraisonRequest.setLat(
                    String.valueOf(address.getLatitude())
                );

                putAdresseLivraisonRequest.setLng(
                    String.valueOf(address.getLongitude())
                );


                PutLocationClient(putAdresseLivraisonRequest);

            }
        });

        showInitialMarkerFromIntent();
    }

    private void addEvents() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchView.getQuery().toString();
                ArrayList<Address> list_address = null;
                if (!location.isEmpty()) {
                    Geocoder geocoder = new Geocoder(DeliveryAddressActivity.this);
                    try {
                        list_address = (ArrayList<Address>) geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                    if (list_address == null || list_address.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Address not found", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    address = list_address.get(0);

                    GeoPoint point = new GeoPoint(address.getLatitude(), address.getLongitude());
                    addMarker(point, location);
                    map.getController().animateTo(point);
                    map.getController().setZoom(10.0);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private void showInitialMarkerFromIntent() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            String latitude = extras.getString("latitude");
            String longitude = extras.getString("longitude");

            GeoPoint point = new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
            addMarker(point, "Bien Hoa");
            map.getController().setCenter(point);
            map.getController().setZoom(10.0);
        }
    }

    private void addMarker(GeoPoint point, String title) {
        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setTitle(title);
        map.getOverlays().add(marker);
        map.invalidate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (map != null) {
            map.onDetach();
        }
    }

    private void PutLocationClient(PutAdresseLivraisonRequest putAdresseLivraisonRequest) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("accessToken", null);
        Call<PutAdresseLivraisonResponse> putAdresseLivraisonResponseCall = ApiClient.getService().putadresseinitial("Bearer " + token, putAdresseLivraisonRequest);
        putAdresseLivraisonResponseCall.enqueue(new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()) {

                    DeliveryAddressActivity.this.finish();

                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        });

    }

    private void SetAdresse(PutAdresseLivraisonRequest putAdresseLivraisonRequest) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        int idclient = sharedPreferences.getInt("idclient", 0);

        Call<PutAdresseLivraisonResponse> putAdresseLivraisonRequestCall = ApiClient.getService().CreeModifierAdresseLivraison(idclient, putAdresseLivraisonRequest);
        putAdresseLivraisonRequestCall.enqueue(new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()) {
                    DeliveryAddressActivity.this.finish();
                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        });

    }
}

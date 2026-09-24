package com.example.QuickDrop.ui.livreur.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.livreur.adapters.DeliveryStopAdapter;
import com.example.QuickDrop.ui.livreur.adapters.OrderGroupAdapter;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.map.RoutingClient;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.DeliveryStop;
import com.example.QuickDrop.core.model.RoutingNode;
import com.example.QuickDrop.core.model.OrderLineItem;
import com.example.QuickDrop.core.model.RoutingSolution;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonRequest;
import com.example.QuickDrop.core.network.dto.PutLivraisonStatus;
import com.example.QuickDrop.core.util.ImageColorChanger;
import com.example.QuickDrop.core.map.OsmMapConfig;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Delivery person's map screen: shows the optimized route (pickup + delivery stops) and
 * the delivery person's own live position.
 *
 * Uses osmdroid (OpenStreetMap) instead of the Google Maps SDK, OSRM instead of the
 * Google Directions API, and Android's built-in {@link LocationManager} instead of the
 * Google Play Services Fused Location Provider -- all free, open-source, and requiring
 * no API key or Google Play Services dependency.
 */
public class LivreurRouteMapFragment extends Fragment implements RecyclerViewClickListener {
    private static final String TAG = "LivreurRouteMapFragment";
    private MapView map;
    private Geocoder geocoder;
    private int ACCESS_LOCATION_REQUEST_CODE = 10001;
    private LocationManager locationManager;

    Marker userLocationMarker;
    Polygon userLocationAccuracyCircle;
    private Intent i;

    private List<List<OrderLineItem>> orderItems;
    private List<OrderLineItem> orderItem;

    private List<DeliveryStop> locationItem;
    private OrderGroupAdapter adapter;

    private DeliveryStopAdapter locationAdapter;

    private String id_order, username, accessToken;
    private int id_client, idlivreur;
    private int id_fournisseur;

    private List<RoutingNode> locations;
    private TextView totalPrice;
    List<String> prices;

    private RecyclerView recyclerView;
    private List<GeoPoint> waypoints;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.activity_map, container, false);

        map = rootView.findViewById(R.id.my_Map);
        map.setMultiTouchControls(true);
        map.setTileSource(OsmMapConfig.OPEN_TOPO_MAP);   // <-- this line
        map.getController().setZoom(11.0);
        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                onMapLongClick(p);
                return true;
            }
        }));

        geocoder = new Geocoder(getContext());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        username = sharedPreferences.getString("username", "");
        idlivreur = sharedPreferences.getInt("idlivreur", 0);

        Button showOverviewDialog = rootView.findViewById(R.id.overviewBtn);
        orderItems = new ArrayList<List<OrderLineItem>>();

        locationItem = new ArrayList<DeliveryStop>();

        showOverviewDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Call method to show dialog
                showPickUpDialog();
            }
        });

        loadRoutingSolution();

        return rootView;
    }

    private void getPanierDetails(int panier, int position) {
        Call<GetPanierResponse> getArticleResponseCall = ApiClient.getService().getPanierDetails(panier);
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse = response.body();
                if (getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison = getPanierResponse.getLigneLivraison();
                    prices.set(position, String.valueOf(getPanierResponse.getMontant()));
                    for (DeliveryOrderLine orderItem : ligneLivraison) {
                        int articleID = orderItem.getArticle();
                        int qte = orderItem.getQuantity();
                        getArticle(articleID, qte, position, panier);
                    }

                } else {
                    String message = "not succesfull";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

    public void getArticle(int id, int qte, int position, int panier) {
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(id);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse = response.body();
                if (response.isSuccessful()) {
                    String name = getArticleResponse.getName();
                    String price = getArticleResponse.getPrice();
                    double price1 = Double.parseDouble(price);
                    double totalPrice = price1 * (qte);
                    orderItems.get(position).add(new OrderLineItem(name, Integer.toString(qte), price + " DZD", panier));

                    adapter.notifyDataSetChanged();

                    String message = "name  " + name + " price " + price;
                } else {
                    String message = "Error";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message = "Failure";
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void recyclerViewListClicked(View vi, int position) {

        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.order_dialog);
        recyclerView = dialog.findViewById(R.id.themotherrecyclerview);
        Button pickupButton = dialog.findViewById(R.id.dialog_button_pick);

        recyclerView.setHasFixedSize(true);

        TextView titleTextView = dialog.findViewById(R.id.dialog_title);
        titleTextView.setText("You arrived to" + locationItem.get(position).getTitle());

        List<Integer> ids = new ArrayList<>();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        List<String> titles = new ArrayList<>();
        prices = new ArrayList<>();

        // Initialize and set adapter for RecyclerView
        adapter = new OrderGroupAdapter(getActivity(), orderItems, titles, prices);
        recyclerView.setAdapter(adapter);
        orderItems.clear();

        int pos = 0;
        ids = locationItem.get(position).getId_order();
        for (int id : ids) {
            orderItems.add(new ArrayList<>());
            titles.add(String.valueOf(id));
            prices.add(" ");
            getPanierDetails(id, pos);

            pos++;

        }
        if (Objects.equals(locationItem.get(position).getType(), "client")) {
            pickupButton.setText("Delivered");
        }

        // Set click listener for close button
        pickupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the dialog when close button is clicked
                dialog.dismiss();
                pickupButton.setBackgroundResource(R.drawable.green_actif_status);
                if (Objects.equals(locationItem.get(position).getType(), "client")) {
                    PutLivraisonStatus status = new PutLivraisonStatus();
                    status.setStatus("livre");
                    status.updateOrderDetails(locationItem.get(position).getId_order().get(0), status);
                }

                ImageColorChanger.changeImageColor(vi.findViewById(R.id.location_icon), getResources().getColor(R.color.green));

            }
        });
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
        // Show the dialog
        dialog.show();

    }

    private DeliveryStop findExistingStop(String title, String type) {
        for (DeliveryStop stop : locationItem) {
            if (Objects.equals(stop.getTitle(), title) && Objects.equals(stop.getType(), type)) {
                return stop;
            }
        }
        return null;
    }

    private void loadRoutingSolution() {
        if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            enableUserLocation();
//            zoomToUserLocation();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
            }

        }

        Call<RoutingSolution> getPerfectRoadResponse = ApiClient.getService().GetDelivrerRoad(idlivreur);
        getPerfectRoadResponse.enqueue(new Callback<RoutingSolution>() {

            @Override
            public void onResponse(Call<RoutingSolution> call, Response<RoutingSolution> response) {
                if (response.isSuccessful()) {
                    RoutingSolution theRoad = response.body();
                    if (theRoad != null) {
                        locations = new ArrayList<RoutingNode>();
                        waypoints = new ArrayList<GeoPoint>();
                        locations = theRoad.getNodes();
                        // If locations are available, update the map
                        for (RoutingNode location : locations) {
                            // Add a marker for each location
                            String locationString = location.getCord();
                            id_order = String.valueOf(location.getId_ord());
                            String[] parts = locationString.split(",");
                            // Extract latitude and longitude strings
                            String latitudeString = parts[0].trim(); // Trim to remove leading/trailing whitespace
                            String longitudeString = parts[1].trim();

                            // Parse latitude and longitude strings as double values
                            double latitude = Double.parseDouble(latitudeString);
                            double longitude = Double.parseDouble(longitudeString);

                            try {

                                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                // Get the address list from the given latitude and longitude
                                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                                if (addresses != null && addresses.size() > 0) {
                                    Address address = addresses.get(0);

                                    // Here you can get various information about the location
                                    String locationName = address.getAddressLine(0); // Get the full address
                                    String cityName = address.getLocality(); // Get the city
                                    String stateName = address.getAdminArea(); // Get the state
                                    String countryName = address.getCountryName(); // Get the country
                                    String postalCode = address.getPostalCode(); // Get the postal code
                                    if (Objects.equals(location.getType(), "fournisseur")) {
                                        // BUG CORRIGÉ : on ne comparait qu'au dernier arrêt ajouté (locationItem.size()-1),
                                        // donc deux commandes du même fournisseur/client non consécutives dans la
                                        // liste créaient deux marqueurs en double au lieu d'être regroupées.
                                        // On recherche maintenant un arrêt existant correspondant dans toute la liste.
                                        DeliveryStop existing = findExistingStop(locationName + " " + cityName, location.getType());
                                        if (existing != null) {
                                            existing.setText(existing.getText() + ", #" + id_order);
                                            existing.getId_order().add(Integer.valueOf(id_order));
                                        } else {
                                            List<Integer> ids = new ArrayList<>();
                                            ids.add(Integer.valueOf(id_order));
                                            DeliveryStop location2 = new DeliveryStop(locationName + " " + cityName, "Pick up order #" + id_order, ids, location.getType());
                                            locationItem.add(location2);
                                        }

                                    } else if (Objects.equals(location.getType(), "client")) {
                                        DeliveryStop existing = findExistingStop(locationName + " " + cityName, location.getType());
                                        if (existing != null) {
                                            existing.setText(existing.getText() + ", #" + id_order);
                                            existing.getId_order().add(Integer.valueOf(id_order));

                                        } else {
                                            List<Integer> ids = new ArrayList<>();
                                            ids.add(Integer.valueOf(id_order));
                                            DeliveryStop location2 = new DeliveryStop(locationName + " " + cityName, "Deliver order #" + id_order, ids, location.getType());
                                            locationItem.add(location2);
                                        }
                                    }

                                    // Use the location information as needed
                                    Log.d("Location", "Location Name: " + locationName);
                                    Log.d("Location", "City: " + cityName);
                                    Log.d("Location", "State: " + stateName);
                                    Log.d("Location", "Country: " + countryName);
                                    Log.d("Location", "Postal Code: " + postalCode);

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            // Create a GeoPoint object
                            if (map == null || !isAdded()) {
                                Log.w(TAG, "Cannot add marker: map is null or Fragment is detached");
                                return;
                            }

                            GeoPoint locationPoint = new GeoPoint(latitude, longitude);
                            waypoints.add(locationPoint);

                            Marker stopMarker = new Marker(map);
                            stopMarker.setPosition(locationPoint);
                            stopMarker.setTitle(location.getType());

                            map.getOverlays().add(stopMarker);

                            locationAdapter =
                                    new DeliveryStopAdapter(locationItem, LivreurRouteMapFragment.this);
                        }
                        if (waypoints.isEmpty()) {
                            Log.w(TAG, "No waypoints returned by backend");
                            Toast.makeText(
                                    getContext(),
                                    "No delivery locations available",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        GeoPoint cameraLocation = waypoints.get(0);

                        map.getController().setZoom(11.0);
                        map.getController().setCenter(cameraLocation);
                        map.invalidate();

                        if (waypoints.size() >= 2) {
                            fetchAndDrawRoute(waypoints);
                        } else {
                            Log.w(TAG, "Only one waypoint: routing requires at least two");
                        }

                    } else {
                        String message = "No routing solution available!";
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String message = "Failed to fetch routing solution: " + response.message();
                    //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RoutingSolution> call, Throwable t) {
                // Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void fetchAndDrawRoute(List<GeoPoint> waypoints) {

        if (waypoints == null || waypoints.size() < 2) {
            Log.w(TAG, "Not enough waypoints for routing");
            return;
        }

        RoutingClient.fetchRoute(
                waypoints,
                new RoutingClient.RouteCallback() {

                    @Override
                    public void onSuccess(RoutingClient.RouteResult result) {

                        if (!isAdded() || getView() == null || map == null) {
                            Log.w(TAG, "Map unavailable when route returned");
                            return;
                        }

                        Polyline routeLine = new Polyline();

                        routeLine.setPoints(result.points);

                        routeLine.getOutlinePaint()
                                .setColor(Color.rgb(21, 4, 74));

                        routeLine.getOutlinePaint()
                                .setStrokeWidth(10f);

                        map.getOverlays().add(routeLine);

                        map.invalidate();

                        Log.d(
                                TAG,
                                "Route drawn: " +
                                        result.distanceMeters +
                                        " meters, " +
                                        result.durationSeconds +
                                        " seconds"
                        );
                    }

                    @Override
                    public void onFailure(Exception error) {

                        Log.e(
                                TAG,
                                "OSRM routing failed",
                                error
                        );

                        if (isAdded() && getActivity() != null) {

                            Toast.makeText(
                                    getActivity(),
                                    "Failed to fetch directions",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }
                }
        );
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            if (map != null) {
                setUserLocationMarker(location);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Intentionally empty. Do NOT call LocationListener.super.onStatusChanged(...)
            // here: onStatusChanged/onProviderEnabled/onProviderDisabled only became
            // default (no-op) interface methods starting at API 29 (Android Q). On a
            // device/emulator running an older platform version, the actual
            // LocationListener interface has no default body for them -- it's still
            // plain abstract there -- so invoking "super" throws AbstractMethodError at
            // runtime even though it compiles cleanly against a newer SDK. Leaving these
            // overrides empty (required to implement, but doing nothing) is safe on
            // every API level.
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            // Intentionally empty -- see note in onStatusChanged above.
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            // Intentionally empty -- see note in onStatusChanged above.
        }
    };

    private void setUserLocationMarker(Location location) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        int id_livreur = sharedPreferences.getInt("idlivreur", 0);
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                PutAdresseLivraisonRequest putAdresseLivraisonRequest = new PutAdresseLivraisonRequest();
                putAdresseLivraisonRequest.setCity(addresses.get(0).getLocality());
                putAdresseLivraisonRequest.setAddress(addresses.get(0).getAddressLine(0));
                putAdresseLivraisonRequest.setWilaya(addresses.get(0).getAdminArea());

                Call<PutAdresseLivraisonRequest> putAdresseLivraisonResponseCall = ApiClient.getService().PutAddressLivreur(id_livreur, putAdresseLivraisonRequest);
                putAdresseLivraisonResponseCall.enqueue(new Callback<PutAdresseLivraisonRequest>() {
                    @Override
                    public void onResponse(Call<PutAdresseLivraisonRequest> call, Response<PutAdresseLivraisonRequest> response) {
                        if (response.isSuccessful()) {

                        }
                    }

                    @Override
                    public void onFailure(Call<PutAdresseLivraisonRequest> call, Throwable t) {

                    }
                });

            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Geocoder", "Error while getting address from latitude and longitude");
        }

        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

        if (userLocationMarker == null) {
            userLocationMarker = new Marker(map);
            userLocationMarker.setPosition(point);
            userLocationMarker.setIcon(androidx.core.content.res.ResourcesCompat.getDrawable(getResources(), R.drawable.ic_livreur_scooter, null));
            userLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER); // <-- important, voir note
            userLocationMarker.setRotation(location.getBearing());
            map.getOverlays().add(userLocationMarker);
            map.getController().setZoom(12.0);
            map.getController().animateTo(point);
        } else {
            userLocationMarker.setPosition(point);
            userLocationMarker.setRotation(location.getBearing());
            map.getController().setZoom(12.0);
            map.getController().animateTo(point);
        }

        if (userLocationAccuracyCircle != null) {
            map.getOverlays().remove(userLocationAccuracyCircle);
        }
        userLocationAccuracyCircle = new Polygon();
        userLocationAccuracyCircle.setPoints(Polygon.pointsAsCircle(point, location.getAccuracy()));
        userLocationAccuracyCircle.getOutlinePaint().setColor(Color.argb(255, 255, 0, 0));
        userLocationAccuracyCircle.getOutlinePaint().setStrokeWidth(4);
        userLocationAccuracyCircle.getFillPaint().setColor(Color.argb(32, 255, 0, 0));
        map.getOverlays().add(userLocationAccuracyCircle);

        map.invalidate();
    }

    @SuppressWarnings("MissingPermission")
    private void startLocationUpdates() {

        if (getContext() == null || locationManager == null) {
            Log.e(TAG, "Context or LocationManager is null");
            return;
        }

        if (ContextCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_LOCATION_REQUEST_CODE
            );

            return;
        }

        boolean gpsEnabled =
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        boolean networkEnabled =
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d(TAG, "GPS enabled = " + gpsEnabled);
        Log.d(TAG, "Network enabled = " + networkEnabled);

        if (gpsEnabled) {

            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    5,
                    locationListener,
                    Looper.getMainLooper()
            );

            Log.d(TAG, "GPS location updates started");

        } else if (networkEnabled) {

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    5,
                    locationListener,
                    Looper.getMainLooper()
            );

            Log.d(TAG, "Network location updates started");

        } else {

            Log.e(TAG, "No location provider enabled");

            Toast.makeText(
                    getContext(),
                    "Please enable GPS/location",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void stopLocationUpdates() {
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Unable to stop location updates", e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            // you need to request permissions...
            ActivityCompat.requestPermissions(
                getActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                ACCESS_LOCATION_REQUEST_CODE
            );
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) map.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            map.onDetach();
        }
    }

    @SuppressWarnings("MissingPermission")
    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }
    }

    @SuppressWarnings("MissingPermission")
    private void zoomToUserLocation() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last == null) {
                last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (last != null) {
                GeoPoint point = new GeoPoint(last.getLatitude(), last.getLongitude());
                map.getController().setZoom(12.0);
                map.getController().animateTo(point);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }
    }

    public void onMapLongClick(GeoPoint point) {
        Log.d(TAG, "onMapLongClick: " + point.toString());
        try {
            List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String streetAddress = address.getAddressLine(0);
                Marker marker = new Marker(map);
                marker.setPosition(point);
                marker.setTitle(streetAddress);
                marker.setDraggable(true);
                marker.setOnMarkerDragListener(markerDragListener);
                map.getOverlays().add(marker);
                map.invalidate();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final Marker.OnMarkerDragListener markerDragListener = new Marker.OnMarkerDragListener() {
        @Override
        public void onMarkerDragStart(Marker marker) {
            Log.d(TAG, "onMarkerDragStart: ");
        }

        @Override
        public void onMarkerDrag(Marker marker) {
            Log.d(TAG, "onMarkerDrag: ");
        }

        @Override
        public void onMarkerDragEnd(Marker marker) {
            Log.d(TAG, "onMarkerDragEnd: ");
            GeoPoint point = marker.getPosition();
            try {
                List<Address> addresses = geocoder.getFromLocation(point.getLatitude(), point.getLongitude(), 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String streetAddress = address.getAddressLine(0);
                    marker.setTitle(streetAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation();
                zoomToUserLocation();
                startLocationUpdates();
            } else {
                //We can show a dialog that permission is not granted...
            }
        }
    }

    private void showPickUpDialog() {
        // BUG CORRIGÉ : locationAdapter n'est créé qu'à la fin du chargement de la tournée
        // (loadRoutingSolution). Si l'utilisateur ouvrait "Overview" avant la fin de l'appel
        // réseau, locationAdapter valait encore null -> plantage / rien ne s'affichait.
        if (locationAdapter == null) {
            Toast.makeText(getActivity(), "Chargement de la tournée en cours...", Toast.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.destination_dialog);
        recyclerView = dialog.findViewById(R.id.recyclerview_location);

        // Set the adapter to the RecyclerView
        recyclerView.setAdapter(locationAdapter);

        // Optionally, you can set layout manager and other properties for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
        dialog.show();
    }
}
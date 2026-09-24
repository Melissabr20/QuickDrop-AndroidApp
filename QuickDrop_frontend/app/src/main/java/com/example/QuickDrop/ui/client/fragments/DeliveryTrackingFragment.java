package com.example.QuickDrop.ui.client.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.client.adapters.DeliveryTrackingAdapter;
import com.example.QuickDrop.core.map.RoutingClient;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import com.example.QuickDrop.core.map.OsmMapConfig;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Client-facing "where's my delivery" screen: shows the client's location and, once a
 * delivery is under way, the delivery person's live position with the route between
 * them.
 *
 * Uses osmdroid (OpenStreetMap) instead of the Google Maps SDK and OSRM instead of the
 * Google Directions API -- both free, open-source and require no API key.
 */
public class DeliveryTrackingFragment extends Fragment {
    private static final String TAG = "DeliveryTrackingFragment";

    private MapView map;
    RecyclerView rcqlorders;
    Geocoder geocoder;
    private List<GeoPoint> waypoints;

    Marker userLocationMarker;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        rcqlorders = view.findViewById(R.id.recyclerorder);
        rcqlorders.setHasFixedSize(false);

        waypoints = new ArrayList<>();

        map = view.findViewById(R.id.map);
        map.setTileSource(OsmMapConfig.OPEN_TOPO_MAP);
        map.setMultiTouchControls(true);
        map.getController().setZoom(12.0);

        geocoder = new Geocoder(getContext());

        setUpClientMarker();

        return view;
    }

    private void setUpClientMarker() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        int clientId = sharedPreferences.getInt("idclient", 0);

        Call<PutAdresseLivraisonResponse> clientResponseCall = ApiClient.getService().GetAdresseLivraison(clientId);
        clientResponseCall.enqueue((new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()) {
                    PutAdresseLivraisonResponse clientResponse = response.body();
                    GeoPoint client = new GeoPoint(clientResponse.getLat(), clientResponse.getLng());

                    Marker clientMarker = new Marker(map);
                    clientMarker.setPosition(client);
                    clientMarker.setTitle("you");
                    map.getOverlays().add(clientMarker);

                    waypoints.add(client);
                    getfinal();

                    map.getController().setZoom(12.0);
                    map.getController().setCenter(client);
                    map.invalidate();
                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        }));
    }

    public void addMarkerToMap(Location location) {
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());

        Marker marker = new Marker(map);
        marker.setPosition(point);
        marker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.maptracking, null));
        marker.setRotation(location.getBearing());
        marker.setAnchor(0.5f, 0.5f);
        map.getOverlays().add(marker);
        userLocationMarker = marker;

        map.getController().setZoom(12.0);
        map.getController().animateTo(point);
        map.invalidate();
    }

    private void getfinal() {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("accessToken", null);
        Call<List<GetPanierResponse>> getpanierCall = ApiClient.getService().getfinal("Bearer " + token);
        getpanierCall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                List<GetPanierResponse> getlivraisonvalideResponses = response.body();
                if (getlivraisonvalideResponses != null) {

                    setAdapter(getlivraisonvalideResponses);

                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {

            }
        });

    }

    private Marker livreurMarker;

    private void setAdapter(List<GetPanierResponse> getlivraisonvalideResponse) {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 1);
        rcqlorders.setLayoutManager(layoutManager);
        DeliveryTrackingAdapter cardViewAdapter = new DeliveryTrackingAdapter(requireContext(), getlivraisonvalideResponse, new DeliveryTrackingAdapter.OnLocationListener() {
            @Override
            public void onLocationSelected(Location location, GetPanierResponse livraison) {
                if (livraison.getStatus().equals("en_cours")) {

                    updateLivreurPositionPeriodically(livraison.getLivreur());

                } else if (livraison.getStatus().equals("en_attente")) {
                    addMarkerToMap(location);
                }
            }
        });
        rcqlorders.setAdapter(cardViewAdapter);
    }

    private void updateLivreurPositionPeriodically(int livreurId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {

                        Call<LoginResponse.Livreur> deliveryAddressCall = ApiClient.getService().GetLivreur(livreurId);
                        Response<LoginResponse.Livreur> response = deliveryAddressCall.execute();
                        if (response.isSuccessful()) {
                            LoginResponse.Livreur deliveryAddress = response.body();
                            double livreurLat = deliveryAddress.getLat();
                            double livreurLng = deliveryAddress.getLng();
                            Location livreurLocation = new Location("");
                            livreurLocation.setLatitude(livreurLat);
                            livreurLocation.setLongitude(livreurLng);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateLivreurMarker(livreurLocation);

                                }
                            });
                        }

                        Thread.sleep(50000);
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void updateLivreurMarker(Location livreurLocation) {
        GeoPoint newLivreurPoint = new GeoPoint(livreurLocation.getLatitude(), livreurLocation.getLongitude());

        if (livreurMarker == null) {
            livreurMarker = new Marker(map);
            livreurMarker.setPosition(newLivreurPoint);
            livreurMarker.setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.maptracking, null));
            map.getOverlays().add(livreurMarker);

            waypoints.add(newLivreurPoint);
            fetchAndDrawRoute(waypoints);
        } else {
            livreurMarker.setPosition(newLivreurPoint);
        }
        map.invalidate();
    }

    private Polyline routeLine;

    private void fetchAndDrawRoute(List<GeoPoint> waypoints) {
        RoutingClient.fetchRoute(waypoints, new RoutingClient.RouteCallback() {
            @Override
            public void onSuccess(RoutingClient.RouteResult result) {
                if (routeLine != null) {
                    map.getOverlays().remove(routeLine);
                }
                routeLine = new Polyline();
                routeLine.setPoints(result.points);
                routeLine.getOutlinePaint().setColor(Color.rgb(21, 4, 74));
                routeLine.getOutlinePaint().setStrokeWidth(10f);
                map.getOverlays().add(routeLine);
                map.invalidate();
            }

            @Override
            public void onFailure(Exception error) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), "Failed to fetch directions", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
}

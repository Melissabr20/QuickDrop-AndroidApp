package com.example.QuickDrop.ui.livreur;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.adapters.OrderItemAdapter;
import com.example.QuickDrop.core.map.RoutingClient;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.OrderLineItem;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.map.OsmMapConfig;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
/**
 * Shows the delivery person's current stop on a map, with the driving route drawn to it.
 *
 * Uses osmdroid (OpenStreetMap) instead of the Google Maps SDK, and OSRM instead of the
 * Google Directions API -- both free and open-source, no API key required.
 */
public class LiveTrackingMapActivity extends AppCompatActivity {

    private MapView map;
    private Intent i;

    private List<OrderLineItem> orderItems;
    private OrderItemAdapter adapter;

    private String id_order;
    private int id_client;
    private int id_fournisseur;
    private TextView totalPrice;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        
        map = findViewById(R.id.my_Map);
        map.setTileSource(OsmMapConfig.OPEN_TOPO_MAP);
        map.setMultiTouchControls(true);
        map.getController().setZoom(13.0);

        GeoPoint pickupLocation = new GeoPoint(36.7538, 3.0588);
        map.getController().setCenter(pickupLocation);

        Marker marker = new Marker(map);
        marker.setPosition(pickupLocation);
        marker.setTitle("Point de récupération");
        map.getOverlays().add(marker);

        fetchAndDrawRoute();
    }

    private void fetchAndDrawRoute() {
        GeoPoint origin = new GeoPoint(36.7372, 3.0823); // Default origin
        GeoPoint destination = new GeoPoint(36.4833, 2.8167); // Default destination

        RoutingClient.fetchRoute(java.util.Arrays.asList(origin, destination), new RoutingClient.RouteCallback() {
            @Override
            public void onSuccess(RoutingClient.RouteResult result) {
                Polyline routeLine = new Polyline();
                routeLine.setPoints(result.points);
                routeLine.getOutlinePaint().setColor(Color.RED);
                routeLine.getOutlinePaint().setStrokeWidth(10f);
                map.getOverlays().add(routeLine);
                map.invalidate();
            }

            @Override
            public void onFailure(Exception error) {
                Log.e("ROUTING_ERROR", "Route failed", error);

                Toast.makeText(
                    LiveTrackingMapActivity.this,
                    "Routing error: " + error.getMessage(),
                    Toast.LENGTH_LONG
                ).show();
            }
        });
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

    private void getPanierDetails(int panier) {
        Call<GetPanierResponse> getArticleResponseCall = ApiClient.getService().getPanierDetails(panier);
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse = response.body();
                if (getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison = getPanierResponse.getLigneLivraison();
                    for (DeliveryOrderLine orderItem : ligneLivraison) {
                        int articleID = orderItem.getArticle();
                        int qte = orderItem.getQuantity();
                        getArticle(articleID, qte);
                    }
                    Double total = getPanierResponse.getMontant();

                } else {
                    String message = "Error fetching panier details: " + response.message();
                    Toast.makeText(LiveTrackingMapActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

    public void getArticle(int id, int qte) {
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(id);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse = response.body();
                if (response.isSuccessful()) {
                    String name = getArticleResponse.getName();
                    String price = getArticleResponse.getPrice();
                    double price1 = Double.parseDouble(price);
                    double totalPrice = price1 * (qte);
                    String message = "name  " + name + "price" + price;
                    adapter.notifyDataSetChanged();

                    orderItems.clear();
                } else {
                    String message = "Error fetching article details: " + response.message();
                    Toast.makeText(LiveTrackingMapActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message = "Error fetching article details: " + t.getMessage();
                Toast.makeText(LiveTrackingMapActivity.this, message, Toast.LENGTH_LONG).show();

            }
        });
    }

    private void showPickUpDialog() {
        // Create a dialog object
        final Dialog dialog = new Dialog(LiveTrackingMapActivity.this);

        // Set the custom layout for the dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.order_dialog);
        recyclerView= dialog.findViewById(R.id.recyclerview_order_dialog);

        // BUG CORRIGÉ : orderItems n'était jamais initialisé (toujours null) avant d'être
        // passé à l'adapter -> NullPointerException dès l'ouverture du dialogue, empêchant
        // l'affichage des détails de la livraison.
        orderItems = new ArrayList<>();
        adapter = new OrderItemAdapter(orderItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(LiveTrackingMapActivity.this));
        recyclerView.setAdapter(adapter);

        if(i!=null){
            String id_order=i.getStringExtra("id_order");
            String adr_four=i.getStringExtra("adr_four");

            TextView titleTextView=dialog.findViewById(R.id.dialog_title);
            titleTextView.setText("You arrived to"+adr_four);
            TextView id_ordTextView=dialog.findViewById(R.id.order);
            id_ordTextView.setText("#"+id_order);
            getPanierDetails(Integer.parseInt(id_order));
        }

        Button pickupButton = dialog.findViewById(R.id.dialog_button_pick);

        pickupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent i = new Intent(LiveTrackingMapActivity.this,LivreurHomeActivity.class);
                startActivity(i);
            }
        });
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
        dialog.show();
    }

    private void showDeliveredDialog() {
        final Dialog dialog = new Dialog(LiveTrackingMapActivity.this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delivred_dialog);

        if(i!=null){
            id_order=i.getStringExtra("id_order");
            id_client=i.getIntExtra("id_client",id_client);
            String liv_username=i.getStringExtra("liv_username");
            TextView liv_username_TxtView=dialog.findViewById(R.id.dialog_title);
            TextView id_ordTextView=dialog.findViewById(R.id.dialog_title2);
            id_ordTextView.setText("You just delivered the order #"+id_order);
            liv_username_TxtView.setText("WELL DONE "+liv_username+"!");
        }

        Button backButton = dialog.findViewById(R.id.dialogBackBtn);

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
        dialog.show();
    }

}
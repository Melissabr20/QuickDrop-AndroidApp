package com.example.QuickDrop.ui.client;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.adapters.OrderItemAdapter;
import com.example.QuickDrop.core.model.Distance;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.SelectedLivreur;
import com.example.QuickDrop.core.model.OrderLineItem;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.PutPanier;
import com.example.QuickDrop.core.network.dto.PutLivraisonStatus;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity  {

    /*private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetView;
    private TextView totalPrice;
    private OrderItemAdapter adapter;
    private List<OrderLineItem> orderItems;
    private RecyclerView recyclerView;

    private int initialHeight ; // Initial height of the bottom sheet

    private String id_order;
    private int id_client,id_fournisseur,id_livreur;
    private String adr_client;
    private String adr_four;
    private String liv_username;
    private float distance;

    private GoogleMap gMap;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order_details);

        SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.myMap);
        mapFragment.getMapAsync(this);

        initialHeight = getScreenHeight(getApplicationContext()) - 670;

        // Initialize the bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(this);

        // Inflate the layout for the bottom sheet
        bottomSheetView = getLayoutInflater().inflate(R.layout.resizable_bottom_sheet_layout, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        bottomSheetDialog.setContentView(bottomSheetView);

        // Get the BottomSheetBehavior from the parent view of the bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());

        // Set the initial peek height and maximum height of the bottom sheet

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        bottomSheetDialog.setCanceledOnTouchOutside (false);
        bottomSheetBehavior.setHideable(false);

        bottomSheetView.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private int originalHeight;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        startY = event.getRawY();
                        originalHeight = bottomSheetView.getHeight();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - startY;
                        int newHeight = (int) (originalHeight + deltaY);
                        if (newHeight > initialHeight) {
                            ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                            layoutParams.height = newHeight;
                            bottomSheetView.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return true;
            }

        });



        // Show the bottom sheet dialog
        bottomSheetDialog.show();


        Intent intent = getIntent();
        if (intent != null) {
            id_order = intent.getStringExtra("id_order"); // Replace "key" with the key you used in the sender activity
            adr_four =intent.getStringExtra("adr_four");
            adr_client =intent.getStringExtra("adr_client");
            String client_name = intent.getStringExtra("client_name");
            liv_username=intent.getStringExtra("liv_username");
            id_client=intent.getIntExtra("id_client",id_client);
            id_fournisseur=intent.getIntExtra("id_fournisseur",id_fournisseur);
            id_livreur = intent.getIntExtra("id_livreur",id_livreur);

            // Use the retrieved data as needed
            TextView textView_idord=bottomSheetView.findViewById(R.id.order);
            TextView textView_adr_four=bottomSheetView.findViewById(R.id.adr_four);
            TextView textView_adr_client=bottomSheetView.findViewById(R.id.adr_client);
            TextView textView_client_name=bottomSheetView.findViewById(R.id.client_name);
            calcDistancesupplierClient(id_client,id_fournisseur,id_livreur);

            textView_idord.setText(id_order);
            textView_adr_four.setText(adr_four);
            textView_adr_client.setText(adr_client);
            textView_client_name.setText(client_name);
        }
        totalPrice=bottomSheetView.findViewById(R.id.totalPriceView);
        orderItems=new ArrayList<OrderLineItem>();

        recyclerView=bottomSheetView.findViewById(R.id.recyclerview_order);

        adapter = new OrderItemAdapter(orderItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(OrderDetailsActivity.this));
        recyclerView.setAdapter(adapter);

        getPanierDetails(Integer.parseInt(String.valueOf(id_order)));

        bottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);


        Button accButton = bottomSheetView.findViewById(R.id.acceptBtn);
        Button decButton = bottomSheetView.findViewById(R.id.DeclineBtn);
        // Set an OnClickListener to handle button clicks
        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(OrderDetailsActivity.this,LiveTrackingMapActivity.class);
                intent.putExtra("adr_client",adr_client);
                intent.putExtra("id_order",id_order);
                intent.putExtra("adr_four",adr_four);
                intent.putExtra("liv_username",liv_username);
                intent.putExtra("id_client",id_client);
                intent.putExtra("id_fournisseur",id_fournisseur);

                PutLivraisonStatus status =new PutLivraisonStatus();
                status.setId_client(id_client);
                status.setStatus("en_cours");
                status.updateOrderDetails(Integer.parseInt(id_order),status);
                startActivity(intent);
            }
        });

        decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Assign the delivry to another delivrer
                selectDelivrer(id_fournisseur);

            }
        });

    }


    public static int getScreenHeight(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    private void PutPanierDetails(PutPanier postPanierRequest,int panier) {
        Call<GetPanierResponse> putpanier = ApiClient.getService().PutPanierDetails(panier, postPanierRequest);
        putpanier.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                if (response.isSuccessful()) {
                    String message = "haha";
                    Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

    private void selectDelivrer(int id_fournisseur){
        Call<SelectedLivreur> livreurselectionneCall=ApiClient.getService().SelectedLivreur(id_fournisseur);
        livreurselectionneCall.enqueue(new Callback<SelectedLivreur>() {
            @Override
            public void onResponse(Call<SelectedLivreur> call, Response<SelectedLivreur> response) {
                if (response.isSuccessful()) {
                    SelectedLivreur livreurselectionne = response.body();
                    PutPanier postPanierRequest = new PutPanier();
                    postPanierRequest.setLivreur(livreurselectionne.getLivreur());
                    postPanierRequest.setFournisseur(id_fournisseur);
                    Intent intent= new Intent(OrderDetailsActivity.this,LivreurHomeActivity.class);
                    startActivity(intent);

                    PutPanierDetails(postPanierRequest, Integer.valueOf(id_order));
                }
            }

            @Override
            public void onFailure(Call<SelectedLivreur> call, Throwable t) {

            }
        });
    }

    private void getPanierDetails(int panier){
        Call<GetPanierResponse> getArticleResponseCall=ApiClient.getService().getPanierDetails(panier);
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse=response.body();
                if(getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison= getPanierResponse.getLigneLivraison();
                    for (DeliveryOrderLine orderItem : ligneLivraison) {
                        int articleID = orderItem.getArticle();
                        int qte = orderItem.getQuantity();
                        getArticle(articleID,qte);
                    }
                    Double total=getPanierResponse.getMontant();
                    totalPrice.setText(""+total +" DZD ");

                }else {
                    String message = "not succesfull";
                    Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }
    public void getArticle(int id,int qte){
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(id);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse= response.body();
                if(response.isSuccessful()){
                    String name=getArticleResponse.getName();
                    String price =getArticleResponse.getPrice();
                    double price1 = Double.parseDouble(price);
                    double totalPrice = price1 * (qte);
                    orderItems.add(new OrderLineItem(name,Integer.toString(qte),price+" DZD"));
                    String message ="name  "+ name+ "price" +price;
                    Toast.makeText(OrderDetailsActivity.this,message,Toast.LENGTH_LONG).show();
                    adapter.notifyDataSetChanged();
                }else{
                    String message ="Merde";
                    Toast.makeText(OrderDetailsActivity.this,message,Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message ="Not";
                Toast.makeText(OrderDetailsActivity.this,message,Toast.LENGTH_LONG).show();

            }
        });
    }

    private void calcDistancesupplierClient(int clientId, int supplierId,int id_livreur) {
        // Fetch client address
        Call<Distance> distanceResponseCall = ApiClient.getService().GetDistanceDetails(clientId,supplierId);
        distanceResponseCall.enqueue(new Callback<Distance>() {
            @Override
            public void onResponse(Call<Distance> call, Response<Distance> response) {
                if (response.isSuccessful()) {
                    Distance distanceResponse = response.body();
                    if (distanceResponse != null) {
                        distance = distanceResponse.getDistance();
                        calcDistancesupplierDelivrer(id_fournisseur,id_livreur);
                    }
                } else {
                    String message = "Error fetching distance: " + response.message();
                    Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Distance> call, Throwable t) {
                String message = "Error fetching distance: " + t.getMessage();
                Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    private void calcDistancesupplierDelivrer(int supplierId, int delivrerId) {
        // Fetch client address
        Call<Distance> distanceResponseCall = ApiClient.getService().GetDistanceDetails2(supplierId,delivrerId);
        distanceResponseCall.enqueue(new Callback<Distance>() {
            @Override
            public void onResponse(Call<Distance> call, Response<Distance> response) {
                if (response.isSuccessful()) {
                    Distance distanceResponse = response.body();
                    if (distanceResponse != null) {
                        distance = distanceResponse.getDistance()+distance;
                        TextView textView_distance=bottomSheetView.findViewById(R.id.distance_txt);

                        textView_distance.setText(distance+"Km");
                    }
                } else {
                    String message = "Error fetching distance: " + response.message();
                    Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Distance> call, Throwable t) {
                String message = "Error fetching distance: " + t.getMessage();
                Toast.makeText(OrderDetailsActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        gMap = googleMap;



        LatLng markerLocation = new LatLng(36.7538, 3.0588);
        gMap.addMarker(new MarkerOptions().position(markerLocation).title("Marker Title"));

        gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Toast.makeText(OrderDetailsActivity.this, "Map Clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        LatLng cameraLocation = new LatLng(36.7538, 3.0588);
        float zoomLevel = 12.0f;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(cameraLocation, zoomLevel);
        gMap.moveCamera(cameraUpdate);
    }

    public void onBackPressed() {
        // Handle fragment back navigation or any other actions here
        // For example, you can navigate back to the previous fragment or finish the activity
        // In this case, let's just finish the activity
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetDialog.setCanceledOnTouchOutside (false);
        bottomSheetBehavior.setHideable(false);
        finish();
    }*/


}
package com.example.QuickDrop.ui.livreur.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.livreur.adapters.LivreurTaskAdapter;
import com.example.QuickDrop.ui.common.adapters.OrderItemAdapter;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.CartArticleItem;
import com.example.QuickDrop.core.model.Distance;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.SelectedLivreur;
import com.example.QuickDrop.core.model.DeliveryTask;
import com.example.QuickDrop.core.model.OrderLineItem;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import com.example.QuickDrop.core.network.dto.PutPanier;
import com.example.QuickDrop.core.network.dto.PutLivraisonStatus;
import com.example.QuickDrop.ui.common.viewmodel.SharedViewModel;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.QuickDrop.databinding.ActivityFragmentBinding;
import com.example.QuickDrop.databinding.FragmentLivreurActivityBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivreurTasksFragment extends Fragment  implements RecyclerViewClickListener {

    public List<DeliveryTask> dataList = new ArrayList<>();
    private LivreurTaskAdapter adapter;
    public List<Integer> refusedLivreurIds = new ArrayList<>();

    private String username, accessToken;
    Handler handler;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetView;

    private TextView totalPrice;
    private OrderItemAdapter adapter2;
    private List<OrderLineItem> orderItems;
    private RecyclerView recyclerView;

    Set<Integer> idSet;
    private float distance;
    ArrayList<String> list;
    private static SharedViewModel sharedViewModel;

    private int  id_client,id_fournisseur,idlivreur;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FragmentLivreurActivityBinding binding = FragmentLivreurActivityBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        username = sharedPreferences.getString("username", "");
        idlivreur = sharedPreferences.getInt("idlivreur", 0);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getIdSet().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> idSet) {
                // Update the UI with the new data
                list = new ArrayList<>(idSet);

            }
        });

        // Update UI with retrieved data
        TextView textView_user_name = view.findViewById(R.id.user_name);
        TextView textView_user_id = view.findViewById(R.id.liv_id);
        textView_user_name.setText("Bonjours, "+username);



        RecyclerView recyclerView = binding.recyclerview;
        adapter = new LivreurTaskAdapter(dataList, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);


        //getOrderDetails(accessToken);

        // Initialize the bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(getActivity());

        // Inflate the layout for the bottom sheet
        bottomSheetView = getLayoutInflater().inflate(R.layout.resizable_bottom_sheet_layout, null);

        bottomSheetDialog.setContentView(bottomSheetView);

        // Get the BottomSheetBehavior from the parent view of the bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());

        // Set the initial peek height and maximum height of the bottom sheet

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

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
                        if (newHeight > originalHeight) {
                            ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                            layoutParams.height = newHeight;
                            bottomSheetView.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return true;
            }

        });

        Switch onOffSwitch = view.findViewById(R.id.on_off_switch);

        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // isChecked is true if the switch is in the "on" position, false otherwise
                if (isChecked) {
                    // Do something when the switch is turned on
                    LoginResponse.Livreur livreur=new LoginResponse.Livreur();
                    livreur.setActif(true);
                    PutActifStatus(idlivreur,livreur);
                } else {
                    // Do something when the switch is turned off
                    LoginResponse.Livreur livreur=new LoginResponse.Livreur();
                    livreur.setActif(false);
                    PutActifStatus(idlivreur,livreur);
                }
            }
        });


        return view;
    }

    private void PutActifStatus(int id_livreur ,LoginResponse.Livreur livreur) {
        Call<LoginResponse.Livreur> putpanier = ApiClient.getService(). UpdateLivreurDetails(id_livreur, livreur);
        putpanier.enqueue(new Callback<LoginResponse.Livreur>() {
            @Override
            public void onResponse(Call<LoginResponse.Livreur> call, Response<LoginResponse.Livreur> response) {
                if (response.isSuccessful()) {
                    if(livreur.isActif()) {
                        String message = "you are in service";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }else{
                        String message = "you are out of service";
                        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Livreur> call, Throwable t) {

            }

        });
    }


    public void fetchClientAddress(int clientId, int supplierId, int id,int prix) {
        // BUG CORRIGÉ : id_fournisseur n'était jamais assigné dans ce fragment et restait
        // donc toujours à 0, y compris quand decButton l'envoyait à l'API via choixlivreur().
        this.id_client = clientId;
        this.id_fournisseur = supplierId;
        // Fetch client address
        Call<PutAdresseLivraisonResponse> clientResponseCall = ApiClient.getService().GetAdresseLivraison(clientId);
        clientResponseCall.enqueue(new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()) {
                    PutAdresseLivraisonResponse clientResponse = response.body();
                    if (clientResponse != null) {
                        String city = clientResponse.getCity();
                        String country = clientResponse.getCountry();
                        String address = clientResponse.getAdress();
                        String clientAddress = city + "-" + country;
                        Log.d("fetchClientAddress", "Client address fetched: " + clientAddress);
                        getClientDetails(clientId, clientAddress, supplierId, id,prix);
                    } else {
                        Log.e("fetchClientAddress", "Client response body is null");
                    }
                } else {
                    Log.e("fetchClientAddress", "Error fetching client address: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {
                Log.e("fetchClientAddress", "Error fetching client address: " + t.getMessage());
            }
        });
    }


    private void fetchClientAddressSupplier(String clientAddress, int supplierId, int id, String clientname,int prix ) {
        // Fetch supplier details
        Call<LoginResponse.Fournisseur> supplierResponseCall = ApiClient.getService().GetFournisseur(supplierId);
        supplierResponseCall.enqueue(new Callback<LoginResponse.Fournisseur>() {
            @Override
            public void onResponse(Call<LoginResponse.Fournisseur> call, Response<LoginResponse.Fournisseur> response) {
                if (response.isSuccessful()) {
                    LoginResponse.Fournisseur fournisseur = response.body();
                    if (fournisseur != null) {
                        String city = fournisseur.getCity();
                        String country = fournisseur.getWilaya();
                        String address = fournisseur.getAdress();

                        String supplierAddress = city + "-" + country;
                        DeliveryTask model = new DeliveryTask();
                        model.setId_ord(Integer.toString(id));
                        model.setAdr_client(clientAddress);
                        model.setAdr_four(supplierAddress);
                        model.setName(clientname);
                        model.setPrix_livraison(prix);
                        int newPosition = dataList.size();
                        dataList.add(model);
                        adapter.notifyItemInserted(newPosition);
                    } else {
                        Log.e("fetchClientAddressSupplier", "Fournisseur response body is null");
                    }
                } else {
                    Log.e("fetchClientAddressSupplier", "Error fetching fournisseur details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {
                Log.e("fetchClientAddressSupplier", "Error fetching fournisseur details: " + t.getMessage());
            }
        });
    }


    private void getClientDetails(int id_client, String clientAddress, int supplierId, int id,int prix) {

        Call<LoginResponse.Client> getClientResponseCall = ApiClient.getService().GetClient(id_client);
        getClientResponseCall.enqueue(new Callback<LoginResponse.Client>() {
            @Override
            public void onResponse(Call<LoginResponse.Client> call, Response<LoginResponse.Client> response) {
                if (response.isSuccessful()) {
                    LoginResponse.Client getClientResponse = response.body();
                    if (getClientResponse != null) {
                        int id_client = getClientResponse.getId();

                        String clientname = getClientResponse.getUser().getUsername();
                        fetchClientAddressSupplier(clientAddress, supplierId, id, clientname,prix);

                    } else {
                        Log.e("getClientDetails", "Client response body is null");
                    }
                } else {
                    Log.e("getClientDetails", "Error fetching client details: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Client> call, Throwable t) {
                Log.e("getClientDetails", "Error fetching client details: " + t.getMessage());
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
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Distance> call, Throwable t) {
                String message = "Error fetching distance: " + t.getMessage();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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

                    }
                } else {
                    String message = "Error fetching distance: " + response.message();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Distance> call, Throwable t) {
                String message = "Error fetching distance: " + t.getMessage();
                Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
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
                    String message = "Error fetching panier details: " + response.message();
                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
                    orderItems.add(new OrderLineItem(name,Integer.toString(qte),price+" DZD",0));
                    adapter2.notifyDataSetChanged();
                }else{
                    String message ="Error fetching article details: " + response.message();
                    Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message ="Error fetching article details: " + t.getMessage();
                Toast.makeText(getActivity(),message,Toast.LENGTH_LONG).show();

            }
        });
    }

    private void PutPanierDetails(PutPanier postPanierRequest,int panier) {
        Call<GetPanierResponse> putpanier = ApiClient.getService().PutPanierDetails(panier, postPanierRequest);
        putpanier.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                if (response.isSuccessful()) {
                    String message = "Panier updated successfully";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }


    @Override
    public void recyclerViewListClicked(View v, int position) {
        bottomSheetDialog.show();

        TextView textView_idord=bottomSheetView.findViewById(R.id.order);
        TextView textView_adr_four=bottomSheetView.findViewById(R.id.adr_four);
        TextView textView_adr_client=bottomSheetView.findViewById(R.id.adr_client);
        TextView textView_client_name=bottomSheetView.findViewById(R.id.client_name);

        textView_idord.setText(dataList.get(position).getId_ord());
        textView_adr_four.setText(dataList.get(position).getAdr_four());
        textView_adr_client.setText( dataList.get(position).getAdr_client());
        textView_client_name.setText(dataList.get(position).getName());

        TextView prixlivraison=bottomSheetView.findViewById(R.id.livPriceView);
        prixlivraison.setText("Tarif livraison "+ dataList.get(position).getPrix_livraison());



        totalPrice=bottomSheetView.findViewById(R.id.totalPriceView);
        orderItems=new ArrayList<>();

        recyclerView=bottomSheetView.findViewById(R.id.recyclerview_order);

        adapter2 = new OrderItemAdapter(orderItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter2);

        getPanierDetails(Integer.parseInt(dataList.get(position).getId_ord()));

        Button accButton = bottomSheetView.findViewById(R.id.acceptBtn);
        Button decButton = bottomSheetView.findViewById(R.id.DeclineBtn);
        // Set an OnClickListener to handle button clicks
        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PutLivraisonStatus status = new PutLivraisonStatus();
                status.setStatus("en_attente");
                status.updateOrderDetails(Integer.parseInt(dataList.get(position).getId_ord()), status);
                Call<CartArticleItem> vider=ApiClient.getService().vider();
                vider.enqueue(new Callback<CartArticleItem>() {
                    @Override
                    public void onResponse(Call<CartArticleItem> call, Response<CartArticleItem> response) {
                        if (response.isSuccessful()){

                        }
                    }

                    @Override
                    public void onFailure(Call<CartArticleItem> call, Throwable t) {

                    }
                });


                dataList.remove(dataList.get(position));
                adapter.notifyItemRemoved(position);
                bottomSheetDialog.dismiss();

            }
        });

        decButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!refusedLivreurIds.contains(idlivreur)) {
                    // Si l'ID n'existe pas, l'ajouter à la liste
                    refusedLivreurIds.add(idlivreur);

                    // Construire la chaîne de caractères contenant tous les IDs des livreurs refusés
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < refusedLivreurIds.size(); i++) {
                        builder.append(refusedLivreurIds.get(i));
                        if (i < refusedLivreurIds.size() - 1) {
                            builder.append(",");
                        }
                    }
                    String listeStr = builder.toString();
                    choixlivreur(Integer.parseInt(dataList.get(position).getId_ord()), id_fournisseur, listeStr);

                    dataList.remove(dataList.get(position));
                    adapter.notifyItemRemoved(position);
                    bottomSheetDialog.dismiss();

                }

            }
        });

        //finish();
    }

    public void choixlivreur( int panier,int idfournisseur,String listeStr) {
        Call<SelectedLivreur> livreurselectionneCall = ApiClient.getService().SelectedLivreur(idfournisseur,listeStr,panier);
        livreurselectionneCall.enqueue(new Callback<SelectedLivreur>() {
            @Override
            public void onResponse(Call<SelectedLivreur> call, Response<SelectedLivreur> response) {
                if (response.isSuccessful()) {
                    SelectedLivreur livreurselectionne = response.body();
                    PutPanier postPanierRequest = new PutPanier();
                    postPanierRequest.setLivreur(livreurselectionne.getLivreur());
                    postPanierRequest.setFournisseur(idfournisseur);

                    PutPanierDetails(postPanierRequest, panier);



                }
            }

            @Override
            public void onFailure(Call<SelectedLivreur> call, Throwable t) {

            }
        });


    }

}
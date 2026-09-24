package com.example.QuickDrop.ui.common;

import com.example.QuickDrop.ui.client.OrderDetailsActivity;
import com.example.QuickDrop.ui.common.adapters.LivreurHistoryAdapter;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.DeliveryTask;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.QuickDrop.databinding.FragmentHistoryBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivreurHistoryFragment extends Fragment implements RecyclerViewClickListener{
    private List<DeliveryTask> dataList;
    private LivreurHistoryAdapter adapter;
    private String username;
    private String  accessToken;
    private int  id_client,id_fournisseur,idlivreur;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentHistoryBinding binding = FragmentHistoryBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        username = sharedPreferences.getString("username", "");
        idlivreur = sharedPreferences.getInt("idlivreur", 0);
        int type = sharedPreferences.getInt("fourOrliv", -1);


        RecyclerView recyclerView = binding.recyclerview2;
        dataList = new ArrayList<>();
        adapter = new LivreurHistoryAdapter(dataList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        // Retrieve data passed as arguments

        // Now you have the data, you can use it as needed
        if(type==0){
            getOrderDetails2(accessToken);
        }else {
            getOrderDetails(accessToken);
        }

        return view;
    }


    private void getOrderDetails(String accessToken) {
        String token = accessToken;
        Call<List<GetPanierResponse>> getOrderResponseCall = ApiClient.getService().GetOrderDetails("Bearer " + token);
        getOrderResponseCall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetPanierResponse> getOrderResponses = response.body();
                    if (getOrderResponses != null && !getOrderResponses.isEmpty()) {
                        for (GetPanierResponse getOrderResponse : getOrderResponses) {
                            int id_client = getOrderResponse.getClient();
                            int id_fournisseur = getOrderResponse.getFournisseur();
                            int id = getOrderResponse.getId();
                            // Fetch client address and supplier details
                            String status =getOrderResponse.getStatus();
                            if (status.equals("livre")) {
                                fetchClientAddress(id_client, id_fournisseur, id);
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {

            }
        });
    }
    private void getOrderDetails2(String accessToken) {
        String token = "Bearer " + accessToken;
        Call<List<GetPanierResponse>> getOrderResponseCall = ApiClient.getService().GetOrderDetailsbyfour(token);
        getOrderResponseCall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetPanierResponse> getOrderResponses = response.body();
                    if (getOrderResponses != null && !getOrderResponses.isEmpty()) {
                        for (GetPanierResponse getOrderResponse : getOrderResponses) {
                            id_client = getOrderResponse.getClient();
                            id_fournisseur = getOrderResponse.getFournisseur();
                            int id = getOrderResponse.getId();
                            String status = getOrderResponse.getStatus();
                            if ((status.equals("en_cours")) || (status.equals("livre"))) {
                                fetchClientAddress(id_client, id_fournisseur, id);
                            }


                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {

            }
        });
    }

    private void fetchClientAddress(int clientId, int supplierId, int id) {
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
                        getClientDetails(clientId, clientAddress, supplierId, id);
                    }
                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {
               }
        });
    }

    private void fetchClientAddressSupplier(String clientAddress, int supplierId, int id, String clientname) {
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
                        dataList.add(model);
                        adapter.notifyDataSetChanged();

                        } else {
                      }
                } else {

                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {

            }
        });
    }


    private void getClientDetails(int id_client, String clientAddress, int supplierId, int id) {

        Call<LoginResponse.Client> getClientResponseCall = ApiClient.getService().GetClient(id_client);
        getClientResponseCall.enqueue(new Callback<LoginResponse.Client>() {
            @Override
            public void onResponse(Call<LoginResponse.Client> call, Response<LoginResponse.Client> response) {
                if (response.isSuccessful()) {
                    LoginResponse.Client getClientResponse = response.body();
                    if (getClientResponse != null) {
                        int id_client = getClientResponse.getId();

                        String clientname = getClientResponse.getUser().getUsername();
                        fetchClientAddressSupplier(clientAddress, supplierId, id, clientname);

                    } else {

                    }
                } else {

                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Client> call, Throwable t) {

            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Intent intent = new Intent(getActivity(), OrderDetailsActivity.class);
        intent.putExtra("id_order", dataList.get(position).getId_ord());
        intent.putExtra("adr_four", dataList.get(position).getAdr_four());
        intent.putExtra("adr_client", dataList.get(position).getAdr_client());
        intent.putExtra("client_name",dataList.get(position).getName());
        intent.putExtra("liv_username",username);
        startActivity(intent);
        //finish();
    }
}

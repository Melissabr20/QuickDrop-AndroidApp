package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.adapters.ClientAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientListActivity extends AppCompatActivity {

    RecyclerView clients;
    ClientAdapter clientAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list);
        clients=findViewById(R.id.Client_list);

        SearchView searchView = findViewById(R.id.search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                clientAdapter.filter(newText);
                return true;
            }
        });

        getClients();



    }
    public void getClients(){
        Call<List<LoginResponse.Client>> clientcall=ApiClient.getService().getClientvList();
        clientcall.enqueue(new Callback<List<LoginResponse.Client>>() {
            @Override
            public void onResponse(Call<List<LoginResponse.Client>> call, Response<List<LoginResponse.Client>> response) {
                List<LoginResponse.Client> clientList= response.body();
                setAdapter(clientList);

            }

            @Override
            public void onFailure(Call<List<LoginResponse.Client>> call, Throwable t) {

            }
        });


    }
    private void setAdapter(List<LoginResponse.Client> clientList) {
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        clients.setLayoutManager(layoutManager);
        clientAdapter= new ClientAdapter(this,clientList);
        clients.setAdapter(clientAdapter);

    }
}
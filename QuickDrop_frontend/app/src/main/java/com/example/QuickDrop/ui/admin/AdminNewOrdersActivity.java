package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.adapters.AdminOrderListAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminNewOrdersActivity extends AppCompatActivity {

    RecyclerView orders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_new_orders);
        orders=findViewById(R.id.orders_list);
        getPanier();
    }

    private void getPanier() {


        Call<List<GetPanierResponse>> listOrders = ApiClient.getService().getOrder();
        listOrders.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                List<GetPanierResponse> orders =response.body();
                if (orders != null) {
                    setAdapter(orders);
                } else {

                }

            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {

            }
        });
    }

    private void setAdapter(List<GetPanierResponse> livraison) {
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),1);
        orders.setLayoutManager(layoutManager);
        AdminOrderListAdapter listorderAdapter= new AdminOrderListAdapter(this, livraison);
        orders.setAdapter(listorderAdapter);
    }


}


package com.example.QuickDrop.core.network.dto;

import com.example.QuickDrop.core.network.ApiClient;
import android.util.Log;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PutLivraisonStatus {

    private String status;
    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void updateOrderDetails(int id_order, PutLivraisonStatus putlivraisonStatus) {
        Call<GetOrderResponse> call = ApiClient.getService().UpdateOrderDetails(id_order, putlivraisonStatus);
        call.enqueue(new Callback<GetOrderResponse>() {
            @Override
            public void onResponse(Call<GetOrderResponse> call, Response<GetOrderResponse> response) {
                if(response.isSuccessful()) {

                } else {
                    Log.d("Location", response.message());

                    // Handle the error here
                }
            }

            @Override
            public void onFailure(Call<GetOrderResponse> call, Throwable t) {
                // Handle the failure here
            }
        });
    }

}

package com.example.QuickDrop.ui.common.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderLineAdapter extends RecyclerView.Adapter<OrderLineAdapter.ViewHolder> {

    private List<DeliveryOrderLine> cartItemList;
    private Context context;
    RequestOptions option;

    public OrderLineAdapter(Context context, List<DeliveryOrderLine> cartItemList){
        this.context=context;
        this.cartItemList=cartItemList;
        option=new RequestOptions().centerCrop().error(R.drawable.img);
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ligneadmin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryOrderLine cartItem = cartItemList.get(position);
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(cartItem.getArticle());
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse= response.body();
                if(response.isSuccessful()){
                    String name=getArticleResponse.getName();
                    int id_article=getArticleResponse.getId();
                    String price =getArticleResponse.getPrice();
                    double price1 = Double.parseDouble(price);
                    holder.productQuantity.setText(String.valueOf(cartItem.getQuantity()));
                    double totalPrice = price1 * (cartItem.getQuantity());
                    holder.productPrice.setText("prix " +totalPrice +"DZD" );
                    holder.productName.setText(name);
                    fetchImage(id_article,holder.productImage);
                }else{
                    String message ="Merde";
                    Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message ="Not";
                Toast.makeText(context,message,Toast.LENGTH_LONG).show();

            }
        });


    }
    private void fetchImage(int imageId,ImageView imageView) {
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        Call<ImageResponse> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getImageUrl();
                    Glide.with(context).load(imageUrl).apply(option).into(imageView);
                    Log.d("Fetch", imageUrl);
                } else {
                    Log.d("Fetch", "Failed to retrieve image");
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.d("Fetch", "Error: " + t.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView productImage;
        TextView productName, productPrice, productQuantity;
        ImageButton minusButton, plusButton;
        ImageView deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productPrice = itemView.findViewById(R.id.productPrice);
            productQuantity = itemView.findViewById(R.id.productQuantity);
        }
    }
}

package com.example.QuickDrop.ui.client.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import com.example.QuickDrop.core.network.dto.PostLignePanierRequest;
import com.example.QuickDrop.core.network.dto.PostPanierRequest;
import com.example.QuickDrop.core.network.dto.PostPanierResponse;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.load.DataSource;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ArticleCardAdapter extends RecyclerView.Adapter<ArticleCardAdapter.CardViewHolder> {

    private List<GetArticleResponse> getArticleResponses;
    private List<GetArticleResponse> getArticleResponsesFull;
    private Context context;
    RequestOptions option;
    private PostPanierRequest postPanierRequest;




    public ArticleCardAdapter(Context context, List<GetArticleResponse> getArticleResponses) {
        this.context = context;
        this.getArticleResponses = getArticleResponses;
        this.getArticleResponsesFull = new ArrayList<>(getArticleResponses);
        option=new RequestOptions().centerCrop().error(R.drawable.img);
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_products_dispo, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.name.setText(getArticleResponses.get(position).getName());
        holder.price.setText(getArticleResponses.get(position).getPrice());
        fetchImage( getArticleResponses.get(holder.getAdapterPosition()).getId(),holder.img);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int articleId = getArticleResponses.get(holder.getAdapterPosition()).getId();
                String name= getArticleResponses.get(holder.getAdapterPosition()).getName();
                String designation=getArticleResponses.get(holder.getAdapterPosition()).getDesignation();
                String price=getArticleResponses.get(holder.getAdapterPosition()).getPrice();
                String img =getArticleResponses.get(holder.getAdapterPosition()).getProductimage();

                showDialog(articleId,name, designation, price);

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
                    Picasso.get().load(imageUrl).into(imageView);
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
    public void filter(String text) {
        List<GetArticleResponse> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(getArticleResponsesFull); // Utilisez la liste complète lorsque le texte est vide
        } else {
            for (GetArticleResponse article : getArticleResponses) {

                if (article.getName().toLowerCase().contains(text.toLowerCase()) ||
                        article.getDesignation().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(article);
                }
            }
        }
        getArticleResponses.clear();
        getArticleResponses.addAll(filteredList);
        notifyDataSetChanged();
    }
    private Dialog dialog;

    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetView;
    private int initialHeight ;
    public void showDialog(int articlieId,String name, String designation, String price) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.activity_article_details, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setCanceledOnTouchOutside(true); // Permettre de fermer le dialog en cliquant en dehors
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

        TextView nameTextView = bottomSheetView.findViewById(R.id.prdname);
        TextView designationTextView = bottomSheetView.findViewById(R.id.prddesignation);
        TextView priceTextView = bottomSheetView.findViewById(R.id.prdprice);
        ImageView productImageView = bottomSheetView.findViewById(R.id.prdimg);
        TextView quantiteTextView = bottomSheetView.findViewById(R.id.quantitetext);

        nameTextView.setText(name);
        designationTextView.setText(designation);
        priceTextView.setText(price);

        fetchImage(articlieId,productImageView);

        ImageView moinsImageView = bottomSheetView.findViewById(R.id.moinsid);
        moinsImageView.setOnClickListener(v -> moins(quantiteTextView));

        ImageView plusImageView = bottomSheetView.findViewById(R.id.plusid);
        plusImageView.setOnClickListener(v -> plus(quantiteTextView));

        Button panierimg = bottomSheetView.findViewById(R.id.AddCart);
        panierimg.setOnClickListener(v -> {
            ajoutauPanier(quantiteTextView, articlieId);
            dialog.dismiss(); // Fermer le dialog après avoir effectué l'action
        });

        dialog.show();
    }

    public void moins(TextView quantiteTextView) {
        int quantite = Integer.parseInt(quantiteTextView.getText().toString());
        if (quantite > 1) {
            quantite--;
            quantiteTextView.setText(String.valueOf(quantite));
        } else {

        }
    }

    private void plus(TextView quantiteTextView) {
        int quantite = Integer.parseInt(quantiteTextView.getText().toString());
        quantite++;
        quantiteTextView.setText(String.valueOf(quantite));
    }

    public void ajoutauPanier(TextView quantiteTextView,int articlieId) {
        final Dialog dialoge = new Dialog(context);
        dialoge.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialoge.setContentView(R.layout.dialog_message);
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        String token = sharedPreferences.getString("accessToken", null);

        TextView messageTextView = dialoge.findViewById(R.id.text_message);
        messageTextView.setText("Article ajouté au panier");
        PostPanierRequest postPanierRequest=new PostPanierRequest();
        postPanierRequest.setValide(false);

        CreePanierr(token,postPanierRequest,quantiteTextView,articlieId);





        dialoge.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dialoge.dismiss();


            }
        }, 2000);



    }
    public void CreePanierr(String token,PostPanierRequest postPanierRequest,TextView quantiteTextView,int articlieId){

        Call<PostPanierResponse> postPanierResponseCall=ApiClient.getService().CreePanier("Bearer " +token,postPanierRequest);
        postPanierResponseCall.enqueue(new Callback<PostPanierResponse>() {
            @Override
            public void onResponse(Call<PostPanierResponse> call, Response<PostPanierResponse> response) {
                if(response.isSuccessful()){
                    PostPanierResponse postPanierResponse=response.body();
                    int idpanier =postPanierResponse.getId();

                    PostLignePanierRequest postLignePanierRequest=new PostLignePanierRequest();
                    postLignePanierRequest.setLivraison(idpanier);
                    postLignePanierRequest.setArticle(articlieId);
                    int quantite = Integer.parseInt(quantiteTextView.getText().toString());
                    postLignePanierRequest.setQuantity(quantite);

                    CreeLignePanier(postLignePanierRequest);
                    String message ="successful"+ idpanier;
                }else {
                    String message ="not successful";
                }
            }


            @Override
            public void onFailure(Call<PostPanierResponse> call, Throwable t) {
                String message ="error ";

            }
        });

    }


    private  void CreeLignePanier(PostLignePanierRequest postLignePanierRequest){
        Call<DeliveryOrderLine> cartItemCall=ApiClient.getService().CreeLignePanier(postLignePanierRequest);
        cartItemCall.enqueue(new Callback<DeliveryOrderLine>() {
            @Override
            public void onResponse(Call<DeliveryOrderLine> call, Response<DeliveryOrderLine> response) {
                if (response.isSuccessful()){
                    DeliveryOrderLine cartItem=response.body();

                    String message ="successful Ligne panier";
                }else {
                    String message ="not successful ligne panier";
                }


            }

            @Override
            public void onFailure(Call<DeliveryOrderLine> call, Throwable t) {

            }
        });


    }



    @Override
    public int getItemCount() {
        return getArticleResponses.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView name, price;
        ImageView img;


        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.prdimg);
            name = itemView.findViewById(R.id.prdname);
            price = itemView.findViewById(R.id.prdprice);

        }

        public void bind(CardView cardView) {
            // Ajoutez le CardView au ViewHolder
            ViewGroup parent = (ViewGroup) cardView.getParent();
            if (parent != null) {
                parent.removeView(cardView);
            }
            ((ViewGroup) itemView).addView(cardView);
        }

    }

}


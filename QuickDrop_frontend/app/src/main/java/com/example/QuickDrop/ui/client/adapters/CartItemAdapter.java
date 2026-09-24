package com.example.QuickDrop.ui.client.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.AdminArticleListActivity;
import com.example.QuickDrop.ui.common.listeners.CartItemAdapterListener;
import com.example.QuickDrop.core.model.SelectedFournisseur;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.ArticleAvailability;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetFournisseurResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import com.example.QuickDrop.core.network.dto.PostLignePanierRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder> {
    private List<DeliveryOrderLine> cartItemList;
    private Context context;
    RequestOptions option;
    Integer panier ;
    List<Integer> Fournisseursfinaux= new ArrayList<>();
    Map<Integer, Map<String, Set<Integer>>> fournisseursArticles = new HashMap<>();
    private CartItemAdapterListener listener;



    public CartItemAdapter(Context context,List<DeliveryOrderLine> cartItemList,Integer panier,List<Integer> Fournisseursfinaux,CartItemAdapterListener listener) {
        this.cartItemList = cartItemList;
        this.context = context;
        this.panier=panier;
        this.listener = listener;
        this.Fournisseursfinaux=Fournisseursfinaux;
        option=new RequestOptions().centerCrop().error(R.drawable.img);


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_panier, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DeliveryOrderLine cartItem = cartItemList.get(position);
        final int positionToRemove = position;

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

                    holder.plusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            plus( holder.productQuantity,holder.productPrice,cartItem,price1);
                        }
                    });
                    holder.minusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            moins(holder.productQuantity,holder.productPrice,cartItem,price1);
                        }
                    });
                    holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            delete(cartItem,positionToRemove);
                            notifyDataSetChanged();

                        }
                    });
                }else{
                    String message ="Error";
                }
            }
            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message ="Not";

            }
        });







    }
    public void moins(TextView quantiteTextView,TextView priceTextview,DeliveryOrderLine cartItem,double price) {
        int quantite = Integer.parseInt(quantiteTextView.getText().toString());
        if (quantite > 1) {
            quantite--;
            quantiteTextView.setText(String.valueOf(quantite));
            PostLignePanierRequest postLignePanierRequest=new PostLignePanierRequest();
            postLignePanierRequest.setLivraison(cartItem.getLivraison());
            postLignePanierRequest.setArticle(cartItem.getArticle());
            postLignePanierRequest.setQuantity(quantite);
            Call<DeliveryOrderLine> putLigneLivraison=ApiClient.getService().PutLignePanier(cartItem.getId(),postLignePanierRequest );
            putLigneLivraison.enqueue(new Callback<DeliveryOrderLine>() {
                @Override
                public void onResponse(Call<DeliveryOrderLine> call, Response<DeliveryOrderLine> response) {
                    if(response.isSuccessful()){
                        double totalPrice = price * postLignePanierRequest.getQuantity();
                        priceTextview.setText("prix " +totalPrice +"DZD" );

                    }
                }

                @Override
                public void onFailure(Call<DeliveryOrderLine> call, Throwable t) {

                }
            });
        } else {

        }
    }

    private void plus(TextView quantiteTextView,TextView priceTextview,DeliveryOrderLine cartItem,double price) {
        int quantite = Integer.parseInt(quantiteTextView.getText().toString());
        quantite++;
        quantiteTextView.setText(String.valueOf(quantite));
        PostLignePanierRequest postLignePanierRequest=new PostLignePanierRequest();
        postLignePanierRequest.setLivraison(cartItem.getLivraison());
        postLignePanierRequest.setArticle(cartItem.getArticle());
        postLignePanierRequest.setQuantity(quantite);
        Call<DeliveryOrderLine> putLigneLivraison=ApiClient.getService().PutLignePanier(cartItem.getId(),postLignePanierRequest );
        putLigneLivraison.enqueue(new Callback<DeliveryOrderLine>() {
            @Override
            public void onResponse(Call<DeliveryOrderLine> call, Response<DeliveryOrderLine> response) {
                if(response.isSuccessful()){
                    double totalPrice = price * postLignePanierRequest.getQuantity();
                    priceTextview.setText("prix " +totalPrice +"DZD" );

                }
            }

            @Override
            public void onFailure(Call<DeliveryOrderLine> call, Throwable t) {

            }
        });

    }
    private void delete(DeliveryOrderLine cartItem,int position){

        Call<DeliveryOrderLine> deleteligneLivraison=ApiClient.getService().deleteLignePanier(cartItem.getId());
        deleteligneLivraison.enqueue(new Callback<DeliveryOrderLine>() {
            @Override
            public void onResponse(Call<DeliveryOrderLine> call, Response<DeliveryOrderLine> response) {
                if(response.isSuccessful()){
                    cartItemList.remove(position);
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, cartItemList.size());

                    listener.onCartItemDeleted( panier);


                    String message ="delete";

                }
            }

            @Override
            public void onFailure(Call<DeliveryOrderLine> call, Throwable t) {

            }
        });
    }


    public void ArticleAvailability(int id_article,int quantite){
        Call<List<GetFournisseurResponse>> listCall=ApiClient.getService().ListproduitArticleDisponible(id_article, quantite);
        listCall.enqueue(new Callback<List<GetFournisseurResponse>>() {
            @Override
            public void onResponse(Call<List<GetFournisseurResponse>> call, Response<List<GetFournisseurResponse>> response) {
                List<GetFournisseurResponse> listGetfourniseurResponse=response.body();
                List<Integer> idsFournisseurs = new ArrayList<>();

                for (GetFournisseurResponse fournisseurResponse : listGetfourniseurResponse) {
                    int idFournisseur = fournisseurResponse.getId();
                    if (!fournisseursArticles.containsKey(idFournisseur)) {
                        Map<String, Set<Integer>> articles = new HashMap<>();
                        articles.put("AdminArticleListActivity", new HashSet<>());
                        fournisseursArticles.put(idFournisseur, articles);
                    }
                    fournisseursArticles.get(idFournisseur).get("AdminArticleListActivity").add(id_article);
                }
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < idsFournisseurs.size(); i++) {
                    builder.append(idsFournisseurs.get(i));
                    if (i < idsFournisseurs.size() - 1) {
                        builder.append(",");
                    }
                }
                String listeStr = builder.toString();

                Fournisseur(listeStr);
                String message ="ListRecuperer";
            }
            @Override
            public void onFailure(Call<List<GetFournisseurResponse>> call, Throwable t) {

            }
        });

    }

    public void Fournisseur(String idsFournisseurs){
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        String token = sharedPreferences.getString("accessToken", null);

        Call<SelectedFournisseur> fournisseurSelectionneCall=ApiClient.getService().InfofournisseurTarification("Bearer " +token,idsFournisseurs);
        fournisseurSelectionneCall.enqueue(new Callback<SelectedFournisseur>() {
            @Override
            public void onResponse(Call<SelectedFournisseur> call, Response<SelectedFournisseur> response) {
                SelectedFournisseur fournisseurSelectionne= response.body();
                if(fournisseurSelectionne!=null){
                    if (!Fournisseursfinaux.contains(fournisseurSelectionne.getFournisseur())) {
                        Fournisseursfinaux.add(fournisseurSelectionne.getFournisseur());
                    }
                    String message ="fournisseruselectiionnee"+ fournisseurSelectionne.getFournisseur();

                }
                else{
                    String message ="fournisseur non selectionne";
                }
            }

            @Override
            public void onFailure(Call<SelectedFournisseur> call, Throwable t) {

            }
        });

    }

    public List<Integer> getItemList() {
        if (!Fournisseursfinaux.isEmpty()) {
            return Fournisseursfinaux;
        } else {
            String message ="vide";
            Toast.makeText(context,message,Toast.LENGTH_LONG).show();

            return null;
        }
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
            minusButton = itemView.findViewById(R.id.minusButton);
            plusButton = itemView.findViewById(R.id.plusButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

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

}

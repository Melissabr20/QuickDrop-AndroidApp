package com.example.QuickDrop.ui.fournisseur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.CartArticleItem;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetListeProduitResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import static java.lang.Integer.parseInt;

import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private List<CartArticleItem> listData;
    private static RecyclerViewClickListener itemListener;
    private int currentLayout = R.layout.item_article; // Default layout

    public ArticleListAdapter(List<CartArticleItem> listData, RecyclerViewClickListener itemlistener) {
        this.listData = listData;
        this.itemListener = itemlistener;
    }

    @Override
    public int getItemViewType(int position) {
        // Return the current layout type
        return currentLayout;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name, qte, disc, prix;
        public ImageView imageView;
        public ImageButton delete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name_view);
            this.disc = itemView.findViewById(R.id.disc);
            this.qte = itemView.findViewById(R.id.qteview);
            this.prix = itemView.findViewById(R.id.prix);
            this.imageView = itemView.findViewById(R.id.imageview);
            this.delete = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

    @NonNull
    @Override
    public ArticleListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(viewType, parent, false); // Inflate based on viewType
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartArticleItem myListData = listData.get(position);
        holder.name.setText(myListData.getId());
        if(getItemViewType(position)!=R.layout.liste_article_a_choisir){
            holder.qte.setText(myListData.getQte());
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int clickedPosition = holder.getAdapterPosition();
                    delete(myListData.getIdliste(),clickedPosition);

                    Log.d("Fetch", ""+ myListData.getId());

                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CartArticleItem item = myListData;
                    AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                    LayoutInflater inflater = LayoutInflater.from(holder.itemView.getContext());
                    View dialogView = inflater.inflate(R.layout.dialog_input, null);
                    builder.setView(dialogView);

                    EditText inputInteger = dialogView.findViewById(R.id.input_integer);

                    builder.setTitle("Veuillez entrer la quantité disponible dans le stock")
                            .setPositiveButton("OK", (dialog, which) -> {
                                String input = inputInteger.getText().toString();
                                if (!input.isEmpty()) {
                                    int value = parseInt(input);
                                    GetListeProduitResponse liste=new GetListeProduitResponse();
                                    liste.setStock_disponible(value);
                                    Call<GetListeProduitResponse> patchCall=ApiClient.getService().patchlistproduits(myListData.getIdliste(),liste);
                                    patchCall.enqueue(new Callback<GetListeProduitResponse>() {
                                        @Override
                                        public void onResponse(Call<GetListeProduitResponse> call, Response<GetListeProduitResponse> response) {
                                            if(response.isSuccessful()){
                                                item.setQte(String.valueOf(value));

                                                notifyItemChanged(holder.getAdapterPosition());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<GetListeProduitResponse> call, Throwable t) {

                                        }
                                    });



                                } else {
                                    Toast.makeText(holder.itemView.getContext(), "Input is empty", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
        }
        holder.disc.setText(myListData.getDisc());
        holder.prix.setText(myListData.getPrix());
        fetchImage(myListData.getI(),holder.imageView);

    }

    private void delete(int article,int position){

        Call<GetListeProduitResponse> articleCall=ApiClient.getService().deleteLigneProduit(article);
        articleCall.enqueue(new Callback<GetListeProduitResponse>() {
            @Override
            public void onResponse(Call<GetListeProduitResponse> call, Response<GetListeProduitResponse> response) {
                if(response.isSuccessful()){
                    listData.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    notifyItemRangeChanged(position, listData.size());
                }

            }
            @Override
            public void onFailure(Call<GetListeProduitResponse> call, Throwable t) {

            }
        });


    }

    @Override
    public int getItemCount() {
        if(listData.size()>0){
            return listData.size();
        }else {
            return 0;
        }
    }

    // Method to change the layout
    public void changeLayout(int newLayout) {
        currentLayout = newLayout;
        notifyDataSetChanged(); // Notify adapter to refresh the layout
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

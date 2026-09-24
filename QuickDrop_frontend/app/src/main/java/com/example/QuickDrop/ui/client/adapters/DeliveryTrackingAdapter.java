package com.example.QuickDrop.ui.client.adapters;

import com.example.QuickDrop.ui.common.adapters.OrderLineAdapter;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Callable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryTrackingAdapter extends RecyclerView.Adapter<DeliveryTrackingAdapter.ViewHolder> {
    private List<GetPanierResponse> listLivraison;
    private Context context;
    RequestOptions option;
    public interface OnLocationListener {
        void onLocationSelected(Location location,GetPanierResponse livraison);
    }
    private OnLocationListener mListener;
    public DeliveryTrackingAdapter(Context context,List<GetPanierResponse> listLivraison,OnLocationListener listener) {
        this.listLivraison=listLivraison;
        this.context=context;
        this.mListener=listener;


    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView idorder,etat,numlivreur;


        public ViewHolder(View itemView) {
            super(itemView);
            idorder=itemView.findViewById(R.id.idorder);
            etat=itemView.findViewById(R.id.etat);
            numlivreur=itemView.findViewById(R.id.livreurnum);
        }
    }

    @NonNull
    @Override
    public DeliveryTrackingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_valide, parent, false);
        return new DeliveryTrackingAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeliveryTrackingAdapter.ViewHolder holder, int position) {
        GetPanierResponse livraison = listLivraison.get(position);
        holder.idorder.setText(String.valueOf(livraison.getId()));
        int livreur= livraison.getLivreur();
        final Dialog dialog = new Dialog(context);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.confirmation_dialog);
        RecyclerView orders  = dialog.findViewById(R.id.items);

        holder.etat.setText(livraison.getStatus());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView t=dialog.findViewById(R.id.livraisonprix);
                t.setText(livraison.getPrix_livraison()+"DZD");
                t=dialog.findViewById(R.id.total);
                t.setText(livraison.getMontant()+" DZD");
                GridLayoutManager layoutManager = new GridLayoutManager(context,1);
                orders.setLayoutManager(layoutManager);
                OrderLineAdapter listorderAdapter= new OrderLineAdapter(context, livraison.getLigneLivraison());
                orders.setAdapter(listorderAdapter);

                dialog.show();
            }
        });

        if ("en_cours".equals(livraison.getStatus())){

            Call<LoginResponse.Livreur> deliveryAddressCall=ApiClient.getService().GetLivreur(livreur);
            deliveryAddressCall.enqueue(new Callback<LoginResponse.Livreur>() {
                @Override
                public void onResponse(Call<LoginResponse.Livreur> call, Response<LoginResponse.Livreur> response) {
                    if (response.isSuccessful()){
                        LoginResponse.Livreur deliveryAddress= response.body();
                        String number=deliveryAddress.getUser().getPhone().getPhone_number();
                        holder.numlivreur.setText(number);

                        String message =""+deliveryAddress.getLng();
                        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                        Location locationn = new Location("");

                        locationn.setLongitude(deliveryAddress.getLng());
                        locationn.setLatitude(deliveryAddress.getLat());
                        mListener.onLocationSelected(locationn, livraison);

                    }
                }

                @Override
                public void onFailure(Call<LoginResponse.Livreur> call, Throwable t) {

                }
            });



        } else if (livraison.getStatus()=="en Attente") {
            Call<LoginResponse.Fournisseur> fournissuer=ApiClient.getService().GetFournisseur(listLivraison.get(position).getFournisseur());
            fournissuer.enqueue(new Callback<LoginResponse.Fournisseur>() {
                @Override
                public void onResponse(Call<LoginResponse.Fournisseur> call, Response<LoginResponse.Fournisseur> response) {
                    LoginResponse.Fournisseur fournissuer= response.body();
                    Double lat= fournissuer.getLat();
                    Double lng= fournissuer.getLng();
                    Location location = new Location("");
                    location.setLatitude(lat);
                    location.setLongitude(lng);

                    mListener.onLocationSelected(location,livraison);


                }

                @Override
                public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {


                }
            });}

    }

    @Override
    public int getItemCount() {
        return listLivraison.size();
    }

}
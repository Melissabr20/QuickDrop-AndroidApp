package com.example.QuickDrop.ui.admin.adapters;

import com.example.QuickDrop.ui.common.adapters.OrderLineAdapter;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.w3c.dom.Text;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminOrderListAdapter extends RecyclerView.Adapter<AdminOrderListAdapter.ViewHolder>{

    private List<GetPanierResponse> getOrders;
    private Context context;

    public AdminOrderListAdapter(Context context, List<GetPanierResponse> livraison) {
        this.getOrders = livraison;
        this.context = context;
    }



    public static  class ViewHolder extends RecyclerView.ViewHolder{
        public TextView idorder,livreur,supplier,etat;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.idorder = itemView.findViewById(R.id.idorder);
            this.supplier = itemView.findViewById(R.id.suppliername);
            this.livreur = itemView.findViewById(R.id.livreurname);
            this.etat=itemView.findViewById(R.id.prixTotal);

        }
    }

    @NonNull
    @Override
    public AdminOrderListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);

        return new AdminOrderListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminOrderListAdapter.ViewHolder holder, int position) {
        final int positionToRemove = position;

        holder.idorder.setText(String.valueOf(getOrders.get(position).getId()));
        holder.etat.setText(getOrders.get(position).getStatus());



        Call<LoginResponse.Livreur> livreurCall=ApiClient.getService().GetLivreursDetails(getOrders.get(position).getLivreur());
        livreurCall.enqueue(new Callback<LoginResponse.Livreur>() {
            @Override
            public void onResponse(Call<LoginResponse.Livreur> call, Response<LoginResponse.Livreur> response) {
                LoginResponse.Livreur livreur= response.body();
                if (livreur != null && livreur.getUser() != null) {
                    holder.livreur.setText(livreur.getUser().getUsername());
                } else {
                    holder.livreur.setText("");

                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Livreur> call, Throwable t) {

            }
        });

        Call<LoginResponse.Fournisseur> fournisseurCall=ApiClient.getService().GetFournisseur(getOrders.get(position).getFournisseur());
        fournisseurCall.enqueue(new Callback<LoginResponse.Fournisseur>() {
            @Override
            public void onResponse(Call<LoginResponse.Fournisseur> call, Response<LoginResponse.Fournisseur> response) {
                LoginResponse.Fournisseur getFournisseurResponse= response.body();
                if (getFournisseurResponse != null && getFournisseurResponse.getUser() != null) {
                    holder.supplier.setText(getFournisseurResponse.getUser().getUsername());
                } else {

                    holder.supplier.setText(" ");

                }

            }

            @Override
            public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPanierDetails(getOrders.get(positionToRemove));


            }
        });






    }



    public void getPanierDetails(GetPanierResponse panier) {
        Call<GetPanierResponse> getArticleResponseCall = ApiClient.getService().getPanierDetails(panier.getId());
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse = response.body();
                if (getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison = getPanierResponse.getLigneLivraison();
                    Double total = getPanierResponse.getMontant();

                    showDialog(panier,ligneLivraison);




                } else {

                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }
    private int initialHeight ;

    public void showDialog(GetPanierResponse order,List<DeliveryOrderLine> Ligne) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.recycler_view_admin, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setCanceledOnTouchOutside(true);
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
        TextView idorder=bottomSheetView.findViewById(R.id.order);
        TextView livreurr=bottomSheetView.findViewById(R.id.livreur);
        TextView supplier=bottomSheetView.findViewById(R.id.fournisseur);
        RecyclerView ligne=bottomSheetView.findViewById(R.id.recyclerview_order_dialog);
        TextView prix=bottomSheetView.findViewById(R.id.totalPriceView2);
        TextView date=bottomSheetView.findViewById(R.id.date);
        date.setText(order.getDate());


        idorder.setText(String.valueOf(order.getId()));
        prix.setText(String.valueOf(order.getMontant()));

        Call<LoginResponse.Livreur> livreurCall=ApiClient.getService().GetLivreursDetails(order.getLivreur());
        livreurCall.enqueue(new Callback<LoginResponse.Livreur>() {
            @Override
            public void onResponse(Call<LoginResponse.Livreur> call, Response<LoginResponse.Livreur> response) {
                LoginResponse.Livreur livreur= response.body();
                if (livreur != null && livreur.getUser() != null) {
                    livreurr.setText(livreur.getId() +" " + livreur.getUser().getUsername());
                } else {
                    livreurr.setText("");

                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Livreur> call, Throwable t) {

            }
        });

        Call<LoginResponse.Fournisseur> fournisseurCall=ApiClient.getService().GetFournisseur(order.getFournisseur());
        fournisseurCall.enqueue(new Callback<LoginResponse.Fournisseur>() {
            @Override
            public void onResponse(Call<LoginResponse.Fournisseur> call, Response<LoginResponse.Fournisseur> response) {
                LoginResponse.Fournisseur getFournisseurResponse= response.body();
                if (getFournisseurResponse != null && getFournisseurResponse.getUser() != null) {
                    supplier.setText(getFournisseurResponse.getId() + "  " + getFournisseurResponse.getUser().getUsername());
                } else {

                    supplier.setText(" ");

                }

            }

            @Override
            public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {

            }
        });



        GridLayoutManager layoutManager = new GridLayoutManager(context,1);
        ligne.setLayoutManager(layoutManager);
        OrderLineAdapter adapter = new OrderLineAdapter(context, Ligne);
        ligne.setAdapter(adapter);
        dialog.show();
    }




    @Override
    public int getItemCount() {
        if (getOrders != null) {
            return getOrders.size();
        } else {
            return 0;
        }
    }
}

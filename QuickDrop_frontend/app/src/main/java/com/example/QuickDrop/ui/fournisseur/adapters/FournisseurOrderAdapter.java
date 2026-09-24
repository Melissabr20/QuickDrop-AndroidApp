package com.example.QuickDrop.ui.fournisseur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.ui.fournisseur.fragments.FournisseurDashboardFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FournisseurOrderAdapter extends RecyclerView.Adapter<FournisseurOrderAdapter.ViewHolder> {
    private List<FournisseurDashboardFragment.OrderSummaryItem> listData;
    private static RecyclerViewClickListener itemListener;

    public FournisseurOrderAdapter(List<FournisseurDashboardFragment.OrderSummaryItem> listData, RecyclerViewClickListener itemListener) {
        this.listData = listData;
        FournisseurOrderAdapter.itemListener = itemListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView ord;
        public ImageView imageView; // Assuming you have an ImageView, but it's not used in onBindViewHolder

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.ord = itemView.findViewById(R.id.order);
            this.imageView = itemView.findViewById(R.id.imageView); // Assuming you have this in your layout

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

    @NonNull
    @Override
    public FournisseurOrderAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.livraison_item_layout, parent, false);
        return new ViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull FournisseurOrderAdapter.ViewHolder holder, int position) {
        final FournisseurDashboardFragment.OrderSummaryItem myListData = listData.get(position);
        holder.ord.setText(myListData.displayText);
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}
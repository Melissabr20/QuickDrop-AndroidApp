package com.example.QuickDrop.ui.livreur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.OrderLineItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderArticlesAdapter extends RecyclerView.Adapter<OrderArticlesAdapter.InnerViewHolder> {

    private Context mContext;
    private List<OrderLineItem> mData;

    public OrderArticlesAdapter(Context context, List<OrderLineItem> data) {
        mContext = context;
        mData = data;
    }

    @NonNull
    @Override
    public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.order_line, parent, false);
        return new InnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
        holder.price.setText(mData.get(position).getPrice());
        holder.name.setText(mData.get(position).getName());
        holder.qte.setText(mData.get(position).getQte());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static  class InnerViewHolder extends RecyclerView.ViewHolder{
        public TextView name,price,qte;
        public InnerViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.namePrdView);
            this.qte = itemView.findViewById(R.id.qteview);
            this.price = itemView.findViewById(R.id.priceview);
        }
    }
    public int getOrderItems(int position) {
        return mData.get(position).getId_order();
    }
}
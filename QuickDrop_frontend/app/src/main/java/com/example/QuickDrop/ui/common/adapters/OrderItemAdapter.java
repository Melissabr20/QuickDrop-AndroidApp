package com.example.QuickDrop.ui.common.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.OrderLineItem;
import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ViewHolder> {

    private List<OrderLineItem> listData;

    public OrderItemAdapter(List<OrderLineItem> listData) {
        this.listData = listData;
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name,price,qte;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.namePrdView);
            this.qte = itemView.findViewById(R.id.qteview);
            this.price = itemView.findViewById(R.id.priceview);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.order_line,parent,false);

        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.price.setText(listData.get(position).getPrice());
        holder.name.setText(listData.get(position).getName());
        holder.qte.setText(listData.get(position).getQte());

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


}
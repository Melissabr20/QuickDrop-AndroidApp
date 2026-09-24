package com.example.QuickDrop.ui.livreur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.DeliveryTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LivreurTaskAdapter extends RecyclerView.Adapter<LivreurTaskAdapter.ViewHolder> {
    private List<DeliveryTask> listData;
    private static RecyclerViewClickListener itemListener;
    public LivreurTaskAdapter(List<DeliveryTask> listData, RecyclerViewClickListener itemlistener) {
        this.listData = listData;
        this.itemListener= itemlistener;
    }



    public static  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView name,id_ord,adr_four,adr_client;
        public ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name = itemView.findViewById(R.id.name_view);
            this.id_ord = itemView.findViewById(R.id.order);
            this.adr_four = itemView.findViewById(R.id.adr_four);
            this.adr_client = itemView.findViewById(R.id.adr_client);
            this.imageView = itemView.findViewById(R.id.imageview);
            itemView.setOnClickListener(this);


        }

        @Override
        public void onClick(View v) {
            itemListener.recyclerViewListClicked(v, this.getLayoutPosition());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View listItem = inflater.inflate(R.layout.list_item_custom,parent,false);
        ViewHolder viewHolder = new ViewHolder(listItem);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  DeliveryTask myListData= listData.get(position);
        holder.name.setText(listData.get(position).getName());
        holder.id_ord.setText(listData.get(position).getId_ord());
        holder.adr_four.setText(listData.get(position).getAdr_four());
        holder.adr_client.setText(listData.get(position).getAdr_client());
    }

    @Override
    public int getItemCount() {
        return listData.size();
    }
}

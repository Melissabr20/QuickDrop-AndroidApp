package com.example.QuickDrop.ui.livreur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.DeliveryStop;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DeliveryStopAdapter extends RecyclerView.Adapter<DeliveryStopAdapter.ViewHolder> {

    private List<DeliveryStop> listData;
    private static RecyclerViewClickListener itemListener;

    public DeliveryStopAdapter(List<DeliveryStop> listData, RecyclerViewClickListener itemlistener) {
        this.listData = listData;
        this.itemListener= itemlistener;
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView title,text;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.location_title);
            this.text = itemView.findViewById(R.id.location_text);
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
        View listItem = inflater.inflate(R.layout.location_item,parent,false);

        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(listData.get(position).getTitle());
        holder.text.setText(listData.get(position).getText());

    }

    @Override
    public int getItemCount() {
        return listData.size();
    }


}
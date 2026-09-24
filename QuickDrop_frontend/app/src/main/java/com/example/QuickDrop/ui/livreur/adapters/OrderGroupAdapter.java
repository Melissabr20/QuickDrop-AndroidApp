package com.example.QuickDrop.ui.livreur.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.OrderLineItem;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrderGroupAdapter extends RecyclerView.Adapter<OrderGroupAdapter.OuterViewHolder> {

    private Context mContext;
    OrderArticlesAdapter innerAdapter;
    private List<List<OrderLineItem>> mData;
    private List<String> mTitles,mPrices ;  // This list contains the unique titles

    public OrderGroupAdapter(Context context, List<List<OrderLineItem>> data, List<String> titles, List<String> prices) {
        mContext = context;
        mData = data;
        mTitles = titles;
        mPrices = prices;
    }

    @NonNull
    @Override
    public OuterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.recyclerview_item, parent, false);
        return new OuterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OuterViewHolder holder, int position) {
        List<OrderLineItem> innerData = mData.get(position);
        innerAdapter= new OrderArticlesAdapter(mContext,innerData);
        holder.innerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        holder.innerRecyclerView.setAdapter(innerAdapter);

        String title = mTitles.get(position);  // Get the title for this position
        holder.id_order.setText(title);  // Set the title
        String price = mPrices.get(position);
        holder.price.setText(price);

        innerAdapter.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class OuterViewHolder extends RecyclerView.ViewHolder {
        RecyclerView innerRecyclerView;
        TextView id_order,price;

        public OuterViewHolder(@NonNull View itemView) {
            super(itemView);
            id_order = itemView.findViewById(R.id.order);
            price = itemView.findViewById(R.id.totalPriceView2);
            innerRecyclerView = itemView.findViewById(R.id.recyclerview_order_dialog);
        }
    }

}

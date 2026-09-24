package com.example.QuickDrop.ui.admin.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

public class ClientAdapter extends RecyclerView.Adapter<ClientAdapter.ClientViewHolder>{

    private List<LoginResponse.Client> clientList;
    private List<LoginResponse.Client> clientListFull;

    private Context context;
    private int initialHeight;
    public ClientAdapter(Context context, List<LoginResponse.Client> clientList) {
        this.context = context;
        this.clientList = clientList;
        this.clientListFull=new ArrayList<>(clientList);
    }
    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_users, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {

        LoginResponse.Client client=clientList.get(position);
        holder.name.setText(client.getUser().getUsername());
        holder.number.setText(client.getUser().getPhone().getPhone_number());
        holder.email.setText(client.getUser().getEmail());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDialog(client);

            }
        });

    }
    public void filter(String text) {
        List<LoginResponse.Client> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(clientListFull); // Utilisez la liste complète lorsque le texte est vide
        } else {
            for (LoginResponse.Client supplier : clientList) {
                if (supplier.getUser().getUsername().toLowerCase().contains(text.toLowerCase()) ||
                        supplier.getUser().getEmail().toLowerCase().contains(text.toLowerCase())||
                        supplier.getUser().getPhone().getPhone_number().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(supplier);
                }
            }
        }
        clientList.clear();
        clientList.addAll(filteredList);
        notifyDataSetChanged();
    }


    public void showDialog(LoginResponse.Client client) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.admininfoprofile, null);
        initialHeight = bottomSheetView.getHeight();

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

        TextView username=bottomSheetView.findViewById(R.id.username);
        TextView firstname=bottomSheetView.findViewById(R.id.firstname);
        TextView lastname=bottomSheetView.findViewById(R.id.lastname);
        TextView email=bottomSheetView.findViewById(R.id.email);
        TextView number=bottomSheetView.findViewById(R.id.number);

        username.setText(client.getUser().getUsername());
        lastname.setText(client.getUser().getLast_name());
        firstname.setText(client.getUser().getFirst_name());
        email.setText(client.getUser().getEmail());
        number.setText(client.getUser().getPhone().getPhone_number());

        dialog.show();
    }




    @Override
    public int getItemCount() {
        return clientList.size();
    }

    static class ClientViewHolder extends RecyclerView.ViewHolder {

        TextView name, email ,number;




        public ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            name=itemView.findViewById(R.id.name);
            email=itemView.findViewById(R.id.email);
            number=itemView.findViewById(R.id.number);

        }

        public void bind(CardView cardView) {
            ViewGroup parent = (ViewGroup) cardView.getParent();
            if (parent != null) {
                parent.removeView(cardView);
            }
            ((ViewGroup) itemView).addView(cardView);
        }

    }
}
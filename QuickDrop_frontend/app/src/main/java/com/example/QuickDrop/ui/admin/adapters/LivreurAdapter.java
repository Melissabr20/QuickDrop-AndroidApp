package com.example.QuickDrop.ui.admin.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.RegisterRequest;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivreurAdapter extends RecyclerView.Adapter<LivreurAdapter.ViewHolder> {
    List<LoginResponse.Livreur> LivreurList;
    private List<LoginResponse.Livreur> livreurListFull;
    Context context;
    private int initialHeight;
    public LivreurAdapter(Context context, List<LoginResponse.Livreur> LivreurList){
        this.LivreurList=LivreurList;
        this.livreurListFull=new ArrayList<>(LivreurList);
        this.context=context;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_users, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position1) {
        int position=position1;

        holder.name.setText(LivreurList.get(position).getUser().getUsername());
        holder.email.setText(LivreurList.get(position).getUser().getEmail());
        holder.number.setText(LivreurList.get(position).getUser().getPhone().getPhone_number());


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(LivreurList.get(position));



            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                delete(LivreurList.get(position),position);
                notifyDataSetChanged();
            }
        });



    }

    public void filter(String text) {
        List<LoginResponse.Livreur> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(livreurListFull); // Utilisez la liste complète lorsque le texte est vide
        } else {
            for (LoginResponse.Livreur supplier : LivreurList) {
                if (supplier.getUser().getUsername().toLowerCase().contains(text.toLowerCase()) ||
                        supplier.getUser().getEmail().toLowerCase().contains(text.toLowerCase())||
                        supplier.getUser().getPhone().getPhone_number().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(supplier);
                }
            }
        }
        LivreurList.clear();
        LivreurList.addAll(filteredList);
        notifyDataSetChanged();
    }

    private void delete(LoginResponse.Livreur Livreurlist,int position){

        Call<LoginResponse.Livreur> deletelivreur=ApiClient.getService().deleteLivreurDetails(Livreurlist.getId());
        deletelivreur.enqueue(new Callback<LoginResponse.Livreur>() {
            @Override
            public void onResponse(Call<LoginResponse.Livreur> call, Response<LoginResponse.Livreur> response) {
                if(response.isSuccessful()){
                    LivreurList.remove(position);
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, LivreurList.size());
                    String message ="delete";
                    Toast.makeText(context,message,Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Livreur> call, Throwable t) {

            }
        });
    }

    public void showDialog(LoginResponse.Livreur LivreurList ) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.updateuser, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int desiredContentHeight = (int) (0.7 * screenHeight);
        bottomSheetView.setMinimumHeight(desiredContentHeight);

        dialog.setContentView(bottomSheetView);
        dialog.setCanceledOnTouchOutside(true);


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

        String usernamee= LivreurList.getUser().getUsername();
        String firstnamee= LivreurList.getUser().getFirst_name();
        String lastnamee= LivreurList.getUser().getLast_name();
        String emaill= LivreurList.getUser().getEmail();
        String Phonenumber =LivreurList.getUser().getPhone().getPhone_number();
        String addresse= LivreurList.getAdress();

        Button signupbtn;
        EditText username,firstname,lastname, userlocation, password,email,phone;

        signupbtn =bottomSheetView.findViewById(R.id.btnsignup);
        username=bottomSheetView.findViewById(R.id.registeusername);
        email=bottomSheetView.findViewById(R.id.registeremail);
        phone=bottomSheetView.findViewById(R.id.registphone);
        firstname=bottomSheetView.findViewById(R.id.firstname);
        lastname=bottomSheetView.findViewById(R.id.lastname);
        userlocation=bottomSheetView.findViewById(R.id.registaddress);

        username.setText(usernamee);
        email.setText(emaill);
        phone.setText(Phonenumber);
        userlocation.setText(addresse);
        firstname.setText(firstnamee);
        lastname.setText(lastnamee);
        RegisterRequest update=new RegisterRequest();

        CheckBox myCheckBox = bottomSheetView.findViewById(R.id.myCheckBox);
        myCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {


                } else {

                }
            }
        });














        dialog.show();
    }

    @Override
    public int getItemCount() {
        return LivreurList.size();
    }

    public static  class ViewHolder extends RecyclerView.ViewHolder{
        public TextView name,email,number;
        public ImageButton delete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.name= itemView.findViewById(R.id.name);
            this.email = itemView.findViewById(R.id.email);
            number=itemView.findViewById(R.id.number);
            delete=itemView.findViewById(R.id.deleteButton);


        }
    }
}

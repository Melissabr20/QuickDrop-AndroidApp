package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.adapters.LivreurAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.RegisterRequest;
import com.example.QuickDrop.core.network.dto.RegisterResponse;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivreurListActivity extends AppCompatActivity {

    RecyclerView livreur;
    ImageView adduser;
    LivreurAdapter livreurAdapter;
    private int initialHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fournisseur_list);
        livreur=findViewById(R.id.Fournisseurs_list);
        adduser=findViewById(R.id.adduser);
        TextView fourni=findViewById(R.id.fourni);
        fourni.setText("Livreurs");
        SearchView searchView = findViewById(R.id.search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                livreurAdapter.filter(newText);
                return true;
            }
        });
        adduser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showDialog();
            }
        });

        getLivreurs();

    }

    private void getLivreurs(){
        Call<List<LoginResponse.Livreur>> fournissuerCall=ApiClient.getService().getLivreurList();
        fournissuerCall.enqueue(new Callback<List<LoginResponse.Livreur>>() {
            @Override
            public void onResponse(Call<List<LoginResponse.Livreur>> call, Response<List<LoginResponse.Livreur>> response) {
                List<LoginResponse.Livreur> LivreurList= response.body();
                setAdapter(LivreurList);
            }

            @Override
            public void onFailure(Call<List<LoginResponse.Livreur>> call, Throwable t) {

            }
        });
    }

    private void setAdapter(List<LoginResponse.Livreur> livreurList) {
        GridLayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 1);
        livreur.setLayoutManager(layoutManager);
        livreurAdapter=new LivreurAdapter(this,livreurList);
        livreur.setAdapter(livreurAdapter);


    }

    public void showDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.activity_addusers, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        int screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        int desiredContentHeight = (int) (0.7 * screenHeight);
        bottomSheetView.setMinimumHeight(desiredContentHeight);

        dialog.setContentView(bottomSheetView);
        dialog.setCanceledOnTouchOutside(true);

        Button signupbtn = bottomSheetView.findViewById(R.id.btnsignup);
        EditText username = bottomSheetView.findViewById(R.id.registeusername);
        EditText email = bottomSheetView.findViewById(R.id.registeremail);
        EditText password = bottomSheetView.findViewById(R.id.registermdp);
        EditText phone = bottomSheetView.findViewById(R.id.registphone);
        EditText firstname = bottomSheetView.findViewById(R.id.firstname);
        EditText lastname = bottomSheetView.findViewById(R.id.lastname);
        EditText userlocation = bottomSheetView.findViewById(R.id.registaddress);

        signupbtn.setOnClickListener(v -> {
            String name = username.getText().toString();
            String mail = email.getText().toString();
            String pass = password.getText().toString();
            String number = phone.getText().toString();
            String firstName = firstname.getText().toString();
            String lastName = lastname.getText().toString();
            String location = userlocation.getText().toString();

            if (name.isEmpty() || mail.isEmpty() || pass.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
            } else {
                RegisterRequest registerRequest = new RegisterRequest();
                registerRequest.setEmail(mail);
                registerRequest.setUsername(name);
                registerRequest.setPassword(pass);
                registerRequest.setPhone_number(number);
                registerRequest.setAdress(location);
                registerRequest.setType("livreur");

                registerUser(registerRequest);

                dialog.dismiss(); // Dismiss dialog after registration
            }
        });

        dialog.show();
    }

    public void registerUser(RegisterRequest registerRequest){
        Call<RegisterResponse> registerResponseCall =ApiClient.getService().registerUser(registerRequest);
        registerResponseCall.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()){
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {

            }
        });

    }


}

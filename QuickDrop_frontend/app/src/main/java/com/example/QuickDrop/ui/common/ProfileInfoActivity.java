package com.example.QuickDrop.ui.common;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.model.ProfileUpdateRequest;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileInfoActivity extends AppCompatActivity {

    EditText username,firstname,lastname,email,numbeer;
    Button changebtn;
    String name1,name2,name3,name4,name5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infoprofile);

        ImageView backbutton=findViewById(R.id.BackBtn);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileInfoActivity.this.finish();
            }
        });


        username=findViewById(R.id.username);
        lastname=findViewById(R.id.lastname);
        firstname=findViewById(R.id.firstname);
        email=findViewById(R.id.email);
        numbeer=findViewById(R.id.number);
        changebtn=findViewById(R.id.btnsavechanges);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String lastnamee = sharedPreferences.getString("lastname", null);
        String usernamee = sharedPreferences.getString("username", null);
        String firstnamee = sharedPreferences.getString("firstname", null);
        String emaill = sharedPreferences.getString("email", null);
        String number = sharedPreferences.getString("number", null);

        username.setText(usernamee);
        firstname.setText(firstnamee);
        email.setText(emaill);
        lastname.setText( lastnamee );
        numbeer.setText( number );


        changebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name1=username.getText().toString();
                name2=firstname.getText().toString();
                name3=lastname.getText().toString();
                name4=email.getText().toString();
                name5=numbeer.getText().toString();

                ProfileUpdateRequest user= new ProfileUpdateRequest();
                user.setPhone_number(name5);

                user.setUsername(name1);
                user.setFirst_name(name2);
                user.setLast_name(name3);
                user.setEmail(name4);

                updateprofil(user);

            }
        });
    }
    public  void updateprofil(ProfileUpdateRequest user){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("accessToken", null);
        Call<LoginResponse.User> updatecall=ApiClient.getService().updateUser("Bearer " +token, user);
        updatecall.enqueue(new Callback<LoginResponse.User>() {
            @Override
            public void onResponse(Call<LoginResponse.User> call, Response<LoginResponse.User> response) {
                if (response.isSuccessful()){
                    finish();
                }

            }

            @Override
            public void onFailure(Call<LoginResponse.User> call, Throwable t) {

            }
        });




    }
}
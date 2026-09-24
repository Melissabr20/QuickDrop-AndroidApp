package com.example.QuickDrop.ui.common;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.common.ProfileFragment;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.PasswordRequest;
import com.example.QuickDrop.core.network.dto.PasswordResponse;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
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

public class SecuritySettingsActivity extends AppCompatActivity {

    Button changepassword;
    EditText oldpass,newpass,confirmnewpass;
    String pass1,pass2,pass3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security);

        ImageView backbutton=findViewById(R.id.BackBtn);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SecuritySettingsActivity.this.finish();
            }
        });

        changepassword=findViewById(R.id.btnchangepassword);
        oldpass=findViewById(R.id.password);
        newpass=findViewById(R.id.newpassword);
        confirmnewpass=findViewById(R.id.confirmpassword);



        changepassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pass1 = oldpass.getText().toString();
                pass2 = newpass.getText().toString();
                pass3 = confirmnewpass.getText().toString();

                if(pass1.isEmpty() || pass2.isEmpty() || pass3.isEmpty() ) {
                    Toast.makeText(SecuritySettingsActivity.this, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
                }
                else {
                    PasswordRequest passwordRequest=new PasswordRequest();
                    passwordRequest.setOld_password(pass1);
                    passwordRequest.setNew_password(pass2);
                    passwordRequest.setConfirm_new_password(pass3);
                    PasswordUser(passwordRequest);
                }

            }
        });
    }
    public void PasswordUser(PasswordRequest passwordRequest){

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("accessToken", null);

        Call<PasswordResponse> passwordResponseCall=ApiClient.getService().PasswordUser("Bearer " + token,passwordRequest);
        passwordResponseCall.enqueue(new Callback<PasswordResponse>() {
            @Override
            public void onResponse(Call<PasswordResponse> call, Response<PasswordResponse> response) {
                if(response.isSuccessful()){
                    PasswordResponse passwordResponse=response.body();

                    if (passwordResponse != null) {
                        Intent intent = new Intent(SecuritySettingsActivity.this, ProfileFragment.class);
                        startActivity(intent);
                    }

                }

            }

            @Override
            public void onFailure(Call<PasswordResponse> call, Throwable t) {

            }
        });
    }

}
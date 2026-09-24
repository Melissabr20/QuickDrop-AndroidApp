package com.example.QuickDrop.ui.auth;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.RegisterRequest;
import com.example.QuickDrop.core.network.dto.RegisterResponse;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    Button signupbtn;
    EditText username,password,email,phone;
    String name, mail,pass,number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signupbtn =findViewById(R.id.btnsignup);
        username=findViewById(R.id.registeusername);
        email=findViewById(R.id.registeremail);
        password=findViewById(R.id.registermdp);
        phone=findViewById(R.id.registphone);



        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = username.getText().toString();
                mail=email.getText().toString();
                pass = password.getText().toString();
                number=phone.getText().toString();


                if(name.isEmpty() || mail.isEmpty() || pass.isEmpty() || number.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Fields cannot be blank", Toast.LENGTH_SHORT).show();
                    return;
                }
                RegisterRequest registerRequest=new RegisterRequest();
                registerRequest.setUsername(name);
                registerRequest.setEmail(mail);
                registerRequest.setPhone_number(number);
                registerRequest.setPassword(pass);
                registerRequest.setType("client");

                registerUser(registerRequest);

            }
        });


    }
    public void login(View view) {
        Intent login = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(login);
    }

    public void registerUser(RegisterRequest registerRequest){
        Call<RegisterResponse> registerResponseCall =ApiClient.getService().registerUser(registerRequest);
        registerResponseCall.enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful()){
                    String message ="successful";
                    Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_LONG).show();

                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                }else{
                    String message ="error try later";
                    Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {

                String message=t.getLocalizedMessage();
                Toast.makeText(RegisterActivity.this,message,Toast.LENGTH_LONG).show();

            }
        });

    }
}
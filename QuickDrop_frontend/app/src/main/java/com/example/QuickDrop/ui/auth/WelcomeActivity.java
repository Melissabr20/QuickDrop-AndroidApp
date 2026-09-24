package com.example.QuickDrop.ui.auth;

import com.example.QuickDrop.ui.client.ClientHomeActivity;

import com.example.QuickDrop.R;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.util.Log;

import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class WelcomeActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView skip=findViewById(R.id.skip);
        skip.setPaintFlags(skip.getPaintFlags()| Paint.UNDERLINE_TEXT_FLAG);



    }
    public void register(View view) {
        Intent register = new Intent(WelcomeActivity.this, RegisterActivity.class);
        startActivity(register);
    }
    public void login(View view) {
        Intent login = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(login);
    }
    public void skiptohome(View view) {
        Intent skip = new Intent(WelcomeActivity.this, ClientHomeActivity.class);
        startActivity(skip);
    }


}












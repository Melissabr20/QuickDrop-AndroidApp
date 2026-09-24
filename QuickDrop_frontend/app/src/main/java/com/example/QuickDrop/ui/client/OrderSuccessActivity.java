package com.example.QuickDrop.ui.client;

import com.example.QuickDrop.R;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class OrderSuccessActivity extends AppCompatActivity {
    Button continuerShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);
        continuerShopping=findViewById(R.id.continueShopping);

        continuerShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderSuccessActivity.this, ClientHomeActivity.class);
                startActivity(intent);

            }
        });
    }
}
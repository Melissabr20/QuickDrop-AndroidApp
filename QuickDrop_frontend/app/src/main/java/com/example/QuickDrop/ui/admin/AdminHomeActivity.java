
package com.example.QuickDrop.ui.admin;


import com.example.QuickDrop.R;
import android.content.Intent;
import android.os.Bundle;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminHomeActivity extends AppCompatActivity {
    LinearLayout fournisseurs,Livreurs,Clients,Users,articles,livraisons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administrateur);
        fournisseurs=findViewById(R.id.Fournisseurs);
        Livreurs=findViewById(R.id.Livreur);
        Clients=findViewById(R.id.client);
        Users=findViewById(R.id.users);
        articles=findViewById(R.id.article);
        livraisons=findViewById(R.id.Livraison);

        fournisseurs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, FournisseurListActivity.class);
                startActivity(intent);

            }
        });
        Livreurs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, LivreurListActivity.class);
                startActivity(intent);

            }
        });
        Clients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, ClientListActivity.class);
                startActivity(intent);


            }
        });
        Users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, UserListActivity.class);
                startActivity(intent);

            }
        });
        articles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, AdminArticleListActivity.class);
                startActivity(intent);


            }
        });
        livraisons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AdminHomeActivity.this, AdminNewOrdersActivity.class);
                startActivity(intent);

            }
        });


    }


}

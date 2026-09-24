package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.adapters.UserAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserListActivity extends AppCompatActivity {
    RecyclerView rcycleusers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);
        rcycleusers=findViewById(R.id.scrwProducts);
        getUsers();
    }

    private void getUsers(){
        Call<List<LoginResponse.User>> getusers=ApiClient.getService().getusers();
        getusers.enqueue(new Callback<List<LoginResponse.User>>() {
            @Override
            public void onResponse(Call<List<LoginResponse.User>> call, Response<List<LoginResponse.User>> response) {
                List<LoginResponse.User> user=response.body();
                if(user !=null){
                    setAdapter(user);
                  
                }

            }

            @Override
            public void onFailure(Call<List<LoginResponse.User>> call, Throwable t) {

            }
        });


    }

    private void setAdapter(List<LoginResponse.User> userList) {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rcycleusers.setLayoutManager(layoutManager);
        UserAdapter useradapter=new UserAdapter(this,userList);
        rcycleusers.setAdapter(useradapter);




    }
}
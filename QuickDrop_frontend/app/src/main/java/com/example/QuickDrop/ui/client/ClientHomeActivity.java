package com.example.QuickDrop.ui.client;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.client.fragments.CartFragment;
import com.example.QuickDrop.ui.client.fragments.DeliveryTrackingFragment;
import com.example.QuickDrop.ui.client.fragments.ClientHomeFragment;
import com.example.QuickDrop.ui.common.ProfileFragment;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.fragment.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Toast;

import com.example.QuickDrop.databinding.ActivityHomeBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class ClientHomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        replaceFragment(new ClientHomeFragment());


        Intent intent = getIntent();
        if(intent != null && intent.hasExtra("FRAGMENT_ID")) {
            int fragmentId = intent.getIntExtra("FRAGMENT_ID", -1);
            if (fragmentId == R.id.navigation_wishlist) {
                replaceFragment(new DeliveryTrackingFragment());
            }
        }
        else {
            binding.bottomNav.setOnItemReselectedListener(item -> {
                if (item.getItemId() == R.id.navigation_home) {
                    replaceFragment(new ClientHomeFragment());

                } else if (item.getItemId() == R.id.navigation_wishlist) {
                    replaceFragment(new DeliveryTrackingFragment());

                } else if (item.getItemId() == R.id.navigation_profile) {
                    replaceFragment(new ProfileFragment() );

                }
                else if (item.getItemId() == R.id.navigation_cart) {
                    replaceFragment(new CartFragment() );

                }

            });

        }


    }


    public void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager =getSupportFragmentManager();
        FragmentTransaction fragmentTransaction=fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container,fragment);
        fragmentTransaction.commit();
    }
}





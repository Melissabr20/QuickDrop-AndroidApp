package com.example.QuickDrop.ui.common;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.client.ClientHomeActivity;
import com.example.QuickDrop.ui.common.ProfileInfoActivity;
import com.example.QuickDrop.ui.common.SecuritySettingsActivity;
import com.example.QuickDrop.ui.common.SettingsActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileFragment extends Fragment {
    ImageView backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_profile, container, false);
        TextView prsnl=view.findViewById(R.id.personalinfobtn);
        backButton = view.findViewById(R.id.BackBtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent (getActivity(),ClientHomeActivity.class);
                startActivity(intent);

            }
        });

        prsnl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent (getActivity(),ProfileInfoActivity.class);
                startActivity(intent);
            }
        });

        TextView security=view.findViewById(R.id.securitybtn);
        security.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent (getActivity(),SecuritySettingsActivity.class);
                startActivity(intent);

            }
        });
        TextView settings=view.findViewById(R.id.settingsbtn);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent (getActivity(),SettingsActivity.class);
                startActivity(intent);
            }
        });



        return view;
    }
}
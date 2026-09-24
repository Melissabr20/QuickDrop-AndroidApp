package com.example.QuickDrop.ui.fournisseur;

import com.example.QuickDrop.ui.auth.RegisterActivity;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.fournisseur.fragments.ArticleFragment;
import com.example.QuickDrop.ui.fournisseur.fragments.FournisseurDashboardFragment;
import com.example.QuickDrop.ui.common.LivreurHistoryFragment;
import com.example.QuickDrop.ui.common.ProfileFragment;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.ui.common.viewmodel.SharedViewModel;
import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.QuickDrop.databinding.ActivityMainFournisseurBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FournisseurHomeActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "order_notifications";
    ActivityMainFournisseurBinding binding;
    private int id_client, id_fournisseur;
    // Tracks the panier/order id of the most recently processed
    // GetPanierResponse in getOrderDetails(), so it's available to the
    // "firsttime" catch-up call below, which fires after the loop has
    // already finished (the loop's own "id" local is out of scope there).
    private int lastOrderId;
    private Set<String> idSet;
    private Handler handler = new Handler();
    private Runnable runnable;
    private final int refreshInterval = 2000; // 20 seconds
    private String accessToken;
    boolean sendNotif = false,firsttime=true;
    private SharedViewModel sharedViewModel;
    Fragment home = new FournisseurDashboardFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createNotificationChannel();
        binding = ActivityMainFournisseurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        idSet = new TreeSet<>();

        FournisseurDashboardFragment.dataList = new ArrayList<>();

        replaceFragment(home);
        getOrderDetails(accessToken);

        runnable = new Runnable() {
            @Override
            public void run() {

                // Code to refresh the UI
                refreshUI();

                // Re-run the handler with the same delay
                handler.postDelayed(this, refreshInterval);
            }
        };

        // Start the periodic task
        handler.postDelayed(runnable, refreshInterval);

        binding.bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.navigation_home) {
                selectedFragment = new FournisseurDashboardFragment();
            } else if (item.getItemId() == R.id.navigation_article) {
                selectedFragment = new ArticleFragment();
            } else if (item.getItemId() == R.id.navigation_history) {
                selectedFragment = new LivreurHistoryFragment();
            } else if (item.getItemId() == R.id.navigation_profile) {
                selectedFragment = new ProfileFragment();
            }
            replaceFragment(selectedFragment);

            return true;
        });
    }

    private boolean isAppInForeground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        final String packageName = getPackageName();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void getOrderDetails(String accessToken) {
        String token = "Bearer " + accessToken;
        Call<List<GetPanierResponse>> getOrderResponseCall = ApiClient.getService().GetOrderDetailsbyfour(token);
        getOrderResponseCall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                if (response.isSuccessful()) {
                    List<GetPanierResponse> getOrderResponses = response.body();

                    if (getOrderResponses != null && !getOrderResponses.isEmpty()) {

                        for (GetPanierResponse getOrderResponse : getOrderResponses) {
                            id_client = getOrderResponse.getClient();
                            id_fournisseur = getOrderResponse.getFournisseur();
                            int id = getOrderResponse.getId();
                            String status = getOrderResponse.getStatus();

                            if ("non_fournisseur".equals(status) && !idSet.contains(String.valueOf(id))) {
                                idSet.add(String.valueOf(id));
                                lastOrderId = id;
                                FournisseurDashboardFragment.getClientDetails(id_client, id);
                                sendNotif = true;
                            }

                            ArrayList<String> arrayList = new ArrayList<>(idSet);
                            sharedViewModel.setIdSet(arrayList);

                            if (sendNotif && isAppInForeground()) {
                                sendNotification();
                                sendNotif = false;
                                Log.d("melissa",""+idSet.size());
                                Log.d("meli1", String.valueOf(FournisseurDashboardFragment.dataList.size()));
                            }
                        }
                        if(firsttime && FournisseurDashboardFragment.dataList.size()< idSet.size()){
                            FournisseurDashboardFragment.getClientDetails(id_client, lastOrderId);
                            firsttime=false;
                        }

                    } else {
                        Log.d("error", "cant get the order details");
                    }

                } else {
                    String message = "Unsuccessful order " + response.message();
                    Log.d("error", "cant get the order details");
                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {
                String message = "Error fetching order details: " + t.getMessage();
                Log.d("error", message);
            }
        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void refreshUI() {
        // Fetch the latest data and update the RecyclerView
        getOrderDetails(accessToken);
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Order Notification Channel";
            String description = "Channel for order notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // RegisterActivity the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.order2)  // Remplacez avec votre icône
                .setContentTitle("Une nouvelle livraison est arrivée")
                .setContentText("Confirmez la!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true); // La notification disparaît lorsqu'elle est cliquée

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }
}
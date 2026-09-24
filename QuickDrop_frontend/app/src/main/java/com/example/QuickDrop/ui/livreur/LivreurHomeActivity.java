package com.example.QuickDrop.ui.livreur;

import com.example.QuickDrop.ui.auth.RegisterActivity;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.livreur.fragments.LivreurTasksFragment;
import com.example.QuickDrop.ui.common.LivreurHistoryFragment;
import com.example.QuickDrop.ui.common.ProfileFragment;
import com.example.QuickDrop.ui.livreur.fragments.LivreurRouteMapFragment;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.example.QuickDrop.databinding.ActivityMainLivreurBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LivreurHomeActivity extends AppCompatActivity {

    private ActivityMainLivreurBinding binding;
    private int id_client, id_fournisseur, id_livreur;
    private Set<String> idSet;
    private String username, accessToken;
    private static final String CHANNEL_ID = "order_notifications";
    private Handler handler = new Handler();
    private Runnable runnable;
    private final int refreshInterval = 20000; // 20 seconds
    private boolean sendNotif = false;
    private SharedViewModel sharedViewModel;
    // BUG CORRIGÉ : "home" et "livreurTasksFragment" étaient deux instances DIFFÉRENTES
    // de LivreurTasksFragment. Les commandes reçues étaient ajoutées à "livreurTasksFragment"
    // (via fetchClientAddress), mais c'est "home" qui était réellement affiché à l'écran.
    // Résultat : les nouvelles livraisons n'apparaissaient jamais dans la liste visible.
    // On utilise maintenant une seule et même instance pour les deux usages.
    private LivreurTasksFragment livreurTasksFragment = new LivreurTasksFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainLivreurBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");

        idSet = new TreeSet<>();
        createNotificationChannel();

        replaceFragment(livreurTasksFragment);
        getOrderDetails(accessToken);

        runnable = new Runnable() {
            @Override
            public void run() {

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
                selectedFragment = livreurTasksFragment;
            } else if (item.getItemId() == R.id.navigation_map) {
                selectedFragment = new LivreurRouteMapFragment();
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
        Call<List<GetPanierResponse>> getOrderResponseCall = ApiClient.getService().GetOrderDetails("Bearer " + accessToken);
        getOrderResponseCall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                if (response.isSuccessful()) {
                    int prix=0,id=0;
                    List<GetPanierResponse> getOrderResponses = response.body();
                    if (getOrderResponses != null && !getOrderResponses.isEmpty()) {
                        for (GetPanierResponse getOrderResponse : getOrderResponses) {
                            id_client = getOrderResponse.getClient();
                            id_fournisseur = getOrderResponse.getFournisseur();
                            prix = getOrderResponse.getPrix_livraison();
                            id = getOrderResponse.getId();
                            String status = getOrderResponse.getStatus();
                            if ("non_livreur".equals(status) && !idSet.contains(String.valueOf(id))) {
                                idSet.add(String.valueOf(id));
                                livreurTasksFragment.fetchClientAddress(
                                        id_client,
                                        id_fournisseur,
                                        id,
                                        prix
                                );
                                sendNotif = true;
                            }
                            ArrayList<String> arrayList = new ArrayList<>(idSet);
                            sharedViewModel.setIdSet(arrayList);
                            if (sendNotif) {
                                sendNotification();
                                sendNotif = false;
                                Log.d("meli1", String.valueOf(idSet.size()));
                            }
                        }

                    } else {
                        Log.d("error", "cant get the order details");
                    }
                } else {
                    String message = "Unsuccessful order " + response.message();
                    Log.d("error", message);
                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {
                String message = "Error fetching order details: " + t.getMessage();
                Toast.makeText(LivreurHomeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    private void refreshUI() {
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
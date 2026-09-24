package com.example.QuickDrop.ui.fournisseur.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.fournisseur.adapters.FournisseurOrderAdapter;
import com.example.QuickDrop.ui.common.adapters.OrderItemAdapter;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.SelectedLivreur;
import com.example.QuickDrop.core.model.OrderLineItem;
import com.example.QuickDrop.core.model.DeliveryPrice;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.LoginResponse;
import com.example.QuickDrop.core.network.dto.PutPanier;
import com.example.QuickDrop.core.network.dto.PutLivraisonStatus;
import com.example.QuickDrop.ui.common.viewmodel.SharedViewModel;
import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.QuickDrop.databinding.FragmentLivreurActivityBinding;
import com.example.QuickDrop.databinding.FragmentFournisseurActivityBinding;
import com.example.QuickDrop.databinding.ActivityMainFournisseurBinding;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

public class FournisseurDashboardFragment extends Fragment implements RecyclerViewClickListener {

    /**
     * Holds both the panier/order id (needed for API calls) and the
     * human-readable summary text (needed for display). Previously this
     * list held only the display String, and the click handlers tried to
     * Integer.parseInt() that display string as if it were the id, which
     * crashed with NumberFormatException as soon as real data arrived.
     */
    public static class OrderSummaryItem {
        public final int panierId;
        public final String displayText;

        public OrderSummaryItem(int panierId, String displayText) {
            this.panierId = panierId;
            this.displayText = displayText;
        }

        @Override
        public String toString() {
            // Kept so any leftover code that expects toString() to render
            // the display text (e.g. an unmigrated adapter) still shows
            // something sensible instead of the default Object.toString().
            return displayText;
        }
    }

    public static List<OrderSummaryItem> dataList;
    public List<Integer> refusedLivreurIds = new ArrayList<>();
    private static FournisseurOrderAdapter adapter;
    private String username, accessToken;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetView;

    private TextView totalPrice;
    private OrderItemAdapter adapter2;
    private List<OrderLineItem> orderItems;
    private RecyclerView recyclerView;

    private static SharedViewModel sharedViewModel;
    ArrayList<String> list;
    private int id_client, idfournisseur, idlivreur;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        @NonNull FragmentFournisseurActivityBinding binding = FragmentFournisseurActivityBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Retrieve data from SharedPreferences
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        username = sharedPreferences.getString("username", "");
        idfournisseur = sharedPreferences.getInt("idfournisseur", 0);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        sharedViewModel.getIdSet().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> idSet) {
                // Update the UI with the new data
                list = new ArrayList<>(idSet);

            }
        });

        // Update UI with retrieved data
        TextView textView_user_name = view.findViewById(R.id.user_name);
        TextView textView_user_id = view.findViewById(R.id.liv_id);
        textView_user_name.setText("Bonjour, " + username);

        // Initialize RecyclerView
        recyclerView = binding.recyclerview;

        if (dataList == null) {
            dataList = new ArrayList<>();
        }

        adapter = new FournisseurOrderAdapter(dataList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);



        //getOrderDetails(accessToken);

        // Initialize the bottom sheet dialog
        bottomSheetDialog = new BottomSheetDialog(getActivity());

        // Inflate the layout for the bottom sheet
        bottomSheetView = getLayoutInflater().inflate(R.layout.resizable_bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // Get the BottomSheetBehavior from the parent view of the bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());

        // Set the initial peek height and maximum height of the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setHideable(false);

        bottomSheetView.setOnTouchListener(new View.OnTouchListener() {
            private float startY;
            private int originalHeight;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        startY = event.getRawY();
                        originalHeight = bottomSheetView.getHeight();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float deltaY = event.getRawY() - startY;
                        int newHeight = (int) (originalHeight + deltaY);
                        if (newHeight > originalHeight) {
                            ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                            layoutParams.height = newHeight;
                            bottomSheetView.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return true;
            }

        });


        Switch onOffSwitch = view.findViewById(R.id.on_off_switch);
        onOffSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                LoginResponse.Fournisseur livreur=new LoginResponse.Fournisseur();
                if (isChecked) {
                    livreur.setActif(true);
                    PutActifStatus(idfournisseur,livreur);
                } else {
                    livreur.setActif(false);
                    PutActifStatus(idfournisseur,livreur);
                }
            }
        });

        return view;
    }

    private void PutActifStatus(int id_Fournisseur, LoginResponse.Fournisseur Fournisseur) {
        Call<LoginResponse.Fournisseur> putpanier = ApiClient.getService().UpdateFournisseurDetails(id_Fournisseur, Fournisseur);
        putpanier.enqueue(new Callback<LoginResponse.Fournisseur>() {
            @Override
            public void onResponse(Call<LoginResponse.Fournisseur> call, Response<LoginResponse.Fournisseur> response) {
                if (response.isSuccessful()) {
                    String message = Fournisseur.isActif() ? "You are in service" : "You are out of service";
                    Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Fournisseur> call, Throwable t) {
                // Handle the error
            }
        });
    }

    /**
     * NOTE: this now takes the panier/order id as a second parameter.
     * Previously dataList only stored the display sentence, and the click
     * handlers below tried to Integer.parseInt() that sentence as if it
     * were an id -- which is what caused the NumberFormatException crash.
     * The id has to be captured here, at the point the row is built, and
     * carried alongside the text from then on.
     *
     * IMPORTANT: any other caller of getClientDetails(int) elsewhere in
     * the codebase (this method is public static, so it may be called
     * from other Activities/Fragments) must be updated to pass the
     * panier id too, or it will fail to compile.
     */
    public static void getClientDetails(int id_client, int panierId) {
        Call<LoginResponse.Client> getClientResponseCall = ApiClient.getService().GetClient(id_client);
        getClientResponseCall.enqueue(new Callback<LoginResponse.Client>() {
            @Override
            public void onResponse(Call<LoginResponse.Client> call, Response<LoginResponse.Client> response) {
                if (response.isSuccessful()) {
                    LoginResponse.Client getClientResponse = response.body();
                    if (getClientResponse != null) {

                        String clientname = getClientResponse.getUser().getUsername();

                        dataList.add(new OrderSummaryItem(
                                panierId,
                                "Le client " + clientname + " a lancé(e) une livraison"
                        ));
                        // Notifier l'adapter du changement dans la liste
                        Log.d("meli1", String.valueOf(FournisseurDashboardFragment.dataList.size()));
                        adapter.notifyItemInserted(dataList.size() - 1);
                    } else {
                        Log.d("empty", String.valueOf(FournisseurDashboardFragment.dataList.size()));
                    }
                } else {
                    Log.d("hereeeeeeeeeeeeee", String.valueOf(FournisseurDashboardFragment.dataList.size()));
                }
            }

            @Override
            public void onFailure(Call<LoginResponse.Client> call, Throwable t) {

            }
        });
    }


    public void getArticle(int id, int qte) {
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(id);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse = response.body();
                if (response.isSuccessful()) {
                    String name = getArticleResponse.getName();
                    String price = getArticleResponse.getPrice();
                    double price1 = Double.parseDouble(price);
                    double totalPrice = price1 * qte;
                    orderItems.add(new OrderLineItem(name, Integer.toString(qte), price + " DZD", 0));
                    adapter2.notifyItemInserted(adapter2.getItemCount() - 1);
                } else {
                    String message = "Error fetching article";
                }
            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {
                String message = "Error fetching article details: " + t.getMessage();
            }
        });
    }

    private void getPanierDetails(int panier) {
        Call<GetPanierResponse> getArticleResponseCall = ApiClient.getService().getPanierDetails(panier);
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse = response.body();
                if (getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison = getPanierResponse.getLigneLivraison();
                    for (DeliveryOrderLine orderItem : ligneLivraison) {
                        int articleID = orderItem.getArticle();
                        int qte = orderItem.getQuantity();
                        getArticle(articleID, qte);
                    }
                    Double total = getPanierResponse.getMontant();
                    totalPrice.setText(total + " DZD");
                } else {
                    String message = "Error fetching panier details";
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {
                String message = "Error fetching panier details: " + t.getMessage();
            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {

        if (dataList == null || position < 0 || position >= dataList.size()) {
            return; // stale click, ignore
        }

        // Capture the item up front. We use its stored panierId for every
        // API call below instead of re-parsing dataList.get(position),
        // which both avoids the NumberFormatException (display text is
        // not a number) and avoids re-reading a possibly-shifted index
        // later inside the button's onClick.
        final OrderSummaryItem clickedItem = dataList.get(position);

        Dialog dialog = new Dialog(getActivity());

        dialog.setContentView(R.layout.fournisseur_order_dialog);

        TextView textView_idord = dialog.findViewById(R.id.order);
        textView_idord.setText(clickedItem.displayText);

        totalPrice = dialog.findViewById(R.id.totalPriceView2);
        orderItems = new ArrayList<>();

        recyclerView = dialog.findViewById(R.id.recyclerview_order_dialog);
        adapter2 = new OrderItemAdapter(orderItems);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter2);

        getPanierDetails(clickedItem.panierId);

        Button accButton = dialog.findViewById(R.id.acceptBtn);

        accButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentIndex = dataList.indexOf(clickedItem);
                if (currentIndex < 0) {
                    // Item is no longer in the list (already handled/removed).
                    dialog.dismiss();
                    return;
                }

                PutLivraisonStatus status = new PutLivraisonStatus();
                status.setStatus("non_livreur");

                DeliveryPrice(clickedItem.panierId);
                status.updateOrderDetails(clickedItem.panierId, status);
                refusedLivreurIds.add(0);

                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < refusedLivreurIds.size(); i++) {
                    builder.append(refusedLivreurIds.get(i));
                    if (i < refusedLivreurIds.size() - 1) {
                        builder.append(",");
                    }
                }
                String listeStr = builder.toString();
                choixlivreur(clickedItem.panierId, idfournisseur, listeStr);
                dialog.dismiss();

                dataList.remove(currentIndex);
                adapter.notifyItemRemoved(currentIndex);
            }
        });

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
        dialog.show();
    }


    private void DeliveryPrice(int panier) {

        Call<DeliveryPrice> prixLivraisonCall = ApiClient.getService().DeliveryPrice( panier);
        prixLivraisonCall.enqueue(new Callback<DeliveryPrice>() {
            @Override
            public void onResponse(Call<DeliveryPrice> call, Response<DeliveryPrice> response) {
                if (response.isSuccessful()) {


                }
            }

            @Override
            public void onFailure(Call<DeliveryPrice> call, Throwable t) {

            }
        });

    }

    public void choixlivreur( int panier,int idfournisseur,String listeStr) {
        Call<SelectedLivreur> livreurselectionneCall = ApiClient.getService().SelectedLivreur(idfournisseur,listeStr,panier);
        livreurselectionneCall.enqueue(new Callback<SelectedLivreur>() {
            public void onResponse(Call<SelectedLivreur> call, Response<SelectedLivreur> response) {
                if (response.isSuccessful()) {
                    SelectedLivreur livreurselectionne = response.body();
                    PutPanier postPanierRequest = new PutPanier();
                    postPanierRequest.setLivreur(livreurselectionne.getLivreur());
                    postPanierRequest.setFournisseur(idfournisseur);

                    PutPanierDetails(postPanierRequest, panier);



                }
            }

            @Override
            public void onFailure(Call<SelectedLivreur> call, Throwable t) {

            }
        });


    }
    private void PutPanierDetails(PutPanier postPanierRequest, Integer panier) {
        Call<GetPanierResponse> putpanier = ApiClient.getService().PutPanierDetails(panier, postPanierRequest);
        putpanier.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                if (response.isSuccessful()) {


                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

}
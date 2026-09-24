package com.example.QuickDrop.ui.client.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.client.DeliveryAddressActivity;
import com.example.QuickDrop.ui.client.OrderSuccessActivity;
import com.example.QuickDrop.ui.client.adapters.CartItemAdapter;
import com.example.QuickDrop.ui.common.listeners.CartItemAdapterListener;
import com.example.QuickDrop.core.model.DeliveryOrderLine;
import com.example.QuickDrop.core.model.SelectedLivreur;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetPanierResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import com.example.QuickDrop.core.network.dto.PutPanier;
import static android.app.ProgressDialog.show;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CartFragment extends Fragment implements CartItemAdapterListener {
    RecyclerView rcqlproducts;

    TextView totalPrice,livraisonprix,total,totalorder,addresslivraison;
    CartItemAdapter adapter;
    Button btn,payer;
    private BottomSheetDialog bottomSheetDialog;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private View bottomSheetView;
    private int initialHeight ;
    ImageButton setaddress;
    TextView address;

    List<Integer> Fournisseursfinaux = new ArrayList<>();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        rcqlproducts = view.findViewById(R.id.cartrecycler);
        btn = view.findViewById(R.id.buttonCheckout);

        getPanier();
        totalPrice = view.findViewById(R.id.totalpriece);







        return view;
    }

    private void getPanier() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        String token = sharedPreferences.getString("accessToken", null);

        Call<GetPanierResponse> getPanierResponseCall = ApiClient.getService().GetPanier("Bearer " + token);
        getPanierResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                if (response.isSuccessful()) {
                    GetPanierResponse getPanierResponse = response.body();
                    int idPanier = getPanierResponse.getId();
                    getPanierDetails(idPanier);


                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

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
                    Double total = getPanierResponse.getMontant();
                    totalPrice.setText("" + total + " DZD ");
                    setAdapter(ligneLivraison, panier);

                } else {
                    String message = "not succesfull";
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

    private void setAdapter(List<DeliveryOrderLine> ligneLivraisons, Integer panier) {
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 1);
        rcqlproducts.setLayoutManager(layoutManager);
        CartItemAdapter cartItemAdapter = new CartItemAdapter(getContext(), ligneLivraisons, panier, Fournisseursfinaux,this);
        rcqlproducts.setAdapter(cartItemAdapter);

        if(cartItemAdapter.getItemCount()==0){
            btn.setClickable(false);




        }else {

            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

                    String token = sharedPreferences.getString("accessToken", null);
                    showDialog(token,panier);


                }


            });



    }}
    public void onCartItemDeleted(int panier) {

        Call<GetPanierResponse> getArticleResponseCall = ApiClient.getService().getPanierDetails(panier);
        getArticleResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                GetPanierResponse getPanierResponse = response.body();
                if (getPanierResponse != null) {
                    List<DeliveryOrderLine> ligneLivraison = getPanierResponse.getLigneLivraison();
                    Double total = getPanierResponse.getMontant();
                    totalPrice.setText("" + total + " DA ");
                } else {
                    String message = "not succesfull";
                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });



    }
    public void ligne(int panier){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String token = sharedPreferences.getString("accessToken", null);
        Call<List<GetPanierResponse>> cnfirmresponsecall=ApiClient.getService().ligne("Bearer " + token,panier);
        cnfirmresponsecall.enqueue(new Callback<List<GetPanierResponse>>() {
            @Override
            public void onResponse(Call<List<GetPanierResponse>> call, Response<List<GetPanierResponse>> response) {
                if(response.isSuccessful()){
                    List<GetPanierResponse> getPanierResponses = response.body();
                    if (getPanierResponses != null ) {
                        Intent intent=new Intent(getContext(), OrderSuccessActivity.class);
                        startActivity(intent);

                        String message ="youpi";

                    }

                }
                else {
                    String message ="not youpi";
                    Log.d("hiiii","not youpi");

                }
            }

            @Override
            public void onFailure(Call<List<GetPanierResponse>> call, Throwable t) {

            }
        });


    }

    public void showDialog(String token,int panier) {
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());

        View bottomSheetView = LayoutInflater.from(getContext()).inflate(R.layout.item_detail_livraison, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setCanceledOnTouchOutside(false);
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
                        if (newHeight > initialHeight) {
                            ViewGroup.LayoutParams layoutParams = bottomSheetView.getLayoutParams();
                            layoutParams.height = newHeight;
                            bottomSheetView.setLayoutParams(layoutParams);
                        }
                        break;
                }
                return true;
            }
        });

        TextView nameTextView = bottomSheetView.findViewById(R.id.addressLivrison);
        Button set=bottomSheetView.findViewById(R.id.modifieraddress);
        Button terminer=bottomSheetView.findViewById(R.id.dialog_terminer);

        Call<PutAdresseLivraisonResponse> adresseCall=ApiClient.getService().adresseinitial("Bearer "+token);
        adresseCall.enqueue(new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if(response.isSuccessful()){
                    PutAdresseLivraisonResponse adresse= response.body();
                    nameTextView.setText(adresse.getAdress());

                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        });
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Call<PutAdresseLivraisonResponse> clientResponseCall = ApiClient.getService().adresseinitial("Bearer "+ token);
                clientResponseCall.enqueue((new Callback<PutAdresseLivraisonResponse>() {
                    @Override
                    public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                        if (response.isSuccessful()) {
                            // Handle successful response
                            PutAdresseLivraisonResponse clientResponse = response.body();
                            // Process client data
                            String lat = String.valueOf(clientResponse.getLat());
                            String log =String.valueOf(clientResponse.getLng());

                            Intent intent=new Intent(getContext(), DeliveryAddressActivity.class);
                            intent.putExtra("latitude", lat);
                            intent.putExtra("longitude", log);

                            startActivity(intent);
                            dialog.dismiss();

                        }else{
                           
                            // No address set yet for this client: let them set one for
                            // the first time instead of doing nothing.
                            Intent intent = new Intent(getContext(), DeliveryAddressActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {
                        String message ="not successful Fournisseur"  ;


                    }
                }));


            }
        });

        terminer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                ligne(panier);



            }
        });







        dialog.show();
    }

    /**
    public void choixlivreur( int panier,int idfournisseur) {
            Call<SelectedLivreur> livreurselectionneCall = ApiClient.getService().SelectedLivreur(idfournisseur);
            livreurselectionneCall.enqueue(new Callback<SelectedLivreur>() {
                @Override
                public void onResponse(Call<SelectedLivreur> call, Response<SelectedLivreur> response) {
                    if (response.isSuccessful()) {
                        SelectedLivreur livreurselectionne = response.body();
                        PutPanier postPanierRequest = new PutPanier();
                        postPanierRequest.setLivreur(livreurselectionne.getLivreur());
                        postPanierRequest.setFournisseur(idfournisseur);

                        PutPanierDetails(postPanierRequest, panier);
                        Intent intent = new Intent(requireContext(), OrderSuccessActivity.class);
                        startActivity(intent);


                    }
                }

                @Override
                public void onFailure(Call<SelectedLivreur> call, Throwable t) {

                }
            });


    }
*/

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

    private void getPanierFinal(TextView livraisonprix,TextView total) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        String token = sharedPreferences.getString("accessToken", null);

        Call<GetPanierResponse> getPanierResponseCall = ApiClient.getService().GetPanier("Bearer " + token);
        getPanierResponseCall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {
                if (response.isSuccessful()) {
                    GetPanierResponse getPanierResponse = response.body();
                    int idPanier = getPanierResponse.getId();
                    livraisonprix.setText(String.valueOf(getPanierResponse.getPrix_livraison())+ "DA");
                    totalorder.setText(String.valueOf(getPanierResponse.getMontant())+ "DA");
                    total.setText(String.valueOf(getPanierResponse.getPrix_livraison()+getPanierResponse.getMontant())+ "DA");

                }
            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });
    }

    private void GetClientAddress(TextView addresslivraison ) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);


        int clientId = sharedPreferences.getInt("idclient", 0);


        Call<PutAdresseLivraisonResponse> clientResponseCall = ApiClient.getService().GetAdresseLivraison(clientId);
        clientResponseCall.enqueue((new Callback<PutAdresseLivraisonResponse>() {
            @Override
            public void onResponse(Call<PutAdresseLivraisonResponse> call, Response<PutAdresseLivraisonResponse> response) {
                if (response.isSuccessful()) {
                    PutAdresseLivraisonResponse clientResponse = response.body();

                    addresslivraison.setText(clientResponse.getAdress());






                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {

            }
        }));

        // Similar calls can be made for supplier details and any other data related to the order
    }
    private void putPanierValider(int panier,PutPanier putpanier){
        Call<GetPanierResponse> getpaniercall=ApiClient.getService().PutPanierDetails(panier,putpanier);
        getpaniercall.enqueue(new Callback<GetPanierResponse>() {
            @Override
            public void onResponse(Call<GetPanierResponse> call, Response<GetPanierResponse> response) {

            }

            @Override
            public void onFailure(Call<GetPanierResponse> call, Throwable t) {

            }
        });


    }
    private void navigateToMapsFragment() {
        // Créer une instance du fragment contenant la carte
        DeliveryTrackingFragment dashboardFragment = new DeliveryTrackingFragment();

        // Obtenir le gestionnaire de fragments et commencer une transaction
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Ajouter le fragment de la carte et cacher le fragment actuel
        fragmentTransaction.add(R.id.fragment_container, dashboardFragment, "MapsFragment");
        fragmentTransaction.hide(this);

        // Ajouter la transaction à la pile de retour pour permettre le retour
        fragmentTransaction.addToBackStack(null);

        // Valider la transaction
        fragmentTransaction.commit();
    }




}
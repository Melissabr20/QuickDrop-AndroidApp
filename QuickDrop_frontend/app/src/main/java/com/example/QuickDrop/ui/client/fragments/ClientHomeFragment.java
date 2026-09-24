package com.example.QuickDrop.ui.client.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.client.DeliveryAddressActivity;
import com.example.QuickDrop.ui.client.adapters.ArticleCardAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.PutAdresseLivraisonResponse;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientHomeFragment extends Fragment {
    RecyclerView rcqlproducts;
    String lat,log;
    ArticleCardAdapter cardViewAdapter;

    View view;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view= inflater.inflate(R.layout.fragment_home, container, false);
        rcqlproducts=view.findViewById(R.id.scrwProducts);

        GetArticle();
        SearchView searchView = view.findViewById(R.id.search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Filtre la liste d'articles avec le nouveau texte de recherche
                cardViewAdapter.filter(newText);
                return true;
            }
        });

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);

        String valeur = sharedPreferences.getString("username", "ziri");
        TextView textView = view.findViewById(R.id.textViewWelcomeMessage);
        textView.setText("Hello " + valeur );



        return view;

    }

    private void GetClientAddress( int clientId) {

        Call<PutAdresseLivraisonResponse> clientResponseCall = ApiClient.getService().GetAdresseLivraison(clientId);
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

                }
            }

            @Override
            public void onFailure(Call<PutAdresseLivraisonResponse> call, Throwable t) {
                String message ="not successful Fournisseur"  ;


            }
        }));


    }
    public void GetArticle() {


        Call<List<GetArticleResponse>> listArticle = ApiClient.getService().getarticledisponible();
        listArticle.enqueue(new Callback<List<GetArticleResponse>>() {
            @Override
            public void onResponse(Call<List<GetArticleResponse>> call, Response<List<GetArticleResponse>> response) {
                List<GetArticleResponse> getArticleResponses=response.body();
                if(getArticleResponses != null) {


                    setAdapter(getArticleResponses);

                }else {


                }

            }

            @Override
            public void onFailure(Call<List<GetArticleResponse>> call, Throwable t) {

            }
        });
    }

    private void setAdapter(List<GetArticleResponse> getArticleResponse) {
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        rcqlproducts.setLayoutManager(layoutManager);
        cardViewAdapter=new ArticleCardAdapter(getContext(),getArticleResponse);
        rcqlproducts.setAdapter(cardViewAdapter);
    }
}
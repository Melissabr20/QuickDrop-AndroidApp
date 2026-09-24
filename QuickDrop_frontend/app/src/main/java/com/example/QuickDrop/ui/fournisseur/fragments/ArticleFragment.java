package com.example.QuickDrop.ui.fournisseur.fragments;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.fournisseur.adapters.ArticleListAdapter;
import com.example.QuickDrop.ui.common.listeners.RecyclerViewClickListener;
import com.example.QuickDrop.core.model.CartArticleItem;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.GetListeProduitResponse;
import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.QuickDrop.databinding.FragmentArticleBinding;
import com.example.QuickDrop.databinding.FragmentHistoryBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleFragment extends Fragment implements RecyclerViewClickListener {

    private List<CartArticleItem> dataList, dataList2;
    private ArticleListAdapter adapter, adapter2;
    private String accessToken;
    private int id_article, idfournisseur, qte;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArticleBinding binding = FragmentArticleBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        accessToken = sharedPreferences.getString("accessToken", "");
        idfournisseur = sharedPreferences.getInt("idfournisseur", 0);

        RecyclerView recyclerView = binding.recyclerview2;
        dataList = new ArrayList<>();
        dataList2 = new ArrayList<>();

        adapter = new ArticleListAdapter(dataList, this);

        adapter2 = new ArticleListAdapter(dataList2, new RecyclerViewClickListener() {
            @Override
            public void recyclerViewListClicked(View v, int position) {
                // Handle click events
                // Show the dialog with integer input
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                LayoutInflater inflater = LayoutInflater.from(getContext());
                View dialogView = inflater.inflate(R.layout.dialog_input, null);
                builder.setView(dialogView);

                EditText inputInteger = dialogView.findViewById(R.id.input_integer);

                builder.setTitle("Veuillez entrer la quantité disponible dans le stock")
                        .setPositiveButton("OK", (dialog, which) -> {
                            String input = inputInteger.getText().toString();
                            if (!input.isEmpty()) {
                                int value = parseInt(input);
                                GetListeProduitResponse liste=new GetListeProduitResponse();
                                liste.setArticle(dataList2.get(position).getI());

                                liste.setFournisseur(idfournisseur);
                                liste.setStock_disponible(value);
                                Call<GetListeProduitResponse> listCall=ApiClient.getService().addlistproduits(liste);
                                listCall.enqueue(new Callback<GetListeProduitResponse>() {
                                    @Override
                                    public void onResponse(Call<GetListeProduitResponse> call, Response<GetListeProduitResponse> response) {
                                        if(response.isSuccessful()){
                                            dialog.dismiss();
                                            GetListeProduitResponse liste= response.body();
                                            String name=dataList2.get(position).getId();
                                            String disc=dataList2.get(position).getDisc();
                                            String prix=dataList2.get(position).getPrix();
                                            dataList.add(new CartArticleItem(liste.getArticle(),liste.getId(),name,Integer.toString(liste.getStock_disponible()),disc,prix));
                                            adapter.notifyDataSetChanged();

                                        }


                                    }

                                    @Override
                                    public void onFailure(Call<GetListeProduitResponse> call, Throwable t) {

                                    }
                                });

                            } else {
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        getOrderDetails(accessToken);

        ImageView img_add = view.findViewById(R.id.addBtn);

        // Log the value of img_add to help diagnose the issue
        if (img_add == null) {
            Log.e("ArticleFragment", "ImageView addBtn not found in the layout");
        } else {
            img_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ImageView", "Clicked!");
                    adapter2.changeLayout(R.layout.liste_article_a_choisir);
                    final Dialog dialog = new Dialog(requireContext());

                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.activity_fragment);

                    RecyclerView recyclerView2 = dialog.findViewById(R.id.recyclerview);

                    recyclerView2.setLayoutManager(new LinearLayoutManager(requireContext()));
                    recyclerView2.setAdapter(adapter2);

                    dialog.show();

                    Window window = dialog.getWindow();

                    if (window != null) {
                        window.setBackgroundDrawableResource(R.drawable.rounded_corners);

                        window.setLayout(
                                (int)(getResources().getDisplayMetrics().widthPixels * 0.90),
                                (int)(getResources().getDisplayMetrics().heightPixels * 0.80)
                        );
                    }

                    getAllArticles();

                    dialog.getWindow().setBackgroundDrawableResource(R.drawable.rounded_corners);
                    dialog.show();

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // Code to execute when the dialog is dismissed
                            dataList2.clear();
                            adapter.changeLayout(R.layout.item_article);
                            Log.d("Dialog", "Dialog was dismissed");
                        }
                    });
                }
            });
        }


        return view;
    }

    private void getOrderDetails(String accessToken) {
        String token = "Bearer " + accessToken;
        Call<List<GetListeProduitResponse>> getOrderResponseCall = ApiClient.getService().GetListeProduitbyfour(token);
        getOrderResponseCall.enqueue(new Callback<List<GetListeProduitResponse>>() {
            @Override
            public void onResponse(Call<List<GetListeProduitResponse>> call, Response<List<GetListeProduitResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetListeProduitResponse> getOrderResponses = response.body();
                    if (!getOrderResponses.isEmpty()) {
                        for (GetListeProduitResponse getOrderResponse : getOrderResponses) {
                            id_article = getOrderResponse.getArticle();
                            idfournisseur = getOrderResponse.getFournisseur();
                            qte = getOrderResponse.getStock_disponible();
                            getArticle(getOrderResponse.getId(),id_article, qte);
                        }
                    } else {
                    }
                } else {
                    String message = "Unsuccessful order: " + response.message();
                }
            }

            @Override
            public void onFailure(Call<List<GetListeProduitResponse>> call, Throwable t) {
                String message = "Error fetching order details: " + t.getMessage();
            }
        });
    }

    private void getArticle(int idliste,int id, int qte) {
        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(id);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    GetArticleResponse getArticleResponse = response.body();
                    String name = getArticleResponse.getName();
                    String price = getArticleResponse.getPrice();
                    String disc = getArticleResponse.getDesignation();
                    dataList.add(new CartArticleItem(id,idliste,name, Integer.toString(qte), disc, price + " DZD"));
                    adapter.notifyDataSetChanged();
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

    public  void getAllArticles(){
        Call<List<GetArticleResponse>> nonlisteproduitcall=ApiClient.getService().nonarticlefournisseur(idfournisseur);
        nonlisteproduitcall.enqueue(new Callback<List<GetArticleResponse>>() {
            @Override
            public void onResponse(Call<List<GetArticleResponse>> call, Response<List<GetArticleResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GetArticleResponse> getArticleResponse = new ArrayList<>();
                    getArticleResponse= response.body();

                    for (GetArticleResponse article:getArticleResponse) {
                        String name = article.getName();
                        String price = article.getPrice();
                        String disc = article.getDesignation();
                        dataList2.add(new CartArticleItem(article.getId(),0,name,"", disc, price + " DZD"));
                        adapter2.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onFailure(Call<List<GetArticleResponse>> call, Throwable t) {

            }
        });
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        // Handle item click events here
    }




}

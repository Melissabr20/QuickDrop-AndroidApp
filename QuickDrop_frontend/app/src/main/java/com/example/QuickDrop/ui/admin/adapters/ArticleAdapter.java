package com.example.QuickDrop.ui.admin.adapters;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.AdminArticleListActivity;
import com.example.QuickDrop.ui.admin.AdminAddEditArticleActivity;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import com.example.QuickDrop.core.util.FileUtils;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.CardViewHolder> {


    public interface OnImageSelectedListener {
        void onImageSelected(Uri imageUri);
    }

    private List<GetArticleResponse> getArticleResponses;
    private List<GetArticleResponse> getArticleResponsesFull;
    private Context context;
    private int initialHeight;
    RequestOptions option;


    String path;
    private OnImageSelectedListener onImageSelectedListener;

    public void setOnImageSelectedListener(OnImageSelectedListener listener) {
        this.onImageSelectedListener = listener;
    }

    public ArticleAdapter(Context context, List<GetArticleResponse> getArticleResponses) {
        this.context = context;
        this.getArticleResponses = getArticleResponses;
        this.getArticleResponsesFull = new ArrayList<>(getArticleResponses);
        option=new RequestOptions().centerCrop().error(R.drawable.img);
    }
    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_products, parent, false);
        return new CardViewHolder(view);

    }



    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position1) {
        int position=position1;
        GetArticleResponse article = getArticleResponses.get(position);
        holder.name.setText(article.getName());
        holder.price.setText(article.getPrice() + "DZD");
        holder.designation.setText(article.getDesignation());

        fetchImage(article.getId(),holder.img);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(context,AdminAddEditArticleActivity.class);
                intent.putExtra("idarticle",getArticleResponses.get(position).getId());
                context.startActivity(intent);

            }
        });

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delete(article,position);
                notifyDataSetChanged();


            }
        });

    }
    public void filter(String text) {
        List<GetArticleResponse> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(getArticleResponsesFull); // Utilisez la liste complète lorsque le texte est vide
        } else {
            for (GetArticleResponse article : getArticleResponses) {
                if (article.getName().toLowerCase().contains(text.toLowerCase()) ||
                        article.getDesignation().toLowerCase().contains(text.toLowerCase())) {
                    filteredList.add(article);
                }
            }
        }
        getArticleResponses.clear();
        getArticleResponses.addAll(filteredList);
        notifyDataSetChanged();
    }

    private void delete(GetArticleResponse article,int position){

        Call<GetArticleResponse> articleCall=ApiClient.getService().deletearticle(article.getId());
        articleCall.enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                if(response.isSuccessful()){
                    getArticleResponses.remove(position);
                    notifyDataSetChanged();
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getArticleResponses.size());
                    String message ="delete";
                    Toast.makeText(context,message,Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {

            }
        });


    }



    public void showDialog(GetArticleResponse getArticleResponse) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);

        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.activity_add_put_article, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setCanceledOnTouchOutside(true); // Permettre de fermer le dialog en cliquant en dehors
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

        TextView nameTextView = bottomSheetView.findViewById(R.id.prdname);
        TextView designationTextView = bottomSheetView.findViewById(R.id.prddesignation);
        TextView priceTextView = bottomSheetView.findViewById(R.id.prdprice);
        ImageView productImageView = bottomSheetView.findViewById(R.id.prdimg);


        nameTextView.setText(getArticleResponse.getName());
        designationTextView.setText(getArticleResponse.getDesignation());
        priceTextView.setText(getArticleResponse.getPrice());
        fetchImage( getArticleResponse.getId(),productImageView);

        ImageView addimg=bottomSheetView.findViewById(R.id.addimg);
        Button save = bottomSheetView.findViewById(R.id.save);
        addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Récupérer l'activité parente
                    Activity activity = (Activity) context;
                    if (activity instanceof AdminArticleListActivity) {
                        // Appeler la méthode pour gérer le résultat de l'action

                    }
                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }
            }
        });




        dialog.show();
    }


    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        ((Activity) context).startActivityForResult(intent, 10);

    }



    /**public void handleImageSelection(Uri selectedImageUri) {
        String path = FileUtils.handleUri(context,selectedImageUri);
        if (path != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(path);
        } else {
        }
    }*/

    private void fetchImage(int imageId,ImageView imageView) {
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        Call<ImageResponse> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getImageUrl();
                    Glide.with(context).load(imageUrl).apply(option).into(imageView);
                    Log.d("Fetch", imageUrl);
                } else {
                    Log.d("Fetch", "Failed to retrieve image");
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                Log.d("Fetch", "Error: " + t.getMessage());
            }
        });
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView name, price,designation;
        ImageButton delete ;

        ImageView img;


        public CardViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.prdimg);
            name = itemView.findViewById(R.id.prdname);
            price = itemView.findViewById(R.id.prdprice);
            designation =itemView.findViewById(R.id.prddesignation);
            delete =itemView.findViewById(R.id.delete_button);

        }

        public void bind(CardView cardView) {
            ViewGroup parent = (ViewGroup) cardView.getParent();
            if (parent != null) {
                parent.removeView(cardView);
            }
            ((ViewGroup) itemView).addView(cardView);
        }

    }

    @Override
    public int getItemCount() {

        if(getArticleResponses.size()>0){
            return getArticleResponses.size();
        }else {
            return 0;
        }
    }
}

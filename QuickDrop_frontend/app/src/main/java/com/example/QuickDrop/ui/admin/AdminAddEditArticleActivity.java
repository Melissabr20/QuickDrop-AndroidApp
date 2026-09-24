package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.ApiService;
import com.example.QuickDrop.core.network.RetrofitClientInstance;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
import com.example.QuickDrop.core.network.dto.ImageResponse;
import com.example.QuickDrop.core.util.FileUtils;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminAddEditArticleActivity extends AppCompatActivity {
    String path;
    Uri uri;
    int articleId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_put_article);


        TextView nameTextView = findViewById(R.id.prdname);
        TextView designationTextView = findViewById(R.id.prddesignation);
        TextView priceTextView = findViewById(R.id.prdprice);
        ImageView productImageView = findViewById(R.id.prdimg);

        Intent intent = getIntent();

        // Vérifier si des extras ont été envoyés avec l'intent
        if (intent != null) {
            // Extraire l'ID de l'article de l'intent
            articleId = intent.getIntExtra("idarticle", -1);
        }



        Call<GetArticleResponse> listArticle = ApiClient.getService().getArticleDetails(articleId);
        listArticle.enqueue(new Callback<GetArticleResponse>() {
            @Override
            public void onResponse(Call<GetArticleResponse> call, Response<GetArticleResponse> response) {
                GetArticleResponse getArticleResponse= response.body();
                if(response.isSuccessful()){
                    nameTextView.setText(getArticleResponse.getName());
                    designationTextView.setText(getArticleResponse.getDesignation());
                    priceTextView.setText(getArticleResponse.getPrice());
                    fetchImage( getArticleResponse.getId(),productImageView);
                }
            }
            @Override
            public void onFailure(Call<GetArticleResponse> call, Throwable t) {


            }
        });

        ImageView addimg=findViewById(R.id.addimg);
        addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 10);
                } else {
                    ActivityCompat.requestPermissions(AdminAddEditArticleActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
            }
        });
        Button save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameTextView.getText().toString();
                String designation = designationTextView.getText().toString();
                String price = priceTextView.getText().toString();

                if (path != null) {
                    File file = new File(path);

                    RequestBody fileReqBody = RequestBody.create(MediaType.parse("image/*"), file);

                    // Create MultipartBody.Part using file request-body, file name and part name
                    MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), fileReqBody);

                    RequestBody namePart = RequestBody.create(MultipartBody.FORM, name);
                    RequestBody designationPart = RequestBody.create(MultipartBody.FORM, designation);
                    RequestBody pricePart = RequestBody.create(MultipartBody.FORM, price);

                    Call<ResponseBody> call = ApiClient.getService().uploadArticle(namePart, designationPart, pricePart, part);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                AdminAddEditArticleActivity.this.finish();

                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });

                } else {
                    Log.e("AdminArticleListActivity", "Path is null");
                }
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    path = FileUtils.handleUri(this, uri);
                    if (bitmap != null) {
                        ImageView productImageView = findViewById(R.id.prdimg);
                        if (productImageView != null) {
                            productImageView.setImageBitmap(bitmap);
                        } else {
                            Log.e("AdminArticleListActivity", "ImageView is null");
                        }
                    } else {
                        Log.e("AdminArticleListActivity", "Bitmap is null");
                    }
                } catch (FileNotFoundException e) {
                    Log.e("AdminArticleListActivity", "File not found", e);
                }
            } else {
                Log.e("AdminArticleListActivity", "URI is null");
            }

        }
    }
    private void fetchImage(int imageId,ImageView imageView) {
        ApiService apiService = RetrofitClientInstance.getRetrofitInstance().create(ApiService.class);

        Call<ImageResponse> call = apiService.getImage(imageId);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getImageUrl();
                    Glide.with(AdminAddEditArticleActivity.this).load(imageUrl).into(imageView);
                    Log.d("Fetch", imageUrl);
                    Uri urii = Uri.parse(imageUrl);
                    path = FileUtils.handleUri(AdminAddEditArticleActivity.this,urii);
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

}

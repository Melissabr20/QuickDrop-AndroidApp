package com.example.QuickDrop.ui.admin;

import com.example.QuickDrop.R;
import com.example.QuickDrop.ui.admin.adapters.ArticleAdapter;
import com.example.QuickDrop.core.network.ApiClient;
import com.example.QuickDrop.core.network.dto.GetArticleResponse;
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
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import okhttp3.ResponseBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminArticleListActivity extends AppCompatActivity {
    RecyclerView rcqlproducts;
    ArticleAdapter artilces;
    String path;
    Uri uri;
    private int initialHeight;
    View bottomSheetView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles);
        rcqlproducts=findViewById(R.id.products);
        ImageView add=findViewById(R.id.addarticle);

        SearchView searchView = findViewById(R.id.search);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                artilces.filter(newText);
                return true;
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();

            }
        });
        GetArticle();

    }
    public void GetArticle() {


        Call<List<GetArticleResponse>> listArticle = ApiClient.getService().GetArticle();
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
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rcqlproducts.setLayoutManager(layoutManager);
        artilces = new ArticleAdapter(this,getArticleResponse);
        rcqlproducts.setAdapter(artilces);


    }



    public void showDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);

        bottomSheetView = LayoutInflater.from(this).inflate(R.layout.activity_add_put_article, null);
        bottomSheetView.setMinimumHeight(initialHeight);

        dialog.setContentView(bottomSheetView);

        BottomSheetBehavior<View> bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        dialog.setCanceledOnTouchOutside(true);
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
        ImageView addimg=bottomSheetView.findViewById(R.id.addimg);
        Button save = bottomSheetView.findViewById(R.id.save);
        addimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, 10);
                    Log.d("mss","did");
                } else {
                    ActivityCompat.requestPermissions(AdminArticleListActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
            }
        });


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
                                GetArticle();
                            }
                        }
                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {

                        }
                    });

                } else {
                    Log.e("AdminArticleListActivity", "Path is null");
                }
                dialog.dismiss();
            }
        });
        dialog.show();
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
                        ImageView productImageView = bottomSheetView.findViewById(R.id.prdimg);
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







}
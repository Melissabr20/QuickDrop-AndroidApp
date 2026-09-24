package com.example.QuickDrop.core.network;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    public static Retrofit getRetrofit() {

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor authInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();

                // Ajoute le token JWT à l'en-tête "Authorization" de la requête
                // Assurez-vous de remplacer "YOUR_TOKEN_HERE" par votre token JWT récupéré lors de la connexion
                Request newRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer YOUR_TOKEN_HERE")
                        .build();

                return chain.proceed(newRequest);
            }
        };

        OkHttpClient clientWithAuth = new OkHttpClient.Builder()
                .addInterceptor(httpLoggingInterceptor)
                .addInterceptor(authInterceptor)
                .build();
        Retrofit retrofitWithAuth = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://192.168.100.7:8000/")
                .client(clientWithAuth)
                .build();


        OkHttpClient okHttpClient = new OkHttpClient.Builder().addInterceptor(httpLoggingInterceptor).build();

        Retrofit retrofit=new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("http://192.168.100.7:8000/")
                .client(okHttpClient)
                .build();


        return retrofit;
    }
    public static ApiService getService(){
        ApiService apiservice =getRetrofit().create(ApiService.class);
        return apiservice;
    }
}

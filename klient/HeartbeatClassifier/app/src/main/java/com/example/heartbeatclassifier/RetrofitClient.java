package com.example.heartbeatclassifier;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // brez presledkov; uporabljamo port 80 in / kot baseUrl

    private static final String BASE_URL = "http://164.8.67.103/api/";


    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            OkHttpClient ok = new OkHttpClient.Builder()
                    .callTimeout(120, TimeUnit.SECONDS)   // za večje uploade
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // endpointi bodo začeli z "api/..."
                    .client(ok)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

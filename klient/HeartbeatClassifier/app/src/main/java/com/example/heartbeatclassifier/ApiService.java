package com.example.heartbeatclassifier;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // health check (če ga rabiš)
    @GET("api/")
    Call<ResponseBody> health();

    // pridobi zgodovino
    @GET("history")
    Call<List<Measurement>> getHistory();


    // pošlji .wav datoteko
    @Multipart
    @POST("api/upload")
    Call<ResponseBody> uploadFile(@Part MultipartBody.Part file);
}

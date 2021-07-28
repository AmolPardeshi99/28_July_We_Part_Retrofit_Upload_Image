package com.example.retrofit_3;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {


    @Headers("Authorization: Client-ID 322c898bb8cc046")
    @POST("3/image")
    @Multipart
    Call<ResponseDTO> uploadImage(
            @Part MultipartBody.Part file,
            @Part("title") String title
    );
}

package com.myapps.ron.family_recipes.network.S3;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface S3Interface {

    @PUT
    Call<Void> uploadImage(
            @Url String url,
            @Body RequestBody body
    );

    /*@Multipart
    @PUT
    Call<Void> uploadImage(
            @Url String url,
            @Part MultipartBody.Part filePart
    );*/
}

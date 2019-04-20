package com.myapps.ron.family_recipes.layout.S3;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.PUT;
import retrofit2.http.Url;

public interface S3Interface {

    @PUT
    Call<Void> uploadFile(
            @Url String url,
            @Body RequestBody body
    );

    /*@Multipart
    @PUT
    Call<Void> downloadFile(
            @Url String url,
            @Part MultipartBody.Part filePart
    );*/
}

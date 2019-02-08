package com.ayalus.exoplayer2example.Networking;

import android.util.JsonReader;

import com.ayalus.exoplayer2example.model.AccessTokenResponseModel;
import com.ayalus.exoplayer2example.model.ContentTokenResponseModel;
import com.ayalus.exoplayer2example.model.LicenseResponseModel;

import cz.msebera.android.httpclient.entity.mime.content.ByteArrayBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.OPTIONS;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiInterface {
    @POST("content_token")
    Call<ContentTokenResponseModel> getContentToken(@Query("content_id") String contentId,
                                                    @Query("type") String device,
                                                    @Header("Authorization") String authorization,
                                                    @Header("nv-tenant-id") String tenantId);

    @POST("token")
    Call<AccessTokenResponseModel> getAccessToken(
            @Query("grant_type") String grantType,
            @Query("username") String userName,
            @Query("password") String password,
            @Query("client_id") String clientId,
            @Header("tenantid") String tenantId,
            @Header("Content-Type") String contentType
    );

    @POST("EUTY3FMG/wvls/contentlicenseservice/v1/licenses")
    Call<ResponseBody> getContentLicense(
            @Header("nv-tenant-id") String tenantId,
            @Header("nv-authorizations") String authorization
    );
}
package com.macbar.batterygps.api;

import com.macbar.batterygps.Config;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface APIinterface {

    @POST(Config.SEND_URL_PATH)
    Call<String> stringPost(@Body String body);
}

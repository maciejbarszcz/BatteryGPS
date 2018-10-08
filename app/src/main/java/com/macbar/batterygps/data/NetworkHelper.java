package com.macbar.batterygps.data;

import com.macbar.batterygps.Config;
import com.macbar.batterygps.api.APIinterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NetworkHelper {

    private Retrofit mRetrofitClient = null;

    public NetworkHelper() {
        mRetrofitClient = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .baseUrl(Config.SEND_URL_AUTHORITY)
                .build();
    }

    public void sendStringOverHTTP(String stringToSend) {
        if(mRetrofitClient == null) {
            return;
        }

        APIinterface apIinterface = mRetrofitClient.create(APIinterface.class);
        Call<String> call = apIinterface.stringPost(stringToSend);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                //response not handled, according to project requirements
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                //error not handled, according to project requirements
            }
        });
    }
}

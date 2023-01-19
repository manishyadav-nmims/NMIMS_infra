package com.nmims.app.Services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitService {

    private static Retrofit retrofit = null;
    private static String BaseURL = "https://asmsoc-mum.timetable.svkm.ac.in:3000/";


    public static RetrofitInterface getService(){
        if (retrofit == null){
            retrofit = new Retrofit
                    .Builder()
                    .baseUrl(BaseURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(RetrofitInterface .class);
    }
}

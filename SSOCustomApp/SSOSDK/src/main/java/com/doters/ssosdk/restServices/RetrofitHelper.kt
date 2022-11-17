package com.doters.ssosdk.restServices

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitHelper {
    fun getInstance(APIurl: String): Retrofit {
        val baseUrl: String = APIurl+"/v1/"

        return Retrofit.Builder().baseUrl(baseUrl)
            //.addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}
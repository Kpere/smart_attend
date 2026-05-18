package com.example.smart_attend.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {
    // UPDATE THIS IP to your PC's current Wi-Fi IPv4 address whenever it changes
    // Run: ipconfig (Windows) or ifconfig (Mac/Linux) to find your Wi-Fi IP
    private static final String BASE_URL = "http://172.20.10.3:5000/";

    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)   // 30s to establish connection
                    .readTimeout(30, TimeUnit.SECONDS)       // 30s to read response
                    .writeTimeout(30, TimeUnit.SECONDS)      // 30s to send request
                    .retryOnConnectionFailure(true)          // auto-retry on failure
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    /**
     * Call this to force a new Retrofit instance (e.g. after IP change).
     */
    public static void resetInstance() {
        retrofit = null;
    }
}

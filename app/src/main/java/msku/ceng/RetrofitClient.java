package msku.ceng;

import msku.ceng.MovieApiService;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static MovieApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.themoviedb.org/3/")  // API'nin temel URL'si
                    .addConverterFactory(GsonConverterFactory.create())  // JSON'dan nesneye dönüştürme
                    .build();
        }
        return retrofit.create(MovieApiService.class);
    }
}

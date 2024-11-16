package msku.ceng;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MovieApiService {

    // Film arama isteği
    @GET("search/movie")
    Call<MovieSearchResponse> searchMovies(
            @Query("query") String query,
            @Query("api_key") String apiKey
            //@Query("include_adult") boolean includeAdult,
            //@Query("language") String language,
            //@Query("page") int page
    );
}

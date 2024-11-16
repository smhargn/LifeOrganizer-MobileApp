package msku.ceng;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieFragment extends Fragment {
    private RecyclerView searchResultsRecyclerView;
    private RecyclerView selectedMoviesRecyclerView;
    private MovieAdapter searchResultsAdapter;
    private SelectedMovieAdapter selectedMovieAdapter;
    private List<MovieSearchResponse.Movie> searchResultsList = new ArrayList<>();
    private List<MovieSearchResponse.Movie> selectedMovies = new ArrayList<>();

    private EditText searchEditText;  // Arama metni için EditText
    private ImageButton searchButton;  // Arama butonu

    private static final String API_KEY = "1ec5e66a0104a510518c6a37cd36cfcb"; // TMDb API anahtarınızı buraya ekleyin
    private MovieApiService movieApi;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        //RecyclerView selectedMoviesRecyclerView = view.findViewById(R.id.selectedMoviesRecyclerView);
        //selectedMoviesRecyclerView.setAdapter(selectedMovieAdapter);
        // RecyclerView ve Adapter'ı bağla
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        searchResultsAdapter = new MovieAdapter(searchResultsList, this::onMovieSelected);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);


        //selectedMovieAdapter = new SelectedMovieAdapter(selectedMovies);


        // Arama alanı ve buton referanslarını al
        searchEditText = view.findViewById(R.id.searchEditText);
        searchButton = view.findViewById(R.id.searchButton);

        // Retrofit yapılandırması
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.themoviedb.org/3/") // API base URL
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        movieApi = retrofit.create(MovieApiService.class);

        // Arama butonuna tıklama olayını ekle


        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim(); // Kullanıcının girdiği arama sorgusu
            if (!query.isEmpty()) {
                // Arama işlemini başlat
                searchMovies(query);
            } else {
                Toast.makeText(getContext(), "Lütfen geçerli bir film adı girin.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    public void onMovieSelected(MovieSearchResponse.Movie movie) {
        // Film seçildiğinde yapılacak işlemler
        Log.d("MovieFragment", "Film seçildi: " + movie.getTitle());
        // Burada örneğin film detay sayfasına yönlendirme yapılabilir
    }

    public void onSelectClicked(MovieSearchResponse.Movie movie) {
        // Seçilen filmi alt listeye ekleyin
        Log.d("MovieFragment : ","Seçildi iÇERDE");
        selectedMovies.add(movie);
        selectedMovieAdapter.notifyItemInserted(selectedMovies.size() - 1);

        // Seçilen film üzerindeki butonu kaldırmak isterseniz, arama listesinden de çıkarabilirsiniz
        //searchMovies.remove(movie);
        searchResultsAdapter.notifyDataSetChanged();
        selectedMovieAdapter.notifyDataSetChanged();

    }

    private void searchMovies(String query) {
        // API çağrısını yaparak arama sonuçlarını çekiyoruz
        Log.d("MovieFragment", "Arama yapılıyor: " + query);

        movieApi.searchMovies(query, API_KEY).enqueue(new Callback<MovieSearchResponse>() {
            @Override
            public void onResponse(Call<MovieSearchResponse> call, Response<MovieSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    searchResultsList.clear(); // Önceki sonuçları temizle
                    searchResultsList.addAll(response.body().getResults()); // API'den gelen filmleri listeye ekle
                    searchResultsAdapter.notifyDataSetChanged(); // Adapter'ı güncelle
                    Log.d("MovieFragment", "API'den gelen sonuçlar: " + response.body().getResults());
                } else {
                    Log.e("MovieFragment", "Hata: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MovieSearchResponse> call, Throwable t) {
                Log.e("MovieFragment", "API çağrısı başarısız oldu: " + t.getMessage());
            }
        });
    }
}

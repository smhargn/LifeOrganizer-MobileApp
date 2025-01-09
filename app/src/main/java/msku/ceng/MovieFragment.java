package msku.ceng;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import msku.ceng.repository.MovieRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieFragment extends Fragment implements MovieSearchFragment.MovieSelectionListener {
    private RecyclerView moviesRecyclerView;
    private SelectedMovieAdapter movieAdapter;
    private List<MovieSearchResponse.Movie> watchList = new ArrayList<>();
    private List<MovieSearchResponse.Movie> watchedMovies = new ArrayList<>();
    private Button watchListButton;
    private Button watchedButton;
    private FloatingActionButton addMovieButton;
    private Button popularMoviesButton;
    private boolean showingWatchList = true;
    private MovieRepository movieRepository;
    private ImageView emptymovies;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        movieRepository = new MovieRepository();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movie, container, false);

        moviesRecyclerView = view.findViewById(R.id.moviesRecyclerView);
        movieAdapter = new SelectedMovieAdapter(new ArrayList<>(), this::onMovieWatchedStatusChanged);
        moviesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        moviesRecyclerView.setAdapter(movieAdapter);

        addMovieButton = view.findViewById(R.id.addMovieButton);
        watchListButton = view.findViewById(R.id.watchListButton);
        watchedButton = view.findViewById(R.id.watchedButton);
        popularMoviesButton = view.findViewById(R.id.popularMoviesButton);
        emptymovies = view.findViewById(R.id.emptymovie);

        if (addMovieButton != null) {
            addMovieButton.setOnClickListener(v -> showMovieSearchFragment());
        }
        if (popularMoviesButton != null) {
            popularMoviesButton.setOnClickListener(v -> showPopularMovies());
        }
        if (watchListButton != null) {
            watchListButton.setOnClickListener(v -> showWatchList());
        }
        if (watchedButton != null) {
            watchedButton.setOnClickListener(v -> showWatchedMovies());
        }
        fetchMovies();
        showWatchList();
        checkEmptyState();
        return view;
    }

    private void showWatchList() {
        showingWatchList = true;
        movieAdapter.updateMovies(new ArrayList<>(watchList));
        updateButtonStyles();
        checkEmptyState();
    }

    private void showWatchedMovies() {
        showingWatchList = false;
        movieAdapter.updateMovies(new ArrayList<>(watchedMovies));
        updateButtonStyles();
        checkEmptyState();
    }

    private void updateButtonStyles() {
        watchListButton.setEnabled(!showingWatchList);
        watchedButton.setEnabled(showingWatchList);
    }

    private void checkEmptyState() {
        List<MovieSearchResponse.Movie> currentList = showingWatchList ? watchList : watchedMovies;

        if (currentList.isEmpty()) {
            moviesRecyclerView.setVisibility(View.GONE);
            emptymovies.setVisibility(View.VISIBLE);
        } else {
            moviesRecyclerView.setVisibility(View.VISIBLE);
            emptymovies.setVisibility(View.GONE);
        }
    }



    private void onMovieWatchedStatusChanged(MovieSearchResponse.Movie movie, boolean isWatched) {
        if (isWatched) {
            watchList.remove(movie);
            if (!watchedMovies.contains(movie)) {
                watchedMovies.add(movie);
            }
        } else {
            watchedMovies.remove(movie);
            if (!watchList.contains(movie)) {
                watchList.add(movie);
            }
        }

        // Update the current view
        if (showingWatchList) {
            movieAdapter.updateMovies(new ArrayList<>(watchList));
        } else {
            movieAdapter.updateMovies(new ArrayList<>(watchedMovies));
        }
    }

    private void showMovieSearchFragment() {
        MovieSearchFragment searchFragment = new MovieSearchFragment();
        searchFragment.setMovieSelectionListener(this);

        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onMovieSelected(MovieSearchResponse.Movie movieResponse) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
//        if (!watchList.contains(movie) && !watchedMovies.contains(movie)) {
//            watchList.add(movie);
//            if (showingWatchList) {
//                movieAdapter.updateMovies(new ArrayList<>(watchList));
//            }
//        }

        Movie movie = new Movie(
                movieResponse.getId(),
                movieResponse.getTitle(),
                movieResponse.getReleaseDate(),
                movieResponse.getPosterPath(),
                movieResponse.getOverview(),
                false
        );

        movieRepository.addMovie(userId, movie)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Movie added successfully", Toast.LENGTH_SHORT).show();
                    if (!watchList.contains(movieResponse) && !watchedMovies.contains(movieResponse)) {
                        watchList.add(movieResponse);
                        if (showingWatchList) {
                            movieAdapter.updateMovies(new ArrayList<>(watchList));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error adding movie: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchMovies() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        movieRepository.getUserMovies(userId)
                .addOnSuccessListener(querySnapshot -> {
                    watchList.clear();
                    watchedMovies.clear();

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Movie movie = document.toObject(Movie.class);
                        if (movie != null) {
                            if (movie.isWatched()) {
                                watchedMovies.add(convertToMovieResponse(movie));
                            } else {
                                watchList.add(convertToMovieResponse(movie));
                            }
                        }
                    }

                    if (showingWatchList) {
                        movieAdapter.updateMovies(new ArrayList<>(watchList));
                    } else {
                        movieAdapter.updateMovies(new ArrayList<>(watchedMovies));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching movies: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show()
                );
    }

    private MovieSearchResponse.Movie convertToMovieResponse(Movie movieData) {
        // Convert MovieData to MovieSearchResponse.Movie
        MovieSearchResponse.Movie movie = new MovieSearchResponse.Movie(
                movieData.getId(),
                movieData.getTitle(),
                movieData.getReleaseDate(),
                movieData.getPosterPath(),
                movieData.getOverview(),
                false
        );
        movie.setWatched(movieData.isWatched());
        return movie;
    }

    private void showPopularMovies() {
        Log.d("showPopularMovies", "Method called");
        MovieSearchFragment searchFragment = new MovieSearchFragment();
        Bundle args = new Bundle();
        args.putBoolean("isPopularMovies", true);
        searchFragment.setArguments(args);
        searchFragment.setMovieSelectionListener(this);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, searchFragment)
                .addToBackStack(null)
                .commit();
    }


}
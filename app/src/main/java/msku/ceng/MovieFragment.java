package msku.ceng;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MovieFragment extends Fragment implements MovieSearchFragment.MovieSelectionListener {
    private RecyclerView moviesRecyclerView;
    private SelectedMovieAdapter movieAdapter;
    private List<MovieSearchResponse.Movie> watchList = new ArrayList<>();
    private List<MovieSearchResponse.Movie> watchedMovies = new ArrayList<>();
    private Button addMovieButton;
    private Button watchListButton;
    private Button watchedButton;
    private boolean showingWatchList = true;

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

        addMovieButton.setOnClickListener(v -> showMovieSearchFragment());
        watchListButton.setOnClickListener(v -> showWatchList());
        watchedButton.setOnClickListener(v -> showWatchedMovies());

        showWatchList(); // Başlangıçta Watch List'i göster
        return view;
    }

    private void showWatchList() {
        showingWatchList = true;
        movieAdapter.updateMovies(new ArrayList<>(watchList));
        updateButtonStyles();
    }

    private void showWatchedMovies() {
        showingWatchList = false;
        movieAdapter.updateMovies(new ArrayList<>(watchedMovies));
        updateButtonStyles();
    }

    private void updateButtonStyles() {
        watchListButton.setEnabled(!showingWatchList);
        watchedButton.setEnabled(showingWatchList);
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

        // Güncel listeyi göster
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
    public void onMovieSelected(MovieSearchResponse.Movie movie) {
        if (!watchList.contains(movie) && !watchedMovies.contains(movie)) {
            watchList.add(movie);
            if (showingWatchList) {
                movieAdapter.updateMovies(new ArrayList<>(watchList));
            }
        }
    }
}
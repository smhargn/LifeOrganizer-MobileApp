package msku.ceng;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

import msku.ceng.repository.MovieRepository;

public class SelectedMovieAdapter extends RecyclerView.Adapter<SelectedMovieAdapter.SelectedMovieViewHolder> {
    private List<MovieSearchResponse.Movie> movies;
    private final OnMovieWatchedListener watchedListener;
    private final MovieRepository movieRepository;

    public interface OnMovieWatchedListener {
        void onMovieWatchedStatusChanged(MovieSearchResponse.Movie movie, boolean isWatched);

    }

    public SelectedMovieAdapter(List<MovieSearchResponse.Movie> movies, OnMovieWatchedListener listener) {
        this.movies = new ArrayList<>(movies);
        this.watchedListener = listener;
        this.movieRepository = new MovieRepository();
    }

    public void updateMovies(List<MovieSearchResponse.Movie> newMovies) {
        this.movies = new ArrayList<>(newMovies);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelectedMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_movie, parent, false);
        return new SelectedMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedMovieViewHolder holder, int position) {
        MovieSearchResponse.Movie movie = movies.get(position);

        holder.titleTextView.setText(movie.getTitle());
        holder.dateTextView.setText(movie.getReleaseDate());

        holder.watchedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) return;

            String userId = currentUser.getUid();
            movie.setWatched(isChecked);

            movieRepository.updateMovieWatchStatus(userId, movie.getId(), isChecked)
                    .addOnSuccessListener(aVoid -> {
                        if (watchedListener != null) {
                            watchedListener.onMovieWatchedStatusChanged(movie, isChecked);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Revert checkbox state on failure
                        holder.watchedCheckbox.setChecked(!isChecked);
                        Toast.makeText(holder.itemView.getContext(),
                                "Failed to update status: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });

        holder.watchedCheckbox.setChecked(movie.isWatched());

        holder.deleteButton.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Log.d("DeleteMovie", "Current user is null. Exiting.");
                return;
            }

            String userId = currentUser.getUid();
            Log.d("DeleteMovie", "Current user ID: " + userId);

            MovieSearchResponse.Movie movieToDelete = movies.get(holder.getAdapterPosition());
            Log.d("DeleteMovie", "Movie to delete: " + movieToDelete.getId());

            movieRepository.deleteMovie(userId, movieToDelete.getId())
                    .addOnSuccessListener(aVoid -> {
                        Log.d("DeleteMovie", "Movie deletion successful.");
                        int adapterPosition = holder.getAdapterPosition();
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            Log.d("DeleteMovie", "Removing movie from position: " + adapterPosition);
                            movies.remove(adapterPosition);
                            notifyItemRemoved(adapterPosition);

                            if (watchedListener != null) {
                                Log.d("DeleteMovie", "Calling watchedListener with status change.");
                                watchedListener.onMovieWatchedStatusChanged(movieToDelete, false);
                            }

                            Toast.makeText(holder.itemView.getContext(),
                                    "Movie deleted successfully", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.d("DeleteMovie", "Failed to delete movie: " + e.getMessage());
                        Toast.makeText(holder.itemView.getContext(),
                                "Failed to delete movie: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });


        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .into(holder.posterImageView);
        }

        if (holder.summaryTextView != null) {
            holder.summaryTextView.setText(movie.getOverview());
        }

        holder.expandedView.setVisibility(movie.isExpanded() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            movie.setExpanded(!movie.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });

    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class SelectedMovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView, summaryTextView;
        ImageView posterImageView;
        ImageButton deleteButton;
        CheckBox watchedCheckbox;
        View expandedView;

        SelectedMovieViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movieTitle);
            dateTextView = itemView.findViewById(R.id.movieDate);
            posterImageView = itemView.findViewById(R.id.moviePoster);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            watchedCheckbox = itemView.findViewById(R.id.watchedCheckbox);
            expandedView = itemView.findViewById(R.id.expandedView);
            summaryTextView = itemView.findViewById(R.id.movieSummary);
        }
    }
}
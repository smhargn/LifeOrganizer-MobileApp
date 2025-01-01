package msku.ceng;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;

public class SelectedMovieAdapter extends RecyclerView.Adapter<SelectedMovieAdapter.SelectedMovieViewHolder> {
    private List<MovieSearchResponse.Movie> movies;
    private final OnMovieWatchedListener watchedListener;

    public interface OnMovieWatchedListener {
        void onMovieWatchedStatusChanged(MovieSearchResponse.Movie movie, boolean isWatched);
    }

    public SelectedMovieAdapter(List<MovieSearchResponse.Movie> movies, OnMovieWatchedListener listener) {
        this.movies = new ArrayList<>(movies);
        this.watchedListener = listener;
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

        // CheckBox listener'ı temizle ve yeni durumu ayarla
        holder.watchedCheckbox.setOnCheckedChangeListener(null);
        holder.watchedCheckbox.setChecked(movie.isWatched());

        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            Picasso.get()
                    .load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                    .into(holder.posterImageView);
        }

        // Yeni listener'ı ekle
        holder.watchedCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            movie.setWatched(isChecked);
            if (watchedListener != null) {
                watchedListener.onMovieWatchedStatusChanged(movie, isChecked);
            }
        });

        if (holder.summaryTextView != null) {
            holder.summaryTextView.setText(movie.getOverview());
        }

        holder.expandedView.setVisibility(movie.isExpanded() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            movie.setExpanded(!movie.isExpanded());
            notifyItemChanged(holder.getAdapterPosition());
        });

        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && adapterPosition < movies.size()) {
                MovieSearchResponse.Movie movieToRemove = movies.get(adapterPosition);
                movies.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                if (watchedListener != null) {
                    watchedListener.onMovieWatchedStatusChanged(movieToRemove, false);
                }
            }
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
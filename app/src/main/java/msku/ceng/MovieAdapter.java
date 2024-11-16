package msku.ceng;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import msku.ceng.MovieSearchResponse;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private List<MovieSearchResponse.Movie> movieList;
    private OnMovieClickListener movieClickListener;

    // Adapter constructor
    public MovieAdapter(List<MovieSearchResponse.Movie> movieList, OnMovieClickListener listener) {
        this.movieList = movieList;
        this.movieClickListener = listener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater işlemi burada yapılacak
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_movie, parent, false);
        return new MovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieSearchResponse.Movie movie = movieList.get(position);
        holder.titleTextView.setText(movie.getTitle());
        holder.releaseDateTextView.setText(movie.getReleaseDate());
        holder.summaryTextView.setText(movie.getOverview());

        // Poster yükle (Picasso veya Glide kullanabilirsiniz)
        Picasso.get().load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath())
                .into(holder.posterImageView);

        // Genişletilmiş durumu ayarla
        boolean isExpanded = movie.isExpanded();
        holder.expandedView.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        // Tıklama olayı
        holder.itemView.setOnClickListener(v -> {
            movie.setExpanded(!movie.isExpanded());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    public interface OnMovieClickListener {
        void onMovieSelected(MovieSearchResponse.Movie movie);
    }

    public interface OnSelectClickListener {
        void onSelectClicked(MovieSearchResponse.Movie movie);
    }

    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, releaseDateTextView,summaryTextView;
        ImageView posterImageView;
        Button selectButton;
        View expandedView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movieTitle);
            releaseDateTextView = itemView.findViewById(R.id.movieReleaseDate);
            posterImageView = itemView.findViewById(R.id.moviePoster);
            summaryTextView = itemView.findViewById(R.id.movieSummary);
            expandedView = itemView.findViewById(R.id.expandedView);
            selectButton = itemView.findViewById(R.id.selectMovieButton);
        }
    }
}

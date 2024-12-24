package msku.ceng;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class SelectedMovieAdapter extends RecyclerView.Adapter<SelectedMovieAdapter.SelectedMovieViewHolder> {

    private List<MovieSearchResponse.Movie> selectedMovies;

    public SelectedMovieAdapter(List<MovieSearchResponse.Movie> selectedMovies) {
        this.selectedMovies = selectedMovies;
    }

    @NonNull
    @Override
    public SelectedMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_movie, parent, false);
        return new SelectedMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedMovieViewHolder holder, int position) {
        MovieSearchResponse.Movie movie = selectedMovies.get(position);

        holder.titleTextView.setText(movie.getTitle());
        holder.releaseDateTextView.setText(movie.getReleaseDate());
        Picasso.get().load("https://image.tmdb.org/t/p/w500" + movie.getPosterPath()).into(holder.posterImageView);
    }

    @Override
    public int getItemCount() {
        return selectedMovies.size();
    }

    public static class SelectedMovieViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, releaseDateTextView;
        ImageView posterImageView;

        public SelectedMovieViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.movieTitle);
            releaseDateTextView = itemView.findViewById(R.id.movieReleaseDate);
            posterImageView = itemView.findViewById(R.id.moviePoster);
        }
    }
}

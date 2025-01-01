package msku.ceng;
import java.util.List;
import java.util.Objects;

public class MovieSearchResponse {
    private List<Movie> results;

    public List<Movie> getResults() {
        return results;
    }

    public void setResults(List<Movie> results) {
        this.results = results;
    }

    public static class Movie {
        private String title;
        private String release_date;
        private String poster_path;
        private String overview;
        private boolean isExpanded;
        private boolean isWatched;

        public Movie(String title, String release_date, String poster_path, String overview, boolean isExpanded) {
            this.title = title;
            this.release_date = release_date;
            this.poster_path = poster_path;
            this.overview = overview;
            this.isExpanded = isExpanded;
            this.isWatched = false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Movie movie = (Movie) o;
            return Objects.equals(title, movie.title) &&
                    Objects.equals(release_date, movie.release_date);
        }

        @Override
        public int hashCode() {
            return Objects.hash(title, release_date);
        }

        public boolean isWatched() {
            return isWatched;
        }

        public void setWatched(boolean watched) {
            isWatched = watched;
        }

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        public String getOverview() {
            return overview;
        }

        public String getTitle() {
            return title;
        }

        public String getReleaseDate() {
            return release_date;
        }

        public String getPosterPath() {
            return poster_path;
        }
    }
}
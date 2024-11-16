package msku.ceng;
import java.util.List;

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

        public boolean isExpanded() {
            return isExpanded;
        }

        public void setExpanded(boolean expanded) {
            isExpanded = expanded;
        }

        private boolean isExpanded;


        public Movie(String title, String release_date, String poster_path,String overview,boolean isExpanded) {
            this.title = title;
            this.release_date = release_date;
            this.poster_path = poster_path;
            this.overview = overview;
            this.isExpanded = isExpanded;
        }
        public String getOverview(){
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

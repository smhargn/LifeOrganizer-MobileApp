package msku.ceng;

import java.util.ArrayList;

public class Movie {
    private String title,id;
    private String releaseDate;
    private String posterPath;
    private String overview;
    private boolean isExpanded;
    private boolean isWatched;

    public Movie() {
        //  Firestore

    }


    public String getId() {return id;}

    public void setId(String id) {this.id = id;}

    public Movie(String id, String title, String releaseDate, String posterPath, String overview, boolean isExpanded) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.overview = overview;
        this.isExpanded = isExpanded;
        this.isWatched = false;
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

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public boolean isWatched() {return isWatched;}

    public void setWatched(boolean watched) {isWatched = watched;}
}

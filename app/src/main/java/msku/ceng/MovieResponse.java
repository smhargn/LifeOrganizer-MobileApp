package msku.ceng;

import java.util.List;

import msku.ceng.model.Movie;

public class MovieResponse {
    private List<Movie> Search;

    public List<Movie> getSearch() {

        return Search;
    }

    public void setSearch(List<Movie> search) {

        Search = search;
    }
}
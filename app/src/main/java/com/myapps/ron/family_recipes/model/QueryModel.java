package com.myapps.ron.family_recipes.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by ronginat on 05/01/2019.
 *
 * Specifies search search from recipes db and order by key.
 *
 * Help to Query the repository
 * {@link #orderBy} order of the items to be fetched
 * {@link #search} search a base LIKE search with string.
 * {@link #filters} filters a complex LIKE search that searching for array containing this array
 * {@link #favorites} is querying for favorites
 */
public class QueryModel implements Serializable {

    private boolean favorites = false;
    private String search, orderBy;
    private List<String> filters;// = new ArrayList<>();

    private QueryModel() {}

    public boolean isFavorites() {
        return favorites;
    }

    public String getSearch() {
        return search;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public List<String> getFilters() {
        return filters;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public void setFavorites(boolean favorites) {
        this.favorites = favorites;
    }

    public String getSQLSearch() {
        return wrapQueryWithPercent(search);
    }

    public String getSQLFilters() {
        if (filters != null && !filters.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (String c : filters) {
                builder.append(wrapQueryWithPercent(c));
            }
            return builder.toString();
        }
        return wrapQueryWithPercent(null);
    }

    private String wrapQueryWithPercent(String query) {
        if (query != null && !"".equals(query))
            return "%" + query + "%";
        return "%";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryModel that = (QueryModel) o;
        return favorites == that.favorites &&
                Objects.equals(search, that.search) &&
                Objects.equals(orderBy, that.orderBy) &&
                Objects.equals(filters, that.filters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(favorites, search, orderBy, filters);
    }

    public static class Builder {
        private String builderQuery;
        private String builderOrder;
        private List<String> builderFilters;
        private boolean builderFavorites;

        public Builder() {
            this.builderFavorites = false;
        }

        public QueryModel.Builder order(String order) {
            this.builderOrder = order;
            return this;
        }

        public QueryModel.Builder search(String query) {
            this.builderQuery = query;
            return this;
        }

        public QueryModel.Builder filters(List<String> filters){
            this.builderFilters = filters;
            return this;
        }

        public QueryModel.Builder favorites(boolean favorites){
            this.builderFavorites = favorites;
            return this;
        }

        public QueryModel build() {
            if (builderOrder != null) {
                QueryModel queryModel = new QueryModel();

                queryModel.orderBy = builderOrder;
                queryModel.search = builderQuery;
                queryModel.filters = builderFilters;
                queryModel.favorites = builderFavorites;

                return queryModel;
            }

            throw new IllegalArgumentException("QueryModel must contain orderBy value!");
        }
    }

}

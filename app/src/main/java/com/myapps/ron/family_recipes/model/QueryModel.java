package com.myapps.ron.family_recipes.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 05/01/2019.
 *
 * Specifies search query from recipes db and order by key.
 */
public class QueryModel {

    private String query, orderBy;
    private List<String> categories = new ArrayList<>();

    public QueryModel(String query, String orderBy) {
        this.query = query;
        this.orderBy = orderBy;
    }

    public QueryModel(String query, String orderBy, List<String> categories) {
        this(query, orderBy);
        this.categories = categories;
    }

    public String getQuery() {
        return query;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public List<String> getCategories() {
        return categories;
    }
}

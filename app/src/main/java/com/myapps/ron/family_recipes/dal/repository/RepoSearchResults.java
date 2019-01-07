package com.myapps.ron.family_recipes.dal.repository;

import com.myapps.ron.family_recipes.model.RecipeMinimal;

import androidx.lifecycle.LiveData;
import androidx.paging.PagedList;

/**
 * Created by ronginat on 05/01/2019.
 *
 * RepoSearchResult from a search, which contains LiveData<PagedList<Repo>> holding search data
 */
public class RepoSearchResults {

    //LiveData for Search Results
    private final LiveData<PagedList<RecipeMinimal>> data;

    RepoSearchResults(LiveData<PagedList<RecipeMinimal>> data) {
        this.data = data;
    }

    public LiveData<PagedList<RecipeMinimal>> getData() {
        return data;
    }
}

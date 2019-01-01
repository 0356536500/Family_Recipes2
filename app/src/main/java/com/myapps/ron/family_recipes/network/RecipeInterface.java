package com.myapps.ron.family_recipes.network;

import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.model.Category;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;

import retrofit2.http.Header;
import retrofit2.http.HeaderMap;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RecipeInterface {

    /*String recipesGET = Constants.URL_RECIPES + "/{" + Constants.DATE_QUERY + "}";
    String recipesPOST = Constants.URL_RECIPES;
    String recipesUpdate = Constants.URL_RECIPES + "/{" + Constants.ID_QUERY + "}";*/
    String oneRecipe = Constants.URL_RECIPES + "/{" + Constants.ID_QUERY + "}";
    String recipes = Constants.URL_RECIPES;
    String categories = Constants.URL_CATEGORIES;// + "/{" + Constants.DATE_QUERY + "}";
    String food = Constants.URL_FOOD;


    @GET(oneRecipe)
    Call<RecipeTO> getOneRecipe(
            @Header(Constants.AUTHORIZATION) String token,
            @Path(Constants.ID_QUERY) String id
    );
    //region recipes
    @GET(recipes)
    Call<List<RecipeTO>> getAllRecipes(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date
    );

    @GET(recipes)
    Call<List<RecipeTO>> getAllRecipesPagination(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date,
            @Query(Constants.EXCLUSIVE_START_KEY_QUERY) String startKey,
            @Query(Constants.LIMIT_QUERY) int limit
    );

    @POST(recipes)
    Call<JsonObject> postPendRecipe(
            @HeaderMap Map<String, String> headers,
            @Body Map<String, Object> body
    );

    @PATCH(recipes)
    Call<RecipeTO> patchRecipe(
            @HeaderMap Map<String, String> headers,
            @Query(Constants.ID_QUERY) String id,
            @Body Map<String, Object> body
    );

    //endregion

    //region categories
    @GET(categories)
    Call<List<Category>> getAllCategories(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date
    );

    //endregion

    //region food
    @PUT(food)
    Call<List<String>> requestFoodUrls(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.ID_QUERY) String id,
            @Body Map<String, String> body
    );

    //endregion


    /*@GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);*/
}

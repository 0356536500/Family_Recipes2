package com.myapps.ron.family_recipes.network;

import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.model.Category;

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


public interface RecipeInterface {

    String recipesGET = Constants.URL_RECIPES + "/{" + Constants.DATE_QUERY + "}";
    String recipesPOST = Constants.URL_RECIPES;
    String recipesUpdate = Constants.URL_RECIPES + "/{" + Constants.ID_QUERY + "}";
    String categories = Constants.URL_CATEGORIES + "/{" + Constants.DATE_QUERY + "}";
    String food = Constants.URL_FOOD;


    /*@GET(food)
    Call<RecipeTO> getOneRecipe(
            @Header(Constants.AUTHORIZATION) String token);*/
    //region recipes
    @GET(recipesGET)
    Call<List<RecipeTO>> getAllRecipes(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.DATE_QUERY) String date
    );

    @POST(recipesPOST)
    Call<JsonObject> postPendRecipe(
            @HeaderMap Map<String, String> headers,
            @Body Map<String, Object> body
    );

    @PATCH(recipesUpdate)
    Call<RecipeTO> patchRecipe(
            @HeaderMap Map<String, String> headers,
            @Path(Constants.ID_QUERY) String id,
            @Body Map<String, Object> body
    );

    //endregion

    //region categories
    @GET(categories)
    Call<List<Category>> getAllCategories(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.DATE_QUERY) String date
    );

    //endregion

    //region food
    @PUT(food)
    Call<List<String>> requestFoodUrls(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.ID_QUERY) String id,
            @Body Map<String, String> body
    );

    //endregion


    /*@GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);*/
}

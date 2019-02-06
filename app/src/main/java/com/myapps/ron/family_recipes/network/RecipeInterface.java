package com.myapps.ron.family_recipes.network;

import com.google.gson.JsonObject;
import com.myapps.ron.family_recipes.network.modelTO.CategoryTO;
import com.myapps.ron.family_recipes.network.modelTO.CommentTO;
import com.myapps.ron.family_recipes.network.modelTO.RecipeTO;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
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
    String recipeWithID = Constants.URL_RECIPES + "/{" + Constants.ID_QUERY + "}";
    String recipes = Constants.URL_RECIPES;
    String categories = Constants.URL_CATEGORIES;// + "/{" + Constants.DATE_QUERY + "}";
    String food = Constants.URL_FOOD + "/{" + Constants.ID_QUERY + "}";


    @GET(recipeWithID)
    Observable<Response<RecipeTO>> getRecipeObservable(
            @Header(Constants.AUTHORIZATION) String token,
            @Path(Constants.ID_QUERY) String id
    );

    @GET(recipeWithID + "/" + Constants.COMMENTS)
    Call<List<CommentTO>> getRecipeComments(
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
    Observable<Response<List<RecipeTO>>> getAllRecipesObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date,
            @Query(Constants.EXCLUSIVE_START_KEY_QUERY) String startKey,
            @Query(Constants.LIMIT_QUERY) Integer limit
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

    @PATCH(recipeWithID)
    Call<RecipeTO> patchRecipe(
            @HeaderMap Map<String, String> headers,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Body Map<String, Object> body
    );

    @POST(recipeWithID + "/" + Constants.COMMENTS)
    Call<Void> postComment(
            @HeaderMap Map<String, String> headers,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Body Map<String, Object> body
    );


    //endregion

    //region categories
    @GET(categories)
    Call<List<CategoryTO>> getAllCategories(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date
    );

    @GET(categories)
    Observable<Response<List<CategoryTO>>> getAllCategoriesObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.DATE_QUERY) String date
    );

    //endregion

    //region food
    @PUT(food)
    Call<List<String>> requestFoodUrls(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Body Map<String, String> body
    );

    //endregion


    /*@GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);*/
}

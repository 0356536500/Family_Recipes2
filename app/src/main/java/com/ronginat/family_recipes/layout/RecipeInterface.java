package com.ronginat.family_recipes.layout;

import com.ronginat.family_recipes.layout.modelTO.CategoryTO;
import com.ronginat.family_recipes.layout.modelTO.CommentTO;
import com.ronginat.family_recipes.layout.modelTO.ContentTO;
import com.ronginat.family_recipes.layout.modelTO.RecipeTO;

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
import retrofit2.http.QueryMap;


public interface RecipeInterface {

    String recipeWithID = Constants.URL_RECIPES + "/{" + Constants.ID_QUERY + "}";
    String recipes = Constants.URL_RECIPES;
    String contents = recipeWithID + "/" + Constants.URL_CONTENT;
    String categories = Constants.URL_CATEGORIES;// + "/{" + Constants.DATE_QUERY + "}";
    String food = Constants.URL_FOOD + "/{" + Constants.ID_QUERY + "}";
    String subscriptions = Constants.URL_SUBSCRIPTIONS + "/{" + Constants.PATH_DEVICE_ID + "}";
    String registerToken = Constants.URL_TOKENS + "/{" + Constants.PATH_DEVICE_ID + "}" + "/{" + Constants.PATH_TOKEN + "}";
    String userWithID = Constants.URL_USERS + "/{" + Constants.PATH_DEVICE_ID + "}";
    String firebaseToken = Constants.URL_TOKENS + "/firebase";
    String update = Constants.URL_APP + "/{" + Constants.PATH_DEVICE_ID + "}";

    @GET("test")
    Observable<Response<Object>> getTestObservable();

    @GET(recipeWithID)
    Observable<Response<RecipeTO>> getRecipeObservable(
            @Header(Constants.AUTHORIZATION) String token,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String date
    );

    @GET(contents)
    Observable<Response<ContentTO>> getRecipeContentObservable(
            @Header(Constants.AUTHORIZATION) String token,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String date
    );

    @GET(recipeWithID + "/" + Constants.COMMENTS)
    Observable<Response<List<CommentTO>>> getRecipeComments(
            @Header(Constants.AUTHORIZATION) String token,
            @Path(Constants.ID_QUERY) String id
    );
    //region recipes
    @GET(recipes)
    Observable<Response<List<RecipeTO>>> getAllRecipesObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.LAST_MODIFIED_QUERY) String date,
            @Query(Constants.EXCLUSIVE_START_KEY_QUERY) String startKey,
            @Query(Constants.LIMIT_QUERY) Integer limit
    );

    @POST(recipes)
    // response structure { id: "id", lastModifiedDate: "lastModifiedDate" }
    Call<Map<String, String>> postRecipe(
            @HeaderMap Map<String, String> headers,
            @Body Map<String, Object> body
    );

    @PATCH(recipeWithID)
    Observable<Response<RecipeTO>> patchRecipe(
            @HeaderMap Map<String, String> headers,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Body Map<String, Object> body
    );

    @POST(recipeWithID + "/" + Constants.COMMENTS)
    Observable<Response<Void>> postComment(
            @HeaderMap Map<String, String> headers,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Body Map<String, Object> body
    );


    //endregion

    //region categories

    @GET(categories)
    Observable<Response<List<CategoryTO>>> getAllCategoriesObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Query(Constants.LAST_MODIFIED_QUERY) String date
    );

    //endregion

    //region food

    @GET(food)
    Call<List<String>> requestFoodUrls(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.ID_QUERY) String id,
            @Query(Constants.LAST_MODIFIED_QUERY) String lastModifiedDate,
            @Query(Constants.NUM_OF_FILES_QUERY) int numOfFiles,
            @Query(Constants.EXTENSION_QUERY) String extension
    );

    //endregion

    //region users
    @PUT(subscriptions)
    Observable<Response<Void>> manageSubscriptions(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.PATH_DEVICE_ID) String deviceId,
            @QueryMap Map<String, String> queries,
            /*@Query(Constants.QUERY_NEW_RECIPE_SUBSCRIPTION) String newRecipesSubscription,
            @Query(Constants.QUERY_COMMENTS_SUBSCRIPTION) String commentsSubscription,
            @Query(Constants.QUERY_LIKES_SUBSCRIPTION) String likesSubscription,*/
            //@QueryMap Map<String, String> queries,
            @Body Map<String, String> body
    );

    @PUT(registerToken)
    Call<Void> registerNewToken(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.PATH_DEVICE_ID) String deviceId,
            @Path(Constants.PATH_TOKEN) String notificationsToken,
            @QueryMap Map<String, Object> queries
            //@Query(Constants.QUERY_PLATFORM) String platform
    );

    @GET(userWithID)
    Observable<Response<Map<String, Object>>> getUserDetailsObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.PATH_DEVICE_ID) String deviceId
    );

    @GET(firebaseToken)
    Observable<Response<String>> getFirebaseTokenObservable(
            @Header(Constants.AUTHORIZATION) String auth
    );

    //endregion

    // region App

    @GET(update)
    Observable<Response<Map<String, String>>> getAppUpdatesObservable(
            @Header(Constants.AUTHORIZATION) String auth,
            @Path(Constants.PATH_DEVICE_ID) String deviceId,
            @Query(Constants.QUERY_APP_VERSION) String appVersion
    );

    // endregion


    /*@GET("/repos/{owner}/{repo}/contributors")
    Call<List<Contributor>> contributors(
            @Path("owner") String owner,
            @Path("repo") String repo);*/
}

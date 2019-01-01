package com.myapps.ron.family_recipes.network;

/**
 * Created by ronginat on 31/12/2018.
 */
public interface PaginCallback<T> {

    void onFinished(T result);
    void onUpdate(T update, String lastKey);
    //void onError(T error);
}

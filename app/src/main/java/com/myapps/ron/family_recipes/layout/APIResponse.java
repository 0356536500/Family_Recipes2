package com.myapps.ron.family_recipes.layout;

/**
 * Created by ronginat on 31/12/2018.
 */
public class APIResponse<T> {
    private T data;
    private String lastKey;

    APIResponse() {

    }

    public T getData() {
        return data;
    }

    void setData(T data) {
        this.data = data;
    }

    public String getLastKey() {
        return lastKey;
    }

    void setLastKey(String lastKey) {
        this.lastKey = lastKey;
    }
}

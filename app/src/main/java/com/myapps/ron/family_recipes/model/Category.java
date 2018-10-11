package com.myapps.ron.family_recipes.model;

import android.support.annotation.NonNull;

import com.yalantis.filter.model.FilterModel;

public class Category implements FilterModel {
    private String text;
    private int color;

    public Category(String text, int color) {
        this.text = text;
        this.color = color;
    }

    @NonNull
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category)) return false;

        Category tag = (Category) o;

        if (getColor() != tag.getColor()) return false;
        return getText().equals(tag.getText());

    }

    @Override
    public int hashCode() {
        int result = getText().hashCode();
        result = 31 * result + getColor();
        return result;
    }

}

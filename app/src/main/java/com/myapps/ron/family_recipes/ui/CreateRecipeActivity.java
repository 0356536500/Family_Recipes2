package com.myapps.ron.family_recipes.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.fragments.AdvancedStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.FirstStepFragment;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.CreateRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class CreateRecipeActivity extends AppCompatActivity {

    private CreateRecipeViewModel viewModel;
    private List<MyFragment> fragments;
    private int currentIndex = 0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);


        viewModel =  ViewModelProviders.of(this).get(CreateRecipeViewModel.class);
        setFragments();
    }

    private void setFragments() {
        fragments = new ArrayList<>();
        fragments.add(new FirstStepFragment());
        fragments.add(new AdvancedStepFragment());
        /*fragments.add(new PickPhotosFragment());*/

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.create_fragment_container, fragments.get(0));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void nextFragment() {
        if (currentIndex < 1) {//fragments.size() - 1) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.create_fragment_container, fragments.get(0));
            transaction.addToBackStack(null);
            transaction.commit();
            currentIndex++;
        }
    }

    public void previousFragment() {
        if (currentIndex > 0) {
            currentIndex--;
        }

        super.onBackPressed();
    }


    @Override
    public void onBackPressed() {
        if(!fragments.get(currentIndex).onBackPressed())
            super.onBackPressed();
    }
}

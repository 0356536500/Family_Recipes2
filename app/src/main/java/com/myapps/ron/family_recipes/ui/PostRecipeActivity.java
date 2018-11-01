package com.myapps.ron.family_recipes.ui;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.fragments.AdvancedStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.FirstStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.PickPhotosFragment;
import com.myapps.ron.family_recipes.ui.fragments.PreviewDialogFragment;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeActivity extends AppCompatActivity {

    private PostRecipeViewModel viewModel;
    private List<MyFragment> fragments;

    public AppCompatButton nextButton;
    private int currentIndex = 0;
    private boolean inPreview = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        nextButton = findViewById(R.id.create_recipe_next_button);
        viewModel =  ViewModelProviders.of(this).get(PostRecipeViewModel.class);
        setFragments();
    }

    private void setFragments() {
        fragments = new ArrayList<>();

        fragments.add(new AdvancedStepFragment());
        fragments.add(new PickPhotosFragment());
        //fragments.add(new FirstStepFragment());



        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.create_fragment_container, fragments.get(0));
        transaction.addToBackStack(null);
        transaction.commit();
        Log.e(getClass().getSimpleName(), "current index = " + currentIndex);
    }

    public void nextFragment() {
        if (currentIndex < fragments.size() - 1) {
            getSupportFragmentManager()
            .beginTransaction()
            .setCustomAnimations(
                    R.anim.fragment_slide_left_enter,
                    R.anim.fragment_slide_left_exit,
                    R.anim.fragment_slide_right_enter,
                    R.anim.fragment_slide_right_exit)
            .replace(R.id.create_fragment_container, fragments.get(++currentIndex))
            .addToBackStack(null)
            .commit();
            Log.e(getClass().getSimpleName(), "current index = " + currentIndex);
            /*FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.create_fragment_container, fragments.get(++currentIndex));
            transaction.addToBackStack(null);
            transaction.commit();*/
        }
    }

    public void previousFragment() {
        if (currentIndex > 0) {
            currentIndex--;
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
        }
        else
            super.onBackPressed();
    }


    @Override
    public void onBackPressed() {
        Log.e(getClass().getSimpleName(), "backPressed");
        if (inPreview) {
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack();
            inPreview = false;
            return;
            /*if(getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setDisplayShowHomeEnabled(false);
            }*/
        }
        if(!fragments.get(currentIndex).onBackPressed()) {
            NavUtils.navigateUpFromSameTask(this);
        }
        //finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (inPreview) {
                FragmentManager manager = getSupportFragmentManager();
                manager.popBackStack();
                inPreview = false;
                return true;
            }
            if (currentIndex > 0) {
                previousFragment();
                return true;
            }
            else
                NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    public void showMyDialog(String html) {
        Fragment newFragment = new PreviewDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("html", html);
        newFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.create_fragment_container, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();

        /*if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }*/
        inPreview = true;
    }

    public void postRecipe() {
        Toast.makeText(this, "posting the recipe...", Toast.LENGTH_SHORT).show();
    }
}

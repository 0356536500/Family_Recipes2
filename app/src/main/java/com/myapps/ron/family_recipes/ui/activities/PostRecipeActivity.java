package com.myapps.ron.family_recipes.ui.activities;

import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.NavUtils;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.services.PostRecipeToServerService;
import com.myapps.ron.family_recipes.ui.fragments.AdvancedStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.FirstStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.PickPhotosFragment;
import com.myapps.ron.family_recipes.ui.fragments.PreviewDialogFragment;
import com.myapps.ron.family_recipes.utils.MyBaseActivity;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeActivity extends MyBaseActivity {

    private PostRecipeViewModel viewModel;
    private List<MyFragment> fragments;

    public AppCompatButton nextButton;
    private int currentIndex = 0;
    private boolean inPreview = false;

    @Override
    protected void onMyCreate(@Nullable Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        nextButton = findViewById(R.id.create_recipe_next_button);
        viewModel =  ViewModelProviders.of(this).get(PostRecipeViewModel.class);
        setFragments();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRecipe();
            }
        });*/
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setFragments() {
        fragments = new ArrayList<>();

        fragments.add(new FirstStepFragment());
        fragments.add(new AdvancedStepFragment());
        fragments.add(new PickPhotosFragment());

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.create_fragment_container, fragments.get(0));
        transaction.addToBackStack(null);
        transaction.commit();
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
            //Log.e(getClass().getSimpleName(), "current index = " + currentIndex);
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
            else {
                setResult(RESULT_CANCELED);
                NavUtils.navigateUpFromSameTask(this);
            }
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

        inPreview = true;
    }

    public void postRecipe() {
        Toast.makeText(this, "posting the recipe...", Toast.LENGTH_SHORT).show();
        PostRecipeToServerService.startActionPostRecipe(this, viewModel.recipe);
        setResult(RESULT_OK);
        finish();
    }

    /*public void postRecipe1() {
        *//*File dcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Log.e(getClass().getSimpleName(), dcim.getAbsolutePath());
        File file = new File(dcim, "Camera/20180926_160503.jpg");
        Log.e(getClass().getSimpleName(), file.getAbsolutePath());*//*
        Toast.makeText(this, "posting the recipe...", Toast.LENGTH_SHORT).show();
        ArrayList<String> images = new ArrayList<>();
        images.add("/storage/emulated/0/DCIM/Camera/20180926_160503.jpg");
        images.add("/storage/emulated/0/DCIM/Camera/20180929_141712.jpg");
        PostRecipeToServerService.startActionPostImages(this,"kE5zymiS_wD_", images);
        setResult(RESULT_OK);
        finish();
    }

    private Recipe makeRecipe() {
        Recipe recipe = new Recipe();
        recipe.setName("פסטה פרמזן");
        recipe.setDescription("פסטה עם גבינת פרמזן במיוחד בשביל גיא");

        List<String> categories = new ArrayList<>();
        categories.add("חלבי");
        categories.add("פסטה");
        recipe.setCategories(categories);

        recipe.setRecipeFile("/data/user/0/com.myapps.ron.family_recipes/files/פסטה פרמזן.html");
        List<String> images = new ArrayList<>();
        images.add("/document/image:40876");
        images.add("/document/image:38906");
        recipe.setFoodFiles(images);

        return recipe;
    }*/
}

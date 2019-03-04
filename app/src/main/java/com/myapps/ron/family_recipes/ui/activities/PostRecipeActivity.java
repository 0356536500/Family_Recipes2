package com.myapps.ron.family_recipes.ui.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.background.services.PostRecipeToServerService;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.ui.baseclasses.MyFragment;
import com.myapps.ron.family_recipes.ui.fragments.AdvancedStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.FirstStepFragment;
import com.myapps.ron.family_recipes.ui.fragments.PickPhotosFragment;
import com.myapps.ron.family_recipes.ui.fragments.PreviewDialogFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.tunjid.androidbootstrap.material.animator.FabExtensionAnimator;
import com.tunjid.androidbootstrap.view.animator.ViewHider;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeActivity extends MyBaseActivity {

    private PostRecipeViewModel viewModel;
    private List<MyFragment> fragments;

    public MaterialButton expandedButton;
    private FabExtensionAnimator fabExtensionAnimator;
    private ViewHider fabHider;
    private int currentIndex = 0;
    private boolean inPreview = false;

    /*final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallback = new FragmentManager.FragmentLifecycleCallbacks() {

        @Override
        public void onFragmentPreAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
            super.onFragmentPreAttached(fm, f, context);
            Log.e(getClass().getSimpleName(), "onFragmentPreAttached, " + f.getClass().getSimpleName());
        }

        @Override
        public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
            Log.e(getClass().getSimpleName(), "onFragmentViewCreated, " + f.getClass().getSimpleName());
            super.onFragmentViewCreated(fm, f, v, savedInstanceState);
            PostRecipeBaseFragment fragment = (PostRecipeBaseFragment) f;

            fragment.togglePersistentUi();
        }
    };*/

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onMyCreate(@Nullable Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        //getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallback, true);

        expandedButton = findViewById(R.id.create_recipe_expanded_button);
        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(PostRecipeViewModel.class);

        fabHider = ViewHider.of(expandedButton).setDirection(ViewHider.BOTTOM).build();
        fabExtensionAnimator = new FabExtensionAnimator(expandedButton);
        //fabExtensionAnimator.setExtended(false);
        expandedButton.setOnTouchListener((view, motionEvent) -> {
            Log.e(getClass().getSimpleName(), "Touch detected");
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (!isFabExtended())
                        fabExtensionAnimator.setExtended(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if (isFabExtended())
                        fabExtensionAnimator.setExtended(false);
            }
            return false;
        });

        setFragments();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*expandedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                postRecipe();
            }
        });*/
    }

    // region FAB methods

    public void toggleFab(boolean show) {
        if (show) this.fabHider.show();
        else this.fabHider.hide();
    }

    public void setFabExtended(boolean extended) {
        fabExtensionAnimator.setExtended(extended);
    }

    public void updateFab(FabExtensionAnimator.GlyphState glyphState) {
        if (this.fabExtensionAnimator != null) this.fabExtensionAnimator.updateGlyphs(glyphState);
    }

    public void setFabClickListener(View.OnClickListener onClickListener) {
        expandedButton.setOnClickListener(onClickListener);
    }

    public boolean isFabExtended() {
        return fabExtensionAnimator.isExtended();
    }

    // endregion

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    private void setFragments() {
        fragments = new ArrayList<>();

        fragments.add(new AdvancedStepFragment());
        fragments.add(new FirstStepFragment());

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

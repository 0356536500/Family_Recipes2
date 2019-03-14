package com.myapps.ron.family_recipes.ui.activities;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.leinardi.android.speeddial.SpeedDialView;
import com.myapps.ron.family_recipes.FabExtensionAnimator;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ViewHider;
import com.myapps.ron.family_recipes.background.services.PostRecipeToServerService;
import com.myapps.ron.family_recipes.dal.Injection;
import com.myapps.ron.family_recipes.ui.baseclasses.MyBaseActivity;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.ui.fragments.PostRecipeGenerateContentFragment;
import com.myapps.ron.family_recipes.ui.fragments.PostRecipeFirstFragment;
import com.myapps.ron.family_recipes.ui.fragments.PostRecipePickPhotosFragment;
import com.myapps.ron.family_recipes.ui.fragments.PreviewDialogFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NavUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeActivity extends MyBaseActivity {

    public CoordinatorLayout coordinatorLayout;
    public SpeedDialView mSpeedDialView;
    public ViewHider floatingMenuHider;
    private PostRecipeViewModel viewModel;
    private List<PostRecipeBaseFragment> fragments;

    public MaterialButton expandedButton;
    private FabExtensionAnimator fabExtensionAnimator;
    private View.OnClickListener expandedButtonListener;
    private ViewHider fabHider;
    private int currentIndex = 0;
    private boolean inPreview = false;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onMyCreate(@Nullable Bundle savedInstanceState) {
        //super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        coordinatorLayout = findViewById(R.id.create_recipe_container);
        mSpeedDialView = findViewById(R.id.advanced_step_speedDial);
        expandedButton = findViewById(R.id.create_recipe_expanded_button);
        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(PostRecipeViewModel.class);

        floatingMenuHider = ViewHider.of(mSpeedDialView).setDirection(ViewHider.BOTTOM).build();
        fabHider = ViewHider.of(expandedButton).setDirection(ViewHider.BOTTOM).build();
        fabExtensionAnimator = new FabExtensionAnimator(expandedButton);
        //fabExtensionAnimator.setExtended(false);
        expandedButton.setOnTouchListener((view, motionEvent) -> {
            if (fabExtensionAnimator == null)
                return false;
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    setFabExtended(true);
                    break;
                case MotionEvent.ACTION_UP:
                    Rect rect = new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                    if (rect.contains(view.getLeft() + (int) motionEvent.getX(),view.getTop () + (int) motionEvent.getY())) {
                        // user lift his fingers inside the button borders
                        if (expandedButtonListener != null) {
                            if (fabExtensionAnimator.isAnimating())
                                new Handler().postDelayed(() -> expandedButtonListener.onClick(view), FabExtensionAnimator.EXTENSION_DURATION);
                            else
                                expandedButtonListener.onClick(view);
                        }
                    } else {
                        // user lift his finger outside the button borders
                        setFabExtended(false);
                    }
                    break;
            }
            return false;
        });

        setFragments();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // region FAB methods

    public void toggleFab(boolean show) {
        if (show) {
            this.fabHider.show();
            this.floatingMenuHider.show();
        }
        else {
            this.fabHider.hide();
            this.floatingMenuHider.hide();
        }
    }

    public void setFabExtended(boolean extended) {
        fabExtensionAnimator.setExtended(extended);
    }

    public void setFabExtended(boolean extended, long delay) {
        fabExtensionAnimator.setExtended(extended, delay);
    }

    public void updateFab(FabExtensionAnimator.GlyphState glyphState) {
        if (this.fabExtensionAnimator != null) this.fabExtensionAnimator.updateGlyphs(glyphState);
    }

    public void setFabClickListener(View.OnClickListener onClickListener) {
        expandedButtonListener = onClickListener;
        //expandedButton.setOnClickListener(onClickListener);
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

        fragments.add(new PostRecipeFirstFragment());
        fragments.add(new PostRecipeGenerateContentFragment());
        fragments.add(new PostRecipePickPhotosFragment());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.create_fragment_container, fragments.get(0))
                .addToBackStack(null)
                .commit();

        new Handler().postDelayed(this::nextFragmentDelayed, 700);
    }

    public void nextFragmentDelayed() {
        new Handler().postDelayed(this::nextFragment, 2 * FabExtensionAnimator.EXTENSION_DURATION);
    }

    private void nextFragment() {
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
        }
    }

    public void previousFragmentDelayed() {
        new Handler().postDelayed(this::previousFragment, FabExtensionAnimator.EXTENSION_DURATION);
    }

    private void previousFragment() {
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
            backFromPreview();
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
                backFromPreview();
                return true;
            }
            if (currentIndex > 0) {
                previousFragmentDelayed();
                return true;
            }
            else {
                setResult(RESULT_CANCELED);
                NavUtils.navigateUpFromSameTask(this);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void backFromPreview() {
        FragmentManager manager = getSupportFragmentManager();
        manager.popBackStack();
        inPreview = false;
        expandedButton.setVisibility(View.VISIBLE);
        mSpeedDialView.setVisibility(fragments.get(currentIndex).menuFabVisibility());
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

        mSpeedDialView.setVisibility(View.GONE);
        expandedButton.setVisibility(View.GONE);
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

package com.ronginat.family_recipes.ui.activities;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NavUtils;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.button.MaterialButton;
import com.leinardi.android.speeddial.SpeedDialView;
import com.ronginat.family_recipes.R;
import com.ronginat.family_recipes.layout.MiddleWareForNetwork;
import com.ronginat.family_recipes.logic.Injection;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;
import com.ronginat.family_recipes.ui.baseclasses.MyBaseActivity;
import com.ronginat.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.ronginat.family_recipes.ui.fragments.PagerDialogFragment;
import com.ronginat.family_recipes.ui.fragments.PickImagesMethodDialog;
import com.ronginat.family_recipes.ui.fragments.PostRecipeFirstFragment;
import com.ronginat.family_recipes.ui.fragments.PostRecipeGenerateContentFragment;
import com.ronginat.family_recipes.ui.fragments.PostRecipePickPhotosFragment;
import com.ronginat.family_recipes.ui.fragments.PreviewDialogFragment;
import com.ronginat.family_recipes.utils.ui.FabExtensionAnimator;
import com.ronginat.family_recipes.utils.ui.ViewHider;
import com.ronginat.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeActivity extends MyBaseActivity {

    @BindView(R.id.create_recipe_container)
    public CoordinatorLayout coordinatorLayout;
    @BindView(R.id.advanced_step_speedDial)
    public SpeedDialView mSpeedDialView;
    public ViewHider floatingMenuHider;
    private PostRecipeViewModel viewModel;
    private List<PostRecipeBaseFragment> fragments;

    @BindView(R.id.create_recipe_expanded_button)
    public MaterialButton expandedButton;
    private FabExtensionAnimator fabExtensionAnimator;
    private View.OnClickListener expandedButtonListener;
    private ViewHider fabHider;
    private int currentIndex = 0;
    private boolean inPreview = false, fabMayChangeExpandState = true, isRecipeEnqueued = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_recipe);

        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, Injection.provideViewModelFactory(this)).get(PostRecipeViewModel.class);

        initFloatingElementsAndHelpers();

        setFragments();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (savedInstanceState != null)
            isRecipeEnqueued = savedInstanceState.getBoolean("enqueued");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // delete dangling local images
        if (!isRecipeEnqueued && viewModel.recipe.getImages() != null) {
            StorageWrapper.deleteFilesFromLocalPictures(this, viewModel.recipe.getImages());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean("enqueued", isRecipeEnqueued);
        super.onSaveInstanceState(outState);
    }

    private void initFloatingElementsAndHelpers() {
        floatingMenuHider = ViewHider.of(mSpeedDialView).setDirection(ViewHider.BOTTOM).build();
        fabHider = ViewHider.of(expandedButton).setDirection(ViewHider.BOTTOM).build();
        fabExtensionAnimator = new FabExtensionAnimator(expandedButton);
        //fabExtensionAnimator.setExtended(false);
    }

    @OnTouch(R.id.create_recipe_expanded_button)
    public boolean expandedButtonOnTouchListener(View view, MotionEvent motionEvent) {
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
        if (fabMayChangeExpandState)
            fabExtensionAnimator.setExtended(extended);
    }

    public void setFabExtended(boolean extended, long delay) {
        if (fabMayChangeExpandState)
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

    public void setFabGravity(int gravity) {
        expandedButton.setVisibility(View.INVISIBLE);
        ((CoordinatorLayout.LayoutParams) expandedButton.getLayoutParams()).gravity = gravity;
        expandedButton.setVisibility(View.VISIBLE);
    }

    public void setFabMayChangeExpandState(boolean fabMayExpand) {
        this.fabMayChangeExpandState = fabMayExpand;
    }

    // endregion

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

    // region Fragments

    private void setFragments() {
        fragments = new ArrayList<>();

        fragments.add(new PostRecipeFirstFragment());
        fragments.add(new PostRecipeGenerateContentFragment());
        fragments.add(new PostRecipePickPhotosFragment());

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.create_fragment_container, fragments.get(0))
                //.addToBackStack(null)
                .commit();

        //new Handler().postDelayed(this::nextFragmentDelayed, 2500);
        //new Handler().postDelayed(this::nextFragmentDelayed, 5000);
    }

    public void nextFragmentDelayed() {
        new Handler().postDelayed(this::nextFragment, 2 * FabExtensionAnimator.EXTENSION_DURATION);
    }

    private void nextFragment() {
        if (currentIndex < fragments.size() - 1) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_enter_from_left,
                            R.anim.slide_right_to_exit,
                            R.anim.slide_enter_from_right,
                            R.anim.slide_left_to_exit)
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
            onBackPressed();
    }


    @Override
    public void onBackPressed() {
        if (inPreview) {
            backFromPreview();
            return;
        }
        if(!fragments.get(currentIndex).onBackPressed()) {
            //exit();
            super.onBackPressed();
            //NavUtils.navigateUpFromSameTask(this);
        }
        //finish();
    }

    // endregion


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_recipe_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
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
                    //exit();
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
            case R.id.action_help:
                showInstructionDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*private void exit() {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        Log.e(getClass().getSimpleName(), "upIntent != null ? " + Boolean.toString(upIntent != null));
        if (upIntent != null) {
            if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                Log.e(getClass().getSimpleName(), "shouldUpRecreateTask");
                // This activity is NOT part of this app's task, so create a new task
                // when navigating up, with a synthesized back stack.
                TaskStackBuilder.create(this)
                        // Add all of this activity's parents to the back stack
                        .addNextIntentWithParentStack(upIntent)
                        // Navigate up to the closest parent
                        .startActivities();
            } else {
                Log.e(getClass().getSimpleName(), "NOT shouldUpRecreateTask");
                // This activity is part of this app's task, so simply
                // navigate up to the logical parent activity.
                NavUtils.navigateUpTo(this, upIntent);
            }
        }
        else {
            finish();
        }
    }*/

    private void backFromPreview() {
        getSupportFragmentManager()
                .popBackStack();
        inPreview = false;
        expandedButton.setVisibility(View.VISIBLE);
        mSpeedDialView.setVisibility(fragments.get(currentIndex).menuFabVisibility());
    }

    public void showPreviewDialog(String html) {
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

    public void showInstructionDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = new PagerDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PagerDialogFragment.PAGER_TYPE_KEY, PagerDialogFragment.PAGER_TYPE.INSTRUCTIONS);
        newFragment.setArguments(bundle);
        newFragment.show(ft, "dialog");
    }

    public PickImagesMethodDialog showPickImagesDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("photos_dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        PickImagesMethodDialog pickImageDialog = new PickImagesMethodDialog();
        pickImageDialog.show(ft, "photos_dialog");

        return pickImageDialog;
    }

    public void postRecipe() {
        viewModel.postRecipe(this);
        isRecipeEnqueued = true;
        if (MiddleWareForNetwork.checkInternetConnection(this))
            Toast.makeText(this, R.string.post_recipe_online_upload_message, Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, R.string.post_recipe_offline_upload_message, Toast.LENGTH_LONG).show();
        setResult(RESULT_OK);
        new Handler().postDelayed(/*this::exit*/() -> NavUtils.navigateUpFromSameTask(this), 500);
        //PostRecipeToServerService.startActionPostRecipe(this, viewModel.recipe);
        //finish();
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

        recipe.setContent("/data/user/0/com.myapps.family_recipes/files/פסטה פרמזן.html");
        List<String> images = new ArrayList<>();
        images.add("/document/image:40876");
        images.add("/document/image:38906");
        recipe.setFoodFiles(images);

        return recipe;
    }*/
}

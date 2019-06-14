package com.myapps.ron.family_recipes.ui.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.recycler.adapters.HtmlElementsAdapter;
import com.myapps.ron.family_recipes.recycler.helpers.MyRecyclerScroll;
import com.myapps.ron.family_recipes.recycler.helpers.SwipeAndDragHelper;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.SharedPreferencesHandler;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.List;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeGenerateContentFragment extends PostRecipeBaseFragment {
    private final String TAG = getClass().getSimpleName();

    private List<HtmlModel> elements;
    private HtmlElementsAdapter mAdapter;
    private RecyclerView recyclerView;

    //private PostRecipeActivity activity;
    private PostRecipeViewModel viewModel;

    private SpeedDialView mSpeedDialView;

    @Override
    public boolean onBackPressed() {
        if (mSpeedDialView.isOpen())
            mSpeedDialView.close();
        else
            activity.previousFragmentDelayed();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_advanced_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.advanced_step_recycler);
        mSpeedDialView = activity.mSpeedDialView;
        //mSpeedDialView = view.findViewById(R.id.advanced_step_speedDial);
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);

        initFloatingMenu(savedInstanceState == null);
        initRecycler();
        firstTimeInstructionsDialogDelayed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mSpeedDialView.setVisibility(View.GONE);
        this.elements = mAdapter.getElements();
    }

    private void firstTimeInstructionsDialogDelayed() {
        if (SharedPreferencesHandler.getBoolean(activity, getString(R.string.preference_key_first_post_recipe), true)) {
            SharedPreferencesHandler.writeBoolean(activity, getString(R.string.preference_key_first_post_recipe), false);
            new Handler().postDelayed(activity::showInstructionDialog, 1500);
        }
    }

    // region PostRecipeBaseFragment Overrides

    @Override
    protected String getTitle() {
        return getString(R.string.nav_main_post_recipe) + " 2/3";
    }

    @Override
    public int menuFabVisibility() {
        return View.VISIBLE;
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> {
            if(mAdapter.checkValidInput()) {
                String html = mAdapter.generateHtml();
                Log.e(TAG, html);
                viewModel.setRecipeContent(html);
                activity.nextFragmentDelayed();
            } else {
                Toast.makeText(activity, getString(R.string.post_recipe_advanced_step_validation_message, Constants.MIN_NUMBER_OF_HTML_ELEMENTS), Toast.LENGTH_SHORT).show();
                activity.setFabExtended(false, 1000);
            }
        };
    }

    // endregion

    // region Floating Menu

    private void initFloatingMenu(boolean addActionItems) {
        mSpeedDialView.setVisibility(View.VISIBLE);
        initFloatingMenuUI(addActionItems);
        initFloatingMenuListener();
    }

    private void initFloatingMenuUI(boolean addActionItems) {
        if (addActionItems) {
            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_action, R.drawable
                    .ic_action_post_black)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_100,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_add)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_600,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.WHITE)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_preview_action, R.drawable.ic_preview_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_preview)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.WHITE)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_template_action, R.drawable.ic_template_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_template)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.WHITE)
                    //.setTheme(R.style.AppTheme_Purple)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_reset_action, R.drawable.ic_reset_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_reset)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.WHITE)
                    .create());
        }
    }

    private void initFloatingMenuListener() {
        //Set option fab click listeners.
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.fab_add_action:
                    mAdapter.addElementToScreen();
                    break;
                case R.id.fab_preview_action:
                    activity.showPreviewDialog(mAdapter.generateHtml());
                    break;
                case R.id.fab_template_action:
                    if (shouldDisplayDialog())
                        showAlertDialogBeforeDeletingFromAdapter(actionItem.getId());
                    else
                        mAdapter.loadTemplate();
                    break;
                case R.id.fab_reset_action:
                    if (shouldDisplayDialog())
                        showAlertDialogBeforeDeletingFromAdapter(actionItem.getId());
                    break;
                default:
                    break;
            }
            mSpeedDialView.close();
            return true; // To keep the Speed Dial open
        });
    }

    // endregion Floating Menu

    private void removeItem(HtmlModel item, int position) {
        Snackbar snackbar = Snackbar
                .make(activity.coordinatorLayout, getString(R.string.post_recipe_advanced_step_item_removed_message,
                        item.getSpinnerText(activity)), Snackbar.LENGTH_LONG)
                .setAction(R.string.post_recipe_advanced_step_item_undo_message, view ->
                        mAdapter.insertItem(item, position));
        snackbar.show();
    }

    private void initRecycler() {
        mAdapter = new HtmlElementsAdapter(activity, this::removeItem, elements);

        SwipeAndDragHelper swipeAndDragHelper = new SwipeAndDragHelper(mAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeAndDragHelper);

        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        recyclerView.setItemAnimator(itemAnimator);
        recyclerView.setAdapter(mAdapter);
        touchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnScrollListener(new MyRecyclerScroll() {
            @Override
            public void show() {
                toggleFab(true);
            }

            @Override
            public void hide() {
                toggleFab(false);
            }
        });
    }

    private void showAlertDialogBeforeDeletingFromAdapter(@IdRes int id) {
        DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
            switch (which){
                case DialogInterface.BUTTON_POSITIVE:
                    //Yes button clicked
                    if (id == R.id.fab_reset_action)
                        mAdapter.reset();
                    else if (id == R.id.fab_template_action) {
                        mAdapter.loadTemplate();
                    }
                    break;

                case DialogInterface.BUTTON_NEGATIVE:
                    //No button clicked
                    break;
            }
            dialog.dismiss();
        };

        new AlertDialog.Builder(activity)
                .setMessage(R.string.post_recipe_advanced_step_reset_message)
                .setPositiveButton(R.string.yes, dialogClickListener)
                .setNegativeButton(R.string.no, dialogClickListener)
                .show();
    }

    private boolean shouldDisplayDialog() {
        return mAdapter.getItemCount() > 1;
    }
}

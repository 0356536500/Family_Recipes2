package com.myapps.ron.family_recipes.ui.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.ernestoyaquello.dragdropswiperecyclerview.DragDropSwipeRecyclerView;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemDragListener;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnItemSwipeListener;
import com.ernestoyaquello.dragdropswiperecyclerview.listener.OnListScrollListener;
import com.google.android.material.snackbar.Snackbar;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.recycler.adapters.HtmlElementsAdapter;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.recycler.adapters.MyDragDropSwipeAdapter;
import com.myapps.ron.family_recipes.recycler.helpers.MyRecyclerScroll;
import com.myapps.ron.family_recipes.ui.baseclasses.PostRecipeBaseFragment;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 29/10/2018.
 */
public class PostRecipeGenerateContentFragment extends PostRecipeBaseFragment {
    private final String TAG = getClass().getSimpleName();

    private ViewGroup rootView;
    private List<HtmlModel> elements;
    private MyDragDropSwipeAdapter mDragDropSwipeAdapter;
    private DragDropSwipeRecyclerView mList;
    private HtmlElementsAdapter mAdapter;
    private RecyclerView recyclerView;
    private Button preview, sample, reset;

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
        rootView = (ViewGroup) inflater.inflate(R.layout.content_post_advanced_step, container, false);
        return rootView;
    }

    @Override
    public void onMyViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mList = view.findViewById(R.id.advanced_step_recycler);
        mSpeedDialView = view.findViewById(R.id.advanced_step_speedDial);
        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);

        initFloatingMenu(savedInstanceState == null);
        initDragDropSwipeRecycler();
        //initRecycler();

        //viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);

        //activity.setTitle(getString(R.string.nav_main_post_recipe) + " 2/3");
        //setListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isDestroyed = true;
        elements = mAdapter.getElements();
    }

    // region PostRecipeBaseFragment Overrides

    @Override
    protected String getTitle() {
        return getString(R.string.nav_main_post_recipe) + " 2/3";
    }

    @Override
    protected View.OnClickListener getFabClickListener() {
        return view -> {
            if(mAdapter.checkValidInput()) {
                String html = mAdapter.generateHtml(viewModel.recipe.getName(), viewModel.recipe.getDescription());
                Log.e(TAG, html);
                viewModel.setRecipeFile(activity, html);
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
        initFloatingMenuUI(addActionItems);
        initFloatingMenuListener();
    }

    private void initFloatingMenuUI(boolean addActionItems) {
        if (addActionItems) {
            /*Drawable drawable = AppCompatResources.getDrawable(activity, R.drawable.ic_custom_color);
            FabWithLabelView fabWithLabelView = mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id
                    .fab_custom_color, drawable)
                    .setFabImageTintColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary, getTheme()))
                    .setLabel(R.string.label_custom_color)
                    .setLabelColor(Color.WHITE)
                    .setLabelBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_primary,
                            getTheme()))
                    .create());
            if (fabWithLabelView != null) {
                fabWithLabelView.setSpeedDialActionItem(fabWithLabelView.getSpeedDialActionItemBuilder()
                        .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.material_white_1000,
                                getTheme()))
                        .create());
            }*/

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_add_action, R.drawable
                    .ic_action_post_black)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_100,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_add)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_grey_600,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_preview_action, R.drawable.ic_preview_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_preview)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_blue_700,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_template_action, R.drawable.ic_template_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_template)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_light_green_800,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    //.setTheme(R.style.AppTheme_Purple)
                    .create());

            mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_reset_action, R.drawable.ic_reset_fab)
                    .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            activity.getTheme()))
                    .setLabel(R.string.post_recipe_advanced_step_fab_reset)
                    .setLabelColor(ResourcesCompat.getColor(getResources(), R.color.md_grey_900,
                            activity.getTheme()))
                    .setLabelBackgroundColor(Color.TRANSPARENT)
                    .create());

        }
        //Set main action click listener.
        /*mSpeedDialView.setOnChangeListener(new SpeedDialView.OnChangeListener() {
            @Override
            public boolean onMainActionSelected() {
                //Toast.makeText(activity,"Main action clicked!", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "onMainActionSelected");
                return false; // True to keep the Speed Dial open
            }

            @Override
            public void onToggleChanged(boolean isOpen) {
                Log.w(TAG, "Speed dial toggle state changed. Open = " + isOpen);
            }
        });*/
    }

    private void initFloatingMenuListener() {
        //Set option fab click listeners.
        mSpeedDialView.setOnActionSelectedListener(actionItem -> {
            switch (actionItem.getId()) {
                case R.id.fab_add_action:
                    //showToast("No label action clicked!\nClosing with animation");
                    //mSpeedDialView.close(); // To close the Speed Dial with animation
                    mAdapter.addElementToScreen();
                    break;
                    //return true; // false will close it without animation
                case R.id.fab_preview_action:
                    //showSnackbar(actionItem.getLabel(activity) + " clicked!");
                    activity.showMyDialog(mAdapter.generateHtml("some name" , "long description"));
                    break;
                case R.id.fab_template_action:
                    //showToast(actionItem.getLabel(activity) + " clicked!\nClosing without animation.");
                    mAdapter.loadTemplate();
                    return false; // closes without animation (same as mSpeedDialView.close(false); return false;)
                case R.id.fab_reset_action:
                    //showToast(actionItem.getLabel(activity) + " clicked!");
                    mAdapter.reset();
                    break;
                /*case R.id.fab_add_action:
                    mSpeedDialView.addActionItem(new SpeedDialActionItem.Builder(R.id.fab_replace_action,
                            R.drawable.ic_replace_white_24dp)
                            .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color
                                            .material_orange_500,
                                    getTheme()))
                            .setLabel(getString(R.string.label_replace_action))
                            .create(), ADD_ACTION_POSITION);
                    break;
                case R.id.fab_replace_action:
                    mSpeedDialView.replaceActionItem(new SpeedDialActionItem.Builder(R.id
                            .fab_remove_action,
                            R.drawable.ic_delete_white_24dp)
                            .setLabel(getString(R.string.label_remove_action))
                            .setFabBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.inbox_accent,
                                    getTheme()))
                            .create(), ADD_ACTION_POSITION);
                    break;
                case R.id.fab_remove_action:
                    mSpeedDialView.removeActionItemById(R.id.fab_remove_action);
                    break;*/
                default:
                    break;
            }
            mSpeedDialView.close();
            return true; // To keep the Speed Dial open
        });
    }

    /*private Toast mToast;
    private Snackbar mSnackbar;

    private void showToast(String text) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(activity, text, Toast.LENGTH_LONG);
        mToast.show();
    }

    private void showSnackbar(String text) {
        mSnackbar = Snackbar.make(getView(), text, Snackbar.LENGTH_SHORT);
        mSnackbar.setAction("Close", view -> mSnackbar.dismiss());
        mSnackbar.show();
    }*/

    // endregion Floating Menu

    private void initDragDropSwipeRecycler() {
        List<String> list = Arrays.asList("1", "2", "3", "4");
        mDragDropSwipeAdapter = new MyDragDropSwipeAdapter(list);
        mList.setLayoutManager(new LinearLayoutManager(activity));
        mList.setOrientation(DragDropSwipeRecyclerView.ListOrientation.VERTICAL_LIST_WITH_VERTICAL_DRAGGING);
        mList.setAdapter(mDragDropSwipeAdapter);

        setListListeners();
    }

    private void setListListeners() {
        mList.setSwipeListener((OnItemSwipeListener<String>) (position, swipeDirection, item) -> {
            Log.e(TAG, "onSwipe");
            removeItem(item, position);
            return false;
        });
        mList.setDragListener(new OnItemDragListener<String>() {
            @Override
            public void onItemDropped(int i, int i1, String s) {
                // Handle action of item being dragged from one position to another
                Log.e(TAG, mDragDropSwipeAdapter.getDataSet().toString());
            }

            @Override
            public void onItemDragged(int i, int i1, String s) {
                // Handle action of item dropped
            }
        });
        mList.setScrollListener((scrollDirection, distance) -> {
            // Handle scrolling
        });
    }

    private void removeItem(String item, int position) {
        Snackbar
                .make(rootView, getString(R.string.post_recipe_advanced_step_item_removed_message, item), Snackbar.LENGTH_LONG)
                .setAction(R.string.post_recipe_advanced_step_item_undo_message, view ->
                        mDragDropSwipeAdapter.insertItem(position, item, false))
                .show();
    }

    private void initRecycler() {
        mAdapter = new HtmlElementsAdapter(activity, elements);

        //RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        //recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
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

    private void setListeners() {
        preview.setOnClickListener(view ->
                activity.showMyDialog(mAdapter.generateHtml("some name" , "long description")));

        View.OnClickListener clickListener = view -> {
            if (mAdapter.getItemCount() > 1) {
                DialogInterface.OnClickListener dialogClickListener = (dialog, which) -> {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            /*if (view.getId() == R.id.advanced_step_reset_button)
                                mAdapter.reset();
                            else if (view.getId() == R.id.advanced_step_load_sample_button) {
                                mAdapter.loadTemplate();
                            }*/
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
            } /*else if (view.getId() == R.id.advanced_step_load_sample_button){
                mAdapter.loadTemplate();
            }*/
        };

        sample.setOnClickListener(clickListener);
        reset.setOnClickListener(clickListener);

        /*activity.expandedButton.setOnClickListener(view -> {
            if(mAdapter.checkValidInput()) {
                String html = mAdapter.generateHtml(viewModel.recipe.getName(), viewModel.recipe.getDescription());
                Log.e(TAG, html);
                viewModel.setRecipeFile(activity, html);
                activity.nextFragmentDelayed();
            } else {
                Toast.makeText(activity, getString(R.string.post_recipe_advanced_step_validation_message, Constants.MIN_NUMBER_OF_HTML_ELEMENTS), Toast.LENGTH_SHORT).show();
            }
        });*/
    }
}

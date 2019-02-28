package com.myapps.ron.family_recipes.ui.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.HtmlElementsAdapter;
import com.myapps.ron.family_recipes.ui.activities.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ronginat on 29/10/2018.
 */
public class AdvancedStepFragment extends MyFragment {
    private final String TAG = getClass().getSimpleName();

    private View view;
    private LinearLayout parent;

    private HtmlElementsAdapter mAdapter;
    private RecyclerView recyclerView;
    private Button preview, sample, reset;

    private PostRecipeActivity activity;
    private PostRecipeViewModel viewModel;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (PostRecipeActivity)getActivity();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.e(TAG, "on attach");
        /*if (parent != null){
            parent.addView(recyclerView);
            parent.addView(preview);
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "on detach");
        //parent.removeAllViews();
    }

    @Override
    public boolean onBackPressed() {
        activity.previousFragment();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (parent == null) {
            //Log.e(TAG, "on create view");
            view = inflater.inflate(R.layout.content_post_advanced_step, container, false);
            parent = (LinearLayout) view;

        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //super.onViewCreated(view, savedInstanceState);

        if (recyclerView == null) {
            recyclerView = view.findViewById(R.id.advanced_step_recycler);

            viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);

            initRecycler();
        }

        //viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);
        preview = view.findViewById(R.id.advanced_step_preview_button);
        sample = view.findViewById(R.id.advanced_step_load_sample_button);
        reset = view.findViewById(R.id.advanced_step_reset_button);

        activity.setTitle(getString(R.string.nav_main_post_recipe) + " 2/3");
        setListeners();
    }

    private void initRecycler() {
        mAdapter = new HtmlElementsAdapter(activity);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
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
                            if (view.getId() == R.id.advanced_step_reset_button)
                                mAdapter.reset();
                            else if (view.getId() == R.id.advanced_step_load_sample_button) {
                                mAdapter.loadSample();
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
            } else if (view.getId() == R.id.advanced_step_load_sample_button){
                mAdapter.loadSample();
            }
        };

        sample.setOnClickListener(clickListener);
        reset.setOnClickListener(clickListener);

        activity.nextButton.setOnClickListener(view -> {
            if(mAdapter.checkValidInput()) {
                String html = mAdapter.generateHtml(viewModel.recipe.getName(), viewModel.recipe.getDescription());
                Log.e(TAG, html);
                viewModel.setRecipeFile(activity, html);
                activity.nextFragment();
            } else {
                Toast.makeText(activity, getString(R.string.post_recipe_advanced_step_validation_message, Constants.MIN_NUMBER_OF_HTML_ELEMENTS), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.adapters.HtmlElementsAdapter;
import com.myapps.ron.family_recipes.ui.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;
import com.myapps.ron.searchfilter.animator.FiltersListItemAnimator;

/**
 * Created by ronginat on 29/10/2018.
 */
public class AdvancedStepFragment extends MyFragment {
    private final String TAG = getClass().getSimpleName();

    private View view;
    private LinearLayout parent;

    private HtmlElementsAdapter mAdapter;
    private RecyclerView recyclerView;
    private Button preview;

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
        //setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.e(TAG, "on attach");
        if (parent != null){
            parent.addView(recyclerView);
            parent.addView(preview);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.e(TAG, "on detach");
        /*recyclerView = view.findViewById(R.id.advanced_step_recycler);
        preview = view.findViewById(R.id.advanced_step_preview_button);
        parent.removeView(preview);
        parent.removeView(recyclerView);*/
        parent.removeAllViews();
        parent = null;
        view = null;
    }

    @Override
    public boolean onBackPressed() {
        activity.previousFragment();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (recyclerView == null) {
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

        activity.setTitle(getString(R.string.nav_main_post_recipe) + " 2/3");
        setListeners();
    }

    private void initRecycler() {
        mAdapter = new HtmlElementsAdapter(activity);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(activity.getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setItemAnimator(new FiltersListItemAnimator());
    }

    private void setListeners() {
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.showMyDialog(mAdapter.generateHtml());
            }
        });

        activity.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAdapter.checkValidInput()) {
                    String html = mAdapter.generateHtml();
                    Log.e(TAG, html);
                    viewModel.setRecipeFile(activity, html);
                    activity.nextFragment();
                } else {
                    Toast.makeText(activity, getString(R.string.post_recipe_advanced_step_validation_message, Constants.MIN_NUMBER_OF_HTML_ELEMENTS), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

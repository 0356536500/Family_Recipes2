package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.PostRecipeActivity;
import com.myapps.ron.family_recipes.utils.HtmlHelper;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.PostRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class AdvancedStepFragment extends MyFragment {

    private final String TAG = getClass().getSimpleName();
    private List<FlexibleHtmlStructure> elements;
    private LinearLayout layout;

    private PostRecipeActivity activity;
    private PostRecipeViewModel viewModel;

    private Button preview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        elements = new ArrayList<>();
        activity = (PostRecipeActivity)getActivity();
    }

    @Override
    public boolean onBackPressed() {
        activity.previousFragment();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_post_advanced_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        layout = view.findViewById(R.id.advanced_step_container);
        preview = view.findViewById(R.id.advanced_step_preview_button);

        View element = getLayoutInflater().inflate(R.layout.flexible_html_structure, layout, false);
        layout.addView(element);
        elements.add(new FlexibleHtmlStructure(element));

        viewModel =  ViewModelProviders.of(activity).get(PostRecipeViewModel.class);

        activity.setTitle("create 2/3");
        setListeners();
    }

    private void setListeners() {
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.showMyDialog(generateHtml());
            }
        });

        activity.nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String html = generateHtml();
                Log.e(TAG, html);
                viewModel.setRecipeFile(activity, html);
                activity.nextFragment();
            }
        });
    }

    private String generateHtml() {
        HtmlHelper helper = new HtmlHelper();
        helper.openStaticElements();

        for (FlexibleHtmlStructure element : elements) {
            if (element.isElementHasContent())
                helper = element.buildHtmlFromElement(helper);
        }

        helper.closeStaticElements();

        return helper.toString();
    }

    void addElementToScreen() {
        View element = getLayoutInflater().inflate(R.layout.flexible_html_structure, layout, false);
        layout.addView(element);
        elements.add(new FlexibleHtmlStructure(element));
    }


    class FlexibleHtmlStructure {
        private final int UNORDERED_LIST_POS = 3;
        private final int ORDERED_LIST_POS = 4;

        private Spinner spinner;
        private CheckBox checkBoxDivider, checkBoxBold, checkBoxUnderScore ;
        private AppCompatEditText editText;

        private String stringElement;
        private int elementPos = 0;
        private boolean hasUserTypedInEditText;

        private AdapterView.OnItemSelectedListener spinnerListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        stringElement = (String) adapterView.getItemAtPosition(i);
                        elementPos = i;

                        if (elementPos >= UNORDERED_LIST_POS && elementPos <= ORDERED_LIST_POS) {
                            Toast toast = new Toast(activity);
                            toast.setText(R.string.post_recipe_advanced_step_list_message);
                            toast.setDuration(Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        stringElement = null;
                        elementPos = -1;
                    }
                };

        private TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (!hasUserTypedInEditText && !editable.toString().isEmpty()) {
                    hasUserTypedInEditText = true;
                    addElementToScreen();
                }
            }
        };

        FlexibleHtmlStructure(View view) {
            spinner = view.findViewById(R.id.advanced_step_choose_type);
            checkBoxDivider = view.findViewById(R.id.advanced_step_horizontal_divider);
            checkBoxBold = view.findViewById(R.id.advanced_step_bold_checkBox);
            checkBoxUnderScore = view.findViewById(R.id.advanced_step_under_score_checkBox);
            editText = view.findViewById(R.id.advanced_step_details_editText);

            hasUserTypedInEditText = false;
            initUI();
        }

        private void initUI() {
            //init the spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                    R.array.html_elements, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(spinnerListener);
            editText.addTextChangedListener(textWatcher);

        }

        boolean isElementHasContent() {
            return editText.getText() != null && editText.getText().toString().length() > 0;
        }

        HtmlHelper buildHtmlFromElement(HtmlHelper helper) {
            if (editText.getText() == null || editText.getText().toString().isEmpty())
                return helper;
            String fromEditText = editText.getText().toString();

            if (stringElement != null && elementPos >= 0) {
                String htmlElement = getResources().getStringArray(R.array.html_elements_types)[elementPos];

                helper.openElement(htmlElement); // can be list/header/paragraph

                if (checkBoxBold.isChecked())
                    helper.openElement("b");

                if (checkBoxUnderScore.isChecked())
                    helper.openElement("ins");

                if (elementPos != UNORDERED_LIST_POS && elementPos != ORDERED_LIST_POS) {
                    // header or paragraph

                    //split the paragraph to rows
                    if (htmlElement.equals(HtmlHelper.PARAGRAPH))
                        helper.append(fromEditText.split("\\.\\r?\\n"));
                    //write the header as one string
                    else
                        helper.append(fromEditText);

                } else {
                    //list separated by \n
                    String[] rows = fromEditText.split("\\r?\\n");
                    for (String row : rows) {
                        helper.openElement(HtmlHelper.LIST_ROW, row);
                    }
                }

                if (checkBoxUnderScore.isChecked())
                    helper.closeElement(); // close under score

                if (checkBoxBold.isChecked())
                    helper.closeElement(); // close bold

                helper.closeElement(); // close main element of this view
            }

            if (checkBoxDivider.isChecked())
                helper.addTagToBuilder(HtmlHelper.HORIZONTAL_RULE);

            return helper;
        }
    }
}

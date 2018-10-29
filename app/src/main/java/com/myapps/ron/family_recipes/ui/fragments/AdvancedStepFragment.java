package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import static j2html.TagCreator.*;
import com.jaredrummler.android.util.HtmlBuilder;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.dal.storage.StorageWrapper;
import com.myapps.ron.family_recipes.ui.CreateRecipeActivity;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.CreateRecipeViewModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class AdvancedStepFragment extends MyFragment {

    private final String TAG = getClass().getSimpleName();
    private List<FlexibleHtmlStructure> elements;
    private LinearLayout layout;

    private CreateRecipeActivity activity;
    private CreateRecipeViewModel viewModel;

    private Button preview;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        elements = new ArrayList<>();
        activity = (CreateRecipeActivity)getActivity();
    }

    @Override
    public boolean onBackPressed() {
        activity.previousFragment();
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.content_creation_advanced_step, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        layout = view.findViewById(R.id.advanced_step_container);
        preview = view.findViewById(R.id.advanced_step_preview_button);

        View element = getLayoutInflater().inflate(R.layout.flexible_html_structure, layout, false);
        layout.addView(element);
        elements.add(new FlexibleHtmlStructure(element));

        viewModel =  ViewModelProviders.of(activity).get(CreateRecipeViewModel.class);

        setListeners();
    }

    private void setListeners() {
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String str = Html.toHtml(generateSpanned());
                //Log.e(TAG, str);
                activity.showMyDialog(generateHtml());
               /* Spanned spanned = generateSpanned();
                //Log.e(TAG, Html.toHtml(spanned));
                if (spanned != null) {
                    File file = StorageWrapper.createHtmlFile(activity, "demo.html", spanned);
                    if (file != null) {
                        activity.showMyDialog(file.getAbsolutePath());
                    }
                }*/
            }
        });
    }

    private String generateHtml() {
        return html(
                body(
                        main(
                                h2("header2!!!!"),
                                hr()
                        )
                )
        ).render();
    }

    private Spanned generateSpanned() {
        HtmlBuilder html = new HtmlBuilder();
        html.open("html");
        html.open("body");
        html.close();
        html.close();
        if (!elements.get(0).isElementHasContent())
            return null;
        for (FlexibleHtmlStructure element : elements) {
            if (element.isElementHasContent())
                html = element.buildHtmlFromElement(html);
        }
        return html.build();
    }

    void addElementToScreen() {
        View element = getLayoutInflater().inflate(R.layout.flexible_html_structure, layout, false);
        layout.addView(element);
        elements.add(new FlexibleHtmlStructure(element));
    }


    class FlexibleHtmlStructure {
        private final int UNORDERED_LIST_POS = 3;
        private final int ORDERED_LIST_POS = 4;
        private final String LIST_ROW = "li";
        private final String HORIZONTAL_RULE = "<hr>";

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

        public boolean isElementHasContent() {
            return editText.getText() != null && editText.getText().toString().length() > 0;
        }

        public Spanned buildHtmlFromElement() {
            if (editText.getText() == null || editText.getText().toString().isEmpty())
                return null;
            String fromEditTest = editText.getText().toString();
            HtmlBuilder html = new HtmlBuilder();
            if (stringElement != null && elementPos >= 0) {
                String htmlElement = getResources().getStringArray(R.array.html_elements_types)[elementPos];

                html.open(htmlElement);
                if (checkBoxBold.isChecked())
                    html.b();

                if (checkBoxUnderScore.isChecked())
                    html.u();

                if (elementPos != UNORDERED_LIST_POS && elementPos != ORDERED_LIST_POS) {
                    // header or paragraph

                    html.append(fromEditTest);
                } else {
                    //list separated by \n

                    String[] rows = fromEditTest.split("\\r?\\n");
                    for (String row : rows) {
                        html.open(LIST_ROW, row);
                    }
                }

                if (checkBoxUnderScore.isChecked())
                    html.close(); // close under score

                if (checkBoxBold.isChecked())
                    html.close(); // close bold

                html.close(htmlElement);
            }

            if (checkBoxDivider.isChecked())
                html.append(HORIZONTAL_RULE);

            return html.build();
        }

        public HtmlBuilder buildHtmlFromElement(HtmlBuilder html) {
            if (editText.getText() == null || editText.getText().toString().isEmpty())
                return html;
            String fromEditTest = editText.getText().toString();

            if (stringElement != null && elementPos >= 0) {
                String htmlElement = getResources().getStringArray(R.array.html_elements_types)[elementPos];

                html.open(htmlElement);
                if (checkBoxBold.isChecked())
                    html.b();

                if (checkBoxUnderScore.isChecked())
                    html.u();

                if (elementPos != UNORDERED_LIST_POS && elementPos != ORDERED_LIST_POS) {
                    // header or paragraph

                    html.append(fromEditTest);
                } else {
                    //list separated by \n

                    String[] rows = fromEditTest.split("\\r?\\n");
                    for (String row : rows) {
                        html.open(LIST_ROW, row);
                    }
                }

                if (checkBoxUnderScore.isChecked())
                    html.close(); // close under score

                if (checkBoxBold.isChecked())
                    html.close(); // close bold

                html.close(); // close element
            }

            if (checkBoxDivider.isChecked())
                html.append(HORIZONTAL_RULE);

            return html;
        }

        private Spanned buildHtmlFromElement11() {
            HtmlBuilder html = new HtmlBuilder();
            html.h1("Example Usage");

            html.h3().font("cursive", "Code:").close();
            html.font(0xFFCAE682, "HtmlBuilder")
                    .append(' ')
                    .font(0xFFD4C4A9, "html")
                    .append(' ')
                    .font(0xFF888888, "=")
                    .append(" ")
                    .font(0xFF33B5E5, "new")
                    .append(" ")
                    .font(0xFFCAE682, "HtmlBuilder")
                    .append("()")
                    .br();
            html.font(0xFFD4C4A9, "html")
                    .append(".strong(")
                    .font(0xFF95E454, "\"Strong text\"")
                    .append(").br();")
                    .br();
            html.font(0xFFD4C4A9, "html")
                    .append(".font(")
                    .font(0xFFCAE682, "Color")
                    .append('.')
                    .font(0xFF53DCCD, "RED")
                    .append(", ")
                    .font(0xFF95E454, "\"This will be red text\"")
                    .append(");")
                    .br();
            html.font(0xFFCAE682, "textView")
                    .append(".setText(")
                    .font(0xFFD4C4A9, "html")
                    .append(".build());")
                    .close()
                    .br();

            html.h3().font("cursive", "Result:").close();
            html.strong("Strong text").br().font(Color.RED, "This will be red text");

            html.h1("Supported Tags");
            html.append("&lt;a href=&quot;...&quot;&gt;").br();
            html.append("&lt;b&gt;").br();
            html.append("&lt;big&gt;").br();
            html.append("&lt;blockquote&gt;").br();
            html.append("&lt;br&gt;").br();
            html.append("&lt;cite&gt;").br();
            html.append("&lt;dfn&gt;").br();
            html.append("&lt;div align=&quot;...&quot;&gt;").br();
            html.append("&lt;em&gt;").br();
            html.append("&lt;font color=&quot;...&quot; face=&quot;...&quot;&gt;").br();
            html.append("&lt;h1&gt;").br();
            html.append("&lt;h2&gt;").br();
            html.append("&lt;h3&gt;").br();
            html.append("&lt;h4&gt;").br();
            html.append("&lt;h5&gt;").br();
            html.append("&lt;h6&gt;").br();
            html.append("&lt;i&gt;").br();
            html.append("&lt;img src=&quot;...&quot;&gt;").br();
            html.append("&lt;p&gt;").br();
            html.append("&lt;small&gt;").br();
            html.append("&lt;strike&gt;").br();
            html.append("&lt;strong&gt;").br();
            html.append("&lt;sub&gt;").br();
            html.append("&lt;sup&gt;").br();
            html.append("&lt;tt&gt;").br();
            html.append("&lt;u&gt;").br();
            html.append("&ul;u&gt;").br();
            html.append("&li;u&gt;").br();

            html.h1("Links");
            html.p()
                    .strong().a("https://twitter.com/jaredrummler", "Twitter").close()
                    .append("&nbsp;&nbsp;|&nbsp;&nbsp;")
                    .strong().a("https://github.com/jaredrummler", "GitHub").close()
                    .close();

            return html.build();
        }
    }


}

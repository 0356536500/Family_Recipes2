package com.myapps.ron.family_recipes.ui.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatEditText;
import android.text.Layout;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.jaredrummler.android.util.HtmlBuilder;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.ui.CreateRecipeActivity;
import com.myapps.ron.family_recipes.utils.MyFragment;
import com.myapps.ron.family_recipes.viewmodels.CreateRecipeViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 29/10/2018.
 */
public class AdvancedStepFragment extends MyFragment {

    private List<FlexibleHtmlStructureView> elements;
    private LinearLayout layout;

    private CreateRecipeActivity activity;
    private CreateRecipeViewModel viewModel;

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
        View element = getLayoutInflater().inflate(R.layout.flexible_html_structure, layout, false);
        elements.add(new FlexibleHtmlStructureView(element));

        viewModel =  ViewModelProviders.of(activity).get(CreateRecipeViewModel.class);
    }


    class FlexibleHtmlStructureView {
        private final int UNORDERED_LIST_POS = 3;
        private final int ORDERED_LIST_POS = 4;
        private final String LIST_ROW = "li";

        private Spinner spinner;
        private CheckBox checkBoxDivider, checkBoxBold ;
        private AppCompatEditText editText;

        private String stringElement;
        private int elementPos = 0;

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

        FlexibleHtmlStructureView(View view) {
            spinner = view.findViewById(R.id.advanced_step_choose_type);
            checkBoxDivider = view.findViewById(R.id.advanced_step_horizontal_divider);
            checkBoxBold = view.findViewById(R.id.advanced_step_bold_checkBox);
            editText = view.findViewById(R.id.advanced_step_details_editText);

            initUI();
        }

        private void initUI() {
            //init the spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                    R.array.html_elements, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(spinnerListener);


        }

        public Spanned buildHtmlFromElement() {
            if (editText.getText() == null || editText.getText().toString().isEmpty())
                return null;
            String fromEditTest = editText.getText().toString();
            HtmlBuilder html = new HtmlBuilder();
            if (stringElement != null && elementPos >= 0) {
                String htmlElement = getResources().getStringArray(R.array.html_elements_types)[elementPos];

                html.open(htmlElement);

                if (elementPos != UNORDERED_LIST_POS && elementPos != ORDERED_LIST_POS) {
                    // header or paragraph
                    if(checkBoxBold.isChecked())
                        html.b(fromEditTest);
                    else
                        html.append(fromEditTest);

                } else {
                    //list separated by \n
                    if (checkBoxBold.isChecked())
                        html.b();
                    String[] rows = fromEditTest.split("\\r?\\n");
                    for (String row : rows) {
                        html.open(LIST_ROW, row);
                    }
                    if (checkBoxBold.isChecked())
                        html.close(); // close bold

                    //html.open(htmlElement);
                }

                html.close(htmlElement);
            }
            
            return html.build();
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

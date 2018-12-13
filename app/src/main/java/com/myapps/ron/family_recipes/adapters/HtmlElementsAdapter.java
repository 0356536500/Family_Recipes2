package com.myapps.ron.family_recipes.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.HtmlHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ronginat on 01/11/2018.
 */
public class HtmlElementsAdapter extends RecyclerView.Adapter<HtmlElementsAdapter.FlexibleHtmlStructureHolder>{
    //private final String TAG = getClass().getSimpleName();
    public static final int UNORDERED_LIST_POS = 3;
    public static final int ORDERED_LIST_POS = 4;

    private List<HtmlModel> elements;
    private Context context;

    class FlexibleHtmlStructureHolder extends RecyclerView.ViewHolder {
        private Spinner spinner;
        private CheckBox checkBoxDivider, checkBoxBold, checkBoxUnderScore ;
        private AppCompatEditText editText;

        private HtmlModel model;

        private boolean hasUserTypedInEditText;

        private AdapterView.OnItemSelectedListener spinnerListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                        model.setSpinnerPos(pos);

                        if (pos >= UNORDERED_LIST_POS && pos <= ORDERED_LIST_POS) {
                            Toast toast = Toast.makeText(context, R.string.post_recipe_advanced_step_list_message, Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {
                        model.setSpinnerPos(-1);
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
                model.setText(editable);
                if (!hasUserTypedInEditText && !editable.toString().isEmpty()) {
                    hasUserTypedInEditText = true;
                    addElementToScreen();
                }
            }
        };

        private CheckBox.OnCheckedChangeListener boldListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (model != null)
                    model.setBold(b);
            }
        };

        private CompoundButton.OnCheckedChangeListener underscoreListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (model != null)
                    model.setUnderscore(b);
            }
        };

        private CompoundButton.OnCheckedChangeListener dividerListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (model != null)
                    model.setDivider(b);
            }
        };

        FlexibleHtmlStructureHolder(View view) {
            super(view);
            spinner = view.findViewById(R.id.advanced_step_choose_type);
            checkBoxDivider = view.findViewById(R.id.advanced_step_horizontal_divider);
            checkBoxBold = view.findViewById(R.id.advanced_step_bold_checkBox);
            checkBoxUnderScore = view.findViewById(R.id.advanced_step_under_score_checkBox);
            editText = view.findViewById(R.id.advanced_step_details_editText);

            hasUserTypedInEditText = false;
        }

        private void setModel(HtmlModel model) {
            this.model = model;

            if (model.getText() != null)
                editText.setText(model.getText());
            if (model.getSpinnerPos() > -1)
                spinner.setSelection(model.getSpinnerPos());
            checkBoxBold.setChecked(model.isBold());
            checkBoxUnderScore.setChecked(model.isUnderscore());
            checkBoxDivider.setChecked(model.isDivider());

            initUI();
        }

        private void initUI() {
            //init the spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.html_elements, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(spinnerListener);
            editText.addTextChangedListener(textWatcher);
            checkBoxBold.setOnCheckedChangeListener(boldListener);
            checkBoxUnderScore.setOnCheckedChangeListener(underscoreListener);
            checkBoxDivider.setOnCheckedChangeListener(dividerListener);
        }
    }

    public HtmlElementsAdapter(Context context) {
        this.context = context;
        this.elements = new ArrayList<>();
        addElementToScreen();
    }

    @NonNull
    @Override
    public FlexibleHtmlStructureHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_flexible_html_structure, parent, false);

        return new FlexibleHtmlStructureHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlexibleHtmlStructureHolder holder, int position) {
        Log.e(getClass().getSimpleName(), "bind view position = " + position);
        holder.setModel(elements.get(position));
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    private void addElementToScreen() {
        elements.add(new HtmlModel(context));
        notifyItemInserted(elements.size());
    }

    public boolean checkValidInput() {
        int numberOfValidElements = 0;
        if(elements.size() <= 1)
            return false;
        for (int i = 0; i < elements.size(); i++) {
            //check if any element is not valid except for the last one
            if (!elements.get(i).isElementHasContent() && i < elements.size() - 1)
                return false;
            numberOfValidElements++;
        }
        return numberOfValidElements > Constants.MIN_NUMBER_OF_HTML_ELEMENTS;
    }

    public String generateHtml() {
        HtmlHelper helper = new HtmlHelper();
        helper.openStaticElements();

        for (HtmlModel element : elements) {
            if (element.isElementHasContent())
                helper = element.buildHtmlFromElement(helper);
        }

        helper.closeStaticElements();

        return helper.toString();
    }
}
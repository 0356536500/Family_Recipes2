package com.myapps.ron.family_recipes.adapters;

import android.content.Context;
import android.os.Handler;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.HtmlHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;

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
                    Log.e("afterTextChanged", "addElementToScreen");
                    addElementToScreen(getAdapterPosition());
                }
            }
        };

        private CheckBox.OnCheckedChangeListener checkboxListener = (compoundButton, b) -> {
            if (model != null) {
                switch (compoundButton.getId()) {
                    case R.id.advanced_step_bold_checkBox:
                        model.setBold(b);
                        break;
                    case R.id.advanced_step_under_score_checkBox:
                        model.setUnderscore(b);
                        break;
                    case R.id.advanced_step_horizontal_divider:
                        model.setDivider(b);
                        break;
                }
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
            Log.e("setModel", model.toString());

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
            checkBoxBold.setOnCheckedChangeListener(checkboxListener);
            checkBoxUnderScore.setOnCheckedChangeListener(checkboxListener);
            checkBoxDivider.setOnCheckedChangeListener(checkboxListener);
        }
    }

    public HtmlElementsAdapter(Context context) {
        this.context = context;
        this.elements = new ArrayList<>();
        addElementToScreen(0);
    }

    @NonNull
    @Override
    public FlexibleHtmlStructureHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        //Log.e(TAG, "onCreateViewHolder , pos " + position);
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_flexible_html_structure, parent, false);

        return new FlexibleHtmlStructureHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FlexibleHtmlStructureHolder holder, int position) {
        //Log.e(TAG, "bind view position = " + position);
        holder.setModel(elements.get(position));
        /*holder.fromSample = true;
        if (position == elements.size() - 1)
            holder.fromSample = false;*/
    }

    @Override
    public int getItemCount() {
        return elements.size();
    }

    private void addElementToScreen(int position) {
        Log.e("addElementToScreen", "addElementToScreen");
        if (position == elements.size()) {
            elements.add(new HtmlModel(context));
            notifyItemInserted(elements.size());
        }
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

    public String generateHtml(String...headers) {
        HtmlHelper helper = new HtmlHelper();
        helper.openStaticElements(headers);

        for (HtmlModel element : elements) {
            if (element.isElementHasContent())
                helper = element.buildHtmlFromElement(helper);
        }

        helper.closeStaticElements();

        return helper.toString();
    }

    public void reset() {
        int size = elements.size();
        elements.clear();
        notifyItemRangeRemoved(0, size);
        Log.e("reset", "addElementToScreen");
        addElementToScreen(0);
    }

    public void loadSample() {
        /*for (Constants.HTML_SAMPLE_SPINNER val: Constants.HTML_SAMPLE_SPINNER.values()) {
            Log.e(getClass().getSimpleName(), val.ordinal() + ", " + val.name());
        }*/
        int size = elements.size();
        elements.clear();
        notifyItemRangeRemoved(0, size);
        Editable.Factory factory = Editable.Factory.getInstance();
        //int[] spinnerPostArr = this.context.getResources().getIntArray(R.array.html_elements_types);

        HtmlModel model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.SUB_HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_INFO));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_INGREDIENTS));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.UNORDERED_LIST.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_INGREDIENT_LIST));
        model.setDivider(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_HOW_TO_MAKE));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.ORDERED_LIST.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_HOW_TO_MAKE_STEPS_LIST));
        model.setDivider(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel(this.context);
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.PARAGRAPH.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_CHECK_THE_PREVIEW));
        model.setBold(true);
        model.setUnderscore(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        notifyItemRangeInserted(0, elements.size());
    }
}
package com.myapps.ron.family_recipes.recycler.adapters;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.recycler.helpers.SwipeAndDragHelper;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.logic.HtmlHelper;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.OnTextChanged;

/**
 * Created by ronginat on 01/11/2018.
 * Implementing {@link com.myapps.ron.family_recipes.recycler.helpers.SwipeAndDragHelper.ActionCompletionContract}
 * for Drag, Drop snd Swipe and deleting items
 */
public class HtmlElementsAdapter extends RecyclerView.Adapter<HtmlElementsAdapter.HtmlContentHolder>
        implements SwipeAndDragHelper.ActionCompletionContract{
    //private final String TAG = getClass().getSimpleName();
    public static final int UNORDERED_LIST_POS = 3;
    public static final int ORDERED_LIST_POS = 4;

    private List<HtmlModel> elements;
    private Context context;
    private SwipeAndDragHelper.SwipingRecycler swipingListener;
    private Vibrator vibrator;

    class HtmlContentHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.row_html_expanded_layout)
        ViewGroup expandedLayout;
        @BindView(R.id.row_html_collapsed_layout)
        ViewGroup collapsedLayout;
        @BindView(R.id.row_html_choose_type_spinner)
        Spinner spinner;
        @BindView(R.id.row_html_horizontal_divider_checkBox)
        CheckBox checkBoxDivider;
        @BindView(R.id.row_html_bold_checkBox)
        CheckBox checkBoxBold;
        @BindView(R.id.row_html_under_score_checkBox)
        CheckBox checkBoxUnderScore;
        @BindView(R.id.row_html_details_editText)
        AppCompatEditText editText;
        @BindView(R.id.row_html_collapsed_title_textView)
        TextView titleCollapsedTextView;
        @BindView(R.id.row_html_collapsed_more_details_textView)
        TextView moreDetailsCollapsedTextView;
        @BindView(R.id.row_html_collapse_button)
        ImageButton collapseButton;
        @BindView(R.id.row_html_expand_button)
        ImageButton expandButton;

        private boolean shouldShowHtmlListMessage; //post_recipe_advanced_step_list_message

        @SuppressWarnings("UnusedParameters")
        @OnItemSelected(value = R.id.row_html_choose_type_spinner, callback = OnItemSelected.Callback.ITEM_SELECTED)
        void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            //Log.e(TAG, "onItemSelected, before " + elements.get(getAdapterPosition()).getSpinnerPos() + ", after " + pos);
            elements.get(getAdapterPosition()).setSpinnerPos(pos);
            titleCollapsedTextView.setText(context.getResources().getStringArray(R.array.html_elements)[pos]);

            if (pos >= UNORDERED_LIST_POS && pos <= ORDERED_LIST_POS && shouldShowHtmlListMessage) {
                Toast toast = Toast.makeText(context, R.string.post_recipe_advanced_step_list_message, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            shouldShowHtmlListMessage = true;
        }

        /*@OnItemSelected(value = R.id.row_html_choose_type_spinner, callback = OnItemSelected.Callback.NOTHING_SELECTED)
        void onNothingSelected(AdapterView<?> adapterView) {
            //Log.e(TAG, "onNothingSelected");
            elements.get(getAdapterPosition()).setSpinnerPos(-1);
        }*/

        @OnTextChanged(value = R.id.row_html_details_editText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        void afterTextChanged(Editable editable) {
            if (editText.isFocused()) // only when user typed, not when displaying basic template
                elements.get(getAdapterPosition()).setText(editable);
            moreDetailsCollapsedTextView.setText(editable);
        }

        @OnCheckedChanged({R.id.row_html_horizontal_divider_checkBox, R.id.row_html_bold_checkBox, R.id.row_html_under_score_checkBox})
        void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (elements.get(getAdapterPosition()) != null) {
                switch (compoundButton.getId()) {
                    case R.id.row_html_bold_checkBox:
                        elements.get(getAdapterPosition()).setBold(b);
                        break;
                    case R.id.row_html_under_score_checkBox:
                        elements.get(getAdapterPosition()).setUnderscore(b);
                        break;
                    case R.id.row_html_horizontal_divider_checkBox:
                        elements.get(getAdapterPosition()).setDivider(b);
                        break;
                }
            }
        }

        HtmlContentHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            setSpinnerAdapter();
            titleCollapsedTextView.setText(context.getResources().getStringArray(R.array.html_elements)[0]);
        }

        private void bindTo(HtmlModel model) {
            shouldShowHtmlListMessage = true;
            if (model != null) {
                if (model.getText() != null)
                    editText.setText(model.getText());
                if (model.getSpinnerPos() >= 0) {
                    shouldShowHtmlListMessage = false;
                    spinner.setSelection(model.getSpinnerPos(), false);
                }
                checkBoxBold.setChecked(model.isBold());
                checkBoxUnderScore.setChecked(model.isUnderscore());
                checkBoxDivider.setChecked(model.isDivider());
            }
        }

        private void setSpinnerAdapter() {
            //init the spinner
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                    R.array.html_elements, R.layout.spinner_item_simple);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }

        private void resetViewsOnRecycle() {
            setExpanded(true);
            spinner.setSelection(0, false);
            editText.clearFocus();
            editText.setText("");
            titleCollapsedTextView.setText(context.getResources().getStringArray(R.array.html_elements)[0]);
        }

        // region expand/collapse view holder

        private void setExpanded(boolean isExpanded) {
            vibrate();
            if (!isExpanded) {
                // Hide the textView when it's showing any text
                moreDetailsCollapsedTextView.setVisibility(moreDetailsCollapsedTextView.getText().length() > 0 ? View.VISIBLE : View.GONE);
            }
            expandedLayout.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            collapsedLayout.setVisibility(!isExpanded ? View.VISIBLE : View.GONE);
        }

        @OnClick({R.id.row_html_collapse_button, R.id.row_html_expand_button, R.id.row_html_collapsed_layout})
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.row_html_collapse_button:
                    setExpanded(false);
                    break;
                case R.id.row_html_expand_button: // same as below
                case R.id.row_html_collapsed_layout:
                    setExpanded(true);
                    break;
            }
            notifyItemChanged(getAdapterPosition());
        }

        private void vibrate() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(40);
            }
        }

        // endregion

    }

    public HtmlElementsAdapter(Context context, @NonNull SwipeAndDragHelper.SwipingRecycler swipingListener, @Nullable List<HtmlModel> elements) {
        this.context = context;
        this.swipingListener = swipingListener;
        if (elements != null) {
            this.elements = elements;
            notifyItemRangeInserted(0 , getItemCount());
        }
        else {
            this.elements = new ArrayList<>();
            addElementToScreen();
        }
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    @NonNull
    @Override
    public HtmlContentHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
        //Log.e(TAG, "onCreateViewHolder , pos " + position);
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item_html, parent, false);

        return new HtmlContentHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HtmlContentHolder holder, int position) {
        holder.bindTo(elements.get(position));
    }

    @Override
    public void onViewRecycled(@NonNull HtmlContentHolder holder) {
        super.onViewRecycled(holder);
        holder.resetViewsOnRecycle();
    }

    @Override
    public int getItemCount() {
        if (elements != null)
            return elements.size();
        return 0;
    }

    public List<HtmlModel> getElements() {
        return elements;
    }

    public void addElementToScreen() {
        elements.add(new HtmlModel());
        notifyItemInserted(elements.size() - 1);
    }

    public void insertItem(HtmlModel item, int position) {
        this.elements.add(position, item);
        notifyItemInserted(position);
    }

    // region swipe and drag

    @Override
    public void onViewMoved(int oldPosition, int newPosition) {
        HtmlModel item = elements.get(oldPosition);
        //User user = new User(targetUser);
        elements.remove(oldPosition);
        elements.add(newPosition, item);
        notifyItemMoved(oldPosition, newPosition);
    }

    @Override
    public void onViewSwiped(int position) {
        HtmlModel item = elements.remove(position);
        notifyItemRemoved(position);
        swipingListener.onItemRemoved(item, position);
    }

    // endregion



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
        return numberOfValidElements >= Constants.MIN_NUMBER_OF_HTML_ELEMENTS;
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

    public void reset() {
        int size = elements.size();
        elements.clear();
        notifyItemRangeRemoved(0, size);
        addElementToScreen();
    }

    public void loadTemplate() {
        int size = elements.size();
        elements.clear();
        notifyItemRangeRemoved(0, size);
        Editable.Factory factory = Editable.Factory.getInstance();
        //int[] spinnerPostArr = this.context.getResources().getIntArray(R.array.html_elements_types);

        HtmlModel model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.SUB_HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_INFO));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_INGREDIENTS));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.UNORDERED_LIST.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_INGREDIENT_LIST));
        model.setDivider(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.HEADER.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_HOW_TO_MAKE));
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.ORDERED_LIST.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_HOW_TO_MAKE_STEPS_LIST));
        model.setDivider(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        model = new HtmlModel();
        model.setSpinnerPos(Constants.HTML_SAMPLE_SPINNER.PARAGRAPH.ordinal());
        model.setText(factory.newEditable(Constants.HTML_SAMPLE_TEXT_CHECK_THE_PREVIEW));
        model.setBold(true);
        model.setUnderscore(true);
        elements.add(model);
        //notifyItemInserted(elements.size());

        notifyItemRangeInserted(0, elements.size());
    }
}
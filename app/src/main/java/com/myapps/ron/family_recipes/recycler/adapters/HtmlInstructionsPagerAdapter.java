package com.myapps.ron.family_recipes.recycler.adapters;

import android.content.Context;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.model.HtmlModel;
import com.myapps.ron.family_recipes.utils.Constants;
import com.myapps.ron.family_recipes.utils.GlideApp;
import com.myapps.ron.family_recipes.utils.HtmlHelper;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.viewpager.widget.PagerAdapter;

/**
 * Created by ronginat on 17/03/2019.
 */
public class HtmlInstructionsPagerAdapter extends PagerAdapter {
    private static final float DIMMED_VIEWS_ALPHA = 0.5f;
    private final int INSTRUCTIONS_SIZE = 5;
    private Context context;
    private Animation fadeIn;

    public HtmlInstructionsPagerAdapter(Context context) {
        this.context = context;
        fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(1000);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (position < INSTRUCTIONS_SIZE) {
            View item = inflateItem(container, position);
            container.addView(item);
            return item;
        }
        return super.instantiateItem(container, position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public int getCount() {
        return INSTRUCTIONS_SIZE;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    // region Init Layouts

    @NonNull
    private View inflateItem(@NonNull ViewGroup container, int position) {
        View view;
        switch (position) {
            case 0:
                view = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.pager_instructions_general_text, container, false);
                break;
            case 4:
                view = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.pager_instructions_drag_drop, container, false);
                break;
            default:
                view = LayoutInflater.from(container.getContext())
                        .inflate(R.layout.pager_instructions_cardview_with_webview, container, false);
                break;
        }
        fillItemWithContent(view, position);
        return view;
    }

    private void fillItemWithContent(@NonNull View view, int position) {
        switch (position) {
            case 0:
                generateGeneralExplanation(view);
                break;
            case 1:
                generateCardWithHtmlForHeader(view);
                break;
            case 2:
                generateCardWithHtmlForUnderscoreAndParagraph(view);
                break;
            case 3:
                generateCardWithHtmlForUnorderedListWithDivider(view);
                break;
            case 4:
                generateDragDropSwipeAnimatedGif(view);
                break;
        }
    }

    // region Init Content
    /**
     * Displays general motivation for the recycler view
     * @param view to populate with content
     */
    private void generateGeneralExplanation(@NonNull View view) {
        // bind the views
        TextView textView1 = view.findViewById(R.id.pager_instructions_general_textView1);
        TextView textView2 = view.findViewById(R.id.pager_instructions_general_textView2);
        TextView textView3 = view.findViewById(R.id.pager_instructions_general_textView3);

        /*Typeface typeFaceBold;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeFaceBold = context.getResources().getFont(R.font.open_sans_extrabold);
        } else {
            typeFaceBold = ResourcesCompat.getFont(context, R.font.open_sans_extrabold);
        }*/
        //Typeface typeFaceSemiBold = Typeface.createFromAsset(context.getAssets(), "font/open_sans_semibold.ttf");
        textView1.setText(R.string.post_recipe_instructions_general_help1);
        textView2.setText(R.string.post_recipe_instructions_general_help2);
        textView3.setText(R.string.post_recipe_instructions_general_help3);

        //textView1.setTypeface(typeFaceBold);

        // animate the views with fade in animation
        textView1.startAnimation(fadeIn);
        textView2.startAnimation(fadeIn);
        textView3.startAnimation(fadeIn);
    }

    /**
     * The card contains header text
     * @param view to populate with content
     */
    private void generateCardWithHtmlForHeader(@NonNull View view) {
        dimmedViews(view, R.id.row_html_bold_checkBox, R.id.row_html_under_score_checkBox, R.id.row_html_horizontal_divider_checkBox);

        EditText elementEditText = view.findViewById(R.id.row_html_details_editText);
        WebView webView = view.findViewById(R.id.pager_instructions_cardView_with_webView_html);

        disableAllViewsInCardWithHtmlLayout(view);

        // populate views
        setSpinnerAdapterAndSelection(view.findViewById(R.id.row_html_choose_type_spinner), Constants.HTML_SAMPLE_SPINNER.HEADER.ordinal());

        Editable editable = getEditableFromResource(R.string.post_recipe_instructions_card_with_html_for_header);
        elementEditText.setText(editable);

        webView.loadData(generateHtml(createModelFromView(view)), "text/html", "utf-8");
    }

    /**
     * The card contains paragraph text with underscore checked
     * @param view to populate with content
     */
    private void generateCardWithHtmlForUnderscoreAndParagraph(@NonNull View view) {
        dimmedViews(view, R.id.row_html_horizontal_divider_checkBox, R.id.row_html_bold_checkBox);
        ((CheckBox)view.findViewById(R.id.row_html_under_score_checkBox)).setChecked(true);

        EditText elementEditText = view.findViewById(R.id.row_html_details_editText);
        WebView webView = view.findViewById(R.id.pager_instructions_cardView_with_webView_html);

        disableAllViewsInCardWithHtmlLayout(view);

        // populate views
        setSpinnerAdapterAndSelection(view.findViewById(R.id.row_html_choose_type_spinner), Constants.HTML_SAMPLE_SPINNER.PARAGRAPH.ordinal());


        Editable editable = getEditableFromResource(R.string.post_recipe_instructions_card_with_html_for_paragraph_with_underscore);
        elementEditText.setText(editable);

        webView.loadData(generateHtml(createModelFromView(view)), "text/html", "utf-8");
    }

    /**
     * The card contains unordered list divider checked
     * @param view to populate with content
     */
    private void generateCardWithHtmlForUnorderedListWithDivider(@NonNull View view) {
        dimmedViews(view, R.id.row_html_bold_checkBox, R.id.row_html_under_score_checkBox);
        ((CheckBox)view.findViewById(R.id.row_html_horizontal_divider_checkBox)).setChecked(true);

        EditText elementEditText = view.findViewById(R.id.row_html_details_editText);
        WebView webView = view.findViewById(R.id.pager_instructions_cardView_with_webView_html);

        disableAllViewsInCardWithHtmlLayout(view);

        // populate views
        setSpinnerAdapterAndSelection(view.findViewById(R.id.row_html_choose_type_spinner), Constants.HTML_SAMPLE_SPINNER.UNORDERED_LIST.ordinal());


        Editable editable = getEditableFromResource(R.string.post_recipe_instructions_card_with_html_for_unordered_list);
        elementEditText.setText(editable);

        webView.loadData(generateHtml(createModelFromView(view)), "text/html", "utf-8");
    }

    /**
     * Displays animated gif of drag, drop and swipe
     * with extra info
     * @param view to populate with content
     */
    private void generateDragDropSwipeAnimatedGif(@NonNull View view) {
        ImageView imageView = view.findViewById(R.id.pager_instructions_drag_drop_swipe_image);
        TextView dragDropSwipeTextView = view.findViewById(R.id.pager_instructions_drag_drop_swipe_info1_textView);
        TextView expandCollapseTextView = view.findViewById(R.id.pager_instructions_drag_drop_swipe_info2_textView);

        dragDropSwipeTextView.setText(R.string.post_recipe_instructions_recycler_features_drag_drop_swipe);
        expandCollapseTextView.setText(R.string.post_recipe_instructions_recycler_features_expand_collapse);

        dragDropSwipeTextView.startAnimation(fadeIn);
        expandCollapseTextView.startAnimation(fadeIn);

        GlideApp.with(context)
                .asGif()
                .load(R.drawable.drag_drop_swipe_animation) // try move the gif to R.raw folder
                .into(imageView);
    }

    // endregion

    // region Helpers

    private void disableAllViewsInCardWithHtmlLayout(@NonNull View view) {
        view.findViewById(R.id.row_html_bold_checkBox).setClickable(false);
        view.findViewById(R.id.row_html_under_score_checkBox).setClickable(false);
        view.findViewById(R.id.row_html_horizontal_divider_checkBox).setClickable(false);
        view.findViewById(R.id.row_html_choose_type_spinner).setClickable(false);
        view.findViewById(R.id.row_html_details_editText).setClickable(false);
        //view.findViewById(R.id.pager_instructions_cardView_with_webView_html).setEnabled(false);
    }

    private void dimmedViews(@NonNull View container, @NonNull @IdRes int...resIds) {
        for (int resId: resIds) {
            container.findViewById(resId).setAlpha(DIMMED_VIEWS_ALPHA);
        }
    }

    private void setSpinnerAdapterAndSelection(@NonNull Spinner spinner, int selection) {
        //init the spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.html_elements, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setSelection(selection);
    }

    private Editable getEditableFromResource(@StringRes int resId) {
        return Editable.Factory.getInstance().newEditable(context.getText(resId));
    }

    private String generateHtml(HtmlModel model) {
        HtmlHelper helper = new HtmlHelper();
        helper.openStaticElements();

        helper = model.buildHtmlFromElement(helper);

        helper.closeStaticElements();

        return helper.toString();
    }

    private HtmlModel createModelFromView(@NonNull View view) {
        HtmlModel model = new HtmlModel();
        model.setBold(((CheckBox)view.findViewById(R.id.row_html_bold_checkBox)).isChecked());
        model.setUnderscore(((CheckBox)view.findViewById(R.id.row_html_under_score_checkBox)).isChecked());
        model.setDivider(((CheckBox)view.findViewById(R.id.row_html_horizontal_divider_checkBox)).isChecked());

        model.setSpinnerPos(((Spinner)view.findViewById(R.id.row_html_choose_type_spinner)).getSelectedItemPosition());
        model.setText(((EditText)view.findViewById(R.id.row_html_details_editText)).getText());

        return model;
    }

    // endregion

    // endregion
}
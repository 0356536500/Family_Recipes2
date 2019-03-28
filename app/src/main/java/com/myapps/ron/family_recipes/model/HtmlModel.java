package com.myapps.ron.family_recipes.model;

import android.content.Context;
import android.text.Editable;

import com.myapps.ron.family_recipes.MyApplication;
import com.myapps.ron.family_recipes.R;
import com.myapps.ron.family_recipes.utils.logic.HtmlHelper;

import java.io.Serializable;

import androidx.annotation.NonNull;

import static com.myapps.ron.family_recipes.recycler.adapters.HtmlElementsAdapter.ORDERED_LIST_POS;
import static com.myapps.ron.family_recipes.recycler.adapters.HtmlElementsAdapter.UNORDERED_LIST_POS;

/**
 * Created by ronginat on 01/11/2018.
 */
public class HtmlModel implements Serializable {

    private int spinnerPos;
    private Editable text;
    private boolean bold, underscore, divider;

    public HtmlModel() {
        text = null;
        spinnerPos = -1;
        bold = false;
        underscore = false;
        divider = false;
    }

    public int getSpinnerPos() {
        return spinnerPos;
    }

    public void setSpinnerPos(int spinnerPos) {
        this.spinnerPos = spinnerPos;
    }

    public Editable getText() {
        return text;
        /*if (text != null)
            return Html.escapeHtml(text);
        return null;*/
    }

    public void setText(Editable text) {
        this.text = text;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isUnderscore() {
        return underscore;
    }

    public void setUnderscore(boolean underscore) {
        this.underscore = underscore;
    }

    public boolean isDivider() {
        return divider;
    }

    public void setDivider(boolean divider) {
        this.divider = divider;
    }

    public boolean isElementHasContent() {
        return text != null && text.toString().length() > 0;
    }

    public HtmlHelper buildHtmlFromElement(HtmlHelper helper) {
        if (text == null || text.toString().isEmpty())
            return helper;
        String fromEditText = getText().toString();

        if (spinnerPos >= 0) {
            String htmlElement = MyApplication.getContext().getResources().getStringArray(R.array.html_elements_types)[spinnerPos];

            helper.openElement(htmlElement); // can be list/header/paragraph

            if (bold)
                helper.openElement("b");

            if (underscore)
                helper.openElement("ins");

            if (spinnerPos != UNORDERED_LIST_POS && spinnerPos != ORDERED_LIST_POS) {
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

            if (underscore)
                helper.closeElement(); // close under score

            if (bold)
                helper.closeElement(); // close bold

            helper.closeElement(); // close main element of this view
        }

        if (divider)
            helper.addTagToBuilder(HtmlHelper.HORIZONTAL_RULE);

        return helper;
    }

    public String getSpinnerText(Context context) {
        return context.getResources().getStringArray(R.array.html_elements)[getSpinnerPos()];
    }

    @NonNull
    @Override
    public String toString() {
        return "HtmlModel{" +
                "spinnerPos=" + spinnerPos +
                ", text=" + text +
                '}';
    }
}

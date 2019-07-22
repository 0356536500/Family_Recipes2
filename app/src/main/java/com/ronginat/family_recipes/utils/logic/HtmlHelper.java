package com.ronginat.family_recipes.utils.logic;

import android.content.Context;

import androidx.annotation.NonNull;

import com.amazonaws.util.IOUtils;
import com.ronginat.family_recipes.BuildConfig;
import com.ronginat.family_recipes.MyApplication;

import java.io.IOException;
import java.util.Stack;

/**
 * Created by ronginat on 30/10/2018.
 *
 * elements are something that <strong>must</strong>must contain more elements or tags inside, like <body>
 * or <h1>. elements entering order saved in stack and closed by it.
 * tags are same as element but aren't saved in stack
 */
public class HtmlHelper {

    //private static final String BODY_TEXT_STYLE = "style=font-family:sans-serif";
    private static final String DIV_META_STYLE_HEB = "dir=rtl lang=he class=recipe";
    private static final String LINE_BREAK = "br";
    public static final String HORIZONTAL_RULE = "hr";
    public static final String LIST_ROW = "li";
    public static final String PARAGRAPH = "p";

    private StringBuilder builder;
    private Stack<String> containers;

    public HtmlHelper() {
        this.builder = new StringBuilder();
        this.containers = new Stack<>();
    }

    private String openContainer(String tag) {
        containers.push(tag);
        return "<" + tag + ">";
    }

    private String addTag(String tag) {
        return "<" + tag + ">";
    }

    private String closeTag(String tag) {
        return "</" + tag + ">";
    }

    @SuppressWarnings("SameParameterValue")
    /*private String autoCloseTagWithAttributes(String tag, String attributes) {
        return "<" + tag + " " + attributes + " />";
    }*/

    private String closeContainer() {
        if (!containers.empty()) {
            return closeTag(containers.pop());
        }
        return null;
    }

    public void addTagToBuilder(String tag) {
        builder.append(addTag(tag));
    }

    /*public void addTagWithAttributeToBuilder(String tag, String key, String value) {
        builder.append(addTag(tag + " " + key + "=" + value));
    }*/


    public void openStaticElements() {
        builder.append(addTag("div " + DIV_META_STYLE_HEB));
        //builder.append(addTag("div dir=rtl lang=he " + BODY_TEXT_STYLE));
    }

    public void openStaticElementsForInstructions() {
        if ("he".equals(MyApplication.getLocale()))
            builder.append(addTag("div " + DIV_META_STYLE_HEB));
            //builder.append(addTag("div dir=rtl lang=he " + BODY_TEXT_STYLE));
        else
            builder.append(addTag("div dir=ltr lang=en"));
            //builder.append(addTag("div dir=ltr lang=en " + BODY_TEXT_STYLE));
    }

    public void closeStaticElements() {
        builder.append(closeTag("div"));
    }

    /*private void closeTagInBuilder(String tag) {
        builder.append(closeTag(tag));
    }
    public void openStaticElements(String...metaHeaders) {
        builder.append(addTag("!DOCTYPE html"));
        builder.append(addTag("html dir=rtl lang=he"));
        addTagToBuilder("head");
        addTagWithAttributeToBuilder("meta", "charset", "utf-8");
        if (metaHeaders != null && metaHeaders.length >= 2) {
            openElement("title", metaHeaders[0]);
            builder.append(addTag("meta name=" + "\"description\"" +  " content=" + "\"" + metaHeaders[1] + "\""));
            builder.append(autoCloseTagWithAttributes("meta", "property=og:title content=" + "\"" + metaHeaders[0] + "\""));
            builder.append(autoCloseTagWithAttributes("meta", "property=og:description content=" + "\"" + metaHeaders[1] + "\""));
        }
        closeTagInBuilder("head");
        builder.append(addTag("body " + BODY_TEXT_STYLE));
    }*/

    /*public void closeStaticElements() {
        closeTagInBuilder("body");
        closeTagInBuilder("html");
    }*/

    public void openElement(String element) {
        builder.append(openContainer(element));
    }

    public void openElement(String element, String text) {
        builder.append(openContainer(element));
        builder.append(text);
        builder.append(closeContainer());
    }

    /*public void openElement(String element, boolean addToStack) {
        if (addToStack)
            openElement(element);
        else
            addTagToBuilder(element);
    }

    public void closeElement(String element, boolean isInStack) {
        if (isInStack)
            closeElement();
        else
            closeTagInBuilder(element);
    }*/

    public void closeElement() {
        builder.append(closeContainer());
    }

    public void append(String text) {
        builder.append(text);
    }

    public void append(String[] rows) {
        if (rows != null) {
            for (String row : rows) {
                builder.append(row);
                addTagToBuilder(LINE_BREAK);
            }
        }
    }

    @NonNull
    @Override
    public String toString() {
        return builder.toString();
    }


    public static String GET_CSS_LINK() {
        // font from online googleapis
        //String openSans = "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans&display=swap\" rel=\"stylesheet\">";

        String styleCss = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style_%s.css\" />";
        String style = MyApplication.isDarkTheme() ? "dark" : "light";
        return String.format(styleCss, style);
    }

    public static String GET_ABOUT_PAGE(@NonNull Context context) {
        String locale = MyApplication.getLocale();

        try {
            String aboutFile = "about_%s.html";
            String about = IOUtils.toString(context.getResources().getAssets().open(String.format(aboutFile, locale)));
            return HtmlHelper.GET_CSS_LINK() + about + String.format("<span style=\"font-size=smaller\">version %s</span>", BuildConfig.VERSION_NAME);
        } catch (IOException e) {
            e.printStackTrace();
            return "<h3>Failed to load about page</h3>";
        }
    }
}

package com.myapps.ron.family_recipes.utils.logic;

import java.util.Stack;

import androidx.annotation.NonNull;

/**
 * Created by ronginat on 30/10/2018.
 *
 * elements are something that <strong>must</strong>must contain more elements or tags inside, like <body>
 * or <h1>. elements entering order saved in stack and closed by it.
 * tags are same as element but aren't saved in stack
 */
public class HtmlHelper {

    private static final String BODY_TEXT_STYLE = "style=\"font-family:sans;\"";
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
    private String autoCloseTagWithAttributes(String tag, String attributes) {
        return "<" + tag + " " + attributes + " />";
    }

    private String closeContainer() {
        if (!containers.empty()) {
            return closeTag(containers.pop());
        }
        return null;
    }

    public void addTagToBuilder(String tag) {
        builder.append(addTag(tag));
    }

    public void addTagWithAttributeToBuilder(String tag, String key, String value) {
        builder.append(addTag(tag + " " + key + "=" + value));
    }

    public void closeTagInBuilder(String tag) {
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
    }

    public void closeStaticElements() {
        closeTagInBuilder("body");
        closeTagInBuilder("html");
    }

    public void openElement(String element) {
        builder.append(openContainer(element));
    }

    public void openElement(String element, String text) {
        builder.append(openContainer(element));
        builder.append(text);
        builder.append(closeContainer());
    }

    public void openElement(String element, boolean addToStack) {
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
    }

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
}
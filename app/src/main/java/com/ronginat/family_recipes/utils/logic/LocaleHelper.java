package com.ronginat.family_recipes.utils.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.ronginat.family_recipes.R;

import java.util.Locale;

/**
 * Created by ronginat on 13/12/2018.
 *
 * taken from https://gunhansancar.com/change-language-programmatically-in-android/
 */
public class LocaleHelper {
    //private static final String SELECTED_LANGUAGE = "Locale.Helper.Selected.Language";

    public static Context onAttach(Context context) {
        String lang = loadLanguage(context, Locale.getDefault().getLanguage());
        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = loadLanguage(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static Locale getLocale(Context context) {
        return new Locale(loadLanguage(context, Locale.getDefault().getLanguage()));
    }

    public static Context setLocale(Context context, String language) {
        //persist(context, language);
        return updateResources(context, language);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);*/
    }

    private static String loadLanguage(Context context, String defaultLanguage) {
        /*SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);*/
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        return preferences.getString(context.getString(R.string.preference_key_language), defaultLanguage);
        //return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.sharedPreferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        //editor.putString(SELECTED_LANGUAGE, language);
        editor.putString(context.getString(R.string.preference_key_language), language);
        editor.apply();
    }

    //@TargetApi(Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        /*LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLocales(localeList);
        configuration.setLayoutDirection(locale);*/

        return context.createConfigurationContext(configuration);
    }

    @SuppressWarnings("deprecation")
    private static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        configuration.setLayoutDirection(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}

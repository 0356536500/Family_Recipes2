package com.ronginat.family_recipes;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.gson.Gson;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.utils.logic.DateUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = ApplicationProvider.getApplicationContext();

        assertEquals("com.myapps.family_recipes", appContext.getPackageName());
    }

    @Test
    public void gsonParseTest() {
        Gson gson = new Gson();
        String string = gson.toJson(Arrays.asList("test0", "test1", "test2"));
        Log.e(getClass().getSimpleName(), string);
        assertTrue(string.contains("\"test1\""));
    }

    //@Test
    public void mailMessageTest() {
        Context context = ApplicationProvider.getApplicationContext();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(AppHelper.getCurrUser())
                .append(" -- I've got an issue with ")
                .append(context.getString(R.string.app_name).concat(" App\n\n"))
                .append(context.getString(R.string.settings_report_bug_mail_list_problem_here))
                .append("\n------------------\n")
                .append("Application and System information:\n")
                .append("Device Model: ")
                .append(Build.BRAND.toUpperCase().concat(" "))
                .append(Build.BRAND.concat(" "))
                .append(Build.MODEL.concat("\n"))
                .append("Android OS Version: ")
                .append(Build.VERSION.RELEASE.concat("\n"))
                .append(context.getString(R.string.app_name).concat(" App Version: "))
                .append(BuildConfig.VERSION_NAME.concat("\n"));


        Log.e("mailMessageTest", stringBuilder.toString());
        assertTrue(true);
    }

    @Test
    public void intentExtrasTest() {
        Intent intent1 = new Intent();
        intent1.putExtra("att1", "val1");
        intent1.putExtra("att2", "val2");

        Intent intent2 = new Intent();
        intent2.putExtras(intent1);

        Intent intent3 = new Intent();
        intent3.putExtras(intent1);

        assertEquals("val1", intent1.getExtras().getString("att1"));
        assertEquals("val1", intent2.getExtras().getString("att1"));
        assertEquals("val2", intent3.getExtras().getString("att2"));
    }

    @Test
    public void parseUriTest() {
        String id = "kE5zymiS_wD_";
        String date = "2019-08-16T15:48:24.095Z";
        //String str =  "https://www.familyrecipes.com/recipes/kE5zymiS_wD_?" + com.ronginat.family_recipes.utils.Constants.SHARE_DATE_QUERY + "=" + date;
        String uriStr = "https://www.familyrecipes.com/recipes/a0U1enltaVNfd0Rf?d=MjAxOS0wOC0xNlQxNDo1NDo0My4yMDJa";
        Uri uri = Uri.parse(uriStr);
        //Log.e("parseUriTest", uri.getPathSegments().toString());
        //Log.e("parseUriTest", uri.getQueryParameterNames().toString());
        //Log.e("parseUriTest", uri.getQueryParameter(com.ronginat.family_recipes.utils.Constants.SHARE_DATE_QUERY));
        assertEquals(uri.getLastPathSegment(), id);
        assertEquals(uri.getQueryParameter(com.ronginat.family_recipes.utils.Constants.SHARE_DATE_QUERY), date);
    }

    @Test
    public void encodeUriTest() {
        String id = "kE5zymiS_wD_";
        String date = DateUtil.getUTCTime();
        Log.e("encodeUriTest", "decoded date, " + date);
        String encodedId = Base64.encodeToString(id.getBytes(), Base64.URL_SAFE);
        String encodedDate = Base64.encodeToString(date.getBytes(), Base64.URL_SAFE);
        assertEquals(date, new String(Base64.decode(encodedDate, Base64.URL_SAFE)));
        Log.e("encodeUriTest", "encoded id, " + encodedId);
        Log.e("encodeUriTest", "encoded date, " + encodedDate);
        String url = ApplicationProvider.getApplicationContext().getString(R.string.share_url, encodedId, encodedDate);
        Log.e("encodeUriTest", Uri.parse(url).toString());
        assertTrue(true);
    }

    private CompositeDisposable compositeDisposable;

    //@Before
    public void initCompositeDisposable() {
        compositeDisposable = new CompositeDisposable();
    }

    //@After
    public void clearCompositeDisposable() {
        compositeDisposable.clear();
    }

    //@Test
    public void catchEmptyValueFromMaybeTest() {
        compositeDisposable.add(Maybe.empty()
                .subscribe(Assert::assertNull));
    }
}

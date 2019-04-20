package com.myapps.ron.family_recipes;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.myapps.ron.family_recipes.layout.cognito.AppHelper;

import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

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
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.myapps.ron.family_recipes", appContext.getPackageName());
    }

    @Test
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

    private CompositeDisposable compositeDisposable;

    @Before
    public void initCompositeDisposable() {
        compositeDisposable = new CompositeDisposable();
    }

    @After
    public void clearCompositeDisposable() {
        compositeDisposable.clear();
    }

    @Test
    public void catchEmptyValueFromMaybeTest() {
        compositeDisposable.add(Maybe.empty()
                .subscribe(Assert::assertNull));
    }
}

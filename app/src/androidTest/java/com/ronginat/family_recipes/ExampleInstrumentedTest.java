package com.ronginat.family_recipes;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.ronginat.family_recipes.layout.Constants;
import com.ronginat.family_recipes.layout.cognito.AppHelper;
import com.ronginat.family_recipes.logic.storage.StorageWrapper;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

import io.reactivex.Maybe;
import io.reactivex.disposables.CompositeDisposable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
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
    public void testCompression() {
        //Uri uri = Uri.parse("/storage/emulated/0/Download/eagckh5.jpg");
        //String realPath = "/storage/emulated/0/Download/eagckh5.jpg";//StorageWrapper.getRealPathFromURI(ApplicationProvider.getApplicationContext(), uri);
        //String realPath = "/storage/emulated/0/Download/legion_ver5_xxlg.jpg";
        //String realPath = "/storage/emulated/0/Download/20190520_121440.jpg"; // 10MB > 902KB > 224KB (-10 quality) | 580KB (-5 quality)
        //String realPath = "/storage/emulated/0/Download/20190119_114310.jpg"; // 7MB > 585KB
        //String compressPath = StorageWrapper.compressFile(ApplicationProvider.getApplicationContext(), realPath);

        String path = "/storage/emulated/0/Android/data/com.myapps.family_recipes/files/Pictures/test_large.jpg"; // ~6MB
        String compressPath = StorageWrapper.compressFile(ApplicationProvider.getApplicationContext(), path);

        //assertTrue(!realPath.equals(compressPath));
        assertThat(new File(compressPath).length(), Matchers.lessThan(new File(path).length()));
    }

    //@Test
    public void copyFileTest() {
        String path = "/storage/emulated/0/Android/data/com.myapps.family_recipes/files/Pictures/food--1.jpg";
        String cloned = StorageWrapper.compressFile(ApplicationProvider.getApplicationContext(), path);
        assertEquals(new File(path).length(), new File(cloned).length());
    }

    //@Test
    public void rotationFromGalleryTest() {
        String path = "/storage/emulated/0/Android/data/com.myapps.family_recipes/files/Pictures/test.jpg";
        File pictures = ApplicationProvider.getApplicationContext().getExternalFilesDir(Constants.TEMP_IMAGES_DIR);
        File image = new File(pictures, "test.jpg");
        StorageWrapper.rotateImageIfRequired(ApplicationProvider.getApplicationContext(), Uri.fromFile(image));
        assertTrue(true);
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

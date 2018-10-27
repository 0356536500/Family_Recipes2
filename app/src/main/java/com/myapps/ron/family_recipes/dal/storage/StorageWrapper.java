package com.myapps.ron.family_recipes.dal.storage;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.Constants;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class StorageWrapper {

    //private RecipesDBHelper dbHelper;
    private static StorageWrapper storage;
    //private Context context;

    public static StorageWrapper getInstance(Context context) {
        if(storage == null) {
            storage = new StorageWrapper(context);
            //storage.context = context;
        }

        return storage;
    }

    private StorageWrapper(Context context) {
        /*dbHelper = new RecipesDBHelper(context)*/;
    }

    public static void getFoodFile(Context context, String fileName, MyCallback<String> callback) {
        if(fileName == null || fileName.equals(""))
            return;
        String path = ExternalStorageHelper.getFileAbsolutePath(context, fileName, Constants.FOOD_DIR);
        //Log.e("StorageWrapper", "get local path - " + path);
        if(path != null)
            callback.onFinished(path);
        else {
            OnlineStorageWrapper.downloadFoodFile(context, fileName, callback);
        }
    }

    public static void getRecipeFile(Context context, String fileName, MyCallback<String> callback) {
        if(fileName == null || fileName.equals(""))
            return;
        String path = ExternalStorageHelper.getFileAbsolutePath(context, fileName, Constants.RECIPES_DIR);
        //Log.e("StorageWrapper", "get local path - " + path);
        if(path != null)
            callback.onFinished(path);
        else {
            OnlineStorageWrapper.downloadRecipeFile(context, fileName, callback);
        }
    }

    public File createHtmlFile(Context context, String fileName, Spanned spanned) {
        //String path = Environment.getExternalStorageDirectory().getPath();
        //String fileName = DateFormat.format("dd_MM_yyyy_hh_mm_ss", System.currentTimeMillis()).toString();
        //fileName = fileName + ".html";
        //String path = ExternalStorageHelper.getFileAbsolutePath(context, fileName, com.myapps.ron.family_recipes.network.Constants.RECIPES_DIR);
        String path = context.getFilesDir().getPath();
        Log.e("StorageWrapper", "path is " + path);
        File file = new File(path, fileName);
        if(file.exists()) {
            try {
                file.delete();
                file.createNewFile();
            } catch (IOException e) {
                Log.e("StorageWrapper", e.getMessage());
            }
        }
        //String html = "<html><head><title>Title</title></head><body>This is random text.</body></html>";

        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] data = Html.toHtml(spanned).getBytes();
            out.write(data);
            out.close();
            Log.e("StorageWrapper", "createHtml File Saved : " + file.getPath());
        }/* catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/ catch (IOException e) {
            Log.e("StorageWrapper", "createHtml error: " + e.getMessage());
            return null;
        }

        return file;
    }

/*    private void showChoosingFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (bitmap != null) {
            bitmap.recycle();
        }

        if (requestCode == CHOOSING_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private boolean validateInputFileName(String fileName) {

        if (TextUtils.isEmpty(fileName)) {
            Toast.makeText(this, "Enter file name!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createFile(Context context, Uri srcUri, File dstFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return;
            OutputStream outputStream = new FileOutputStream(dstFile);
            IOUtils.copy(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /*public void getAllRecipes(final Context context) {
        final String time = DateUtil.getUTCTime();
        APICallsHandler.getAllRecipes(DateUtil.getLastUpdateTime(context), CognitoHelper.getToken(), new MyCallback<List<Recipe>>() {
            @Override
            public void onFinished(List<Recipe> result) {
                HandleServerDataService.startActionUpdateRecipes(context, new ArrayList<>(result), time);
            }
        });
        dbHelper.getAllRecipes();
    }*/

}

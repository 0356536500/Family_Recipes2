package com.myapps.ron.family_recipes.dal.storage;

import android.content.Context;
import android.util.Log;

import com.myapps.ron.family_recipes.model.Recipe;
import com.myapps.ron.family_recipes.network.MyCallback;
import com.myapps.ron.family_recipes.network.S3.OnlineStorageWrapper;

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

    public void getFoodFile(Context context, Recipe recipe, String fileDir, MyCallback<String> callback) {
        String path = ExternalStorageHelper.getFileAbsolutePath(context, recipe.getFoodFiles().get(0), fileDir);
        //Log.e("StorageWrapper", "get local path - " + path);
        if(path != null)
            callback.onFinished(path);
        else {
            OnlineStorageWrapper.downloadFoodFile(context, recipe.getFoodFiles().get(0), callback);
        }
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

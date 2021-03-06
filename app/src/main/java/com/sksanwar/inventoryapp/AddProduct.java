package com.sksanwar.inventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.sksanwar.inventoryapp.data.ProductContract;
import com.sksanwar.inventoryapp.data.ProductContract.ProductEntry;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.os.Environment.getExternalStoragePublicDirectory;
import static com.sksanwar.inventoryapp.Utility.LOG_TAG;

public class AddProduct extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int SOLD = 0;
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    EditText nameEditText;
    EditText priceEditText;
    EditText quantityEditText;
    EditText supplierEmailEditText;
    ImageView imageView;
    Button imageButton;
    Uri mCurrentProductUri;
    String mCurrentPhotoPath;
    private String userChoosenTask;
    private boolean mProductHasChanged = false;
    private boolean isGalleryPicture = false;
    private Uri mUri;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        nameEditText = (EditText)findViewById(R.id.name_add_product_editText);
        priceEditText = (EditText)findViewById(R.id.price_add_product_editText);
        quantityEditText = (EditText)findViewById(R.id.quantity_add_product_editText);
        supplierEmailEditText = (EditText)findViewById(R.id.supplier_add_product_editText);
        imageView = (ImageView)findViewById(R.id.imageView_add_product);
        imageButton = (Button)findViewById(R.id.add_product_image_button);

        mCurrentProductUri = getIntent().getData();

      imageButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              final CharSequence[] items = {getString(R.string.take_photo),
                      getString(R.string.choose_from_library), getString(R.string.cancel)};
              AlertDialog.Builder builder = new AlertDialog.Builder(AddProduct.this);
              builder.setTitle(getString(R.string.add_photo));
              builder.setItems(items, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int item) {
                      boolean result=Utility.checkPermission(AddProduct.this);
                      if (items[item].equals(getString(R.string.take_photo))) {
                          userChoosenTask = getString(R.string.take_photo);
                          if(result)
                              cameraIntent();
                      } else if (items[item].equals(getString(R.string.choose_from_library))) {
                          userChoosenTask = getString(R.string.choose_from_library);
                          if(result)
                              galleryIntent();
                      } else if (items[item].equals(getString(R.string.cancel))) {
                          dialog.dismiss();
                      }
                  }
              });
              builder.show();
          }
      });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals(getString(R.string.take_photo)))
                        cameraIntent();
                    else if (userChoosenTask.equals(getString(R.string.choose_from_library)))
                        galleryIntent();
                } else {
                    Toast.makeText(this, getString(R.string.permission_msg), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void galleryIntent() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_pic)), PICK_IMAGE_REQUEST);
    }

    public void cameraIntent(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try{
            File file =  createImageFile();
            mUri = FileProvider.getUriForFile(getApplication().getApplicationContext(),
                    "com.sksanwar.inventoryapp.fileprovider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        Log.i(LOG_TAG, "Received an \"Activity Result\"");
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                mUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                mBitmap =  Utility.getBitmapFromUri(AddProduct.this, imageView, mUri);
                imageView.setImageBitmap(mBitmap);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                isGalleryPicture = true;
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Log.i(LOG_TAG, "Uri: " + mUri.toString());

            mBitmap = Utility.getBitmapFromUri(AddProduct.this, imageView, mUri);
            imageView.setImageBitmap(mBitmap);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            isGalleryPicture = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor_add_product, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String nameString = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString().trim();
        String quantityString = quantityEditText.getText().toString().trim();
        String emailString = supplierEmailEditText.getText().toString().trim();
        String photoString;

        if (mUri != null) {
            photoString = mUri.toString();
        } else {
            photoString = "";
        }

        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        double price = 0.0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Double.parseDouble(priceString);
        }

        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, emailString);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, SOLD);
        values.put(ProductEntry.COLUMN_PRODUCT_PHOTO, photoString);

        if (isProductExist(nameString)) {
            Toast.makeText(this, getString(R.string.product_exists_msg),
                    Toast.LENGTH_SHORT).show();
        } else if (isValueInvalid(nameString) ||
                isValueInvalid(price) ||
                isValueInvalid(quantity)) {
            Toast.makeText(this, getString(R.string.msg_all_data),
                    Toast.LENGTH_SHORT).show();
            Log.d(LOG_TAG, getString(R.string.notValid_entry));
        } else if (!isEmailValid(emailString)) {
            Toast.makeText(this, getString(R.string.valid_email), Toast.LENGTH_SHORT).show();
        } else {
            this.getContentResolver()
                    .insert(ProductContract.ProductEntry.CONTENT_URI, values);
            if (photoString.isEmpty()) {
                Toast.makeText(this, getString(R.string.product_saved_without_img),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved_msg), Toast.LENGTH_SHORT).show();
                Log.d(LOG_TAG, "Product created into db");
            }
        }
    }

    private boolean isProductExist(String name) {
        Cursor cursor = this.getContentResolver().query(
                ProductContract.ProductEntry.CONTENT_URI,
                new String[]{ProductEntry.COLUMN_PRODUCT_NAME},
                ProductEntry.COLUMN_PRODUCT_NAME + " = ?",
                new String[]{name},
                null);

        boolean productExists;

        if (cursor != null) {
            productExists = cursor.getCount() > 0;
            cursor.close();
        } else {
            productExists = false;
        }
        return productExists;
    }

    private boolean isValueInvalid(String string) {
        return string == null || string.equals("");
    }

    private boolean isValueInvalid(int num) {
        return num < 0;
    }

    private boolean isValueInvalid(double num) {
        return num < 0;
    }

    public boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(emailPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);

        return matcher.matches();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_SALES,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_PRODUCT_PHOTO
        };
        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor.moveToFirst()){

            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PHOTO);

            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            double price = cursor.getDouble(priceColumnIndex);
            String photo = cursor.getString(photoColumnIndex);

            nameEditText.setText(name);
            quantityEditText.setText(Integer.toString(quantity));
            priceEditText.setText(Double.toString(price));

            if (!photo.isEmpty()) {
                mUri = Uri.parse(photo);
                mBitmap = Utility.getBitmapFromUri(this,imageView,mUri);
                imageView.setImageBitmap(mBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.clearComposingText();
        quantityEditText.clearComposingText();
        priceEditText.clearComposingText();
    }
}

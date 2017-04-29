package com.sksanwar.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sksanwar.inventoryapp.data.ProductContract.ProductEntry;

public class ProductDetails extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int PRODUCT_LOADER = 0;
    Uri mCurrentUri;
    Uri mUri;
    TextView nameLabelTextView;
    ImageView imageView;
    TextView priceLabelTextView;
    TextView quantityLabelTextView;
    TextView saleLabelTextView;
    Button makeSaleBtn;
    Button retriveBtn;
    TextView supplierEmail;
    ImageButton orderBtn;
    private String name;
    private String supplier;
    private int quantity;
    private int sold;
    private double price;
    private String photo;
    private boolean mProductHasChanged = false;
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        nameLabelTextView = (TextView) findViewById(R.id.name_detail);
        imageView = (ImageView) findViewById(R.id.imageView_detail);
        priceLabelTextView = (TextView) findViewById(R.id.price_detail);
        quantityLabelTextView = (TextView) findViewById(R.id.quantity_detail);
        saleLabelTextView = (TextView) findViewById(R.id.sold_detail);
        makeSaleBtn = (Button) findViewById(R.id.add_detail_btn);
        retriveBtn = (Button) findViewById(R.id.receive_detail_btn);
        supplierEmail = (TextView) findViewById(R.id.supplier_detail);
        orderBtn = (ImageButton) findViewById(R.id.email_btn);

        mCurrentUri = getIntent().getData();

        makeSaleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (quantityLabelTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityLabelTextView.getText().toString());
                }

                int sold;
                if (saleLabelTextView.getText().toString().isEmpty()) {
                    sold = 0;
                } else {
                    sold = Integer.parseInt(saleLabelTextView.getText().toString());
                }

                if (quantity > 0) {
                    quantity -= 1;
                    sold += 1;

                    quantityLabelTextView.setText(String.valueOf(quantity));
                    saleLabelTextView.setText(String.valueOf(sold));
                } else {
                    Toast.makeText(v.getContext(), "Order Product", Toast.LENGTH_SHORT).show();
                }
            }
        });

        retriveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (quantityLabelTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityLabelTextView.getText().toString());
                }
                quantity = quantity + 1;
                quantityLabelTextView.setText(String.valueOf(quantity));
            }
        });

        orderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] TO = {supplier};
                Intent emailIntent = new Intent(Intent.ACTION_SEND);

                emailIntent.setData(Uri.parse("mailto:"));
                emailIntent.setType("text/plain");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order " + name);
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Please ship " + name +
                        " in quantities " + quantity);
                try {
                    startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        getLoaderManager().initLoader(PRODUCT_LOADER, null, ProductDetails.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(ProductDetails.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(ProductDetails.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveProduct() {
        String quantityString = quantityLabelTextView.getText().toString().trim();
        String soldString = saleLabelTextView.getText().toString().trim();

        ContentValues values = new ContentValues();

        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_SALES, soldString);

        if (mUri == null) {
            Uri uri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(this, getString(R.string.error_saving_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_saved), Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(mCurrentUri, values, null, null);
            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteProduct() {
        if (mCurrentUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.delete_products_failed), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.delete_products_successful), Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
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
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int emailColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int soldColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES);
            int photoColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PHOTO);

            name = cursor.getString(nameColumnIndex);
            supplier = cursor.getString(emailColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            sold = cursor.getInt(soldColumnIndex);
            price = cursor.getDouble(priceColumnIndex);
            photo = cursor.getString(photoColumnIndex);

            nameLabelTextView.setText(name);
            supplierEmail.setText(supplier);
            quantityLabelTextView.setText(Integer.toString(quantity));
            saleLabelTextView.setText(Integer.toString(sold));
            priceLabelTextView.setText("$ " + Double.toString(price));

            if (!photo.isEmpty()) {
                mUri = Uri.parse(photo);
                mBitmap = Utility.getBitmapFromUri(this,imageView, mUri);
                imageView.setImageBitmap(mBitmap);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader loader) {
        nameLabelTextView.clearComposingText();
        quantityLabelTextView.clearComposingText();
        priceLabelTextView.clearComposingText();
    }


}

package com.sksanwar.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sksho on 27-Apr-17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "inventory.db";
    public static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String SQL_CREATE_PRODUCT_TABLE =
                "CREATE TABLE " + ProductContract.ProductEntry.TABLE_NAME + " (" +
                        ProductContract.ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ProductContract.ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE + " DOUBLE NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_PRODUCT_SALES + " INTEGER NOT NULL, " +
                        ProductContract.ProductEntry.COLUMN_SUPPLIER_EMAIL + " TEXT, " +
                        ProductContract.ProductEntry.COLUMN_PRODUCT_PHOTO + " TEXT)";

        db.execSQL(SQL_CREATE_PRODUCT_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}

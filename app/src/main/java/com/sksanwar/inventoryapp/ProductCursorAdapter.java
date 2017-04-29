package com.sksanwar.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.sksanwar.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by sksho on 28-Apr-17.
 */

public class ProductCursorAdapter extends CursorAdapter {
    public static final String LOG_TAG = ProductCursorAdapter.class.getSimpleName();
    private Context mContexts;

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
        mContexts = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_view, parent, false);
    }

    @Override
    public void bindView(final View view, final Context context, Cursor cursor) {

        TextView nameTextView = (TextView) view.findViewById(R.id.product_name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity_text_view);
        final TextView salesTextView = (TextView) view.findViewById(R.id.sold_view);
        TextView priceTextView = (TextView) view.findViewById(R.id.price_view);
        Button saleButton = (Button) view.findViewById(R.id.sell_button);

        final int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        final String nameColumn = cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME));
        final int quantityColumn = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY));
        final int salesColumn = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SALES));
        final double priceColumn = cursor.getDouble(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE));


        String priceStatement = String.valueOf(priceColumn);
        String quantityStatement = String.valueOf(quantityColumn);
        String soldStatement = String.valueOf(salesColumn);

        final Uri currentProductUri = ProductEntry.buildProductUri(id);

        nameTextView.setText(nameColumn);
        quantityTextView.setText(quantityStatement);
        salesTextView.setText(soldStatement);
        priceTextView.setText(priceStatement);

        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int sales;
                if (salesTextView.getText().toString().isEmpty()) {
                    sales = 0;
                } else {
                    sales = Integer.parseInt(salesTextView.getText().toString());
                }

                int quantity;
                if (quantityTextView.getText().toString().isEmpty()) {
                    quantity = 0;
                } else {
                    quantity = Integer.parseInt(quantityTextView.getText().toString());
                }

                if (quantity > 0) {
                    sales = sales + 1;
                    quantity = quantity - 1;
                    salesTextView.setText(String.valueOf(sales));
                    quantityTextView.setText(String.valueOf(quantity));

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    values.put(ProductEntry.COLUMN_PRODUCT_SALES, sales);

                    int rowsAffected = context.getContentResolver().update(currentProductUri, values, null, null);
                    if (rowsAffected == 0) {
                        Toast.makeText(v.getContext(), v.getContext().getString(R.string.error_updating_product), Toast.LENGTH_SHORT).show();
                    } else {
                        //Toast.makeText(v.getContext(), "Sale Product " + nameColumn, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(v.getContext(), "Order Product", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}

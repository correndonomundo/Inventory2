package com.example.andra.inventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.andra.inventory.data.BooksContract.BookEntry;

import java.io.PrintWriter;
import java.io.StringWriter;

public class InvCursorAdapter extends CursorAdapter {

    private Uri mCurrentBookUri;
    private Context mContext;

    public InvCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    @Override
    public void bindView(final View view, final Context context, final Cursor cursor) {
        final TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView supplierTextView = (TextView) view.findViewById(R.id.supplier);
        TextView phoneTextView = (TextView) view.findViewById(R.id.phone);
        Button mBuyButton = (Button) view.findViewById(R.id.buy_book);

        int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
        int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
        int phoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

        final long id = cursor.getLong(idColumnIndex);
        String bookName = cursor.getString(nameColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);
        String bookSupplier = cursor.getString(supplierColumnIndex);
        String supplierPhone = cursor.getString(phoneColumnIndex);

        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent openDetail = new Intent(context, DetailActivity.class);
                    openDetail.putExtra("id", id);
                    Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                    openDetail.setData(currentBookUri);
                    view.getContext().startActivity(openDetail);
                }catch(Exception e){
                    String s = e.getMessage();
                }

                ContentValues values = new ContentValues();
                try {
                    String bookQuantity = quantityTextView.getText().toString().trim();

                    if (Integer.valueOf(bookQuantity) == 0) {
                        throw new IllegalArgumentException("No quantity available");
                    }
                    mCurrentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                    values.put(BookEntry.COLUMN_QUANTITY, String.valueOf(Integer.valueOf(bookQuantity) - 1));
                    int rowsAffected = mContext.getContentResolver().update(mCurrentBookUri, values, null,
                            null);
                } catch (Exception ex) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    ex.printStackTrace(pw);
                    String sStackTrace = sw.toString();
                    System.out.println(sStackTrace);
                }
            }
        });


        nameTextView.setText(bookName);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);
        supplierTextView.setText(bookSupplier);
        phoneTextView.setText(supplierPhone);
    }

}

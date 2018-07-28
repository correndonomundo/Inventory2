package com.example.andra.inventory;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.andra.inventory.data.BooksContract;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;
    private TextView mProductName;
    private TextView mPrice;
    private TextView mQuantity;
    private TextView mSupplierName;
    private TextView mSupplierPhone;

    TextView quantityTextView;
    Button mIncrementButton;
    Button mDecrementButton;
    Button mCallSupplier;


    private boolean mBookHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_detail);

            final Context context = getApplicationContext();

            quantityTextView = (TextView) findViewById(R.id.quantity);
            mIncrementButton = (Button) findViewById(R.id.increment_book);
            mDecrementButton = (Button) findViewById(R.id.decrement_book);
            mCallSupplier = (Button) findViewById(R.id.call_supplier);

            Intent intent = getIntent();
            mCurrentBookUri = intent.getData();
            final long id = intent.getLongExtra("id", 0);
            if (mCurrentBookUri == null) {
                setTitle(getString(R.string.editor_activity_title_new_book));
                invalidateOptionsMenu();
            } else {
                setTitle(getString(R.string.product_detail));
                getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
            }

            mProductName = (TextView) findViewById(R.id.product_name);
            mPrice = (TextView) findViewById(R.id.price);
            mQuantity = (TextView) findViewById(R.id.quantity);
            mSupplierName = (TextView) findViewById(R.id.supplier_name);
            mSupplierPhone = (TextView) findViewById(R.id.supplier_phone);

            mIncrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();

                    int bookQuantity = Integer.valueOf(quantityTextView.getText().toString().trim());

                    mCurrentBookUri = ContentUris.withAppendedId(BooksContract.BookEntry.CONTENT_URI, id);
                    values.put(BooksContract.BookEntry.COLUMN_QUANTITY, String.valueOf(bookQuantity + 1));
                    int rowsAffected = context.getContentResolver().update(mCurrentBookUri, values, null, null);
                }
            });

            mDecrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ContentValues values = new ContentValues();

                    int bookQuantity = Integer.valueOf(quantityTextView.getText().toString().trim());
                    if (bookQuantity == 0) {
                        throw new IllegalArgumentException("No quantity available");
                    }

                    final long id = getIntent().getLongExtra("id", 0);
                    mCurrentBookUri = ContentUris.withAppendedId(BooksContract.BookEntry.CONTENT_URI, id);
                    values.put(BooksContract.BookEntry.COLUMN_QUANTITY, String.valueOf(bookQuantity - 1));
                    int rowsAffected = context.getContentResolver().update(mCurrentBookUri, values, null, null);

                }
            });

            mCallSupplier.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String uri = "tel:" + mSupplierPhone.getText();
                    Intent phoneCall = new Intent(Intent.ACTION_CALL);
                    phoneCall.setData(Uri.parse(uri));
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }

                    startActivity(phoneCall);
                }
            });


        } catch (Exception e) {
            String s = e.getMessage();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BooksContract.BookEntry._ID,
                BooksContract.BookEntry.COLUMN_PRODUCT_NAME,
                BooksContract.BookEntry.COLUMN_PRICE,
                BooksContract.BookEntry.COLUMN_QUANTITY,
                BooksContract.BookEntry.COLUMN_SUPPLIER_NAME,
                BooksContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER};

        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {

            int nameColumnIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_SUPPLIER_NAME);
            int phoneColumnIndex = cursor.getColumnIndex(BooksContract.BookEntry.COLUMN_SUPPLIER_PHONE_NUMBER);

            String bookName = cursor.getString(nameColumnIndex);
            int bookPrice = cursor.getInt(priceColumnIndex);
            int bookQuantity = cursor.getInt(quantityColumnIndex);
            String bookSupplier = cursor.getString(supplierColumnIndex);
            String supplierPhone = cursor.getString(phoneColumnIndex);

            mProductName.setText(bookName);
            mPrice.setText(String.valueOf(bookPrice));
            mQuantity.setText(String.valueOf(bookQuantity));
            mSupplierName.setText(bookSupplier);
            mSupplierPhone.setText(supplierPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
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

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();


    }
}

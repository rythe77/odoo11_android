package org.duckdns.toserba23.toserba23.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by ryanto on 25/02/18.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper sInstance;

    public static final String LOG_TAG = DbHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "odoo.db";

    private static final int DATABASE_VERSION = 1;

    public static synchronized DbHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PRICELISTS_TABLE = "CREATE TABLE " + Contract.PricelistEntry.TABLE_NAME + " ("
                + Contract.PricelistEntry._ID + " INTEGER PRIMARY KEY, "
                + Contract.PricelistEntry.COLUMN_PRODUCT_ID + " INTEGER NOT NULL DEFAULT 0, "
                + Contract.PricelistEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + Contract.PricelistEntry.COLUMN_PRICELIST_NAME + " TEXT NOT NULL, "
                + Contract.PricelistEntry.COLUMN_FIXED_PRICE + " LONG NOT NULL DEFAULT 0, "
                + Contract.PricelistEntry.COLUMN_MIN_QUANTITY + " REAL NOT NULL DEFAULT 1, "
                + Contract.PricelistEntry.COLUMN_X_NOTES + " TEXT, "
                + Contract.PricelistEntry.COLUMN_DATE_START + " STRING, "
                + Contract.PricelistEntry.COLUMN_DATE_END + " STRING);";

        db.execSQL(SQL_CREATE_PRICELISTS_TABLE);

        String SQL_CREATE_PRODUCT_TABLE = "CREATE TABLE " + Contract.ProductEntry.TABLE_NAME + " ("
                + Contract.ProductEntry._ID + " INTEGER PRIMARY KEY, "
                + Contract.ProductEntry.COLUMN_CODE + " TEXT, "
                + Contract.ProductEntry.COLUMN_NAME + " TEXT NOT NULL, "
                + Contract.ProductEntry.COLUMN_QTY_AVAILABLE + " REAL NOT NULL DEFAULT 0, "
                + Contract.ProductEntry.COLUMN_VIRTUAL_AVAILABLE + " REAL NOT NULL DEFAULT 0, "
                + Contract.ProductEntry.COLUMN_CATEGORY_ID + " INTEGER DEFAULT 0, "
                + Contract.ProductEntry.COLUMN_CATEGORY + " TEXT, "
                + Contract.ProductEntry.COLUMN_UNIT_ID + " INTEGER NOT NULL DEFAULT 0, "
                + Contract.ProductEntry.COLUMN_UNIT + " TEXT NOT NULL);"
                ;

        db.execSQL(SQL_CREATE_PRODUCT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    protected void finalize() throws Throwable {
        this.close();
        super.finalize();
    }
}
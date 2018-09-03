package org.duckdns.toserba23.toserba23.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.duckdns.toserba23.toserba23.data.Contract;
import org.duckdns.toserba23.toserba23.data.DbHelper;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static org.duckdns.toserba23.toserba23.utils.QueryUtils.BATCH_SIZE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.PRODUCT_PRICELIST;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.PRODUCT_TEMPLATE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchCountRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 25/02/18.
 */

public class QueryUtilsProductTemplateOffline {

    private static final String LOG_TAG = Context.class.getName();

    /**
     * Query the Odoo server for product template list.
     */
    public static boolean fetchProductTemplateListFromServer(String requestUrl, String databaseName, int userId, String password, Object[] filter, DbHelper dbHelper) {

        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Calculate necessary number of separate RPC call
        int dataSize = searchCountRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_TEMPLATE, filter);
        int rpcCallTotal = (int) Math.ceil(((double)dataSize)/ BATCH_SIZE);

        // Get fields
        HashMap fieldsMap = ProductTemplate.getProductTemplateDetailFields();

        for(int i = 0; i < rpcCallTotal; i++) {
            fieldsMap.put("limit", BATCH_SIZE);
            fieldsMap.put("order", "default_code asc");
            fieldsMap.put("offset", i*BATCH_SIZE);
            try {
                String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_TEMPLATE, filter, fieldsMap);
                ArrayList<ProductTemplate> productTemplates = ProductTemplate.parseJson(jsonResponse);
                saveProductTemplateListToDatabase(productTemplates, dbHelper);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static void saveProductTemplateListToDatabase(ArrayList<ProductTemplate> productTemplates, DbHelper dbHelper) {
        if (productTemplates!=null && !productTemplates.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            for(int i =0; i < productTemplates.size(); i++) {
                ProductTemplate productTemplate = productTemplates.get(i);
                ContentValues values = new ContentValues();
                values.put(Contract.ProductEntry._ID, productTemplate.getId());
                values.put(Contract.ProductEntry.COLUMN_CODE, productTemplate.getRef());
                values.put(Contract.ProductEntry.COLUMN_NAME, productTemplate.getName());
                values.put(Contract.ProductEntry.COLUMN_QTY_AVAILABLE, productTemplate.getQty());
                values.put(Contract.ProductEntry.COLUMN_VIRTUAL_AVAILABLE, productTemplate.getQtyForecast());
                values.put(Contract.ProductEntry.COLUMN_CATEGORY_ID, productTemplate.getProductCategoryId());
                values.put(Contract.ProductEntry.COLUMN_CATEGORY, productTemplate.getProductCategory().getName());
                values.put(Contract.ProductEntry.COLUMN_UNIT_ID, productTemplate.getProductUomId());
                values.put(Contract.ProductEntry.COLUMN_UNIT, productTemplate.getProductUom().getName());

                long newRowId = db.insertWithOnConflict(Contract.ProductEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /**
     * Query the local database for product template list.
     */
    public static ArrayList<ProductTemplate> fetchProductTemplateListFromDatabase(DbHelper dbHelper) {
        ArrayList<ProductTemplate> productTemplates = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                Contract.ProductEntry._ID,
                Contract.ProductEntry.COLUMN_CODE,
                Contract.ProductEntry.COLUMN_NAME,
                Contract.ProductEntry.COLUMN_QTY_AVAILABLE,
                Contract.ProductEntry.COLUMN_VIRTUAL_AVAILABLE,
                Contract.ProductEntry.COLUMN_CATEGORY_ID,
                Contract.ProductEntry.COLUMN_CATEGORY,
                Contract.ProductEntry.COLUMN_UNIT_ID,
                Contract.ProductEntry.COLUMN_UNIT
        };

        Cursor cursor = db.query(
                Contract.ProductEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        try {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(Contract.ProductEntry._ID);
            int codeColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_CODE);
            int nameColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_NAME);
            int qtyColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_QTY_AVAILABLE);
            int virtualColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_VIRTUAL_AVAILABLE);
            int categIdColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_CATEGORY_ID);
            int categColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_CATEGORY);
            int unitIdColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_UNIT_ID);
            int unitColumnIndex = cursor.getColumnIndex(Contract.ProductEntry.COLUMN_UNIT);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                String currentCode = cursor.getString(codeColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                double currentQty = cursor.getDouble(qtyColumnIndex);
                double currentVirtual = cursor.getDouble(virtualColumnIndex);
                GenericModel currentCategory = new GenericModel(cursor.getInt(categIdColumnIndex), cursor.getString(categColumnIndex));
                GenericModel currentUnit = new GenericModel(cursor.getInt(unitIdColumnIndex), cursor.getString(unitColumnIndex));
                productTemplates.add(new ProductTemplate(currentID, currentCode, currentName, currentQty, currentVirtual, currentCategory, currentUnit));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

        return productTemplates;
    }

    /**
     * Query the Odoo server for product template list.
     */
    public static boolean fetchProductPricelistFromServer(String requestUrl, String databaseName, int userId, String password, Object[] filter, DbHelper dbHelper) {

        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Calculate necessary number of separate RPC call
        int dataSize = searchCountRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_PRICELIST, filter);
        int rpcCallTotal = (int) Math.ceil(((double)dataSize)/ BATCH_SIZE);

        // Get fields
        HashMap fieldsMap = ProductPricelistItem.getProductPricelistItemFields();

        for(int i = 0; i < rpcCallTotal; i++) {
            fieldsMap.put("limit", BATCH_SIZE);
            fieldsMap.put("offset", i*BATCH_SIZE);
            try {
                String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_PRICELIST, filter, fieldsMap);
                ArrayList<ProductPricelistItem> productPricelists = ProductPricelistItem.parseJson(jsonResponse);
                saveProductPricelistToDatabase(productPricelists, dbHelper);
                //Log.i(LOG_TAG, "Current rpc call: " + i);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private static void saveProductPricelistToDatabase(ArrayList<ProductPricelistItem> productPricelists, DbHelper dbHelper) {
        if (productPricelists!=null && !productPricelists.isEmpty()) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            for(int i =0; i < productPricelists.size(); i++) {
                ProductPricelistItem productPricelist = productPricelists.get(i);
                ContentValues values = new ContentValues();
                values.put(Contract.PricelistEntry._ID, productPricelist.getId());
                values.put(Contract.PricelistEntry.COLUMN_PRODUCT_ID, productPricelist.getProductId());
                values.put(Contract.PricelistEntry.COLUMN_PRODUCT_NAME, productPricelist.getProduct().getName());
                values.put(Contract.PricelistEntry.COLUMN_PRICELIST_NAME, productPricelist.getPricelistName());
                values.put(Contract.PricelistEntry.COLUMN_FIXED_PRICE, productPricelist.getFixedPrice());
                values.put(Contract.PricelistEntry.COLUMN_MIN_QUANTITY, productPricelist.getMinQuantity());
                values.put(Contract.PricelistEntry.COLUMN_X_NOTES, productPricelist.getXNotes());
                values.put(Contract.PricelistEntry.COLUMN_DATE_START, productPricelist.getDateStart());
                values.put(Contract.PricelistEntry.COLUMN_DATE_END, productPricelist.getDateEnd());

                long newRowId = db.insertWithOnConflict(Contract.PricelistEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            }
        }
    }

    /**
     * Query the local database for product template list.
     */
    public static ArrayList<ProductPricelistItem> fetchProductPricelistFromDatabase(DbHelper dbHelper, int productFilter) {
        ArrayList<ProductPricelistItem> productPricelists = new ArrayList<>();

        // Filter results
        // String selection = Contract.PricelistEntry.COLUMN_PRODUCT_NAME + " = ?";
        // String[] selectionArgs = { mProductNameFilter };
        String selection = Contract.PricelistEntry.COLUMN_PRODUCT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(productFilter) };

        // How you want the results sorted in the resulting Cursor
        String sortOrder = Contract.PricelistEntry.COLUMN_PRICELIST_NAME + " DESC";

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                Contract.PricelistEntry._ID,
                Contract.PricelistEntry.COLUMN_PRODUCT_ID,
                Contract.PricelistEntry.COLUMN_PRODUCT_NAME,
                Contract.PricelistEntry.COLUMN_PRICELIST_NAME,
                Contract.PricelistEntry.COLUMN_FIXED_PRICE,
                Contract.PricelistEntry.COLUMN_MIN_QUANTITY,
                Contract.PricelistEntry.COLUMN_X_NOTES,
                Contract.PricelistEntry.COLUMN_DATE_START,
                Contract.PricelistEntry.COLUMN_DATE_END
        };

        Cursor cursor = db.query(
                Contract.PricelistEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        try {
            // Figure out the index of each column
            int idColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry._ID);
            int productIdColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_PRODUCT_ID);
            int productNameColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_PRODUCT_NAME);
            int pricelistNameColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_PRICELIST_NAME);
            int fixedPriceColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_FIXED_PRICE);
            int minQtyColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_MIN_QUANTITY);
            int xNotesColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_X_NOTES);
            int dateStartColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_DATE_START);
            int dateEndColumnIndex = cursor.getColumnIndex(Contract.PricelistEntry.COLUMN_DATE_END);

            // Iterate through all the returned rows in the cursor
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentID = cursor.getInt(idColumnIndex);
                int currentProductID = cursor.getInt(productIdColumnIndex);
                String currentproductName = cursor.getString(productNameColumnIndex);
                String currentPricelistName = cursor.getString(pricelistNameColumnIndex);
                int currentFixedPrice = cursor.getInt(fixedPriceColumnIndex);
                double currentMinQty = cursor.getDouble(minQtyColumnIndex);
                String currentXNotes = cursor.getString(xNotesColumnIndex);
                String currentDateStart = cursor.getString(dateStartColumnIndex);
                String currentDateEnd = cursor.getString(dateEndColumnIndex);
                productPricelists.add(new ProductPricelistItem(currentID, new GenericModel(currentProductID,currentproductName),currentPricelistName,currentFixedPrice,currentMinQty,currentXNotes,currentDateStart,currentDateEnd));
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }

        return productPricelists;
    }
}

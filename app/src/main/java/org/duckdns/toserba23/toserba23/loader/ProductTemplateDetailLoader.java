package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplate;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ryanto on 24/02/18.
 */

public class ProductTemplateDetailLoader extends AsyncTaskLoader<ProductTemplate> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mItemId;
    private boolean toSave = false;           // if true, perform update/create record before reading data
    private HashMap mDataMap;     // hashmap of data to be saved to Odoo server
    private ArrayList<ProductPricelistItem> mProductPricelistItems;

    /**
     * Constructor used when fetching data from server
     * @param context of the activity
     * @param url to load data from
     */
    public ProductTemplateDetailLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public ProductTemplate loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a stock picking instance.
        ProductTemplate productTemplate = QueryUtilsProductTemplate.fetchProductTemplateDetail(mUrl, mDatabaseName, mUserId, mPassword, mItemId);

        return productTemplate;
    }

}

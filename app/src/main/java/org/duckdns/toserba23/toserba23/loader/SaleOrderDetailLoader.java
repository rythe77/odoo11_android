package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsSaleOrder;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ryanto on 24/02/18.
 */

public class SaleOrderDetailLoader extends AsyncTaskLoader<SaleOrder> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mItemId;

    /**
     * Constructor used when fetching data from server
     * @param context of the activity
     * @param url to load data from
     */
    public SaleOrderDetailLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
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
    public SaleOrder loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a stock picking instance.
        SaleOrder saleOrder = QueryUtilsSaleOrder.fetchSaleOrderDetail(mUrl, mDatabaseName, mUserId, mPassword, mItemId);

        return saleOrder;
    }

}

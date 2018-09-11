package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.model.StockPicking;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsSaleOrder;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsStockPicking;

/**
 * Created by ryanto on 24/02/18.
 */

public class StockPickingDetailLoader extends AsyncTaskLoader<StockPicking> {

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
    public StockPickingDetailLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
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
    public StockPicking loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a stock picking instance.
        StockPicking stockPicking = QueryUtilsStockPicking.fetchStockPickingDetail(mUrl, mDatabaseName, mUserId, mPassword, mItemId);

        return stockPicking;
    }

}

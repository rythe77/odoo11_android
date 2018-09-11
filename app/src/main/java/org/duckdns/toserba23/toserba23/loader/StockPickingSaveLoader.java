package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsSaleOrder;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsStockPicking;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class StockPickingSaveLoader extends AsyncTaskLoader<List<Integer>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mItemId;
    private Boolean mValidatePicking = false;

    /**
     * Constructor used to perform STOCK PICKING VALIDATION to server
     */
    public StockPickingSaveLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
        mValidatePicking = true;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Integer> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<Integer> createdIds = new ArrayList<>();
        if ( mValidatePicking) {
            createdIds = QueryUtilsStockPicking.validatePicking(mUrl, mDatabaseName, mUserId, mPassword, mItemId);
        } else {
            createdIds.add(0);
        }

        return createdIds;
    }

}

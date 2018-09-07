package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsSaleOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class SaleOrderSaveLoader extends AsyncTaskLoader<List<Integer>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mItemId;
    private HashMap mDataMap;
    private Boolean mIsSo = false;
    private Boolean mConfirmSO = false;

    /**
     * Constructor used when saving sale order to server
     * @param context of the activity
     * @param url to load data from
     */
    public SaleOrderSaveLoader(Context context, String url, String databaseName, int userId, String password, int ItemId, HashMap dataMap, Boolean isSo) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
        mDataMap = dataMap;
        mIsSo = isSo;
    }

    /**
     * Constructor used to perform SALE ORDER CONFIRMATION to server
     */
    public SaleOrderSaveLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
        mIsSo = true;
        mConfirmSO = true;
    }

    /**
     * Constructor used when saving sale order line to server
     * @param context of the activity
     * @param url to load data from
     */
    public SaleOrderSaveLoader(Context context, String url, String databaseName, int userId, String password, int ItemId, HashMap dataMap) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
        mDataMap = dataMap;
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

        List<Integer> createdIds;
        if ( mIsSo & !mConfirmSO ) {
            createdIds = QueryUtilsSaleOrder.saveOrder(mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.SALE_ORDER, mItemId, mDataMap);
        } else if ( mIsSo & mConfirmSO ) {
            createdIds = QueryUtilsSaleOrder.confirmOrder(mUrl, mDatabaseName, mUserId, mPassword, mItemId);
        } else {
            createdIds = QueryUtilsSaleOrder.saveOrder(mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.SALE_ORDER_LINE, mItemId, mDataMap);
        }

        return createdIds;
    }

}

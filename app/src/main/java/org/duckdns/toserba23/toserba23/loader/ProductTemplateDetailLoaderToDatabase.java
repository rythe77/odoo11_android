package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.duckdns.toserba23.toserba23.data.Contract;
import org.duckdns.toserba23.toserba23.data.DbHelper;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplateOffline;

/**
 * Created by ryanto on 25/02/18.
 */

public class ProductTemplateDetailLoaderToDatabase extends AsyncTaskLoader<Boolean> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private Object[] mFilter;
    private DbHelper mDbHelper;

    /**
     * Loader managing flag
     * Every time activity resume, android will call onStartLoading.
     * That's why forceLoad() should only be called when both finish and start flag is false.
     * When activity resume while loadInBackground is still running but haven't finished, nothing will happened.
     * When an activity is active when loadInBackground finished, LoaderCallbacks onLoadFinished is called immediately.
     * However, if activity is inactive when loadInBackground finished, LoaderCallbacks onLoadFinished won't be called, and only deliverResult is called.
     * LoaderCallbacks onLoadFinished will be called if when activity resumes (and onStartLoading called), the deliverResult is called within onStartLoading.
     * Therefore, the structure below.
     */
    private Boolean mStartFlag;
    private Boolean mFinishFlag;
    private Boolean mResult;

    public ProductTemplateDetailLoaderToDatabase(Context context, String url, String databaseName, int userId, String password, Object[] filter) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mFilter = filter;
        mStartFlag = false;
        mFinishFlag = false;
    }

    @Override
    protected void onStartLoading() {
        if (!mFinishFlag && !mStartFlag) {
            mDbHelper = DbHelper.getInstance(getContext());
            SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.delete(Contract.PricelistEntry.TABLE_NAME, null, null);
            forceLoad();
            mStartFlag = true;
        } else if (mFinishFlag) {
            deliverResult(mResult);
        }
    }

    /**
     * This is on a background thread.
     */
    @Override
    public Boolean loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and save it to the database, all in this one line.
        return QueryUtilsProductTemplateOffline.fetchProductPricelistFromServer(mUrl, mDatabaseName, mUserId, mPassword, mFilter, mDbHelper);
    }

    @Override
    public void deliverResult(Boolean data) {
        mFinishFlag = true;
        mResult = data;
        super.deliverResult(data);
    }
}

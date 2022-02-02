package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;

/**
 * Created by ryanto on 24/02/18.
 */

public class LeaveDetailLoader extends AsyncTaskLoader<HrHolidays> {

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
    public LeaveDetailLoader(Context context, String url, String databaseName, int userId, String password, int ItemId) {
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
    public HrHolidays loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract an instance.
        HrHolidays hrHolidays = QueryUtilsHrHolidays.fetchHrHolidaysDetail(mUrl, mDatabaseName, mUserId, mPassword, mItemId);

        return hrHolidays;
    }

}

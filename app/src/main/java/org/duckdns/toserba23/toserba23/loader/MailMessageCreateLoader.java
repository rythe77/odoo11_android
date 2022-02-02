package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.model.MailMessage;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsMailMessage;

import java.util.HashMap;
import java.util.List;

public class MailMessageCreateLoader extends AsyncTaskLoader<List<Integer>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private HashMap mDataMap;

    /**
     * Constructor used when fetching data from server
     * @param context of the activity
     * @param url to load data from
     */
    public MailMessageCreateLoader(Context context, String url, String databaseName, int userId, String password, HashMap dataMap) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
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
        createdIds = QueryUtilsMailMessage.saveMailMessage(mUrl, mDatabaseName, mUserId, mPassword, mDataMap);

        return createdIds;
    }
}

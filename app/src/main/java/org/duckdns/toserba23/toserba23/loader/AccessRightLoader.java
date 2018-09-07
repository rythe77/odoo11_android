package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplate;

import java.util.List;

/**
 * Created by ryanto on 23/02/18.
 */

public class AccessRightLoader extends AsyncTaskLoader<List<String>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private Object[] mFilter;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public AccessRightLoader(Context context, String url, String databaseName, int userId, String password, Object[] filter) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mFilter = filter;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<String> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<String> resGroups = QueryUtilsAccessRight.searchReadResGroups(mUrl, mDatabaseName, mUserId, mPassword, mFilter);
        return resGroups;
    }
}

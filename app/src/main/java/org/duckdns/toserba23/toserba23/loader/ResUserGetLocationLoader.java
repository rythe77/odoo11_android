package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.List;

/**
 * Created by ryanto on 03/06/20.
 */

public class ResUserGetLocationLoader extends AsyncTaskLoader<List<Double>> {

    /** Query parameters */
    private String mUrl;
    private int mUserId;
    private String mPassword;
    private String mDatabaseName;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public ResUserGetLocationLoader(Context context, String url, String databaseName, int userId, String password) {
        super(context);
        mUrl = url;
        mUserId = userId;
        mPassword = password;
        mDatabaseName = databaseName;
    }

    @Override
    protected void onStartLoading() { forceLoad(); }

    @Override
    public List<Double> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<Double> userLocation = QueryUtils.getLocation(mUrl, mDatabaseName, mUserId, mPassword);
        return userLocation;
    }
}

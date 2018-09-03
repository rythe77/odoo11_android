package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;

/**
 * Created by ryanto on 22/02/18.
 */

public class LoginLoader extends AsyncTaskLoader<Integer> {

    /** Query parameters */
    private String mUrl;
    private String mUsername;
    private String mPassword;
    private String mDatabaseName;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public LoginLoader(Context context, String url, String username, String password, String databaseName) {
        super(context);
        mUrl = url;
        mUsername = username;
        mPassword = password;
        mDatabaseName = databaseName;
    }

    @Override
    protected void onStartLoading() { forceLoad(); }

    @Override
    public Integer loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        int userId = QueryUtils.getUid(mUrl, mUsername, mPassword, mDatabaseName);
        return userId;
    }
}

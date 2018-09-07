package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsGenericModel;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 23/02/18.
 */

public class GenericModelLoader extends AsyncTaskLoader<List<GenericModel>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private String mModelName;
    private Object[] mFilter;
    private HashMap mMap;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public GenericModelLoader(Context context, String url, String databaseName, int userId, String password, String modelName, Object[] filter, HashMap map) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mModelName = modelName;
        mFilter = filter;
        mMap = map;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<GenericModel> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<GenericModel> genericModels = QueryUtilsGenericModel.searchReadModel(mUrl, mDatabaseName, mUserId, mPassword, mModelName, mFilter, mMap, QueryUtils.LIMIT_PAGING_SIZE, 0);
        return genericModels;
    }
}

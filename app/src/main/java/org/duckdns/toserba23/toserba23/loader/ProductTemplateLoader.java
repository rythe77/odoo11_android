package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplate;

import java.util.List;

/**
 * Created by ryanto on 23/02/18.
 */

public class ProductTemplateLoader extends AsyncTaskLoader<List<ProductTemplate>> {

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
    public ProductTemplateLoader(Context context, String url, String databaseName, int userId, String password, Object[] filter) {
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
    public List<ProductTemplate> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        // Perform the network request, parse the response, and extract a list of earthquakes.
        List<ProductTemplate> productTemplates = QueryUtilsProductTemplate.searchReadProductTemplateList(mUrl, mDatabaseName, mUserId, mPassword, mFilter, QueryUtils.LIMIT_PAGING_SIZE, 0);
        return productTemplates;
    }
}

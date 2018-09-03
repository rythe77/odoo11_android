package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.data.DbHelper;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplateOffline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 25/02/18.
 */

public class ProductTemplateLoaderFromDatabase extends AsyncTaskLoader<List<ProductTemplate>>{

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private DbHelper mDbHelper;

    public ProductTemplateLoaderFromDatabase(Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        mDbHelper = DbHelper.getInstance(getContext());
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public ArrayList<ProductTemplate> loadInBackground() {
        // Perform the database query to retrieve data.
        return QueryUtilsProductTemplateOffline.fetchProductTemplateListFromDatabase(mDbHelper);
    }
}

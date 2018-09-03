package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.data.DbHelper;
import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsProductTemplateOffline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 25/02/18.
 */

public class ProductTemplateDetailLoaderFromDatabase extends AsyncTaskLoader<List<ProductPricelistItem>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private DbHelper mDbHelper;

    private int mProductFilter;

    public ProductTemplateDetailLoaderFromDatabase(Context context, int productFilter) {
        super(context);
        mProductFilter = productFilter;
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
    public ArrayList<ProductPricelistItem> loadInBackground() {
        // Perform the database query to retrieve data.
        return QueryUtilsProductTemplateOffline.fetchProductPricelistFromDatabase(mDbHelper, mProductFilter);
    }
}

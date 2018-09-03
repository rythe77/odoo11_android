package org.duckdns.toserba23.toserba23.fragment;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateDetailLoader;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateDetailLoaderFromDatabase;
import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 25/02/18.
 */

public class ProductDetailOffline extends AppCompatActivity{

    private static final int LOAD_PRODUCT_TEMPLATE_DETAIL_LOADER_ID = 1;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private List<ProductPricelistItem> mProductPricelists;

    Toolbar mToolbar;
    LinearLayout mPricelistViewContainer;

    private int mProductTmplId;
    private String mProductTmplCode;
    private String mProductTmplName;
    private String mProductCategory;
    private String mProductUom;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_template_detail_app_bar_offline);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set pricelist container view
        mPricelistViewContainer = (LinearLayout) findViewById(R.id.container_view);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mProductTmplId = getIntent().getIntExtra("product_tmpl_id", 0);
        mProductTmplCode = getIntent().getStringExtra("product_tmpl_code");
        mProductTmplName = getIntent().getStringExtra("product_tmpl_name");
        mProductCategory = getIntent().getStringExtra("product_categ");
        mProductUom = getIntent().getStringExtra("product_uom");
        setTitle(getString(R.string.detail_product_offline_activity_label));

        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
        loaderManager.restartLoader(LOAD_PRODUCT_TEMPLATE_DETAIL_LOADER_ID, null, loadProductTemplateDetailFromDatabaseListener);
    }

    /**
     * Setup Loader behavior here
     * @param i id of the called loader
     * @param bundle
     * @return
     */
    private LoaderManager.LoaderCallbacks<List<ProductPricelistItem>> loadProductTemplateDetailFromDatabaseListener = new LoaderManager.LoaderCallbacks<List<ProductPricelistItem>>() {
        @Override
        public Loader<List<ProductPricelistItem>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new ProductTemplateDetailLoaderFromDatabase(ProductDetailOffline.this, mProductTmplId);
        }
        @Override
        public void onLoadFinished(Loader<List<ProductPricelistItem>> loader, List<ProductPricelistItem> productPricelists) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mProductPricelists = productPricelists;
            displayUpdate(productPricelists);
        }
        @Override
        public void onLoaderReset(Loader<List<ProductPricelistItem>> loader) {
        }
    };

    private void displayUpdate(List<ProductPricelistItem> productPricelistItems) {
        // If there is a valid list of {@link stock picking}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (productPricelistItems != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_code)).setText(mProductTmplCode);
            ((TextView) findViewById(R.id.detail_name)).setText(mProductTmplName);
            ((TextView) findViewById(R.id.detail_category)).setText(mProductCategory);
            ((TextView) findViewById(R.id.detail_uom)).setText(mProductUom);

            // Prepare linear layout view which will contain inflated product row view
            LayoutInflater internalInflater = LayoutInflater.from(getApplicationContext());

            // Display pricelist data onto product row which will be inflated based on number of products to be displayed
            if (productPricelistItems !=null && !productPricelistItems.isEmpty()) {
                for (int i = 0; i < productPricelistItems.size(); i++) {
                    ProductPricelistItem productPricelist = productPricelistItems.get(i);
                    View rowView = internalInflater.inflate(R.layout.product_template_detail_pricelist_adapter, mPricelistViewContainer, false);
                    ((TextView) rowView.findViewById(R.id.date_text_view)).setText(DisplayFormatter.formatDate(productPricelist.getDateStart()));
                    ((TextView) rowView.findViewById(R.id.pricelist_name)).setText(DisplayFormatter.formatString(productPricelist.getPricelistName()));
                    ((TextView) rowView.findViewById(R.id.fixed_price)).setText(DisplayFormatter.formatCurrency(productPricelist.getFixedPrice()));
                    ((TextView) rowView.findViewById(R.id.min_qty)).setText(DisplayFormatter.formatQuantity(productPricelist.getMinQuantity()));
                    ((TextView) rowView.findViewById(R.id.notes)).setText(DisplayFormatter.formatString(productPricelist.getXNotes()));
                    mPricelistViewContainer.addView(rowView);
                }
            }
        } else {
            Toast.makeText(this, R.string.error_cannot_connect_to_server, Toast.LENGTH_LONG).show();
        }
    }
}

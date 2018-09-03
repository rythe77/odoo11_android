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
import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;

/**
 * Created by ryanto on 24/02/18.
 */

public class ProductDetail extends AppCompatActivity {

    private static final int FETCH_PRODUCT_TEMPLATE_DETAIL_LOADER_ID = 1;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private ProductTemplate mProductTemplate;

    Toolbar mToolbar;
    LinearLayout mPricelistViewContainer;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mProductTmplId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_template_detail_app_bar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set pricelist container view
        mPricelistViewContainer = (LinearLayout) findViewById(R.id.container_view);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mProductTmplId = getIntent().getIntExtra("product_tmpl_id", 0);
        setTitle(getString(R.string.detail_product_activity_label));

        readData();
    }

    public void readData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.restartLoader(FETCH_PRODUCT_TEMPLATE_DETAIL_LOADER_ID, null, loadProductTemplateFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setup Loader behavior here
     * @param i id of the called loader
     * @param bundle
     * @return
     */
    private LoaderManager.LoaderCallbacks<ProductTemplate> loadProductTemplateFromServerListener = new LoaderManager.LoaderCallbacks<ProductTemplate>() {
        @Override
        public Loader<ProductTemplate> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new ProductTemplateDetailLoader(ProductDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mProductTmplId);
        }
        @Override
        public void onLoadFinished(Loader<ProductTemplate> loader, ProductTemplate productTemplate) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mProductTemplate = productTemplate;
            displayUpdate(productTemplate);
        }
        @Override
        public void onLoaderReset(Loader<ProductTemplate> loader) {
        }
    };

    private void displayUpdate(ProductTemplate productTemplate) {
        // If there is a valid list of {@link stock picking}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (productTemplate != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_code)).setText(productTemplate.getRef());
            ((TextView) findViewById(R.id.detail_name)).setText(productTemplate.getName());
            ((TextView) findViewById(R.id.detail_category)).setText(productTemplate.getProductCategory().getName());
            ((TextView) findViewById(R.id.detail_uom)).setText(productTemplate.getProductUom().getName());
            ((TextView) findViewById(R.id.detail_qty_ckl)).setText(DisplayFormatter.formatQuantity(productTemplate.getProductProduct().getQtyCKL()));
            ((TextView) findViewById(R.id.detail_qty2_ckl)).setText(DisplayFormatter.formatQuantity(productTemplate.getProductProduct().getQtyForecastCKL()));
            ((TextView) findViewById(R.id.detail_qty_prl)).setText(DisplayFormatter.formatQuantity(productTemplate.getProductProduct().getQtyPRL()));
            ((TextView) findViewById(R.id.detail_qty2_prl)).setText(DisplayFormatter.formatQuantity(productTemplate.getProductProduct().getQtyForecastPRL()));

            // Prepare linear layout view which will contain inflated product row view
            LayoutInflater internalInflater = LayoutInflater.from(getApplicationContext());
            ArrayList<ProductPricelistItem> productPricelists = mProductTemplate.getProductPricelistItem();

            // Display pricelist data onto product row which will be inflated based on number of products to be displayed
            if (productPricelists !=null && !productPricelists.isEmpty()) {
                for (int i = 0; i < productPricelists.size(); i++) {
                    ProductPricelistItem productPricelist = productPricelists.get(i);
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

package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.SaleOrderDetailLoader;
import org.duckdns.toserba23.toserba23.loader.SaleOrderSaveLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.model.SaleOrderLine;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class SaleLine extends AppCompatActivity {

    private static final int FETCH_SALE_ORDER_LINE_LOADER_ID = 1;
    private static final int CONFIRM_SALE_ORDER_LINE_LOADER_ID = 2;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private SaleOrder mSaleOrder;

    Toolbar mToolbar;
    LinearLayout mSaleOrderLineViewContainer;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mOrderId;
    private AccessRight mAccess;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mOrderId = getIntent().getIntExtra("order_id", 0);
        mAccess = getIntent().getParcelableExtra(QueryUtilsAccessRight.ACCESS_RIGHT);
        setTitle(getString(R.string.detail_sale_line_activity_label));

        setContentView(R.layout.sale_order_detail_app_bar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Sale confirmation FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAccess != null & mAccess.has_access_to_sale_confirm) {
                    if (mSaleOrder.getState().equals("draft")) {
                        // Verify whether user really wants to confirm sale
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        confirmSO();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(SaleLine.this);
                        builder.setMessage(getString(R.string.detail_sale_order_confirm_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                                .setNegativeButton(R.string.no_string, dialogClickListener).show();
                    } else {
                        Toast.makeText(SaleLine.this, getString(R.string.detail_sale_order_confirm_refuse), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(SaleLine.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set pricelist container view
        mSaleOrderLineViewContainer = (LinearLayout) findViewById(R.id.container_view);

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
            loaderManager.restartLoader(FETCH_SALE_ORDER_LINE_LOADER_ID, null, loadSaleOrderDetailFromServerListener);
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

    public void confirmSO() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(CONFIRM_SALE_ORDER_LINE_LOADER_ID, null, confirmSaleOrderListener);
        } else {
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
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
    private LoaderManager.LoaderCallbacks<SaleOrder> loadSaleOrderDetailFromServerListener = new LoaderManager.LoaderCallbacks<SaleOrder>() {
        @Override
        public Loader<SaleOrder> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new SaleOrderDetailLoader(SaleLine.this, mUrl, mDatabaseName, mUserId, mPassword, mOrderId);
        }
        @Override
        public void onLoadFinished(Loader<SaleOrder> loader, SaleOrder saleOrder) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mSaleOrder = saleOrder;
            displayUpdate(saleOrder);
            getLoaderManager().destroyLoader(FETCH_SALE_ORDER_LINE_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<SaleOrder> loader) {
        }
    };

    private void displayUpdate(SaleOrder saleOrder) {
        if (saleOrder != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_order_date)).setText(DisplayFormatter.formatDate(saleOrder.getDateOrder()));
            ((TextView) findViewById(R.id.detail_name)).setText(DisplayFormatter.formatString(saleOrder.getName()));
            ((TextView) findViewById(R.id.detail_partner)).setText(DisplayFormatter.formatString(saleOrder.getPartner().getName()));
            ((TextView) findViewById(R.id.detail_payment_term)).setText(DisplayFormatter.formatString(saleOrder.getPaymentTerm()));
            ((TextView) findViewById(R.id.detail_pricelist)).setText(DisplayFormatter.formatString(saleOrder.getPricelist()));
            ((TextView) findViewById(R.id.detail_warehouse)).setText(DisplayFormatter.formatString(saleOrder.getWarehouse().getName()));
            ((TextView) findViewById(R.id.detail_amount_total)).setText(DisplayFormatter.formatCurrency(saleOrder.getAmountTotal()));
            ((TextView) findViewById(R.id.detail_transporter)).setText(DisplayFormatter.formatString(saleOrder.getTransporter()));
            ((TextView) findViewById(R.id.detail_delivery)).setText(DisplayFormatter.formatString(saleOrder.getIntTransporter()));
            ((TextView) findViewById(R.id.detail_vehicle)).setText(DisplayFormatter.formatString(saleOrder.getVehicleNotes()));
            ((TextView) findViewById(R.id.detail_delivery_date)).setText(DisplayFormatter.formatDate(saleOrder.getRequestedDate()));
            ((TextView) findViewById(R.id.detail_other_notes)).setText(DisplayFormatter.formatString(saleOrder.getOtherNotes()));

            // Prepare linear layout view which will contain inflated product row view
            LayoutInflater internalInflater = LayoutInflater.from(getApplicationContext());
            ArrayList<SaleOrderLine> saleOrderLines = mSaleOrder.getSaleOrderLines();

            // Display pricelist data onto product row which will be inflated based on number of products to be displayed
            if (saleOrderLines !=null && !saleOrderLines.isEmpty()) {
                for (int i = 0; i < saleOrderLines.size(); i++) {
                    SaleOrderLine saleOrderLine = saleOrderLines.get(i);
                    View rowView = internalInflater.inflate(R.layout.sale_order_detail_line_adapter, mSaleOrderLineViewContainer, false);
                    ((TextView) rowView.findViewById(R.id.sale_line_product_code)).setText(DisplayFormatter.splitProductCode(saleOrderLine.getProduct().getName()));
                    ((TextView) rowView.findViewById(R.id.sale_line_product)).setText(DisplayFormatter.splitProductName(saleOrderLine.getProduct().getName()));
                    ((TextView) rowView.findViewById(R.id.sale_line_product_desc)).setText(DisplayFormatter.formatProductDesc(saleOrderLine.getProductDesc()));
                    ((TextView) rowView.findViewById(R.id.sale_line_ordered_qty)).setText(DisplayFormatter.formatQuantity(saleOrderLine.getOrderedQty()));
                    ((TextView) rowView.findViewById(R.id.sale_line_uom)).setText(DisplayFormatter.formatString(saleOrderLine.getUom()));
                    ((TextView) rowView.findViewById(R.id.sale_line_unit_price)).setText(DisplayFormatter.formatCurrency(saleOrderLine.getUnitPrice()));
                    ((TextView) rowView.findViewById(R.id.sale_line_discount)).setText(DisplayFormatter.formatDiscountLine(saleOrderLine.getDiscount()));
                    ((TextView) rowView.findViewById(R.id.sale_line_subtotal)).setText(DisplayFormatter.formatCurrency(saleOrderLine.getSubtotal()));
                    mSaleOrderLineViewContainer.addView(rowView);
                }
            }
        } else {
            Toast.makeText(this, R.string.error_cannot_connect_to_server, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Setup Loader for sale order confirmation
     */
    private LoaderManager.LoaderCallbacks<List<Integer>> confirmSaleOrderListener = new LoaderManager.LoaderCallbacks<List<Integer>>() {
        @Override
        public Loader<List<Integer>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new SaleOrderSaveLoader(SaleLine.this, mUrl, mDatabaseName, mUserId, mPassword, mOrderId);
        }
        @Override
        public void onLoadFinished(Loader<List<Integer>> loader, List<Integer> flagIntegers) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            if (flagIntegers.get(0) == 1) {
                Toast.makeText(SaleLine.this, getString(R.string.detail_sale_order_confirm_success), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(SaleLine.this, getString(R.string.detail_sale_order_confirm_failed), Toast.LENGTH_LONG).show();
            }
            getLoaderManager().destroyLoader(CONFIRM_SALE_ORDER_LINE_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<List<Integer>> loader) {
        }
    };
}

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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.SaleOrderSaveLoader;
import org.duckdns.toserba23.toserba23.loader.StockPickingDetailLoader;
import org.duckdns.toserba23.toserba23.loader.StockPickingSaveLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.StockMove;
import org.duckdns.toserba23.toserba23.model.StockPicking;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class StockDetail extends AppCompatActivity {

    private static final int FETCH_STOCK_PICKING_LOADER_ID = 1;
    private static final int VALIDATE_STOCK_PICKING_LOADER_ID = 2;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private StockPicking mStockPicking;

    Toolbar mToolbar;
    LinearLayout mStockMoveViewContainer;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mPickingId;
    private AccessRight mAccess;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mPickingId = getIntent().getIntExtra("picking_id", 0);
        mAccess = getIntent().getParcelableExtra(QueryUtilsAccessRight.ACCESS_RIGHT);
        setTitle(getString(R.string.detail_stock_detail_activity_label));

        setContentView(R.layout.stock_picking_detail_app_bar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Sale confirmation FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAccess != null & mAccess.has_access_to_stock_validate) {
                    if (mStockPicking.getState().equals("waiting_validation")) {
                        // Verify whether user really wants to validate picking
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        validatePicking();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(StockDetail.this);
                        builder.setMessage(getString(R.string.detail_stock_picking_validate_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                                .setNegativeButton(R.string.no_string, dialogClickListener).show();
                    } else {
                        Toast.makeText(StockDetail.this, getString(R.string.detail_stock_picking_validate_refuse), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(StockDetail.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set stock moves container view
        mStockMoveViewContainer = (LinearLayout) findViewById(R.id.container_view);

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
            loaderManager.restartLoader(FETCH_STOCK_PICKING_LOADER_ID, null, loadStockDetailFromServerListener);
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

    public void validatePicking() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(VALIDATE_STOCK_PICKING_LOADER_ID, null, validateStockPickingListener);
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
    private LoaderManager.LoaderCallbacks<StockPicking> loadStockDetailFromServerListener = new LoaderManager.LoaderCallbacks<StockPicking>() {
        @Override
        public Loader<StockPicking> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new StockPickingDetailLoader(StockDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mPickingId);
        }
        @Override
        public void onLoadFinished(Loader<StockPicking> loader, StockPicking stockPicking) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mStockPicking = stockPicking;
            displayUpdate(stockPicking);
            getLoaderManager().destroyLoader(FETCH_STOCK_PICKING_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<StockPicking> loader) {
        }
    };

    private void displayUpdate(StockPicking stockPicking) {
        if (stockPicking != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_picking_date)).setText(DisplayFormatter.formatDateTime(stockPicking.getScheduledDate()));
            ((TextView) findViewById(R.id.detail_name)).setText(DisplayFormatter.formatString(stockPicking.getName()));
            ((TextView) findViewById(R.id.detail_partner)).setText(DisplayFormatter.formatString(stockPicking.getPartnerName()));
            ((TextView) findViewById(R.id.detail_state)).setText(DisplayFormatter.formatString(stockPicking.getStateName()));
            ((TextView) findViewById(R.id.detail_type)).setText(DisplayFormatter.formatString(stockPicking.getOperationTypes()));
            ((TextView) findViewById(R.id.detail_origin)).setText(DisplayFormatter.formatString(stockPicking.getOrigin()));
            ((TextView) findViewById(R.id.detail_transporter)).setText(DisplayFormatter.formatString(stockPicking.getTransporter()));
            ((TextView) findViewById(R.id.detail_delivery)).setText(DisplayFormatter.formatString(stockPicking.getIntTransporter()));
            ((TextView) findViewById(R.id.detail_vehicle)).setText(DisplayFormatter.formatString(stockPicking.getXVehicleNotes()));
            ((TextView) findViewById(R.id.detail_priority)).setText(DisplayFormatter.formatString(stockPicking.getPriorityName()));
            ((TextView) findViewById(R.id.detail_other_notes)).setText(DisplayFormatter.formatString(stockPicking.getXNotes()));
            ((TextView) findViewById(R.id.detail_location)).setText(DisplayFormatter.formatString(stockPicking.getLocation()));
            ((TextView) findViewById(R.id.detail_location_dest)).setText(DisplayFormatter.formatString(stockPicking.getDestLocation()));

            // Prepare linear layout view which will contain inflated product row view
            LayoutInflater internalInflater = LayoutInflater.from(getApplicationContext());
            ArrayList<StockMove> stockMoves = mStockPicking.getStockMoves();

            // Display pricelist data onto product row which will be inflated based on number of products to be displayed
            if (stockMoves !=null && !stockMoves.isEmpty()) {
                for (int i = 0; i < stockMoves.size(); i++) {
                    StockMove stockMove = stockMoves.get(i);
                    View rowView = internalInflater.inflate(R.layout.stock_picking_detail_line_adapter, mStockMoveViewContainer, false);
                    ((TextView) rowView.findViewById(R.id.stock_move_product_code)).setText(DisplayFormatter.splitProductCode(stockMove.getProduct().getName()));
                    ((TextView) rowView.findViewById(R.id.stock_move_product)).setText(DisplayFormatter.splitProductName(stockMove.getProduct().getName()));
                    ((TextView) rowView.findViewById(R.id.stock_move_product_desc)).setText(DisplayFormatter.formatProductDesc(stockMove.getProductDesc()));
                    ((TextView) rowView.findViewById(R.id.stock_line_qty)).setText(DisplayFormatter.formatQuantity(stockMove.getOrderedQty()));
                    ((TextView) rowView.findViewById(R.id.stock_line_qty_res)).setText(DisplayFormatter.formatQuantity(stockMove.getReservedQtyQty()));
                    ((TextView) rowView.findViewById(R.id.stock_line_qty_done)).setText(DisplayFormatter.formatQuantity(stockMove.getDoneQty()));
                    ((TextView) rowView.findViewById(R.id.stock_line_uom)).setText(DisplayFormatter.formatString(stockMove.getUom()));
                    mStockMoveViewContainer.addView(rowView);
                }
            }
        } else {
            Toast.makeText(this, R.string.error_cannot_connect_to_server, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Setup Loader for sale order confirmation
     */
    private LoaderManager.LoaderCallbacks<List<Integer>> validateStockPickingListener = new LoaderManager.LoaderCallbacks<List<Integer>>() {
        @Override
        public Loader<List<Integer>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new StockPickingSaveLoader(StockDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mPickingId);
        }
        @Override
        public void onLoadFinished(Loader<List<Integer>> loader, List<Integer> flagIntegers) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            if (flagIntegers.get(0) == 1) {
                Toast.makeText(StockDetail.this, getString(R.string.detail_stock_picking_validate_success), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(StockDetail.this, getString(R.string.detail_stock_picking_validate_failed), Toast.LENGTH_LONG).show();
            }
            getLoaderManager().destroyLoader(VALIDATE_STOCK_PICKING_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<List<Integer>> loader) {
        }
    };
}

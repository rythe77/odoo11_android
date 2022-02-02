package org.duckdns.toserba23.toserba23.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.MainActivity;
import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.adapter.StockPickingAdapter;
import org.duckdns.toserba23.toserba23.loader.StockPickingLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.StockPicking;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class Stock extends Fragment {

    private static final int FETCH_STOCK_PICKING_LOADER_ID = 1;
    private static final String ALL = "";
    private static final String PENERIMAAN = "Penerimaan";
    private static final String TRANSFER = "Transfer Internal";
    private static final String PENGIRIMAN = "Order Pengiriman";
    private static final int STOCK_ALL = 0;
    private static final int STOCK_NOT_DONE = 1;
    private static final int STOCK_WAITING = 2;

    private StockPickingAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeView;
    private EditText mInputText;
    private boolean mToggleInputText = false;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private AccessRight mAccess;

    // Filter data
    private ArrayList<Object[]> mStockPickingFilterElements = new ArrayList<Object[]>(){};
    private String mStockTypeFilter = ALL;
    private int mStockStateFilter = STOCK_ALL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Initialize account information with data from Preferences
        mPref = this.getActivity().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mAccess = ((MainActivity)getActivity()).mAccess;

        final View rootView = inflater.inflate(R.layout.standard_list_view, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView stockPickingListView = (ListView) rootView.findViewById(R.id.list);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        stockPickingListView.setEmptyView(mEmptyStateTextView);

        mAdapter = new StockPickingAdapter(getActivity(), new ArrayList<StockPicking>());
        stockPickingListView.setAdapter(mAdapter);

        stockPickingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int stockPickingId = mAdapter.getItem(i).getId();

                // Send picking id to stock detail activity
                Intent intent = new Intent(getActivity(), StockDetail.class);
                intent.putExtra("picking_id", stockPickingId);
                intent.putExtra(QueryUtilsAccessRight.ACCESS_RIGHT, mAccess);
                getActivity().startActivity(intent);
            }
        });

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        mSwipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
        mSwipeView.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getData();
                    }
                }
        );

        mInputText = (EditText) rootView.findViewById(R.id.input_search_text);
        if (mToggleInputText) {
            mInputText.setVisibility(View.VISIBLE);
        } else {
            mInputText.setVisibility(View.GONE);
        }
        mInputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                createFilterData();
                getData();
                InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
        });

        return rootView;
    }

    private void createFilterData() {
        mStockPickingFilterElements.clear();
        // Internal transfers have null partner_id. So only when the transfer type is not internal, then perform the partner search
        if (!mStockTypeFilter.isEmpty()) {
            if (!mStockTypeFilter.equals(TRANSFER)) {
                mStockPickingFilterElements.add(new Object[] {"partner_id", "ilike", mInputText.getText().toString()});
            }
            mStockPickingFilterElements.add(new Object[]{"picking_type_id", "ilike", mStockTypeFilter});
        } else {
            mStockPickingFilterElements.add(new Object[] {"partner_id", "ilike", mInputText.getText().toString()});
        }
        if (mStockStateFilter==STOCK_NOT_DONE) {
            mStockPickingFilterElements.add(new Object[]{"state", "in", new Object[]{"draft", "confirmed", "partially_available", "assigned"}});
        } else if (mStockStateFilter==STOCK_WAITING) {
            mStockPickingFilterElements.add(new Object[]{"state", "in", new Object[]{"waiting_validation"}});
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.stock_picking_title));

        // Fetch data from server
        createFilterData();
        initData();
    }

    public void initData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.initLoader(FETCH_STOCK_PICKING_LOADER_ID, null, loadStockPickingFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    public void getData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.restartLoader(FETCH_STOCK_PICKING_LOADER_ID, null, loadStockPickingFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.stock_picking_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_search_input_text:
                mToggleInputText = !mToggleInputText;
                if (mToggleInputText) {
                    mInputText.setVisibility(View.VISIBLE);
                } else {
                    mInputText.setVisibility(View.GONE);
                }
                return true;
            case R.id.checkbox_stock_type_all:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockTypeFilter = ALL;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_in:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockTypeFilter = PENERIMAAN;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_int:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockTypeFilter = TRANSFER;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_out:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockTypeFilter = PENGIRIMAN;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_state_all:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockStateFilter = STOCK_ALL;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_not_done:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockStateFilter = STOCK_NOT_DONE;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_stock_waiting:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mStockStateFilter = STOCK_WAITING;
                createFilterData();
                getData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<List<StockPicking>> loadStockPickingFromServerListener = new LoaderManager.LoaderCallbacks<List<StockPicking>>() {
        @Override
        public Loader<List<StockPicking>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mStockPickingFilterElements
            };
            return new StockPickingLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<StockPicking>> loader, List<StockPicking> stockPickings) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);

            // Set empty state text to display "No available sale order."
            mEmptyStateTextView.setText(R.string.stock_picking_not_found);

            // Clear the adapter of previous sale order
            mAdapter.clear();

            // If there is a valid list of {@link sale order}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (stockPickings != null && !stockPickings.isEmpty()) {
                Collections.sort(stockPickings, new Comparator<StockPicking>() {
                    @Override
                    public int compare(StockPicking item1, StockPicking item2) {
                        return item2.getName().compareToIgnoreCase(item1.getName());
                    }
                });
                mAdapter.addAll(stockPickings);
            } else {
                Toast.makeText(getActivity(), R.string.stock_picking_not_found, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<StockPicking>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };
}

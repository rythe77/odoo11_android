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
import org.duckdns.toserba23.toserba23.adapter.SaleOrderAdapter;
import org.duckdns.toserba23.toserba23.loader.SaleOrderLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class Sale extends Fragment {

    private static final int FETCH_SALE_ORDER_LOADER_ID = 1;

    private SaleOrderAdapter mAdapter;
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
    private String mDefPartnerName;
    private AccessRight mAccess;

    // Filter data
    private ArrayList<Object[]> mSaleOrderFilterElements = new ArrayList<Object[]>(){};
    private Boolean saleDraft = false;
    private Boolean saleSale = false;

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
        mDefPartnerName = mPref.getString(getString(R.string.settings_def_partner_name_key), null);
        mAccess = ((MainActivity)getActivity()).mAccess;

        final View rootView = inflater.inflate(R.layout.standard_list_view, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView saleOrderListView = (ListView) rootView.findViewById(R.id.list);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        saleOrderListView.setEmptyView(mEmptyStateTextView);

        mAdapter = new SaleOrderAdapter(getActivity(), new ArrayList<SaleOrder>());
        saleOrderListView.setAdapter(mAdapter);

        saleOrderListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int saleOrderId = mAdapter.getItem(i).getId();

                // Send picking id to stock detail activity
                Intent intent = new Intent(getActivity(), SaleLine.class);
                intent.putExtra("order_id", saleOrderId);
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
        if (mDefPartnerName!=null) {
            mInputText.setText(mDefPartnerName);
        }
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
        mSaleOrderFilterElements.clear();
        mSaleOrderFilterElements.add(new Object[] {"partner_id", "ilike", mInputText.getText().toString()});
        if (saleDraft & !saleSale) {mSaleOrderFilterElements.add(new Object[] {"state", "=", "draft"});}
        else if (!saleDraft & saleSale) {mSaleOrderFilterElements.add(new Object[] {"state", "=", "sale"});}
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.sale_order_title));

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
            loaderManager.initLoader(FETCH_SALE_ORDER_LOADER_ID, null, loadSaleOrderFromServerListener);
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
            loaderManager.restartLoader(FETCH_SALE_ORDER_LOADER_ID, null, loadSaleOrderFromServerListener);
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
        inflater.inflate(R.menu.sale_order_options, menu);
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
            case R.id.checkbox_sale_all:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                saleDraft = false;
                saleSale = false;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_sale_draft:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                saleDraft = true;
                saleSale = false;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_sale_sale:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                saleDraft = false;
                saleSale = true;
                createFilterData();
                getData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<List<SaleOrder>> loadSaleOrderFromServerListener = new LoaderManager.LoaderCallbacks<List<SaleOrder>>() {
        @Override
        public Loader<List<SaleOrder>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mSaleOrderFilterElements
            };
            return new SaleOrderLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<SaleOrder>> loader, List<SaleOrder> saleOrders) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);

            // Set empty state text to display "No available sale order."
            mEmptyStateTextView.setText(R.string.sale_order_not_found);

            // Clear the adapter of previous sale order
            mAdapter.clear();

            // If there is a valid list of {@link sale order}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (saleOrders != null && !saleOrders.isEmpty()) {
                Collections.sort(saleOrders, new Comparator<SaleOrder>() {
                    @Override
                    public int compare(SaleOrder item1, SaleOrder item2) {
                        return item2.getName().compareToIgnoreCase(item1.getName());
                    }
                });
                mAdapter.addAll(saleOrders);
            } else {
                Toast.makeText(getActivity(), R.string.error_order_not_found, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<SaleOrder>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };
}

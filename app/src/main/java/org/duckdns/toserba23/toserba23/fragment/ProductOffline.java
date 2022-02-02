package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.adapter.ProductTemplateAdapter;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateDetailLoaderToDatabase;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateLoaderFromDatabase;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateLoaderToDatabase;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class ProductOffline extends Fragment{

    private static final int LOAD_PRODUCT_FROM_DATABASE_LOADER_ID = 1;

    private static final int FETCH_PRODUCT_FROM_SERVER_LOADER_ID = 2;

    private static final int FETCH_PRICELIST_FROM_SERVER_LOADER_ID = 3;

    private ProductTemplateAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeView;
    private EditText mInputText;
    private View mLoadingIndicatorView;
    private boolean mToggleInputText = false;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;

    private boolean isDownloadingProduct = false;
    private boolean isDownloadingPricelist = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.product_template_list_view, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView productTemplateListView = (ListView) rootView.findViewById(R.id.list);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        productTemplateListView.setEmptyView(mEmptyStateTextView);
        productTemplateListView.setFastScrollEnabled(true);
        mLoadingIndicatorView = rootView.findViewById(R.id.loading_spinner);

        // Change header color
        LinearLayout header = (LinearLayout) rootView.findViewById(R.id.product_list_header_container);
        header.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.offlineColorPrimaryDark));

        mAdapter = new ProductTemplateAdapter(getActivity(), new ArrayList<ProductTemplate>());
        productTemplateListView.setAdapter(mAdapter);

        productTemplateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ProductTemplate selectedProduct = mAdapter.getItem(i);
                int productTmplId = selectedProduct.getId();
                String productTmplCode = selectedProduct.getRef();
                String productTmplName = selectedProduct.getName();
                String productCategory = selectedProduct.getProductCategory().getName();
                String productUnit = selectedProduct.getProductUom().getName();

                // Send picking id to stock detail activity
                Intent intent = new Intent(getActivity(), ProductDetailOffline.class);
                intent.putExtra("product_tmpl_id", productTmplId);
                intent.putExtra("product_tmpl_code", productTmplCode);
                intent.putExtra("product_tmpl_name", productTmplName);
                intent.putExtra("product_categ", productCategory);
                intent.putExtra("product_uom", productUnit);
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
        mInputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAdapter.getFilter().filter(s);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Initialize account information with data from Preferences
        mPref = this.getActivity().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.product_template_title_offline));

        // Display initial data
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Show loading indicator if downloading is in progress
        if(isDownloadingProduct) {
            mSwipeView.setRefreshing(true);
        }
        if(isDownloadingPricelist) {
            mLoadingIndicatorView.setVisibility(View.VISIBLE);
        }
    }

    public void initData() {
        // Get a reference to the LoaderManager, in order to interact with loaders.
        LoaderManager loaderManager = getLoaderManager();

        // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
        loaderManager.restartLoader(LOAD_PRODUCT_FROM_DATABASE_LOADER_ID, null, loadProductTemplateFromDatabaseListener);
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
            loaderManager.initLoader(FETCH_PRODUCT_FROM_SERVER_LOADER_ID, null, fetchProductTemplateFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicatorView.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    public void getPricelistData() {
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
            loaderManager.initLoader(FETCH_PRICELIST_FROM_SERVER_LOADER_ID, null, fetchProductPricelistFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            mLoadingIndicatorView.setVisibility(View.GONE);

            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.product_template_offline_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DialogInterface.OnClickListener dialogClickListener;
        AlertDialog.Builder builder;
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.show_search_input_text:
                mToggleInputText = !mToggleInputText;
                if (mToggleInputText) {
                    mInputText.setVisibility(View.VISIBLE);
                } else {
                    mInputText.setVisibility(View.GONE);
                }
                return true;
            case R.id.fetch_product_pricelist:
                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                alertDialog.setTitle(getText(R.string.start_download_alert_dialog_title_string));
                alertDialog.setMessage(getText(R.string.start_download_alert_dialog_string));
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getText(R.string.yes_string),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //Check whether data download is in progress
                                if(isDownloadingPricelist) {
                                    // Inform that data download is in progress
                                    dialog.dismiss();
                                    Toast.makeText(getActivity(), R.string.warning_download_is_in_progress, Toast.LENGTH_LONG).show();
                                } else {
                                    getPricelistData();
                                    dialog.dismiss();
                                }
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getText(R.string.no_string),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<List<ProductTemplate>> loadProductTemplateFromDatabaseListener = new LoaderManager.LoaderCallbacks<List<ProductTemplate>>() {
        @Override
        public Loader<List<ProductTemplate>> onCreateLoader(int i, Bundle bundle) {
            return new ProductTemplateLoaderFromDatabase(getActivity());
        }

        @Override
        public void onLoadFinished(Loader<List<ProductTemplate>> loader, List<ProductTemplate> productTemplates) {
            // Hide loading indicator because the data has been loaded
            mLoadingIndicatorView.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);

            // Set empty state text to display "No available stock picking."
            mEmptyStateTextView.setText(R.string.no_product_template);

            // Clear the adapter of previous stock picking
            mAdapter.clear();

            // If there is a valid list of {@link stock picking}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (productTemplates != null && !productTemplates.isEmpty()) {
                Collections.sort(productTemplates, new Comparator<ProductTemplate>() {
                    @Override
                    public int compare(ProductTemplate item1, ProductTemplate item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });
                mAdapter.addAll(productTemplates);
            } else {
                Toast.makeText(getActivity(), R.string.error_cannot_get_database, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ProductTemplate>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };

    private LoaderManager.LoaderCallbacks<Boolean> fetchProductTemplateFromServerListener = new LoaderManager.LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            isDownloadingProduct=true;
            ArrayList<Object[]> productTemplateFilterElements = new ArrayList<Object[]>(){};
            productTemplateFilterElements.add(new Object[] {"active", "=", true});
            //productTemplateFilterElements.add(new Object[] {"name", "ilike", "Tikar Eva"});
            Object[] filterArray = new Object[] {
                    productTemplateFilterElements
            };
            return new ProductTemplateLoaderToDatabase(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean successFlag) {
            // Hide loading indicator because the data has been loaded
            mLoadingIndicatorView.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);
            isDownloadingProduct = false;

            // Set empty state text to display "No available stock picking."
            mEmptyStateTextView.setText(R.string.no_product_template);

            // Fix problem where loader automatically started when activity restart
            if (loader != null) {
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.destroyLoader(loader.getId());
            }

            // Show dialog to inform that data successfully downloaded
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getText(R.string.finish_download_product_alert_dialog_title_string));
            alertDialog.setMessage(getText(R.string.finish_download_product_alert_dialog_string));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok_string),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            initData();
                        }
                    });
            alertDialog.show();
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };

    private LoaderManager.LoaderCallbacks<Boolean> fetchProductPricelistFromServerListener = new LoaderManager.LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            mLoadingIndicatorView.setVisibility(View.VISIBLE);
            isDownloadingPricelist = true;

            ArrayList<Object[]> productPricelistFilterElements = new ArrayList<Object[]>(){};
            //productPricelistFilterElements.add(new Object[] {"product_tmpl_id", "ilike", "Tikar Eva"});
            Object[] filterArray = new Object[] {
                    productPricelistFilterElements
            };
            return new ProductTemplateDetailLoaderToDatabase(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean successFlag) {
            // Hide loading indicator because the data has been loaded
            mLoadingIndicatorView.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);
            isDownloadingPricelist = false;

            // Set empty state text to display "No available stock picking."
            mEmptyStateTextView.setText(R.string.no_product_template);

            // Fix problem where loader automatically started when activity restart
            if (loader != null) {
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.destroyLoader(loader.getId());
            }

            // Show dialog to inform that data successfully downloaded
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
            alertDialog.setTitle(getText(R.string.finish_download_pricelist_alert_dialog_title_string));
            alertDialog.setMessage(getText(R.string.finish_download_pricelist_alert_dialog_string));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getText(R.string.ok_string),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };
}

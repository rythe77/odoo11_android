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
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.MainActivity;
import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.adapter.HrEmployeeAttendanceAdapter;
import org.duckdns.toserba23.toserba23.loader.HrEmployeeLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.HrEmployee;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 02/09/18.
 */

public class Attendance extends Fragment{

    private static final int FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID = 1;

    private ArrayAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeView;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private AccessRight mAccess;

    private ArrayList<Object[]> mEmployeeFilterElements = new ArrayList<Object[]>(){};

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

        final View rootView = inflater.inflate(R.layout.standard_list_view_button, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView hrEmployeeListView = (ListView) rootView.findViewById(R.id.list);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        hrEmployeeListView.setEmptyView(mEmptyStateTextView);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open scanner activity
                Intent intent = new Intent(getActivity(), AttendanceScanner.class);
                getActivity().startActivity(intent);
            }
        });

        mAdapter = new HrEmployeeAttendanceAdapter(getActivity(), new ArrayList<HrEmployee>());
        hrEmployeeListView.setAdapter(mAdapter);

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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void createFilterData() {
        mEmployeeFilterElements.clear();
        mEmployeeFilterElements.add(new Object[] {"active", "=", true});
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.attendance_list_title));

        // Fetch data from server
        createFilterData();
        initData();
    }

    public void initData() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID, null, loadHrEmployeeFromServerListener);
        } else {
            // Otherwise, display error
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    public void getData() {
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID, null, loadHrEmployeeFromServerListener);
        } else {
            // Otherwise, display error
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    private LoaderManager.LoaderCallbacks<List<HrEmployee>> loadHrEmployeeFromServerListener = new LoaderManager.LoaderCallbacks<List<HrEmployee>>() {
        @Override
        public Loader<List<HrEmployee>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            Object[] filterArray = new Object[] {
                    mEmployeeFilterElements
            };
            return new HrEmployeeLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<HrEmployee>> loader, List<HrEmployee> hrEmployees) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);
            mEmptyStateTextView.setText(R.string.error_employee_not_found);

            // Clear the adapter of previous data
            mAdapter.clear();

            // If there is a valid list of {@link sale order}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (hrEmployees != null && !hrEmployees.isEmpty()) {
                mAdapter.addAll(hrEmployees);
            } else {
                Toast.makeText(getActivity(), R.string.error_employee_not_found, Toast.LENGTH_LONG).show();
            }
            getLoaderManager().destroyLoader(FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID);
        }

        @Override
        public void onLoaderReset(Loader<List<HrEmployee>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };

}

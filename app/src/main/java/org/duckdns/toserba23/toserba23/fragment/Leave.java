package org.duckdns.toserba23.toserba23.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.duckdns.toserba23.toserba23.MainActivity;
import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.adapter.HrHolidayAdapter;
import org.duckdns.toserba23.toserba23.loader.GenericModelLoader;
import org.duckdns.toserba23.toserba23.loader.HrHolidaysLoader;
import org.duckdns.toserba23.toserba23.loader.HrHolidaysSaveLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Leave extends Fragment {

    private static final int FETCH_HR_HOLIDAYS_LOADER_ID = 1;
    private static final int FETCH_HOLIDAY_STATUS_LOADER_ID = 2;
    private static final int CREATE_HR_HOLIDAYS_LOADER_ID = 3;
    private static final int FETCH_HR_EMPLOYEE_LOADER_ID = 4;
    private static final int LEAVE_ALL = 0;
    private static final int LEAVE_CONFIRM = 1;
    private static final int LEAVE_VALIDATE = 2;

    private HrHolidayAdapter mAdapter;
    private TextView mEmptyStateTextView;
    private SwipeRefreshLayout mSwipeView;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private HrHolidays mHrHolidays;
    private GenericModel mHrEmployee;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private AccessRight mAccess;

    private ArrayList<Object[]> mLeaveFilterElements = new ArrayList<Object[]>(){};
    private int mLeaveStateFilter = LEAVE_ALL;

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

        final View rootView = inflater.inflate(R.layout.standard_list_view_button, container, false);

        // Find a reference to the {@link ListView} in the layout
        ListView hrHolidaysListView = (ListView) rootView.findViewById(R.id.list);
        mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty_view);
        hrHolidaysListView.setEmptyView(mEmptyStateTextView);

        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setImageResource(R.drawable.ic_add_circle);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getEmployeeId();
            }
        });

        mAdapter = new HrHolidayAdapter(getActivity(), new ArrayList<HrHolidays>());
        hrHolidaysListView.setAdapter(mAdapter);

        hrHolidaysListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int hrHolidaysId = mAdapter.getItem(i).getId();

                Intent intent = new Intent(getActivity(), LeaveDetail.class);
                intent.putExtra("leave_id", hrHolidaysId);
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

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getData();
    }

    private void createFilterData() {
        mLeaveFilterElements.clear();
        if (mLeaveStateFilter==LEAVE_CONFIRM) {
            mLeaveFilterElements.add(new Object[] {"state", "in", new Object[]{"confirm"}});
        } else if (mLeaveStateFilter==LEAVE_VALIDATE) {
            mLeaveFilterElements.add(new Object[] {"state", "in", new Object[]{"validate1"}});
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.leave_list_title));

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
            loaderManager.initLoader(FETCH_HR_HOLIDAYS_LOADER_ID, null, loadHrHolidaysFromServerListener);
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
            loaderManager.restartLoader(FETCH_HR_HOLIDAYS_LOADER_ID, null, loadHrHolidaysFromServerListener);
        } else {
            // Otherwise, display error
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(R.string.error_no_internet_connection);
        }
    }

    public void getEmployeeId() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(FETCH_HR_EMPLOYEE_LOADER_ID, null, loadEmployeeIdListener);
        } else {
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getString(R.string.error_no_internet_connection));
        }
    }

    public void loadHolidayStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(FETCH_HOLIDAY_STATUS_LOADER_ID, null, loadHolidayStatusListener);
        } else {
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getString(R.string.error_no_internet_connection));
        }
    }

    public void saveHolidays() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(CREATE_HR_HOLIDAYS_LOADER_ID, null, saveLeaveDetailListener);
        } else {
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getString(R.string.error_no_internet_connection));
        }
    }

    public void openCreateWindow(List<GenericModel> holidaysStatus) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.hr_holidays_detail_edit_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final TextView employeeView = (TextView) dialog.findViewById(R.id.detail_leave_edit_employee);
        final EditText nameView = (EditText) dialog.findViewById(R.id.detail_leave_edit_name);
        final Spinner holidayStatusView = dialog.findViewById(R.id.detail_leave_edit_holiday_status);
        final DatePicker dateFromDateView = dialog.findViewById(R.id.detail_leave_edit_date_from_date);
        final TimePicker dateFromTimeView = dialog.findViewById(R.id.detail_leave_edit_date_from_time);
        final DatePicker dateToDateView = dialog.findViewById(R.id.detail_leave_edit_date_to_date);
        final TimePicker dateToTimeView = dialog.findViewById(R.id.detail_leave_edit_date_to_time);
        final Button saveButton = dialog.findViewById(R.id.detail_leave_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // edit view cannot be empty
                if (nameView.getText().toString().isEmpty()) {
                    nameView.setError(getString(R.string.detail_leave_edit_name_empty));
                    return;
                }
                // Save data to a class object
                String name = nameView.getText().toString();
                GenericModel holidayStatus = holidaysStatus.get(holidayStatusView.getSelectedItemPosition());
                String dateFrom = DisplayFormatter.getDateTimeFromPicker(dateFromDateView, dateFromTimeView);
                String dateTo = DisplayFormatter.getDateTimeFromPicker(dateToDateView, dateToTimeView);
                GenericModel employee = mHrEmployee;
                mHrHolidays = new HrHolidays(name, holidayStatus, dateFrom, dateTo, employee);
                saveHolidays();
                dialog.dismiss();
            }
        });
        dialog.show();

        // Convert List<GenericModel> to Arraylist of holiday status
        ArrayList<String> holidayStatusList = new ArrayList<>();
        for (int i=0; i<holidaysStatus.size(); i++) {
            holidayStatusList.add(holidaysStatus.get(i).getName());
        }
        ArrayAdapter spinnerHolidayStatusAdapter = new ArrayAdapter(
                getActivity(),
                android.R.layout.simple_spinner_dropdown_item,
                holidayStatusList
        );
        holidayStatusView.setAdapter(spinnerHolidayStatusAdapter);

        // Set default value
        employeeView.setText(mHrEmployee.getName());
        holidayStatusView.setSelection(2);
        dateFromTimeView.setIs24HourView(Boolean.TRUE);
        dateFromTimeView.setCurrentHour(8);
        dateFromTimeView.setCurrentMinute(00);
        dateToTimeView.setIs24HourView(Boolean.TRUE);
        dateToTimeView.setCurrentHour(17);
        dateToTimeView.setCurrentMinute(30);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.leave_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.checkbox_leave_all:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mLeaveStateFilter = LEAVE_ALL;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_leave_confirm:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mLeaveStateFilter = LEAVE_CONFIRM;
                createFilterData();
                getData();
                return true;
            case R.id.checkbox_leave_validate:
                if (item.isChecked()) { item.setChecked(false);
                } else { item.setChecked(true); }
                mLeaveStateFilter = LEAVE_VALIDATE;
                createFilterData();
                getData();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private LoaderManager.LoaderCallbacks<List<HrHolidays>> loadHrHolidaysFromServerListener = new LoaderManager.LoaderCallbacks<List<HrHolidays>>() {
        @Override
        public Loader<List<HrHolidays>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            Object[] filterArray = new Object[] {
                    mLeaveFilterElements
            };
            return new HrHolidaysLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<HrHolidays>> loader, List<HrHolidays> hrHolidays) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            mSwipeView.setRefreshing(false);
            mEmptyStateTextView.setText(R.string.error_leaves_not_found);

            // Clear the adapter of previous data
            mAdapter.clear();

            // If there is a valid list of {@link sale order}s, then add them to the adapter's
            // data set. This will trigger the ListView to update.
            if (hrHolidays != null && !hrHolidays.isEmpty()) {
                mAdapter.addAll(hrHolidays);
            } else {
                Toast.makeText(getActivity(), R.string.error_leaves_not_found, Toast.LENGTH_LONG).show();
            }
            getLoaderManager().destroyLoader(FETCH_HR_HOLIDAYS_LOADER_ID);
        }

        @Override
        public void onLoaderReset(Loader<List<HrHolidays>> loader) {
            // Loader reset, so we can clear out our existing data.
            mAdapter.clear();
        }
    };

    private LoaderManager.LoaderCallbacks<List<GenericModel>> loadEmployeeIdListener = new LoaderManager.LoaderCallbacks<List<GenericModel>>() {
        @Override
        public Loader<List<GenericModel>> onCreateLoader(int i, Bundle bundle) {
            ArrayList<Object[]> employeeIdFilter = new ArrayList<Object[]>(){};
            employeeIdFilter.add(new Object[] {"active", "=", true});
            employeeIdFilter.add(new Object[] {"user_id", "=", mUserId});
            Object[] filterArray = new Object[] {
                    employeeIdFilter
            };
            // Get fields
            HashMap hrEmployeeMap = new HashMap();
            hrEmployeeMap.put("fields", Arrays.asList(
                    "id",
                    "name"
            ));
            return new GenericModelLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.HR_EMPLOYEE, filterArray, hrEmployeeMap);
        }
        @Override
        public void onLoadFinished(Loader<List<GenericModel>> loader, List<GenericModel> hrEmployee) {
            // Keep data as this class attributes and load holiday status
            mHrEmployee = !hrEmployee.isEmpty()?hrEmployee.get(0):null;
            getLoaderManager().destroyLoader(FETCH_HR_EMPLOYEE_LOADER_ID);
            loadHolidayStatus();
        }
        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<List<GenericModel>> loadHolidayStatusListener = new LoaderManager.LoaderCallbacks<List<GenericModel>>() {
        @Override
        public Loader<List<GenericModel>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    new ArrayList<Object[]>(){}
            };
            // Get fields
            HashMap hrHolidaysStatusMap = new HashMap();
            hrHolidaysStatusMap.put("fields", Arrays.asList(
                    "id",
                    "name"
            ));
            return new GenericModelLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.HR_HOLIDAYS_STATUS, filterArray, hrHolidaysStatusMap);
        }
        @Override
        public void onLoadFinished(Loader<List<GenericModel>> loader, List<GenericModel> hrHolidayStatus) {
            // Keep data as this class attributes and open edit view
            getLoaderManager().destroyLoader(FETCH_HOLIDAY_STATUS_LOADER_ID);
            openCreateWindow(hrHolidayStatus);
        }
        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<List<Integer>> saveLeaveDetailListener = new LoaderManager.LoaderCallbacks<List<Integer>>() {
        @Override
        public Loader<List<Integer>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            int saveType = QueryUtilsHrHolidays.HR_HOLIDAYS_CREATE_NEW;
            HrHolidaysSaveLoader newLoader = new HrHolidaysSaveLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, 0, saveType);
            newLoader.setDataMap(mHrHolidays.getHashmapToCreate());
            return newLoader;
        }
        @Override
        public void onLoadFinished(Loader<List<Integer>> loader, List<Integer> flagIntegers) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = getView().findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            if (flagIntegers != null && !flagIntegers.isEmpty()){
                if (flagIntegers.get(0) == 1) {
                    Toast.makeText(getActivity(), getString(R.string.detail_leave_confirm_success), Toast.LENGTH_LONG).show();
                }
            }
            getLoaderManager().destroyLoader(CREATE_HR_HOLIDAYS_LOADER_ID);
            getData();
        }
        @Override
        public void onLoaderReset(Loader<List<Integer>> loader) {
        }
    };
}

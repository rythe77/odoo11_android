package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.Result;

import org.duckdns.toserba23.toserba23.MainActivity;
import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.adapter.HrEmployeeAttendanceAdapter;
import org.duckdns.toserba23.toserba23.loader.AttendanceLoader;
import org.duckdns.toserba23.toserba23.loader.GenericModelLoader;
import org.duckdns.toserba23.toserba23.loader.HrEmployeeLoader;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.HrEmployee;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by ryanto on 02/09/18.
 */

public class AttendanceScanner extends Fragment implements ZXingScannerView.ResultHandler{

    private static final int FETCH_EMPLOYEE_LOADER_ID = 1;
    private static final int PUNCH_ATTENDANCE_RECORD_ID = 2;
    private static final int FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID = 3;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;

    int mEmployeeId = 0;
    private List<GenericModel> mEmployee;
    private ArrayList<Object[]> mEmployeeFilterElements = new ArrayList<Object[]>(){};

    private ZXingScannerView mScannerView;

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

        mScannerView = new ZXingScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.attendance_scan_title));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.attendance_scanner_options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.view_attendance:
                viewAttendance();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleResult(Result rawResult) {
        mEmployeeFilterElements.clear();
        mEmployeeFilterElements.add(new Object[] {"barcode", "ilike", rawResult.getText().toString()});
        getLoaderManager().restartLoader(FETCH_EMPLOYEE_LOADER_ID, null, loadEmployeeFromServerListener);
    }

    public void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.scan_result_title));
        builder.setMessage(message);
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public void restartCamera() {
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        restartCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    private LoaderManager.LoaderCallbacks<List<GenericModel>> loadEmployeeFromServerListener = new LoaderManager.LoaderCallbacks<List<GenericModel>>() {
        @Override
        public Loader<List<GenericModel>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mEmployeeFilterElements
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
        public void onLoadFinished(Loader<List<GenericModel>> loader, List<GenericModel> hrEmployees) {
            mEmployee = hrEmployees;
            if (mEmployee != null && !mEmployee.isEmpty() && mEmployee.size() == 1) {
                mEmployeeId = mEmployee.get(0).getId();
                getLoaderManager().destroyLoader(FETCH_EMPLOYEE_LOADER_ID);
                punchAttendance();
            } else if (mEmployee != null && !mEmployee.isEmpty() && mEmployee.size() != 1) {
                // Multiple products found for the current qr code, create dialog windows to let user choose which to see
                Toast.makeText(getActivity(), R.string.employee_multiple_scan_results, Toast.LENGTH_LONG).show();
            } else {
                showMessageDialog(getString(R.string.error_employee_not_found));
                restartCamera();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };

    public void punchAttendance() {
        getLoaderManager().restartLoader(PUNCH_ATTENDANCE_RECORD_ID, null, recordEmployeeAttendanceListener);
        restartCamera();
    }
    public void viewAttendance() {
        getLoaderManager().restartLoader(FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID, null, loadHrEmployeeFromServerListener);
        restartCamera();
    }

    private LoaderManager.LoaderCallbacks<Boolean> recordEmployeeAttendanceListener = new LoaderManager.LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            return new AttendanceLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, mEmployeeId);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(getActivity(), R.string.punching_attendance_success, Toast.LENGTH_LONG).show();
                viewAttendance();
            } else {
                showMessageDialog(getString(R.string.error_failed_punching_attendance));
            }
            getLoaderManager().destroyLoader(PUNCH_ATTENDANCE_RECORD_ID);
            restartCamera();
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<List<HrEmployee>> loadHrEmployeeFromServerListener = new LoaderManager.LoaderCallbacks<List<HrEmployee>>() {
        @Override
        public Loader<List<HrEmployee>> onCreateLoader(int i, Bundle bundle) {
            mEmployeeFilterElements.clear();
            mEmployeeFilterElements.add(new Object[] {"active", "=", true});
            Object[] filterArray = new Object[] {
                    mEmployeeFilterElements
            };
            return new HrEmployeeLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<HrEmployee>> loader, List<HrEmployee> hrEmployees) {
            // show employee with attendance
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.simple_list_view);
            dialog.setTitle(getString(R.string.attendance_list_title));
            ListView listView = (ListView) dialog.findViewById(R.id.list);
            ArrayAdapter adapter = new HrEmployeeAttendanceAdapter(getActivity(), new ArrayList<HrEmployee>());
            adapter.addAll(hrEmployees);
            listView.setAdapter(adapter);
            dialog.show();
            getLoaderManager().destroyLoader(FETCH_EMPLOYEE_ATTENDANCE_LOADER_ID);
        }

        @Override
        public void onLoaderReset(Loader<List<HrEmployee>> loader) {

        }
    };

}

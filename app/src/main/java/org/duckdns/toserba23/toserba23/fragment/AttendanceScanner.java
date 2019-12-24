package org.duckdns.toserba23.toserba23.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.zxing.Result;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.AttendanceLoader;
import org.duckdns.toserba23.toserba23.loader.GenericModelLoader;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by ryanto on 02/09/18.
 */

public class AttendanceScanner extends AppCompatActivity implements ZXingScannerView.ResultHandler{

    private static final int FETCH_EMPLOYEE_LOADER_ID = 1;
    private static final int PUNCH_ATTENDANCE_RECORD_ID = 2;
    private static final int MY_CAMERA_REQUEST_CODE = 100;

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

        // Initialize account information with data from Preferences
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        setTitle(getString(R.string.attendance_scan_title));

        // Ask for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);

        mScannerView = new ZXingScannerView(this);
        setContentView(mScannerView);
    }
    @Override
    public void handleResult(Result rawResult) {
        mEmployeeFilterElements.clear();
        mEmployeeFilterElements.add(new Object[] {"barcode", "ilike", rawResult.getText().toString()});
        getLoaderManager().restartLoader(FETCH_EMPLOYEE_LOADER_ID, null, loadEmployeeFromServerListener);
    }

    public void showMessageDialog(String title, String message, Boolean error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        if (error) {
            builder.setIcon(R.drawable.ic_error_red);
        } else {
            builder.setIcon(R.drawable.ic_success);
        }
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok_string, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
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
            return new GenericModelLoader(AttendanceScanner.this, mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.HR_EMPLOYEE, filterArray, hrEmployeeMap);
        }

        @Override
        public void onLoadFinished(Loader<List<GenericModel>> loader, List<GenericModel> hrEmployees) {
            mEmployee = hrEmployees;
            if (mEmployee != null && !mEmployee.isEmpty() && mEmployee.size() == 1) {
                mEmployeeId = mEmployee.get(0).getId();
                getLoaderManager().destroyLoader(FETCH_EMPLOYEE_LOADER_ID);
                //punch attendance
                getLoaderManager().restartLoader(PUNCH_ATTENDANCE_RECORD_ID, null, recordEmployeeAttendanceListener);
            } else if (mEmployee != null && !mEmployee.isEmpty() && mEmployee.size() != 1) {
                // Multiple employees found for the current qr code
                showMessageDialog(getString(R.string.scan_failed), getString(R.string.employee_multiple_scan_results), Boolean.TRUE);
            } else {
                showMessageDialog(getString(R.string.scan_failed), getString(R.string.error_employee_not_found), Boolean.TRUE);
            }
        }

        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<Boolean> recordEmployeeAttendanceListener = new LoaderManager.LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            return new AttendanceLoader(AttendanceScanner.this, mUrl, mDatabaseName, mUserId, mPassword, mEmployeeId);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean aBoolean) {
            if (aBoolean) {
                showMessageDialog(getString(R.string.scan_success), getString(R.string.punching_attendance_success), Boolean.FALSE);
            } else {
                showMessageDialog(getString(R.string.scan_failed), getString(R.string.error_failed_punching_attendance), Boolean.TRUE);
            }
            getLoaderManager().destroyLoader(PUNCH_ATTENDANCE_RECORD_ID);
        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {

        }
    };
}

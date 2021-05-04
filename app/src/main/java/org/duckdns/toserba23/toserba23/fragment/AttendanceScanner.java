package org.duckdns.toserba23.toserba23.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
    private static final int LOCATION_REQUEST_CODE = 200;

    private static final int ALLOWED_LOCATION_RANGE = 300; // in meter
    private static final int LOCATION_MAX_AGE = 600; // in seconds

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

    private LocationManager mLm;
    private LocationListener mLocationListener = new MyLocationListenerGPS();

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

        mLm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mScannerView = new ZXingScannerView(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        checkLocationPermission();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location request
        mLm.removeUpdates(mLocationListener);
        // Stop camera on pause
        mScannerView.removeAllViews();
        mScannerView.stopCamera();
    }

    public void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            // Ask for location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            // Check whether location service is enabled
            if (mLm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Check last known location age
                Location location = mLm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    long location_age = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1000000000;
                    if (location_age > LOCATION_MAX_AGE) {
                        mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                    }
                    checkLocation(location);
                } else {
                    // show error, can not get current location, please try again
                    showMessageDialog(getString(R.string.error_location_title), getString(R.string.error_location_body1), true);
                    mLm.requestSingleUpdate(LocationManager.GPS_PROVIDER, mLocationListener, null);
                }
            } else {
                // Show location service disabled error
                showMessageDialog(getString(R.string.error_service_disabled_title), getString(R.string.error_service_disabled_body), true);
            }
        }
    }

    public class MyLocationListenerGPS implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            checkLocation(location);
        }
        @Override public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override public void onProviderEnabled(String provider) { }
        @Override public void onProviderDisabled(String provider) { }
    }

    public void checkLocation(Location location) {
        // Check mock provider
        if (location.isFromMockProvider()) {
            showMessageDialog(getString(R.string.error_mock_provider_title), getString(R.string.error_mock_provider_body), true);
        } else {
            float latitude = (float) location.getLatitude();
            float longitude = (float) location.getLongitude();
            // Get allowed location
            float userLat = mPref.getFloat(getString(R.string.settings_user_lat_key), 256);
            float userLon = mPref.getFloat(getString(R.string.settings_user_lon_key), 256);

            if (isLocationAllowed(latitude, longitude, userLat, userLon, ALLOWED_LOCATION_RANGE)) {
                // Activate qr code scanner
                setContentView(mScannerView);
                mScannerView.startCamera();
            } else {
                // Stop camera
                mScannerView.removeAllViews();
                mScannerView.stopCamera();
            }
        }
    }

    public Boolean isLocationAllowed(float currentLat, float currentLon, float userLat, float userLon, float range) {
        if (userLat == 255 & userLon == 255) {
            // show error, can not get allowed location data from server, please try to restart the app or contact system administrator if you have tried restarting
            showMessageDialog(getString(R.string.error_location_title), getString(R.string.error_location_body2), true);
        } else if (userLat == 256 & userLon == 256) {
            // show error, can not get allowed location data preferences, please try to restart the app or contact system administrator if you have tried restarting
            showMessageDialog(getString(R.string.error_location_title), getString(R.string.error_location_body3), true);
        } else if (currentLat < -90 || currentLat > 90 ||
                currentLon < -180 || currentLon > 180 ||
                userLat < -90 || userLat > 90 ||
                userLon < -180 || userLon > 180) {
            // show error, coordinate data not valid, contact system administrator with this error message
            showMessageDialog(getString(R.string.error_invalid_data_title),
                    getString(R.string.error_invalid_data_body) + " UserLat : " + userLat +
                            ". UserLon : " + userLon + ". CurrentLat : " + currentLat + ". CurrentLon : " + currentLon, true);
        } else if (userLat == 0 & userLon == 0) {
            return true;
        } else {
            // Check whether current location is allowed
            float[] result = new float[1];
            Location.distanceBetween(currentLat, currentLon, userLat, userLon, result);
            //Log.v("LOG_TAG", "Distance between result: " + result[0]);
            if (result[0] < range) {
                return true;
            } else {
                // Attendance scan is not allowed from current location
                showMessageDialog(getString(R.string.error_not_allowed_title),
                        getString(R.string.error_not_allowed_body) + " UserLat : " + userLat +
                        ". UserLon : " + userLon + ". CurrentLat : " + currentLat + ". CurrentLon : " + currentLon + ". Dist : " + result[0], true);
            }
        }
        return false;
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

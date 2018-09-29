package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAttendance;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsGenericModel;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 29/09/18.
 */

public class AttendanceLoader extends AsyncTaskLoader<Boolean> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mEmployeeId;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public AttendanceLoader(Context context, String url, String databaseName, int userId, String password, int employeeId) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mEmployeeId = employeeId;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public Boolean loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        Boolean result = QueryUtilsAttendance.recordAttendance(mUrl, mDatabaseName, mUserId, mPassword, mEmployeeId);
        return result;
    }
}

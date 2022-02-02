package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsSaleOrder;

import java.util.HashMap;
import java.util.List;

public class HrHolidaysSaveLoader extends AsyncTaskLoader<List<Integer>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mItemId;
    private HashMap mDataMap;
    private int mSaveType;

    public HrHolidaysSaveLoader(Context context, String url, String databaseName, int userId, String password, int ItemId, int saveType) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mItemId = ItemId;
        mSaveType = saveType;
    }

    public void setDataMap(HashMap dataMap) { mDataMap = dataMap; }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<Integer> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<Integer> createdIds;
        switch (mSaveType) {
            case QueryUtilsHrHolidays.HR_HOLIDAYS_SAVE_CHANGES:
                createdIds = QueryUtilsHrHolidays.saveHolidays(mUrl, mDatabaseName, mUserId, mPassword, mItemId, mDataMap);
                break;
            case QueryUtilsHrHolidays.HR_HOLIDAYS_CREATE_NEW:
                createdIds = QueryUtilsHrHolidays.saveHolidays(mUrl, mDatabaseName, mUserId, mPassword, 0, mDataMap);
                break;
            case QueryUtilsHrHolidays.HR_HOLIDAYS_VALIDATE:
                createdIds = QueryUtilsHrHolidays.validateLeave(mUrl, mDatabaseName, mUserId, mPassword, mItemId);
                break;
            default:
                createdIds = QueryUtilsHrHolidays.approveLeave(mUrl, mDatabaseName, mUserId, mPassword, mItemId);
                break;
        }

        return createdIds;
    }
}

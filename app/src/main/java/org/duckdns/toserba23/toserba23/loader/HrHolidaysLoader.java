package org.duckdns.toserba23.toserba23.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;

import org.duckdns.toserba23.toserba23.model.HrEmployee;
import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAttendance;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;

import java.util.List;

public class HrHolidaysLoader extends AsyncTaskLoader<List<HrHolidays>> {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    /** Query URL */
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private Object[] mFilter;

    /**
     * @param context of the activity
     * @param url to load data from
     */
    public HrHolidaysLoader(Context context, String url, String databaseName, int userId, String password, Object[] filter) {
        super(context);
        mUrl = url;
        mDatabaseName = databaseName;
        mUserId = userId;
        mPassword = password;
        mFilter = filter;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    /**
     * This is on a background thread.
     */
    @Override
    public List<HrHolidays> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<HrHolidays> hrHolidays = QueryUtilsHrHolidays.searchReadHrHolidaysList(mUrl, mDatabaseName, mUserId, mPassword, mFilter, QueryUtils.LIMIT_PAGING_SIZE, 0);
        return hrHolidays;
    }
}

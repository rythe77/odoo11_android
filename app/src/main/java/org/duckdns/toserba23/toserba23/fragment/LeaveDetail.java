package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.GenericModelLoader;
import org.duckdns.toserba23.toserba23.loader.HrHolidaysSaveLoader;
import org.duckdns.toserba23.toserba23.loader.LeaveDetailLoader;
import org.duckdns.toserba23.toserba23.loader.MailMessageCreateLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.model.MailMessage;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsHrHolidays;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class LeaveDetail extends AppCompatActivity {

    private static final int FETCH_LEAVE_DETAIL_LOADER_ID = 1;
    private static final int CONFIRM_LEAVE_DETAIL_LOADER_ID = 2;
    private static final int FETCH_HOLIDAY_STATUS_LOADER_ID = 3;
    private static final int SAVE_MAIL_MESSAGE_LOADER_ID = 4;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    private HrHolidays mHrHolidays;
    private List<GenericModel> mHolidaysStatus;
    private MailMessage mNewMailMessage;

    Toolbar mToolbar;
    LinearLayout mLeaveLogViewContainer;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mLeaveId;
    private AccessRight mAccess;
    private int mSaveType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mLeaveId = getIntent().getIntExtra("leave_id", 0);
        mAccess = getIntent().getParcelableExtra(QueryUtilsAccessRight.ACCESS_RIGHT);
        setTitle(getString(R.string.detail_leave_detail_activity_label));

        setContentView(R.layout.hr_holidays_detail_app_bar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Sale confirmation FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mHrHolidays.getStatus().equals("confirm")) {
                    if (mAccess != null & mAccess.has_access_to_leave_approval) {
                        // Verify whether user really wants to approve
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        mSaveType = QueryUtilsHrHolidays.HR_HOLIDAYS_APPROVE;
                                        saveHolidays();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveDetail.this);
                        builder.setMessage(getString(R.string.detail_leave_confirm_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                                .setNegativeButton(R.string.no_string, dialogClickListener).show();
                    } else {
                        Toast.makeText(LeaveDetail.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                    }
                } else if (mHrHolidays.getStatus().equals("validate1")) {
                    if (mAccess != null & mAccess.has_access_to_leave_validation) {
                        // Verify whether user really wants to validate
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        mSaveType = QueryUtilsHrHolidays.HR_HOLIDAYS_VALIDATE;
                                        saveHolidays();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(LeaveDetail.this);
                        builder.setMessage(getString(R.string.detail_leave_confirm_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                                .setNegativeButton(R.string.no_string, dialogClickListener).show();
                    } else {
                        Toast.makeText(LeaveDetail.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LeaveDetail.this, getString(R.string.detail_leave_confirm_refuse), Toast.LENGTH_LONG).show();
                }
            }
        });

        // Set listener for edit and log note button
        TextView edit_button = findViewById(R.id.edit_button);
        edit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Block edit if status is not confirm or draft
                if (mHrHolidays.getStatus().equals("confirm") || mHrHolidays.getStatus().equals("draft")) {
                    loadHolidayStatus();
                } else {
                    Toast.makeText(LeaveDetail.this, getString(R.string.detail_leave_edit_refuse), Toast.LENGTH_LONG).show();
                }
            }
        });
        TextView log_note_button = findViewById(R.id.log_note_button);
        log_note_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openLogNoteWindow();
            }
        });

        // Set pricelist container view
        mLeaveLogViewContainer = (LinearLayout) findViewById(R.id.container_view);

        readData();
    }

    public void readData() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.restartLoader(FETCH_LEAVE_DETAIL_LOADER_ID, null, loadLeaveDetailFromServerListener);
        } else {
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    public void loadHolidayStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(FETCH_HOLIDAY_STATUS_LOADER_ID, null, loadHolidayStatusListener);
        } else {
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    public void saveHolidays() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(CONFIRM_LEAVE_DETAIL_LOADER_ID, null, saveLeaveDetailListener);
        } else {
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    public void saveMailMessage() {
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            getLoaderManager().restartLoader(SAVE_MAIL_MESSAGE_LOADER_ID, null, saveMailMessageListener);
        } else {
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    private void openEditWindow() {
        final Dialog dialog = new Dialog(LeaveDetail.this);
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
                // Save data to the class object
                mHrHolidays.setName(nameView.getText().toString());
                mHrHolidays.setHolidayStatus(mHolidaysStatus.get(holidayStatusView.getSelectedItemPosition()));
                mHrHolidays.setDateFrom(DisplayFormatter.getDateTimeFromPicker(dateFromDateView, dateFromTimeView));
                mHrHolidays.setDateTo(DisplayFormatter.getDateTimeFromPicker(dateToDateView, dateToTimeView));
                mSaveType = QueryUtilsHrHolidays.HR_HOLIDAYS_SAVE_CHANGES;
                saveHolidays();
                dialog.dismiss();
            }
        });
        dialog.show();

        // Set default value
        employeeView.setText(mHrHolidays.getEmployee().getName());
        nameView.setText(mHrHolidays.getName());

        // Convert List<GenericModel> to Arraylist of holiday status
        ArrayList<String> holidayStatusList = new ArrayList<>();
        for (int i=0; i<mHolidaysStatus.size(); i++) {
            holidayStatusList.add(mHolidaysStatus.get(i).getName());
        }
        ArrayAdapter spinnerHolidayStatusAdapter = new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                holidayStatusList
        );
        holidayStatusView.setAdapter(spinnerHolidayStatusAdapter);
        int spinner_position = spinnerHolidayStatusAdapter.getPosition(mHrHolidays.getHolidayStatus().getName());
        holidayStatusView.setSelection(spinner_position);

        Calendar date_from = Calendar.getInstance(TimeZone.getDefault());
        date_from.setTime(DisplayFormatter.parseDateTime(mHrHolidays.getDateFrom()));
        dateFromDateView.updateDate(date_from.get(Calendar.YEAR), date_from.get(Calendar.MONTH), date_from.get(Calendar.DAY_OF_MONTH));
        dateFromTimeView.setIs24HourView(Boolean.TRUE);
        dateFromTimeView.setCurrentHour(date_from.get(Calendar.HOUR_OF_DAY));
        dateFromTimeView.setCurrentMinute(date_from.get(Calendar.MINUTE));
        Calendar date_to = Calendar.getInstance(TimeZone.getDefault());
        date_to.setTime(DisplayFormatter.parseDateTime(mHrHolidays.getDateTo()));
        dateToDateView.updateDate(date_to.get(Calendar.YEAR), date_to.get(Calendar.MONTH), date_to.get(Calendar.DAY_OF_MONTH));
        dateToTimeView.setIs24HourView(Boolean.TRUE);
        dateToTimeView.setCurrentHour(date_to.get(Calendar.HOUR_OF_DAY));
        dateToTimeView.setCurrentMinute(date_to.get(Calendar.MINUTE));
    }

    private void openLogNoteWindow() {
        final Dialog dialog = new Dialog(LeaveDetail.this);
        dialog.setContentView(R.layout.mail_message_create_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final EditText bodyView = (EditText) dialog.findViewById(R.id.mail_message_create_dialog_body);
        final Button saveButton = dialog.findViewById(R.id.mail_message_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // edit view cannot be empty
                if (bodyView.getText().toString().isEmpty()) {
                    bodyView.setError(getString(R.string.detail_leave_edit_name_empty));
                    return;
                }
                // Save data to the class object
                String body = Html.toHtml(bodyView.getText());
                mNewMailMessage = new MailMessage(QueryUtils.HR_HOLIDAYS, mLeaveId, "comment", 2, body);
                saveMailMessage();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private LoaderManager.LoaderCallbacks<HrHolidays> loadLeaveDetailFromServerListener = new LoaderManager.LoaderCallbacks<HrHolidays>() {
        @Override
        public Loader<HrHolidays> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new LeaveDetailLoader(LeaveDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mLeaveId);
        }
        @Override
        public void onLoadFinished(Loader<HrHolidays> loader, HrHolidays hrHoliday) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mHrHolidays = hrHoliday;
            displayUpdate(hrHoliday);
            getLoaderManager().destroyLoader(FETCH_LEAVE_DETAIL_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<HrHolidays> loader) {
        }
    };

    private void displayUpdate(HrHolidays hrHoliday) {
        if (hrHoliday != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_holiday_status)).setText(DisplayFormatter.formatString(hrHoliday.getHolidayStatus().getName()));
            ((TextView) findViewById(R.id.detail_date_from)).setText(DisplayFormatter.formatDateTime(hrHoliday.getDateFrom()));
            ((TextView) findViewById(R.id.detail_date_to)).setText(DisplayFormatter.formatDateTime(hrHoliday.getDateTo()));
            ((TextView) findViewById(R.id.detail_employee)).setText(DisplayFormatter.formatString(hrHoliday.getEmployee().getName()));
            ((TextView) findViewById(R.id.detail_status)).setText(DisplayFormatter.formatString(hrHoliday.getStatusName()));
            ((TextView) findViewById(R.id.detail_name)).setText(DisplayFormatter.formatString(hrHoliday.getName()));

            // Prepare linear layout view which will contain inflated product row view
            LayoutInflater internalInflater = LayoutInflater.from(getApplicationContext());
            ArrayList<MailMessage> mailMessages = mHrHolidays.getMailMessage();

            // Display pricelist data onto product row which will be inflated based on number of products to be displayed
            mLeaveLogViewContainer.removeAllViews();
            if (mailMessages !=null && !mailMessages.isEmpty()) {
                for (int i = 0; i < mailMessages.size(); i++) {
                    MailMessage mailMessage = mailMessages.get(i);
                    View rowView = internalInflater.inflate(R.layout.mail_message_adapter, mLeaveLogViewContainer, false);
                    ((TextView) rowView.findViewById(R.id.mail_message_date)).setText(DisplayFormatter.formatDateTime(mailMessage.getDate()));
                    ((TextView) rowView.findViewById(R.id.mail_message_from)).setText(DisplayFormatter.formatString(mailMessage.getFrom()));
                    ((TextView) rowView.findViewById(R.id.mail_message_body)).setText(Html.fromHtml(mailMessage.getBody()));
                    mLeaveLogViewContainer.addView(rowView);
                }
            }
        } else {
            Toast.makeText(this, R.string.error_cannot_connect_to_server, Toast.LENGTH_LONG).show();
        }
    }

    private LoaderManager.LoaderCallbacks<List<Integer>> saveLeaveDetailListener = new LoaderManager.LoaderCallbacks<List<Integer>>() {
        @Override
        public Loader<List<Integer>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            HrHolidaysSaveLoader newLoader = new HrHolidaysSaveLoader(LeaveDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mLeaveId, mSaveType);
            // Set hashmap if saving or create new leave
            if ((mSaveType == QueryUtilsHrHolidays.HR_HOLIDAYS_SAVE_CHANGES) || (mSaveType == QueryUtilsHrHolidays.HR_HOLIDAYS_CREATE_NEW)) {
                newLoader.setDataMap(mHrHolidays.getHashmap());
            }
            return newLoader;
        }
        @Override
        public void onLoadFinished(Loader<List<Integer>> loader, List<Integer> flagIntegers) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            if (flagIntegers != null && !flagIntegers.isEmpty()){
                if (flagIntegers.get(0) == 1) {
                    Toast.makeText(LeaveDetail.this, getString(R.string.detail_leave_confirm_success), Toast.LENGTH_LONG).show();
                }
            }
            readData();
            getLoaderManager().destroyLoader(CONFIRM_LEAVE_DETAIL_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<List<Integer>> loader) {
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
            return new GenericModelLoader(LeaveDetail.this, mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.HR_HOLIDAYS_STATUS, filterArray, hrHolidaysStatusMap);
        }
        @Override
        public void onLoadFinished(Loader<List<GenericModel>> loader, List<GenericModel> hrHolidayStatus) {
            // Keep data as this class attributes and open edit view
            mHolidaysStatus = hrHolidayStatus;
            getLoaderManager().destroyLoader(FETCH_HOLIDAY_STATUS_LOADER_ID);
            openEditWindow();
        }
        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };

    private LoaderManager.LoaderCallbacks<List<Integer>> saveMailMessageListener = new LoaderManager.LoaderCallbacks<List<Integer>>() {
        @Override
        public Loader<List<Integer>> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            return new MailMessageCreateLoader(LeaveDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mNewMailMessage.getHashmapToCreate());
        }
        @Override
        public void onLoadFinished(Loader<List<Integer>> loader, List<Integer> flagIntegers) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            readData();
            getLoaderManager().destroyLoader(SAVE_MAIL_MESSAGE_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<List<Integer>> loader) {
        }
    };
}

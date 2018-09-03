package org.duckdns.toserba23.toserba23;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.loader.LoginLoader;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 22/02/18.
 */

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    private EditText edtUsername, edtPassword, edtSelfHosted;
    private Boolean mConnectedToServer = false;
    private Boolean mAutoLogin = false;
    private Spinner databaseSpinner = null;
    private List<String> databases = new ArrayList<>();
    private TextView mLoginProcessStatus = null;

    // Account information
    private String mUrl = null;
    private String mUsername = null;
    private String mPassword = null;
    private String mDatabaseName = null;

    // Preferences variable
    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;
    private int PRIVATE_MODE = 0;

    private static final int LOGIN_LOADER_ID = 1;
    private LoaderManager loginLoaderManager = getLoaderManager();

    // Setup the listener for loader to fetch data from remote server
    private LoaderManager.LoaderCallbacks<Integer> loginLoaderListener = new LoaderManager.LoaderCallbacks<Integer>() {
        @Override
        public Loader<Integer> onCreateLoader(int i, Bundle bundle) {
            // For Autologin, get parameter values from preferences, else get values from edittext field
            if (mAutoLogin) {
                String url = mPref.getString(getString(R.string.settings_url_key), null);
                String username = mPref.getString(getString(R.string.settings_account_name_key), null);
                String password = mPref.getString(getString(R.string.settings_password_key), null);
                String databasename = mPref.getString(getString(R.string.settings_database_name__key), null);
                return new LoginLoader(LoginActivity.this, url, username, password, databasename);
            } else {
                return new LoginLoader(LoginActivity.this, mUrl, mUsername, mPassword, mDatabaseName);
            }
        }

        @Override
        public void onLoadFinished(Loader<Integer> loader, Integer userId) {
            if ( userId != 0 ) {
                if ( !mAutoLogin ) {
                    // Store login value in preferences
                    mPrefEditor.putBoolean(getString(R.string.settings_is_login_key), true);
                    mPrefEditor.putString(getString(R.string.settings_url_key), mUrl);
                    mPrefEditor.putString(getString(R.string.settings_account_name_key), mUsername);
                    mPrefEditor.putString(getString(R.string.settings_password_key), mPassword);
                    mPrefEditor.putString(getString(R.string.settings_database_name__key), mDatabaseName);
                    mPrefEditor.putInt(getString(R.string.settings_user_id_key), userId);
                    mPrefEditor.commit();
                }

                // Update progres bar view
                mLoginProcessStatus.setText(getString(R.string.status_login_success));

                // Transfer intent to main activity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                if ( mAutoLogin ) {
                    mAutoLogin = false;
                    findViewById(R.id.controls).setVisibility(View.VISIBLE);
                    findViewById(R.id.login_progress).setVisibility(View.GONE);
                    edtSelfHosted.setText(mPref.getString(getString(R.string.settings_url_key), null));
                    edtUsername.setText(mPref.getString(getString(R.string.settings_account_name_key), null));
                    edtPassword.setText(mPref.getString(getString(R.string.settings_password_key), null));
                    edtSelfHosted.setError(getString(R.string.error_cannot_connect_to_server));
                    edtSelfHosted.requestFocus();
                } else {
                    mPrefEditor.putBoolean(getString(R.string.settings_is_login_key), false);
                    findViewById(R.id.controls).setVisibility(View.VISIBLE);
                    findViewById(R.id.login_progress).setVisibility(View.GONE);
                    edtUsername.setError(getString(R.string.error_invalid_username_or_password));
                }
            }

            if (loader != null) {    //this will fix you NPE
                loginLoaderManager.destroyLoader(loader.getId());
            }
        }

        @Override
        public void onLoaderReset(Loader<Integer> loader) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_layout);

        init();

        // Initialize Preferences
        mPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mPrefEditor = mPref.edit();
        mAutoLogin = mPref.getBoolean(getString(R.string.settings_is_login_key), false);
        if ( mAutoLogin ) {
            loginProcess();
        }
    }

    private void init() {
        mLoginProcessStatus = (TextView) findViewById(R.id.login_process_status);
        databaseSpinner = (Spinner) findViewById(R.id.spinnerDatabaseList);
        findViewById(R.id.btnLogin).setOnClickListener(this);
        findViewById(R.id.btnToOffline).setOnClickListener(this);
        edtSelfHosted = (EditText) findViewById(R.id.edtSelfHostedURL);
        edtUsername = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtSelfHosted.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, final boolean hasFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (v.getId() == R.id.edtSelfHostedURL && !hasFocus) {
                            if (!TextUtils.isEmpty(edtSelfHosted.getText())
                                    && validateURL(edtSelfHosted.getText().toString())) {
                                edtSelfHosted.setError(null);
                                findViewById(R.id.imgValidURL).setVisibility(View.GONE);
                                findViewById(R.id.serverURLCheckProgress).setVisibility(View.VISIBLE);
                                findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
                                findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
                                String test_url = createServerURL(edtSelfHosted.getText().toString());
                                Log.v("", "Testing URL :" + test_url);
                                new checkServerTask().execute(test_url);
                            }
                        }
                    }
                }, 500);
            }
        });
        edtSelfHosted.requestFocus();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnLogin:
                loginUser();
                break;
            case R.id.btnToOffline:
                // Transfer intent to offline activity
                Intent intent = new Intent(LoginActivity.this, OfflineActivity.class);
                startActivity(intent);
                finish();
                break;
        }
    }

    private boolean validateURL(String url) {
        return (url.contains("."));
    }

    private String createServerURL(String server_url) {
        StringBuilder serverURL = new StringBuilder();
        if (!server_url.contains("http://") && !server_url.contains("https://")) {
            serverURL.append("https://");
        }
        serverURL.append(server_url);
        return serverURL.toString();
    }

    // User Login
    private void loginUser() {
        Log.v("", "LoginUser()");
        String serverURL = createServerURL(edtSelfHosted.getText().toString());
        String databaseName;
        edtUsername = (EditText) findViewById(R.id.edtUserName);
        edtPassword = (EditText) findViewById(R.id.edtPassword);

        edtSelfHosted.setError(null);
        if (TextUtils.isEmpty(edtSelfHosted.getText())) {
            edtSelfHosted.setError(getString(R.string.error_provide_server_url));
            edtSelfHosted.requestFocus();
            return;
        }
        if (databaseSpinner != null && databases.size() > 1 && databaseSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, getString(R.string.label_select_database), Toast.LENGTH_LONG).show();
            findViewById(R.id.controls).setVisibility(View.VISIBLE);
            findViewById(R.id.login_progress).setVisibility(View.GONE);
            return;
        }

        edtUsername.setError(null);
        edtPassword.setError(null);
        if (TextUtils.isEmpty(edtUsername.getText())) {
            edtUsername.setError(getString(R.string.error_provide_username));
            edtUsername.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(edtPassword.getText())) {
            edtPassword.setError(getString(R.string.error_provide_password));
            edtPassword.requestFocus();
            return;
        }
        if (databaseSpinner != null) {
            mDatabaseName = databases.get(databaseSpinner.getSelectedItemPosition());
        } else {
            Toast.makeText(this, getString(R.string.error_provide_database), Toast.LENGTH_LONG).show();
            edtSelfHosted.requestFocus();
        }
        loginProcess();
    }

    private void loginProcess() {
        Log.v("", "LoginProcess");
        Log.v("", "Processing Self Hosted Server Login");
        findViewById(R.id.controls).setVisibility(View.GONE);
        findViewById(R.id.login_progress).setVisibility(View.VISIBLE);
        mLoginProcessStatus.setText(getString(R.string.status_connecting_to_server));
        mUsername = edtUsername.getText().toString();
        mPassword = edtPassword.getText().toString();
        mLoginProcessStatus.setText(getString(R.string.status_logging_in));
        loginLoaderManager.initLoader(LOGIN_LOADER_ID, null, loginLoaderListener);
    }

    private void showDatabases() {
        if (databases.size() > 1) {
            findViewById(R.id.layoutBorderDB).setVisibility(View.VISIBLE);
            findViewById(R.id.layoutDatabase).setVisibility(View.VISIBLE);
            databaseSpinner = (Spinner) findViewById(R.id.spinnerDatabaseList);
            databases.add(0, getString(R.string.label_select_database));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, databases);
            databaseSpinner.setAdapter(adapter);
        } else {
            databaseSpinner = null;
            findViewById(R.id.layoutBorderDB).setVisibility(View.GONE);
            findViewById(R.id.layoutDatabase).setVisibility(View.GONE);
        }
    }

    private class checkServerTask extends AsyncTask<String, Integer, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String... url) {
            mUrl = url[0];
            ArrayList<String> databaseList = QueryUtils.fetchDatabaseList(url[0]);
            return databaseList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            super.onPostExecute(strings);
            if (strings != null && strings.size() > 0) {
                databases = strings;
                findViewById(R.id.imgValidURL).setVisibility(View.VISIBLE);
                findViewById(R.id.serverURLCheckProgress).setVisibility(View.GONE);
                showDatabases();
                mConnectedToServer = true;
            } else {
                mUrl = null;
                findViewById(R.id.imgValidURL).setVisibility(View.GONE);
                findViewById(R.id.serverURLCheckProgress).setVisibility(View.GONE);
                edtSelfHosted.setError(getString(R.string.error_invalid_odoo_url));
                edtSelfHosted.requestFocus();
                mConnectedToServer = false;
                return;
            }
        }
    }
}
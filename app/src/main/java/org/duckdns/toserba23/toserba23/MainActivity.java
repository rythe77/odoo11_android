package org.duckdns.toserba23.toserba23;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.fragment.Attendance;
import org.duckdns.toserba23.toserba23.fragment.Leave;
import org.duckdns.toserba23.toserba23.fragment.Partner;
import org.duckdns.toserba23.toserba23.fragment.Product;
import org.duckdns.toserba23.toserba23.fragment.QRScanner;
import org.duckdns.toserba23.toserba23.fragment.Sale;
import org.duckdns.toserba23.toserba23.fragment.Settings;
import org.duckdns.toserba23.toserba23.fragment.Stock;
import org.duckdns.toserba23.toserba23.loader.AccessRightLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();
    private static final int FETCH_ACCESS_RIGHT_LOADER_ID = 1;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mUsermail;
    private String mPassword;
    public AccessRight mAccess;

    private ArrayList<Object[]> mAccessRightFilterElements = new ArrayList<Object[]>(){};

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FrameLayout frameLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        frameLayout = (FrameLayout) findViewById(R.id.content_frame);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Initialize account information with data from Preferences
        mPref = getApplicationContext().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mUsermail = mPref.getString(getString(R.string.settings_account_name_key), null);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        View header=navigationView.getHeaderView(0);
        ((TextView)header.findViewById(R.id.email)).setText(mUsermail);
        ((TextView)header.findViewById(R.id.database)).setText(mDatabaseName);

        // Get app version
        String versionName = BuildConfig.VERSION_NAME;
        ((TextView)header.findViewById(R.id.appv)).setText("Toserba 23 v" + versionName);

        // Get access right data from server
        mAccessRightFilterElements.clear();
        mAccessRightFilterElements.add(new Object[] {"users", "=", mUserId});
        getAccessData();

        //add this line to display menu1 when the activity is loaded
        displaySelectedScreen(R.id.nav_product_online);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_login) {
            // Verify whether user really wants to change account information
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            //Yes button clicked
                            // Disable auto login
                            SharedPreferences.Editor prefEditor = mPref.edit();
                            prefEditor.putBoolean(getString(R.string.settings_is_login_key), false);
                            prefEditor.commit();

                            // Open the login activity
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            //No button clicked
                            break;
                    }
                }
            };
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.logout_confirm)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                    .setNegativeButton(R.string.no_string, dialogClickListener).show();
        } else if (id == R.id.nav_offline_mode) {
            Intent intent = new Intent(this, OfflineActivity.class);
            this.startActivity(intent);
            finish();
        } else {
            displaySelectedScreen(id);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_product_online:
                fragment = new Product();
                break;
            case R.id.qr_scanner:
                if (mAccess != null & mAccess.has_access_to_product) {
                    fragment = new QRScanner();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.sale:
                if (mAccess != null & mAccess.has_access_to_sale) {
                    fragment = new Sale();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.nav_partner:
                if (mAccess != null & mAccess.has_access_to_customer) {
                    fragment = new Partner();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.stock:
                if (mAccess != null & mAccess.has_access_to_stock) {
                    fragment = new Stock();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.attendance:
                if (mAccess != null & mAccess.has_access_to_attendance) {
                    fragment = new Attendance();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.leave:
                if (mAccess != null) {
                    fragment = new Leave();
                } else {
                    Toast.makeText(MainActivity.this, R.string.no_access_right_error, Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.show_preference_settings:
                fragment = new Settings();
                break;
        }

        //replacing the fragment
        if (fragment != null) {
            android.app.FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    public void getAccessData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.restartLoader(FETCH_ACCESS_RIGHT_LOADER_ID, null, loadAccessRightFromServerListener);
        }
    }

    private LoaderManager.LoaderCallbacks<List<String>> loadAccessRightFromServerListener = new LoaderManager.LoaderCallbacks<List<String>>() {
        @Override
        public Loader<List<String>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mAccessRightFilterElements
            };
            return new AccessRightLoader(MainActivity.this, mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> resGroups) {
            mAccess = new AccessRight((ArrayList)resGroups);
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {
            // Loader reset, so we can clear out our existing data.
        }
    };
}

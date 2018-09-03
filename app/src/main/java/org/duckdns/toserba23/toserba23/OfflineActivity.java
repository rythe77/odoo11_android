package org.duckdns.toserba23.toserba23;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.duckdns.toserba23.toserba23.fragment.Product;
import org.duckdns.toserba23.toserba23.fragment.ProductOffline;

/**
 * Created by ryanto on 24/02/18.
 */

public class OfflineActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    /** Tag for log messages */
    private static final String LOG_TAG = Context.class.getName();

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mDatabaseName;
    private String mUsermail;
    private String mPassword;

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    FrameLayout frameLayout;
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getText(R.string.product_template_title_offline));

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
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUsermail = mPref.getString(getString(R.string.settings_account_name_key), null);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        View header=navigationView.getHeaderView(0);
        ((TextView)header.findViewById(R.id.email)).setText(mUsermail);

        //add this line to display menu1 when the activity is loaded
        displaySelectedScreen(R.id.nav_product_offline);
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
        } else if (id == R.id.nav_online_mode) {
            Intent intent = new Intent(this, MainActivity.class);
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

    private void displaySelectedScreen(int itemId) {

        //creating fragment object
        Fragment fragment = null;

        //initializing the fragment object which is selected
        switch (itemId) {
            case R.id.nav_product_offline:
                fragment = new ProductOffline();
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
}
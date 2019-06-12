package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.ResPartnerDetailLoader;
import org.duckdns.toserba23.toserba23.model.AccessRight;
import org.duckdns.toserba23.toserba23.model.ResPartner;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class PartnerDetail extends AppCompatActivity {

    private static final int FETCH_RES_PARTNER_DETAIL_LOADER_ID = 1;

    LinearLayout mEmailContainer;
    LinearLayout mPhoneContainer;
    LinearLayout mMobileContainer;

    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;
    private int PRIVATE_MODE = 0;

    private ResPartner mResPartner;

    Toolbar mToolbar;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;
    private int mPartnerId;
    private AccessRight mAccess;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize account information with data from Preferences and bundle
        mPref = this.getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mPrefEditor = mPref.edit();
        mPartnerId = getIntent().getIntExtra("partner_id", 0);
        mAccess = getIntent().getParcelableExtra(QueryUtilsAccessRight.ACCESS_RIGHT);
        setTitle(getString(R.string.detail_res_partner_title));

        setContentView(R.layout.res_partner_detail_app_bar);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        // Set default partner FAB
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Verify whether user really wants to set default partner
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                mPrefEditor.putInt(getString(R.string.settings_def_partner_id_key), mResPartner.getId());
                                mPrefEditor.putString(getString(R.string.settings_def_partner_name_key), mResPartner.getName());
                                mPrefEditor.commit();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(PartnerDetail.this);
                builder.setMessage(getString(R.string.detail_res_partner_set_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                        .setNegativeButton(R.string.no_string, dialogClickListener).show();
            }
        });

        mEmailContainer = (LinearLayout) findViewById(R.id.detail_email_container);
        mEmailContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Verify whether user really wants to send email
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                if (android.util.Patterns.EMAIL_ADDRESS.matcher(mResPartner.getEmail()).matches()) {
                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + mResPartner.getEmail()));
                                    startActivity(emailIntent);
                                } else {
                                    Toast.makeText(PartnerDetail.this, R.string.detail_res_partner_email_not_valid, Toast.LENGTH_LONG).show();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(PartnerDetail.this);
                builder.setMessage(getString(R.string.detail_res_partner_send_email_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                        .setNegativeButton(R.string.no_string, dialogClickListener).show();
                return false;
            }
        });
        mPhoneContainer = (LinearLayout) findViewById(R.id.detail_phone_container);
        mPhoneContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Verify whether user really wants to call partner
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                String phone_number = mResPartner.getPhone().trim();
                                if (PhoneNumberUtils.isGlobalPhoneNumber(phone_number)) {
                                    Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_number));
                                    startActivity(phoneIntent);
                                } else {
                                    Toast.makeText(PartnerDetail.this, R.string.detail_res_partner_phone_not_valid, Toast.LENGTH_LONG).show();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(PartnerDetail.this);
                builder.setMessage(getString(R.string.detail_res_partner_call_dialog)).setPositiveButton(getString(R.string.yes_string), dialogClickListener)
                        .setNegativeButton(R.string.no_string, dialogClickListener).show();
                return false;
            }
        });
        mMobileContainer = (LinearLayout) findViewById(R.id.detail_mobile_container);
        mMobileContainer.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PartnerDetail.this);
                String[] action_array = new String[] {getString(R.string.detail_res_partner_call),getString(R.string.detail_res_partner_SMS),getString(R.string.detail_res_partner_WA),getString(R.string.cancel_string)};
                builder.setTitle(getString(R.string.detail_res_partner_choose_action_dialog))
                        .setItems(action_array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String phone_number = mResPartner.getMobile().trim();
                                if (PhoneNumberUtils.isGlobalPhoneNumber(phone_number)) {
                                    switch (i) {
                                        case 0:
                                            Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone_number));
                                            startActivity(callIntent);
                                            break;
                                        case 1:
                                            Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone_number));
                                            startActivity(smsIntent);
                                            break;
                                        case 2:
                                            Intent WAIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone_number));
                                            WAIntent.setPackage("com.whatsapp");
                                            try {
                                                startActivity(WAIntent);
                                            } catch (android.content.ActivityNotFoundException ex) {
                                                Toast.makeText(PartnerDetail.this, getString(R.string.detail_res_partner_whatsapp_not_installed), Toast.LENGTH_SHORT).show();
                                            }
                                            break;
                                        case 3:
                                            break;
                                    }
                                } else {
                                    Toast.makeText(PartnerDetail.this, R.string.detail_res_partner_phone_not_valid, Toast.LENGTH_LONG).show();
                                }
                            }
                        }).show();
                return false;
            }
        });

        readData();
    }

    public void readData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.restartLoader(FETCH_RES_PARTNER_DETAIL_LOADER_ID, null, loadResPartnerDetailFromServerListener);
        } else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Update empty state with no connection error message
            TextView noConnectionView = (TextView) findViewById(R.id.empty_view);
            noConnectionView.setText(getString(R.string.error_no_internet_connection));
            noConnectionView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Setup Loader behavior here
     * @param i id of the called loader
     * @param bundle
     * @return
     */
    private LoaderManager.LoaderCallbacks<ResPartner> loadResPartnerDetailFromServerListener = new LoaderManager.LoaderCallbacks<ResPartner>() {
        @Override
        public Loader<ResPartner> onCreateLoader(int i, Bundle bundle) {
            // Show loading indicator
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.VISIBLE);

            // Start appropriate loader to "read" or "save" data to/from server.
            // Default to "read" data from server
            return new ResPartnerDetailLoader(PartnerDetail.this, mUrl, mDatabaseName, mUserId, mPassword, mPartnerId);
        }
        @Override
        public void onLoadFinished(Loader<ResPartner> loader, ResPartner resPartner) {
            // Hide loading indicator because the data has been loaded
            View loadingIndicator = findViewById(R.id.loading_spinner);
            loadingIndicator.setVisibility(View.GONE);

            // Keep data as this class attributes and update view
            mResPartner = resPartner;
            displayUpdate(resPartner);
            getLoaderManager().destroyLoader(FETCH_RES_PARTNER_DETAIL_LOADER_ID);
        }
        @Override
        public void onLoaderReset(Loader<ResPartner> loader) {
        }
    };

    private void displayUpdate(ResPartner resPartner) {
        if (resPartner != null) {
            // Display detailed view header document
            ((TextView) findViewById(R.id.detail_ref)).setText(DisplayFormatter.formatString(resPartner.getRef()));
            ((TextView) findViewById(R.id.detail_name)).setText(DisplayFormatter.formatString(resPartner.getName()));
            ((TextView) findViewById(R.id.detail_street)).setText(DisplayFormatter.formatString(resPartner.getStreet()));
            ((TextView) findViewById(R.id.detail_street2)).setText(DisplayFormatter.formatString(resPartner.getStreet2()));
            ((TextView) findViewById(R.id.detail_city_state)).setText(DisplayFormatter.formatString(resPartner.getCity()).concat(", ").concat(DisplayFormatter.formatString(resPartner.getState())));
            ((TextView) findViewById(R.id.detail_phone)).setText(DisplayFormatter.formatString(resPartner.getPhone()));
            ((TextView) findViewById(R.id.detail_email)).setText(DisplayFormatter.formatString(resPartner.getEmail()));
            ((TextView) findViewById(R.id.detail_mobile)).setText(DisplayFormatter.formatString(resPartner.getMobile()));
            ((TextView) findViewById(R.id.detail_pricelist)).setText(DisplayFormatter.formatString(resPartner.getDefPricelist()));
            ((TextView) findViewById(R.id.detail_trust)).setText(DisplayFormatter.formatString(resPartner.getTrustName()));
            ((TextView) findViewById(R.id.detail_payment_term)).setText(DisplayFormatter.formatString(resPartner.getPaymentTerm()));
            ((TextView) findViewById(R.id.detail_comment)).setText(DisplayFormatter.formatString(resPartner.getComment()));
            ((TextView) findViewById(R.id.detail_credit)).setText(DisplayFormatter.formatCurrency(resPartner.getCredit()));
            ((TextView) findViewById(R.id.detail_credit_limit)).setText(DisplayFormatter.formatCurrency(resPartner.getCreditLimit()));
        } else {
            Toast.makeText(this, R.string.error_cannot_connect_to_server, Toast.LENGTH_LONG).show();
        }
    }
}

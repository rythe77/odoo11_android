package org.duckdns.toserba23.toserba23.fragment;

import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.GenericModelLoader;
import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.utils.DisplayFormatter;
import org.duckdns.toserba23.toserba23.utils.QueryUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 05/09/18.
 */

public class Settings extends Fragment {

    private static final int FETCH_RES_PARTNER_LOADER_ID = 1;

    private LinearLayout mDefaultPartnerContainer;
    private TextView mDefaultPartnerNameView;

    private SharedPreferences mPref;
    private SharedPreferences.Editor mPrefEditor;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;

    // default partner attributes
    private int mDefPartnerId;
    private String mDefPartnerName;

    private ArrayList<Object[]> mResPartnerFilterElements = new ArrayList<Object[]>(){};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.settings_page, container, false);

        // Initialize account information with data from Preferences
        mPref = this.getActivity().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);
        mPrefEditor = mPref.edit();

        mDefaultPartnerContainer = (LinearLayout) rootView.findViewById(R.id.settings_partner_container);
        mDefaultPartnerNameView = (TextView) rootView.findViewById(R.id.settings_partner);

        mDefaultPartnerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.settings_partner_dialog);
                final EditText searchTextView = (EditText) dialog.findViewById(R.id.input_search_text);
                final Button clearButtonView = (Button)dialog.findViewById(R.id.settings_clear_def_partner);

                // add search function on partner list
                searchTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        mResPartnerFilterElements.clear();
                        mResPartnerFilterElements.add(new Object[] {"customer", "=", true});
                        mResPartnerFilterElements.add(new Object[] {"parent_id", "=", false});
                        mResPartnerFilterElements.add(new Object[] {"name", "ilike", textView.getText().toString()});
                        getResPartnerData();
                        InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                        dialog.dismiss();
                        return true;
                    }
                });

                // give options to clear pref data
                clearButtonView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPrefEditor.remove(getString(R.string.settings_def_partner_id_key));
                        mPrefEditor.remove(getString(R.string.settings_def_partner_name_key));
                        mPrefEditor.commit();
                        mDefaultPartnerNameView.setText(null);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.settings_title));
    }

    @Override
    public void onResume(){
        // Initialize default patrner model
        mDefPartnerId = mPref.getInt(getString(R.string.settings_def_partner_id_key), 0);
        mDefPartnerName = mPref.getString(getString(R.string.settings_def_partner_name_key), null);

        // initialize default partner name view
        if (mDefPartnerName != null) {
            mDefaultPartnerNameView.setText(DisplayFormatter.formatString(mDefPartnerName));
        } else {
            mDefaultPartnerNameView.setText(null);
        }
        super.onResume();
    }

    public void getResPartnerData() {
        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMgr = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        // Get details on the currently active default data network
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo != null && networkInfo.isConnected()) {
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager = getLoaderManager();

            // number the loaderManager with mPage as may be requesting up to three lots of JSON for each tab
            loaderManager.restartLoader(FETCH_RES_PARTNER_LOADER_ID, null, loadResPartnerFromServerListener);
        } else {
            // Otherwise, display error
            Toast.makeText(getActivity(), R.string.error_no_internet_connection, Toast.LENGTH_LONG).show();
        }
    }

    private LoaderManager.LoaderCallbacks<List<GenericModel>> loadResPartnerFromServerListener = new LoaderManager.LoaderCallbacks<List<GenericModel>>() {
        @Override
        public Loader<List<GenericModel>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mResPartnerFilterElements
            };
            // Get fields
            HashMap resPartnerMap = new HashMap();
             resPartnerMap.put("fields", Arrays.asList(
             "id",
             "name"
             ));
            return new GenericModelLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, QueryUtils.RES_PARTNER, filterArray, resPartnerMap);
        }

        @Override
        public void onLoadFinished(Loader<List<GenericModel>> loader, final List<GenericModel> resPartners) {
            // show partner list to choose from
            final Dialog dialog = new Dialog(getActivity());
            dialog.setContentView(R.layout.simple_list_view);
            dialog.setTitle(getString(R.string.dialog_list_title));
            ListView listView = (ListView) dialog.findViewById(R.id.list);
            final ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < resPartners.size(); ++i) {
                list.add(resPartners.get(i).getName());
            }
            ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, list);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mPrefEditor.putInt(getString(R.string.settings_def_partner_id_key), resPartners.get(i).getId());
                    mPrefEditor.putString(getString(R.string.settings_def_partner_name_key), resPartners.get(i).getName());
                    mPrefEditor.commit();
                    mDefaultPartnerNameView.setText(DisplayFormatter.formatString(resPartners.get(i).getName()));
                    dialog.dismiss();
                }
            });
            dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                    }
                    return true;
                }
            });
            dialog.show();

        }

        @Override
        public void onLoaderReset(Loader<List<GenericModel>> loader) {
        }
    };
}

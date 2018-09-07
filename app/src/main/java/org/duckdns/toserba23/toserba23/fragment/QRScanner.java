package org.duckdns.toserba23.toserba23.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.Result;

import org.duckdns.toserba23.toserba23.MainActivity;
import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.loader.ProductTemplateLoader;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;
import org.duckdns.toserba23.toserba23.utils.QueryUtilsAccessRight;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by ryanto on 02/09/18.
 */

public class QRScanner extends Fragment implements ZXingScannerView.ResultHandler{

    private static final int FETCH_PRODUCT_TEMPLATE_LOADER_ID = 1;

    private SharedPreferences mPref;
    private int PRIVATE_MODE = 0;

    // Account information for xmlrpc
    private String mUrl;
    private String mDatabaseName;
    private int mUserId;
    private String mPassword;

    int mProductTmplId = 0;
    private List<ProductTemplate> mProductTemplates;
    private ArrayList<Object[]> mProductTemplateFilterElements = new ArrayList<Object[]>(){};

    private ZXingScannerView mScannerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Initialize account information with data from Preferences
        mPref = this.getActivity().getSharedPreferences(getString(R.string.settings_shared_preferences_label), PRIVATE_MODE);
        mUrl = mPref.getString(getString(R.string.settings_url_key), null);
        mDatabaseName = mPref.getString(getString(R.string.settings_database_name__key), null);
        mUserId = mPref.getInt(getString(R.string.settings_user_id_key), 0);
        mPassword = mPref.getString(getString(R.string.settings_password_key), null);

        mScannerView = new ZXingScannerView(getActivity());
        return mScannerView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //you can set the title for your toolbar here for different fragments different titles
        getActivity().setTitle(getString(R.string.qr_scan_title));
    }

    @Override
    public void handleResult(Result rawResult) {
        mProductTemplateFilterElements.clear();
        mProductTemplateFilterElements.add(new Object[] {"default_code", "ilike", rawResult.getText().toString()});
        getLoaderManager().restartLoader(FETCH_PRODUCT_TEMPLATE_LOADER_ID, null, loadProductTemplateFromServerListener);
    }

    public void showMessageDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.scan_result_title));
        builder.setMessage(message);
        AlertDialog alert1 = builder.create();
        alert1.show();
    }

    public void restartCamera() {
        // Register ourselves as a handler for scan results.
        mScannerView.setResultHandler(this);
        // Start camera on resume
        mScannerView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        restartCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop camera on pause
        mScannerView.stopCamera();
    }

    private LoaderManager.LoaderCallbacks<List<ProductTemplate>> loadProductTemplateFromServerListener = new LoaderManager.LoaderCallbacks<List<ProductTemplate>>() {
        @Override
        public Loader<List<ProductTemplate>> onCreateLoader(int i, Bundle bundle) {
            Object[] filterArray = new Object[] {
                    mProductTemplateFilterElements
            };
            return new ProductTemplateLoader(getActivity(), mUrl, mDatabaseName, mUserId, mPassword, filterArray);
        }

        @Override
        public void onLoadFinished(Loader<List<ProductTemplate>> loader, List<ProductTemplate> productTemplates) {
            mProductTemplates = productTemplates;
            if (mProductTemplates != null && !mProductTemplates.isEmpty() && mProductTemplates.size() == 1) {
                mProductTmplId = mProductTemplates.get(0).getId();
                getLoaderManager().destroyLoader(FETCH_PRODUCT_TEMPLATE_LOADER_ID);
                startProductDetailActivity();
            } else if (mProductTemplates != null && !mProductTemplates.isEmpty() && mProductTemplates.size() != 1) {
                // Multiple products found for the current qr code, create dialog windows to let user choose which to see
                Toast.makeText(getActivity(), R.string.qr_multiple_scan_results, Toast.LENGTH_LONG).show();
                final Dialog dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.simple_list_view);
                dialog.setTitle(getString(R.string.dialog_list_title));
                ListView listView = (ListView) dialog.findViewById(R.id.list);
                final ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < mProductTemplates.size(); ++i) {
                    list.add(mProductTemplates.get(i).getName());
                }
                ArrayAdapter adapter = new ArrayAdapter(getActivity(),android.R.layout.simple_list_item_1, list);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        mProductTmplId = mProductTemplates.get(i).getId();
                        getLoaderManager().destroyLoader(FETCH_PRODUCT_TEMPLATE_LOADER_ID);
                        dialog.dismiss();
                        startProductDetailActivity();
                    }
                });
                dialog.setOnKeyListener(new Dialog.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            dialog.dismiss();
                            restartCamera();
                        }
                        return true;
                    }
                });
                dialog.show();
            } else {
                showMessageDialog(getString(R.string.error_product_not_found));
                restartCamera();
            }
        }

        @Override
        public void onLoaderReset(Loader<List<ProductTemplate>> loader) {
        }
    };

    public void startProductDetailActivity() {
        Intent intent = new Intent(getActivity(), ProductDetail.class);
        intent.putExtra("product_tmpl_id", mProductTmplId);
        intent.putExtra(QueryUtilsAccessRight.ACCESS_RIGHT, ((MainActivity)getActivity()).mAccess);
        getActivity().startActivity(intent);
    }
}

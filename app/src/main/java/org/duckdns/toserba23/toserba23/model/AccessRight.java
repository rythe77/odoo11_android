package org.duckdns.toserba23.toserba23.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by ryanto on 04/09/18.
 */

public class AccessRight implements Parcelable {
    // Group names
    private static final String GROUP_SALE_SALESMAN = "Penjualan / User: Own Documents Only";
    private static final String GROUP_SALE_SALESMAN_ALL_LEADS = "Penjualan / User: All Documents";
    private static final String GROUP_SALE_MANAGER = "Penjualan / Manajer";
    private static final String GROUP_STOCK_USER = "Persediaan / Pengguna";
    private static final String GROUP_STOCK_MANAGER = "Persediaan / Manajer";
    private static final String ALLOW_BADGE_SCAN = "Pembukaan Akses / Akses Pindai Lencana";

    private ArrayList<String> mGroupList;

    // Boolean value for access right group
    private Boolean group_sale_salesman = false;
    private Boolean group_sale_salesman_all_leads = false;
    private Boolean group_sale_manager = false;
    private Boolean group_stock_user = false;
    private Boolean group_stock_manager = false;
    private Boolean allow_badge_scan = false;

    // Boolean value for access right check
    public Boolean has_access_to_product = false;
    public Boolean has_access_to_sale = false;
    public Boolean has_access_to_sale_confirm = false;
    public Boolean has_access_to_customer = false;
    public Boolean has_access_to_stock = false;
    public Boolean has_access_to_stock_validate = false;
    public Boolean has_access_to_badge_scan = false;

    /**
     * Main constructor of this class
     */
    public AccessRight(ArrayList<String> groupList) {
        mGroupList = groupList;
        initialize();
    }

    private void initialize() {
        if (mGroupList.contains(GROUP_SALE_MANAGER) ) {
            group_sale_manager = true;
            group_sale_salesman_all_leads = true;
            group_sale_salesman = true;
        } else if ( mGroupList.contains(GROUP_SALE_SALESMAN_ALL_LEADS) ) {
            group_sale_salesman_all_leads = true;
            group_sale_salesman = true;
        } else if ( mGroupList.contains(GROUP_SALE_SALESMAN) ) {
            group_sale_salesman = true;
        }
        if (mGroupList.contains(GROUP_STOCK_MANAGER) ) {
            group_stock_manager = true;
            group_stock_user = true;
        } else if ( mGroupList.contains(GROUP_STOCK_USER) ) {
            group_stock_user = true;
        }
        if (mGroupList.contains(ALLOW_BADGE_SCAN) ) {
            allow_badge_scan = true;
        }
        open_access_right();
    }

    private void open_access_right() {
        if ( group_sale_salesman || group_stock_user ) {
            has_access_to_product = true;
        }
        if ( group_sale_salesman ) {
            has_access_to_sale = true;
            has_access_to_customer = true;
        }
        if ( group_sale_manager ) {
            has_access_to_sale_confirm = true;
        }
        if ( group_stock_user ) {
            has_access_to_stock = true;
        }
        if ( group_stock_manager ) {
            has_access_to_stock_validate = true;
        }
        if ( allow_badge_scan ) {
            has_access_to_badge_scan = true;
        }
    }

    /**
     * Below is series of implementation of parcelable
     * @return
     */
    @Override
    public int describeContents() {
        return hashCode();
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(has_access_to_product);
        dest.writeValue(has_access_to_sale);
        dest.writeValue(has_access_to_sale_confirm);
        dest.writeValue(has_access_to_customer);
        dest.writeValue(has_access_to_stock);
        dest.writeValue(has_access_to_stock_validate);
        dest.writeValue(has_access_to_badge_scan);
    }

    public AccessRight(Parcel P) {
        has_access_to_product = (Boolean) P.readValue( null );
        has_access_to_sale = (Boolean) P.readValue( null );
        has_access_to_sale_confirm = (Boolean) P.readValue( null );
        has_access_to_customer = (Boolean) P.readValue( null );
        has_access_to_stock = (Boolean) P.readValue(null);
        has_access_to_stock_validate = (Boolean) P.readValue(null);
        has_access_to_badge_scan = (Boolean) P.readValue(null);
    }

    public static final Parcelable.Creator<AccessRight> CREATOR = new Parcelable.Creator<AccessRight>() {
        @Override
        public AccessRight createFromParcel(Parcel parcel){
            return new AccessRight(parcel);
        }
        @Override
        public AccessRight[] newArray(int size) {
            return new AccessRight[size];
        }
    };
}

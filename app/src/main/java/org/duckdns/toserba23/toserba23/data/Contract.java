package org.duckdns.toserba23.toserba23.data;

import android.provider.BaseColumns;

/**
 * Created by ryanto on 25/02/18.
 */

public final class Contract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private Contract() {}

    /* Inner class that defines the table contents */
    public static class ProductEntry implements BaseColumns {
        public final static String TABLE_NAME = "product_template";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_CODE = "product_code";
        public final static String COLUMN_NAME = "product_name";
        public final static String COLUMN_QTY_AVAILABLE = "qty_available";
        public final static String COLUMN_VIRTUAL_AVAILABLE = "virtual_available";
        public final static String COLUMN_CATEGORY_ID = "product_category_id";
        public final static String COLUMN_CATEGORY = "product_category";
        public final static String COLUMN_UNIT_ID = "product_uom_id";
        public final static String COLUMN_UNIT = "product_uom";
    }

    /* Inner class that defines the table contents */
    public static class PricelistEntry implements BaseColumns {
        public final static String TABLE_NAME = "product_pricelist_item";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_ID = "product_tmpl_id";
        public final static String COLUMN_PRODUCT_NAME = "product_name";
        public final static String COLUMN_PRICELIST_NAME = "pricelist_name";
        public final static String COLUMN_FIXED_PRICE = "fixed_price";
        public final static String COLUMN_MIN_QUANTITY = "min_quantity";
        public final static String COLUMN_X_NOTES = "x_notes";
        public final static String COLUMN_DATE_START = "date_start";
        public final static String COLUMN_DATE_END = "date_end";
    }
}

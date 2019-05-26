package org.duckdns.toserba23.toserba23.model;

import android.util.Log;

import org.duckdns.toserba23.toserba23.utils.QueryUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class ProductPricelistItem {
    private int mId;
    private GenericModel mProduct;
    private String mPricelistName;
    private int mFixedPrice;
    private double mMinQuantity;
    private String mXNotes;
    private String mDateStart;
    private String mDateEnd;

    /**
     * Series of get method
     */
    public int getId() { return mId; }
    public void setProduct(GenericModel product) { mProduct = product; }
    public GenericModel getProduct() {
        return mProduct;
    }
    public int getProductId() {return mProduct!=null?mProduct.getId():0;}
    public String getPricelistName() { return mPricelistName; }
    public int getFixedPrice() { return mFixedPrice; }
    public double getMinQuantity() { return mMinQuantity; }
    public String getXNotes() { return mXNotes; }
    public String getDateStart() { return mDateStart; }
    public String getDateEnd() { return mDateEnd; }

    /**
     * Main constructor of this class
     */
    public ProductPricelistItem(int id, GenericModel product, String pricelistName, int fixedPrice, double minQuantity, String xNotes, String dateStart, String dateEnd) {
        mId = id;
        mProduct = product;
        mPricelistName = pricelistName;
        mFixedPrice = fixedPrice;
        mMinQuantity = minQuantity;
        mXNotes = xNotes;
        mDateStart = dateStart;
        mDateEnd = dateEnd;
    }

    /**
     * hashmap of fields
     * should be used to limit returned fields when querying Odoo server
     */
    public static HashMap<String,Arrays> getProductPricelistItemFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "product_tmpl_id",
                "pricelist_id",
                "x_formula_price",
                "min_quantity",
                "x_notes",
                "date_start",
                "date_end"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of object
     * @param jsonResponse jsonResponse from Odoo server
     * @return arraylist of product pricelist item
     */
    public static ArrayList<ProductPricelistItem> parseJson(String jsonResponse) {
        ArrayList<ProductPricelistItem> productPricelistItems = new ArrayList<>();

        List<String> fieldProductPricelistItem = (List<String>) getProductPricelistItemFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldProductPricelistItem.get(0), 0);
                    JSONArray product = field.optJSONArray(fieldProductPricelistItem.get(1));
                    int productId = 0;
                    String productName = null;
                    if (product!=null) {
                        productId = product.optInt(0);
                        productName = product.optString(1);
                    }
                    GenericModel productModel = new GenericModel(productId,productName);
                    JSONArray pricelist = field.optJSONArray(fieldProductPricelistItem.get(2));
                    String pricelistName = null;
                    if (pricelist!=null) {pricelistName = pricelist.getString(1);}
                    int fixedPrice = field.optInt(fieldProductPricelistItem.get(3), 0);
                    double minQuantity = field.getDouble(fieldProductPricelistItem.get(4));
                    String xNotes = field.optString(fieldProductPricelistItem.get(5), "");
                    String startDate = field.optString(fieldProductPricelistItem.get(6));
                    String stopDate = field.optString(fieldProductPricelistItem.get(7));
                    productPricelistItems.add( new ProductPricelistItem(id, productModel, pricelistName, fixedPrice, minQuantity, xNotes, startDate, stopDate));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ProductPricelistItem", "Problem parsing the JSON results", e);
            }
        }
        return productPricelistItems;
    }
}

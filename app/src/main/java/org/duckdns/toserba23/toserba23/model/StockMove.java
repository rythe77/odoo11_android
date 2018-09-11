package org.duckdns.toserba23.toserba23.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class StockMove {
    private int mId;
    private GenericModel mProduct;
    private String mProductDesc;
    private double mOrderedQty;
    private double mReservedQty;
    private double mDoneQty;
    private String mUom;

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public void setProduct(GenericModel product) { mProduct = product; }
    public GenericModel getProduct() {
        return mProduct;
    }
    public int getProductId() {return mProduct!=null?mProduct.getId():0;}
    public String getProductDesc() { return mProductDesc; }
    public double getOrderedQty() { return mOrderedQty; }
    public double getReservedQtyQty() { return mReservedQty; }
    public double getDoneQty() { return mDoneQty; }
    public String getUom() { return mUom; }

    /**
     * Main constructor of this class
     */
    public StockMove(int id, GenericModel product, String productDesc, double orderedQty, double reservedQty, double doneQty, String uom) {
        mId = id;
        mProduct = product;
        mProductDesc = productDesc;
        mOrderedQty = orderedQty;
        mReservedQty = reservedQty;
        mDoneQty = doneQty;
        mUom = uom;
    }

    /**
     * hashmap of fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getStockMoveFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "product_id",
                "product_desc",
                "product_uom_qty",
                "reserved_availability",
                "quantity_done",
                "product_uom"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<StockMove> parseJson(String jsonResponse) {
        ArrayList<StockMove> stockMoves = new ArrayList<>();

        List<String> fieldStockMoveLine = (List<String>) getStockMoveFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldStockMoveLine.get(0), 0);
                    JSONArray product = field.optJSONArray(fieldStockMoveLine.get(1));
                    int productId = 0;
                    String productName = null;
                    if (product!=null) {
                        productId = product.optInt(0);
                        productName = product.optString(1);
                    }
                    GenericModel productModel = new GenericModel(productId,productName);
                    String productDesc = field.optString(fieldStockMoveLine.get(2), "");
                    double orderedQty = field.optDouble(fieldStockMoveLine.get(3), 0);
                    double reservedQty = field.optDouble(fieldStockMoveLine.get(4), 0);
                    double doneQty = field.optDouble(fieldStockMoveLine.get(5), 0);
                    JSONArray uom = field.optJSONArray(fieldStockMoveLine.get(6));
                    String uomName = null;
                    if (uom!=null) {uomName = uom.getString(1);}
                    stockMoves.add( new StockMove(id, productModel, productDesc, orderedQty, reservedQty, doneQty, uomName));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("StockMove", "Problem parsing the JSON results", e);
            }
        }
        return stockMoves;
    }
}

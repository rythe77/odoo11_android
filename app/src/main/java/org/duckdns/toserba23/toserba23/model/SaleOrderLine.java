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

public class SaleOrderLine {
    private int mId;
    private GenericModel mProduct;
    private String mProductDesc;
    private double mOrderedQty;
    private String mUom;
    private int mUnitPrice;
    private int mDiscount;
    private int mSubtotal;
    private int mOrderId;

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
    public String getUom() { return mUom; }
    public int getUnitPrice() { return mUnitPrice; }
    public int getDiscount() { return mDiscount; }
    public int getSubtotal() { return mSubtotal; }
    public int getOrderId() { return mOrderId; }
    public void setOrderId(int orderId) { mOrderId = orderId; }

    /**
     * Main constructor of this class
     */
    public SaleOrderLine(int id, GenericModel product, String productDesc, double orderedQty, String uom, int unitPrice, int discount, int subtotal) {
        mId = id;
        mProduct = product;
        mProductDesc = productDesc;
        mOrderedQty = orderedQty;
        mUom = uom;
        mUnitPrice = unitPrice;
        mDiscount = discount;
        mSubtotal = subtotal;
    }

    /**
     * Constructor for writing data to server
     */
    public SaleOrderLine(int id, GenericModel product, String productDesc, double orderedQty, int orderId) {
        mId = id;
        mProduct = product;
        mProductDesc = productDesc;
        mOrderedQty = orderedQty;
        mOrderId = orderId;
    }

    /**
     * hashmap of fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getSaleOrderLineFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "product_id",
                "product_desc",
                "product_uom_qty",
                "product_uom",
                "price_unit",
                "discount",
                "price_subtotal"
        ));
        return map;
    }

    /**
     * hashmap of sale order line fields
     * @return
     */
    public static HashMap<String,Arrays> getSaleOrderLineFieldsToSave() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "product_id",
                "product_desc",
                "product_uom_qty",
                "order_id"
        ));
        return map;
    }

    /**
     * Pack data to be saved to server in a hashmap
     * @return
     */
    // create hashmap data pack to be sent to Odoo server
    public HashMap getHashmap() {
        HashMap fieldsArray = getSaleOrderLineFieldsToSave();
        List<String> fields = (List<String>) fieldsArray.get("fields");

        HashMap map = new HashMap<>();
        map.put(fields.get(0), getId()!=0?getId():"");
        map.put(fields.get(1), getProductId()!=0?getProductId():"");
        map.put(fields.get(2), getProductDesc()!=null?getProductDesc():"");
        map.put(fields.get(3), getOrderedQty()!=0?getOrderedQty():"");
        map.put(fields.get(4), getOrderId()!=0?getOrderId():"");
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<SaleOrderLine> parseJson(String jsonResponse) {
        ArrayList<SaleOrderLine> saleOrderLines = new ArrayList<>();

        List<String> fieldSaleOrderLine = (List<String>) getSaleOrderLineFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldSaleOrderLine.get(0), 0);
                    JSONArray product = field.optJSONArray(fieldSaleOrderLine.get(1));
                    int productId = 0;
                    String productName = null;
                    if (product!=null) {
                        productId = product.optInt(0);
                        productName = product.optString(1);
                    }
                    GenericModel productModel = new GenericModel(productId,productName);
                    String productDesc = field.optString(fieldSaleOrderLine.get(2), "");
                    double orderedQty = field.getDouble(fieldSaleOrderLine.get(3));
                    JSONArray uom = field.optJSONArray(fieldSaleOrderLine.get(4));
                    String uomName = null;
                    if (uom!=null) {uomName = uom.getString(1);}
                    int priceUnit = field.optInt(fieldSaleOrderLine.get(5), 0);
                    int discount = field.optInt(fieldSaleOrderLine.get(6), 0);
                    int subtotal = field.optInt(fieldSaleOrderLine.get(7), 0);
                    saleOrderLines.add( new SaleOrderLine(id, productModel, productDesc, orderedQty, uomName, priceUnit, discount, subtotal));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("StockPackOperation", "Problem parsing the JSON results", e);
            }
        }
        return saleOrderLines;
    }
}

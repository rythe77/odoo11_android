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
 * Created by ryanto on 22/02/18.
 */

public class ProductProduct {
    private int mId;
    private String mName;
    private double mQtyCKL;
    private double mQtyForecastCKL;
    private double mQtyPRL;
    private double mQtyForecastPRL;

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getName() { return mName; }
    public double getQtyCKL() { return mQtyCKL; }
    public double getQtyForecastCKL() { return mQtyForecastCKL; }
    public double getQtyPRL() { return mQtyPRL; }
    public double getQtyForecastPRL() { return mQtyForecastPRL; }

    /**
     * Main constructor of this class
     */
    public ProductProduct(int id, String name, double qtyCKL, double qtyForecastCKL, double qtyPRL, double qtyForecastPRL) {
        mId = id;
        mName = name;
        mQtyCKL = qtyCKL;
        mQtyForecastCKL = qtyForecastCKL;
        mQtyPRL = qtyPRL;
        mQtyForecastPRL = qtyForecastPRL;
    }

    /**
     * hashmap of the fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getProductProductFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "x_qty_available_0",
                "x_virtual_available_0",
                "x_qty_available_1",
                "x_virtual_available_1"));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<ProductProduct> parseJson(String jsonResponse) {
        ArrayList<ProductProduct> productProducts = new ArrayList<>();

        List<String> fieldProductProduct = (List<String>) getProductProductFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldProductProduct.get(0), 0);
                    String productName = field.optString(fieldProductProduct.get(1));
                    double qtyCKL = field.optDouble(fieldProductProduct.get(2), 0);
                    double qtyForecastCKL = field.optDouble(fieldProductProduct.get(3), 0);
                    double qtyPRL = field.optDouble(fieldProductProduct.get(4), 0);
                    double qtyForecastPRL = field.optDouble(fieldProductProduct.get(5), 0);
                    productProducts.add( new ProductProduct(id, productName, qtyCKL, qtyForecastCKL, qtyPRL, qtyForecastPRL));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ProductProduct", "Problem parsing the JSON results", e);
            }
        }
        return productProducts;
    }
}

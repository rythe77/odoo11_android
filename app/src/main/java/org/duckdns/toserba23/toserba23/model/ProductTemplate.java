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

public class ProductTemplate {
    private int mId;
    private String mRef;
    private String mName;
    private double mQty;
    private double mQtyForecast;
    private GenericModel mCategory;
    private GenericModel mUnit;
    private ArrayList<ProductPricelistItem> mProductPricelistItem;
    private ProductProduct mProductProduct;
    private String mImage;

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getRef() { return mRef; }
    public String getName() { return mName; }
    public double getQty() { return mQty; }
    public double getQtyForecast() { return mQtyForecast; }
    public void setProductCategory(GenericModel category) { mCategory = category; }
    public GenericModel getProductCategory() {
        return mCategory;
    }
    public int getProductCategoryId() {return mCategory!=null?mCategory.getId():0;}
    public void setProductUom(GenericModel uom) { mUnit = uom; }
    public GenericModel getProductUom() {
        return mUnit;
    }
    public int getProductUomId() {return mUnit!=null?mUnit.getId():0;}
    public void setImage(String image) { mImage = image; }
    public String getImage() { return mImage; }

    /**
     * Series of set and get method for linked fields
     * @return
     */
    public void setProductPricelistItem(ArrayList<ProductPricelistItem> productPricelistItems) {mProductPricelistItem = productPricelistItems;}
    public ArrayList<ProductPricelistItem> getProductPricelistItem() { return mProductPricelistItem; }
    public void setProductProduct(ProductProduct productProduct) {mProductProduct = productProduct;}
    public ProductProduct getProductProduct() { return mProductProduct; }

    /**
     * Main constructor of this class
     */
    public ProductTemplate(int id, String ref, String name, double qty, double qtyForecast) {
        mId = id;
        mRef = ref;
        mName = name;
        mQty = qty;
        mQtyForecast = qtyForecast;
    }

    /**
     * Sub constructor of this class
     */
    public ProductTemplate(int id, String ref, String name, double qty, double qtyForecast, GenericModel category, GenericModel unit) {
        mId = id;
        mRef = ref;
        mName = name;
        mQty = qty;
        mQtyForecast = qtyForecast;
        mCategory = category;
        mUnit = unit;
    }

    /**
     * hashmap of the fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getProductTemplateBulkFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "default_code",
                "name",
                "qty_available",
                "virtual_available"));
        return map;
    }

    /**
     * hashmap of detail fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getProductTemplateDetailFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "default_code",
                "name",
                "qty_available",
                "virtual_available",
                "categ_id",
                "uom_id",
                "image_medium"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<ProductTemplate> parseJson(String jsonResponse) {
        ArrayList<ProductTemplate> productTemplates = new ArrayList<>();

        List<String> fieldProductTemplate = (List<String>) getProductTemplateDetailFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldProductTemplate.get(0), 0);
                    String productRef = field.optString(fieldProductTemplate.get(1));
                    String productName = field.optString(fieldProductTemplate.get(2));
                    double qty = field.optDouble(fieldProductTemplate.get(3), 0);
                    double qtyForecast = field.optDouble(fieldProductTemplate.get(4), 0);
                    productTemplates.add( new ProductTemplate(id, productRef, productName, qty, qtyForecast));

                    // Additional fields for product template detail
                    if (field.has(fieldProductTemplate.get(5))) {
                        JSONArray category = field.optJSONArray(fieldProductTemplate.get(5));
                        int categoryId = 0;
                        String categoryName = null;
                        if (category!=null) {
                            categoryId = category.optInt(0);
                            categoryName = category.optString(1);
                        }
                        productTemplates.get(j).setProductCategory(new GenericModel(categoryId, categoryName));
                    }
                    if (field.has(fieldProductTemplate.get(6))) {
                        JSONArray unit = field.optJSONArray(fieldProductTemplate.get(6));
                        int unitId = 0;
                        String unitName = null;
                        if (unit!=null) {
                            unitId = unit.optInt(0);
                            unitName = unit.optString(1);
                        }
                        productTemplates.get(j).setProductUom(new GenericModel(unitId, unitName));
                    }
                    if (field.has(fieldProductTemplate.get(7))) {
                        String image = field.optString(fieldProductTemplate.get(7));
                        productTemplates.get(j).setImage(image);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ProductTemplate", "Problem parsing the JSON results", e);
            }
        }
        return productTemplates;
    }
}

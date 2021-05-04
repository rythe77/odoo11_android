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
    private int mHargaJual;
    private int mHargaGrosir;
    private int mHargaToko;
    private int mHargaBulukumba;
    private int mHargaBulukumbas;
    private int mHargaPromo;
    private int mPromoCash;
    private String mDescription;

    /**
     * Series of get method
     */
    public int getId() { return mId; }
    public String getRef() { return mRef; }
    public String getName() { return mName; }
    public String getPathName() { return mName.replace("\"", ""); }
    public double getQty() { return mQty; }
    public double getQtyForecast() { return mQtyForecast; }
    private void setProductCategory(GenericModel category) { mCategory = category; }
    public GenericModel getProductCategory() {
        return mCategory;
    }
    public int getProductCategoryId() {return mCategory!=null?mCategory.getId():0;}
    private void setProductUom(GenericModel uom) { mUnit = uom; }
    public GenericModel getProductUom() {
        return mUnit;
    }
    public int getProductUomId() {return mUnit!=null?mUnit.getId():0;}
    public void setImage(String image) { mImage = image; }
    public String getImage() { return mImage; }
    private void setHargaJual(int hargaJual) { mHargaJual = hargaJual; }
    public int getHargaJual() { return mHargaJual; }
    private void setHargaGrosir(int hargaGrosir) { mHargaGrosir = hargaGrosir; }
    public int getHargaGrosir() { return mHargaGrosir; }
    private void setHargaToko(int hargaToko) { mHargaToko = hargaToko; }
    public int getHargaToko() { return mHargaToko; }
    private void setHargaBulukumba(int hargaBulukumba) { mHargaBulukumba = hargaBulukumba; }
    public int getHargaBulukumba() { return mHargaBulukumba; }
    private void setHargaBulukumbas(int hargaBulukumbas) { mHargaBulukumbas = hargaBulukumbas; }
    public int getHargaBulukumbas() { return mHargaBulukumbas; }
    private void setHargaPromo(int hargaPromo) { mHargaPromo = hargaPromo; }
    public int getHargaPromo() { return mHargaPromo; }
    private void setPromoCash(int promoCash) { mPromoCash = promoCash; }
    public int getPromoCash() { return mPromoCash; }
    private void setDescription(String description) { mDescription = description; }
    public String getDescription() { return mDescription; }

    /**
     * Series of set and get method for linked fields
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
                "image_medium",
                "x_harga_jual",
                "x_harga_grosir",
                "x_harga_toko",
                "x_harga_bulukumba",
                "x_harga_bulukumbas",
                "x_harga_promo",
                "x_promo_cash",
                "description"
        ));
        return map;
    }

    /**
     * hashmap of for highres image fields
     */
    public static HashMap<String,Arrays> getProductTemplateImageFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "image"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse jsonResponse from Odoo server
     * @return arraylist of product template
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
                    if (field.has(fieldProductTemplate.get(8))) {
                        int hargaJual = field.optInt(fieldProductTemplate.get(8));
                        productTemplates.get(j).setHargaJual(hargaJual);
                    }
                    if (field.has(fieldProductTemplate.get(9))) {
                        int hargaGrosir = field.optInt(fieldProductTemplate.get(9));
                        productTemplates.get(j).setHargaGrosir(hargaGrosir);
                    }
                    if (field.has(fieldProductTemplate.get(10))) {
                        int hargaToko = field.optInt(fieldProductTemplate.get(10));
                        productTemplates.get(j).setHargaToko(hargaToko);
                    }
                    if (field.has(fieldProductTemplate.get(11))) {
                        int hargaBulukumba = field.optInt(fieldProductTemplate.get(11));
                        productTemplates.get(j).setHargaBulukumba(hargaBulukumba);
                    }
                    if (field.has(fieldProductTemplate.get(12))) {
                        int hargaBulukumbas = field.optInt(fieldProductTemplate.get(12));
                        productTemplates.get(j).setHargaBulukumbas(hargaBulukumbas);
                    }
                    if (field.has(fieldProductTemplate.get(13))) {
                        int hargaPromo = field.optInt(fieldProductTemplate.get(13));
                        productTemplates.get(j).setHargaPromo(hargaPromo);
                    }
                    if (field.has(fieldProductTemplate.get(14))) {
                        int promoCash = field.optInt(fieldProductTemplate.get(14));
                        productTemplates.get(j).setPromoCash(promoCash);
                    }
                    if (field.has(fieldProductTemplate.get(15))) {
                        String description = field.optString(fieldProductTemplate.get(15));
                        productTemplates.get(j).setDescription(description);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ProductTemplate", "Problem parsing the JSON results", e);
            }
        }
        return productTemplates;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse jsonResponse from Odoo server
     * @return arraylist of product template
     */
    public static String parseJsonImage(String jsonResponse) {
        String image = "false";
        List<String> fieldProductTemplate = (List<String>) getProductTemplateImageFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    if (field.has(fieldProductTemplate.get(0))) {
                        image = field.optString(fieldProductTemplate.get(0));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ProductTemplate", "Problem parsing the JSON results", e);
            }
        }
        return image;
    }
}

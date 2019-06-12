package org.duckdns.toserba23.toserba23.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.ImageView;

import org.duckdns.toserba23.toserba23.R;
import org.duckdns.toserba23.toserba23.model.ProductPricelistItem;
import org.duckdns.toserba23.toserba23.model.ProductProduct;
import org.duckdns.toserba23.toserba23.model.ProductTemplate;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import de.timroes.base64.Base64;

import static org.duckdns.toserba23.toserba23.utils.QueryUtils.*;

/**
 * Created by ryanto on 23/02/18.
 */

public class QueryUtilsProductTemplate {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsProductTemplate() {
    }

    /**
     * Query the Odoo server for stock picking list.
     */
    public static ArrayList<ProductTemplate> searchReadProductTemplateList(String requestUrl, String databaseName, final int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<ProductTemplate> productTemplates = null;

        // Get fields
        HashMap fieldsMap = ProductTemplate.getProductTemplateBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        fieldsMap.put("order", "default_code asc");
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_TEMPLATE, filter, fieldsMap);
            productTemplates = ProductTemplate.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productTemplates;
    }

    public static ProductTemplate fetchProductTemplateDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Get data for a single product template details
        ArrayList<ProductTemplate> productTemplates = null;
        HashMap fieldsProductTemplateMap = ProductTemplate.getProductTemplateDetailFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_TEMPLATE, itemId, fieldsProductTemplateMap);
            productTemplates = ProductTemplate.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because the data is only a single record, take only the first element from array list
        ProductTemplate productTemplate = null;
        if(productTemplates!=null && !productTemplates.isEmpty()){
            productTemplate = productTemplates.get(0);
        }

        if (productTemplate!=null) {
            // Get pricelist records
            ArrayList<ProductPricelistItem> productPricelistItems = null;
            HashMap fieldsProductPricelistItemMap = ProductPricelistItem.getProductPricelistItemFields();
            fieldsProductPricelistItemMap.put("order", "pricelist_id asc");
            //Create filter for pricelist related to the above product template
            Object[] filter = new Object[]{
                    new Object[]{
                            new Object[]{"product_tmpl_id", "=", productTemplate.getName()},
                    }
            };
            try {
                String jsonResponses2 = searchReadRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_PRICELIST, filter, fieldsProductPricelistItemMap);
                productPricelistItems = ProductPricelistItem.parseJson(jsonResponses2);
            } catch (Exception e) {
                e.printStackTrace();
            }
            productTemplate.setProductPricelistItem(productPricelistItems);

            // Get data for a the related product product
            // After fetching product product data, set it as related to the original product template
            ArrayList<ProductProduct> productProducts = null;
            HashMap fieldsProductProductMap = ProductProduct.getProductProductFields();
            //Create filter for product product related to the above product template
            Object[] filter3 = new Object[]{
                    new Object[]{
                            new Object[]{"name", "=", productTemplate.getName()},
                    }
            };
            try {
                String jsonResponses3 = searchReadRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_PRODUCT, filter3, fieldsProductProductMap);
                productProducts = ProductProduct.parseJson(jsonResponses3);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Because the data is only a single record, take only the first element from array list
            ProductProduct productProduct = null;
            if (productProducts != null && !productProducts.isEmpty()) {
                productProduct = productProducts.get(0);
            }
            productTemplate.setProductProduct(productProduct);
        }

        return productTemplate;
    }

    public static String fetchProductTemplateImage(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        String imgStr64 = "false";
        HashMap fieldsProductTemplateImageMap = ProductTemplate.getProductTemplateImageFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, PRODUCT_TEMPLATE, itemId, fieldsProductTemplateImageMap);
            imgStr64 = ProductTemplate.parseJsonImage(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imgStr64;
    }
}

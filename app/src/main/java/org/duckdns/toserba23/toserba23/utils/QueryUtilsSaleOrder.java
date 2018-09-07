package org.duckdns.toserba23.toserba23.utils;

import android.util.Log;

import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.model.SaleOrderLine;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.duckdns.toserba23.toserba23.data.DbHelper.LOG_TAG;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.SALE_ORDER;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.SALE_ORDER_LINE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.readRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.saveRecord;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 23/02/18.
 */

public class QueryUtilsSaleOrder {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsSaleOrder() {
    }

    /**
     * Query the Odoo server for sale order list.
     */
    public static ArrayList<SaleOrder> searchReadSaleOrderList(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<SaleOrder> saleOrders = null;

        // Get fields
        HashMap fieldsMap = SaleOrder.getSaleOrderBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        fieldsMap.put("order", "date_order desc");
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, SALE_ORDER, filter, fieldsMap);
            saleOrders = SaleOrder.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return saleOrders;
    }

    public static SaleOrder fetchSaleOrderDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Get data for a single sale order details
        ArrayList<SaleOrder> saleOrders = null;
        HashMap fieldsSaleOrderMap = SaleOrder.getSaleOrderDetailFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, SALE_ORDER, itemId, fieldsSaleOrderMap);
            saleOrders = SaleOrder.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because the data is only a single record, take only the first element from array list
        SaleOrder saleOrder = null;
        if(saleOrders!=null && !saleOrders.isEmpty()){
            saleOrder = saleOrders.get(0);
        }

        // Get sale order line records
        ArrayList<SaleOrderLine> saleOrderLines = null;
        HashMap fieldsSaleOrderLineMap = SaleOrderLine.getSaleOrderLineFields();
        //Create filter for sale line related to the above sale order
        Object[] filter = new Object[] {
                new Object[] {
                        new Object[] {"order_id", "=", saleOrder.getId()},
                }
        };
        try {
            String jsonResponses2 = searchReadRpcRequest(url.get(2), databaseName, userId, password, SALE_ORDER_LINE, filter, fieldsSaleOrderLineMap);
            saleOrderLines = SaleOrderLine.parseJson(jsonResponses2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        saleOrder.setSaleOrderLine(saleOrderLines);

        return saleOrder;
    }

    /**
    * Save single so or so line to Odoo server
     * and trigger onchange to update fields automatically
    **/
    public static List<Integer> saveOrder(String requestUrl, String databaseName, int userId, String password, String model, int id, HashMap map) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Save sale.order.line
        try {
            String jsonResponse = saveRecord(url.get(2), databaseName, userId, password, model, id, map);
            createdIds.add(Integer.parseInt(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Trigger on change to set correct default field
        try {
            if (!createdIds.isEmpty() & model == QueryUtils.SALE_ORDER) {
                QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, model, "onchange_partner_id", Arrays.asList(createdIds)});
            } else if (!createdIds.isEmpty() & model == QueryUtils.SALE_ORDER_LINE) {
                QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, model, "product_uom_change", Arrays.asList(createdIds)});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdIds;
    }

    /**
     * Confirm single quotation at Odoo server.
     **/
    public static List<Integer> confirmOrder(String requestUrl, String databaseName, int userId, String password, int id) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Confirm sale.order
        try {
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, QueryUtils.SALE_ORDER, "action_confirm", Arrays.asList(id)});
            Log.i(LOG_TAG, "Json response from server: ____________________________" + jsonResponse);
            // add success flag as createdIds item
            if (Boolean.parseBoolean(jsonResponse)) { createdIds.add(1);
            } else { createdIds.add(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdIds;
    }
}
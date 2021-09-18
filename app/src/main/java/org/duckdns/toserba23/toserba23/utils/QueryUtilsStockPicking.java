package org.duckdns.toserba23.toserba23.utils;

import android.util.Log;

import org.duckdns.toserba23.toserba23.model.StockMove;
import org.duckdns.toserba23.toserba23.model.StockPicking;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.duckdns.toserba23.toserba23.data.DbHelper.LOG_TAG;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.STOCK_MOVE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.STOCK_PICKING;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.readRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.saveRecord;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 11/09/18.
 */

public class QueryUtilsStockPicking {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsStockPicking() {
    }

    /**
     * Query the Odoo server for sale order list.
     */
    public static ArrayList<StockPicking> searchReadStockPickingList(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<StockPicking> stockPickings = null;

        // Get fields
        HashMap fieldsMap = StockPicking.getStockPickingBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        fieldsMap.put("order", "scheduled_date desc");
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_PICKING, filter, fieldsMap);
            stockPickings = StockPicking.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockPickings;
    }

    public static StockPicking fetchStockPickingDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Get data for a single stock picking details
        ArrayList<StockPicking> stockPickings = null;
        HashMap fieldsStockPickingMap = StockPicking.getStockPickingDetailFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, STOCK_PICKING, itemId, fieldsStockPickingMap);
            stockPickings = StockPicking.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because the data is only a single record, take only the first element from array list
        StockPicking stockPicking = null;
        if(stockPickings!=null && !stockPickings.isEmpty()){
            stockPicking = stockPickings.get(0);
        }

        // Get stock move records
        ArrayList<StockMove> stockMoves = null;
        HashMap fieldsStockMoveMap = StockMove.getStockMoveFields();
        //Create filter for stock move related to the above stock picking
        Object[] filter = new Object[] {
                new Object[] {
                        new Object[] {"picking_id", "=", stockPicking.getId()},
                }
        };
        try {
            String jsonResponses2 = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_MOVE, filter, fieldsStockMoveMap);
            stockMoves = StockMove.parseJson(jsonResponses2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stockPicking.setStockMoves(stockMoves);

        return stockPicking;
    }

    /**
     * Do transfer for single stock picking at Odoo server.
     **/
    public static List<Integer> donePicking(String requestUrl, String databaseName, int userId, String password, int id) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Confirm sale.order
        try {
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, QueryUtils.STOCK_PICKING, "action_transfer", Arrays.asList(id)});
            // add success flag as createdIds item
            if (Boolean.parseBoolean(jsonResponse)) { createdIds.add(1);
            } else { createdIds.add(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdIds;
    }

    /**
     * Validate single stock picking at Odoo server.
     **/
    public static List<Integer> validatePicking(String requestUrl, String databaseName, int userId, String password, int id) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Confirm sale.order
        try {
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, QueryUtils.STOCK_PICKING, "action_done", Arrays.asList(id)});
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
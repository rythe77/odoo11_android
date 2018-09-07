package org.duckdns.toserba23.toserba23.utils;

/**
 * Created by ryanto on 22/02/18.
 */

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Helper methods related to requesting and receiving data from Odoo server.
 */
public final class QueryUtils {

    // Model names for querying
    public static final String PRODUCT_TEMPLATE = "product.template";
    public static final String PRODUCT_PRODUCT = "product.product";
    public static final String PRODUCT_PRICELIST = "product.pricelist.item";
    public static final String SALE_ORDER = "sale.order";
    public static final String SALE_ORDER_LINE = "sale.order.line";
    public static final String RES_GROUPS = "res.groups";
    public static final String RES_USERS = "res.users";
    public static final String RES_PARTNER = "res.partner";
    public static final String STOCK_WAREHOUSE = "stock.warehouse";
    public static final String STOCK_LOCATION = "stock.location";
    public static final String STOCK_PICKING = "stock.picking";
    public static final String STOCK_PACK_OPERATION = "stock.pack.operation";
    public static final String STOCK_MOVE = "stock.move";
    public static final String STOCK_BACKORDER_CONFIRMATION = "stock.backorder.confirmation";

    private static final String LOG_TAG = Context.class.getName();

    // Separate RPC call to minimize string object buffer in memory
    // This variable determine number of records queried in a batch
    // Appropriate number tested!! Optimal value is 200-300
    public static final int BATCH_SIZE = 200;
    public static final int LIMIT_PAGING_SIZE = 50;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtils() {
    }

    /**
     * Query the Odoo server for database list.
     */
    public static ArrayList<String> fetchDatabaseList(String requestUrl) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a list of databases
        ArrayList<String> databaseList = new ArrayList<>();
        try {
            //databaseList = getDatabase(url.get(0));
            String jsonResponse = null;
            jsonResponse = makeXmlRpcRequest(url.get(0), "list", new Object[]{});
            // Parse json response for database list request
            if(jsonResponse!=null) {
                try {
                    JSONArray dbList = new JSONArray(jsonResponse);
                    for (int i = 0; i < dbList.length(); i++) {
                        databaseList.add(i, dbList.getString(i));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("QueryUtils", "Problem parsing the databases JSON results", e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return databaseList;
    }

    /**
     * Query the Odoo server for stock picking list.
     *
    public static ArrayList<ResPartner> searchReadResPartner(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<ResPartner> resPartners = null;

        // Get fields
        HashMap fieldsMap = ResPartner.getResPartnerBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);

        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, RES_PARTNER, filter, fieldsMap);
            resPartners = ResPartner.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resPartners;
    }/

    /**
     * Query the Odoo server for stock picking list.
     *
    public static ArrayList<StockLocation> searchReadStockLocation(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<StockLocation> stockLocations = null;

        // Get fields
        HashMap fieldsMap = StockLocation.getStockLocationBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);

        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_LOCATION, filter, fieldsMap);
            stockLocations = StockLocation.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockLocations;
    }/

    /**
     * Query the Odoo server for stock picking list.
     *
    public static ArrayList<StockPicking> searchReadStockPickingList(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<StockPicking> stockPickings = null;

        // Get fields
        HashMap fieldsMap = StockPicking.getStockPickingListFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        fieldsMap.put("order", "min_date desc");
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_PICKING, filter, fieldsMap);
            stockPickings = StockPicking.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stockPickings;
    }/

    /**
     * Query the Odoo server for stock picking detail.
     *
    public static StockPicking fetchStockPickingDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Get data for a single stock picking details
        ArrayList<StockPicking> stockPickings = null;
        HashMap fieldsStockPickingMap = StockPicking.getStockPickingDetailFields();
        try {
            String jsonResponse = makeXmlRpcRequest(url.get(2), "execute_kw", new Object[]{databaseName, userId, password, STOCK_PICKING, "read", Arrays.asList(itemId), fieldsStockPickingMap});
            stockPickings = StockPicking.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because the data is only a single record, take only the first element from array list
        StockPicking stockPicking = null;
        if(stockPickings!=null && !stockPickings.isEmpty()){
            stockPicking = stockPickings.get(0);
        }

        // Get stock pack operation records
        ArrayList<StockPackOperation> stockPackOperations = null;
        HashMap fieldsStockPackOperationMap = StockPackOperation.getStockPackOperationFields();
        //Create filter for stock pack operation related to the above picking
        Object[] filter = new Object[] {
                new Object[] {
                        new Object[] {"picking_id", "=", stockPicking.getName()},
                }
        };
        try {
            String jsonResponses2 = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_PACK_OPERATION, filter, fieldsStockPackOperationMap);
            stockPackOperations = StockPackOperation.parseJson(jsonResponses2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stockPicking.setStockPackOperation(stockPackOperations);

        // Get stock move operation records
        ArrayList<StockMove> stockMoves = null;
        HashMap fieldsStockMoveMap = StockMove.getStockMoveFields();
        //Create filter for stock move related to the above picking which is not converted to stock pack operation yet
        Object[] filterStockMove = new Object[] {
                new Object[] {
                        new Object[] {"picking_id", "=", stockPicking.getName()},
                }
        };
        try {
            String jsonResponse3 = searchReadRpcRequest(url.get(2), databaseName, userId, password, STOCK_MOVE, filterStockMove, fieldsStockMoveMap);
            stockMoves = StockMove.parseJson(jsonResponse3);
        } catch (Exception e) {
            e.printStackTrace();
        }
        stockPicking.setStockMove(stockMoves);

        return stockPicking;
    }/

    /**
     * Save single stock picking detail change to Odoo server.
     *
    public static void saveOrder(String requestUrl, String databaseName, int userId, String password, int pickingId, HashMap map, ArrayList<StockPackOperation> stockPackOperations) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Save stock.picking
        try {
            String jsonResponse = saveRecord(url.get(2), databaseName, userId, password, STOCK_PICKING, pickingId, map);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save stock.pack.operation associated to this picking
        for ( int i = 0; i < stockPackOperations.size(); i++ ) {
            StockPackOperation stockPackOperation = stockPackOperations.get(i);
            int stockOpId = stockPackOperation.getId();
            HashMap stockOpMapData = stockPackOperation.getHashmap();
            // Save individual stock.pack.operation
            try {
                String jsonResponse = saveRecord(url.get(2), databaseName, userId, password, STOCK_PACK_OPERATION, stockOpId, stockOpMapData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean updateStockPickingWorkflow(String requestUrl, String databaseName, int userId, String password, String command, int pickingId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Set call method based on workflow change command
        // Call without response is called directly inside the switch. Else only call method is defined
        String callMethod;
        switch (command) {
            case "mark_todo":
                callMethod = "action_confirm";
                break;
            case "assign":
                callMethod = "action_assign";
                break;
            case "finish":
                callMethod = "action_transfer";
                break;
            case "validate":
                try {
                    boolean isBackorder = Boolean.parseBoolean(makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, STOCK_PICKING, "check_backorder", Arrays.asList(Arrays.asList(pickingId))}));
                    return isBackorder;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            case "continue_validate":
                try {
                    makeXmlRpcRequest(url.get(2), "execute_kw", new Object[]{databaseName, userId, password, STOCK_PICKING, "do_new_transfer", Arrays.asList(Arrays.asList(pickingId))});
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            case "process_backorder":
                // TODO: Should modify Odoo stock.picking to have "stock.backorder.confirmation" process method
                try {
                    makeXmlRpcRequest(url.get(2), "execute_kw", new Object[]{databaseName, userId, password, STOCK_BACKORDER_CONFIRMATION, "process", Arrays.asList(Arrays.asList(pickingId))});
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            case "unreserve":
                try {
                    makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, STOCK_PICKING, "do_unreserve", Arrays.asList(Arrays.asList(pickingId))});
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            case "correction":
                callMethod = "redo_transfer";
                break;
            default:
                return false;
        }

        try {
            String jsonResponse = makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, STOCK_PICKING, callMethod, Arrays.asList(Arrays.asList(pickingId))});
            Log.i(LOG_TAG, "Json response from server for update workflow: " + jsonResponse);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }/

    /**
     * Authenticate the Odoo server for given username, password, and database name. Return user ID
     */
    public static int getUid(String requestUrl, String userName, String password, String databaseName) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        int userId = 0;
        try {
            userId = Integer.parseInt(makeXmlRpcRequest(url.get(1), "authenticate", new Object[] {databaseName,userName,password, Collections.emptyList()}));
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Problem authenticating with server.", e);
        }
        return userId;
    }

    /**
     * Returns new URL list object from the given string URL.
     * First item is for authentication, while second item is for query
     */
    public static ArrayList<URL> createUrl(String stringUrl) {
        URL databaseListUrl = null;
        URL authenticationUrl = null;
        URL queryUrl = null;
        ArrayList<URL> result = new ArrayList<>();
        try {
            databaseListUrl = new URL(stringUrl + "/xmlrpc/db");
            result.add(0, databaseListUrl);
            authenticationUrl = new URL(stringUrl + "/xmlrpc/2/common");
            result.add(1, authenticationUrl);
            queryUrl = new URL (stringUrl + "/xmlrpc/2/object");
            result.add(2,queryUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return result;
    }

    /**
     * Parse date response from odoo server
     * @param dateString
     * @return
     */
    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        if (dateString!="false") {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }

    /**
     * Parse date time response from odoo server
     * @param dateTimeString
     * @return
     */
    public static Date parseDateTime(String dateTimeString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date scheduledDate = null;
        if (dateTimeString!="false") {
            try {
                scheduledDate = sdf.parse(dateTimeString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return scheduledDate;
    }

    /**
     * Create datetime string for saving to odoo server
     * @param dateTime
     * @return
     */
    public static String toServerDateTimeFormat(Date dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String scheduledDate = "";
        if (dateTime!=null) {
            try {
                scheduledDate = sdf.format(dateTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return scheduledDate;
    }

    /**
     * Make RPC call in batch, in order to reduce buffering object size in memory
     * Should be used in 'getSomething' helper method
     * @param url request URL
     * @param databaseName name of the database to be queried
     * @param userId
     * @param password
     * @param filter only query interesting record
     * @param model name of the model to be queried
     * @param map map of fields to be retrieved
     * @param is_limit if true, make only one rpc call
     * @return list of json response string
     */
    public static String[] serializeRpcRequest(URL url, String databaseName, int userId, String password, String model, String command, Object[] filter, HashMap map, boolean is_limit) {
        int rpcCallTotal;

        // decides pagination or limit result
        if (!is_limit) {
            // get number of records
            String searchCountResponse = makeXmlRpcRequest(url, "execute_kw", new Object[]{databaseName, userId, password, model, "search_count", filter});
            int dataSize = Integer.parseInt(searchCountResponse);

            // Calculate necessary number of separate RPC call
            rpcCallTotal = (int) Math.ceil(((double) dataSize) / BATCH_SIZE);
        } else {
            rpcCallTotal = 1;
        }
        // Create container for rpc responses
        String[] responses = new String[rpcCallTotal];

        // Make call in batches
        for(int i = 0; i < rpcCallTotal; i++) {
            map.put("limit", BATCH_SIZE);
            map.put("offset", i*BATCH_SIZE);

            String response = makeXmlRpcRequest(url, "execute_kw", new Object[] {databaseName, userId, password, model, command, filter, map});
            responses[i] = response;
        }

        return responses;
    }

    /**
     * Wrapper for "read" RPC call
     * @param url request URL
     * @param databaseName name of the database to be queried
     * @param userId
     * @param password
     * @param filter only query interesting record
     * @param model name of the model to be queried
     * @param map map of fields to be retrieved
     * @return json response string
     */
    public static String readRpcRequest(URL url, String databaseName, int userId, String password, String model, int itemId, HashMap map) {
        //Add language context to rpc request
        HashMap lang = new HashMap();
        lang.put("lang", "id_ID");
        map.put("context", lang);

        String response = null;
        try {
            response = makeXmlRpcRequest(url, "execute_kw", new Object[]{databaseName, userId, password, model, "read", Arrays.asList(itemId), map});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Wrapper for "search_read" RPC call
     * @param url request URL
     * @param databaseName name of the database to be queried
     * @param userId
     * @param password
     * @param filter only query interesting record
     * @param model name of the model to be queried
     * @param map map of fields to be retrieved
     * @return json response string
     */
    public static String searchReadRpcRequest(URL url, String databaseName, int userId, String password, String model, Object[] filter, HashMap map) {
        //Add language context to rpc request
        HashMap lang = new HashMap();
        lang.put("lang", "id_ID");
        map.put("context", lang);

        String response = null;
        try {
            response = makeXmlRpcRequest(url, "execute_kw", new Object[]{databaseName, userId, password, model, "search_read", filter, map});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * Wrapper for "search_count" RPC call
     * @param url request URL
     * @param databaseName name of the database to be queried
     * @param userId
     * @param password
     * @param filter only query interesting record
     * @param model name of the model to be queried
     * @return json response string
     */
    public static int searchCountRpcRequest(URL url, String databaseName, int userId, String password, String model, Object[] filter) {
        int recordsCount = 0;
        try {
            recordsCount = Integer.parseInt(makeXmlRpcRequest(url, "execute_kw", new Object[]{databaseName, userId, password, model, "search_count", filter}));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return recordsCount;
    }

    /**
     * Wrapper for "create" and "write" RPC call
     * Save record to Odoo server
     * Update existing item record, with id @param itemId
     * If itemId is zero, create new record instead
     * Hashmap @param map contains data to be saved
     */
    public static String saveRecord(URL url, String databaseName, int userId, String password, String model, int itemId, HashMap map) {
        Object[] newData;

        String jsonResponse = null;
        if(itemId == 0) {
            // Create new record on Odoo server
            newData = new Object[] {map};
            try {
                jsonResponse = makeXmlRpcRequest(url, "execute_kw", new Object[] {databaseName, userId, password, model, "create", newData});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Update new record on Odoo server
            newData = new Object[] {Arrays.asList(itemId), map};
            try {
                jsonResponse = makeXmlRpcRequest(url, "execute_kw", new Object[] {databaseName, userId, password, model, "write", newData});
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jsonResponse;
    }

    /**
     * Make basic RPC call an return json response in string
     * Note : Buffering object cost memory
     * For querying large record, use {serializeRpcRequest} to make call in batches,
     * or use is_limit flag to return only limited result
     * @param url
     * @param command
     * @param params
     * @return
     */
    public static String makeXmlRpcRequest(URL url, String command, Object[] params) {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        try{
            XMLRPCClient client = new XMLRPCClient(url, XMLRPCClient.FLAGS_SSL_IGNORE_ERRORS);
            Gson gson = new Gson();
            Object responseObject = client.call(command, params);
            jsonResponse = gson.toJson(responseObject);

            //Log.i(LOG_TAG, "Json response from server: " + jsonResponse);
        } catch(XMLRPCServerException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Problem connecting server XML-RPC", e);
        } catch(XMLRPCException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Problem iniating XML-RPC", e);
        } catch(Exception e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Problem before initiating XML-RPC request", e);
        }

        return jsonResponse;
    }
}
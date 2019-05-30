package org.duckdns.toserba23.toserba23.utils;

import org.duckdns.toserba23.toserba23.model.ResPartner;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import static org.duckdns.toserba23.toserba23.utils.QueryUtils.RES_PARTNER;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.readRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 23/02/18.
 */

public class QueryUtilsResPartner {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsResPartner() {
    }

    /**
     * Query the Odoo server for res partner list.
     */
    public static ArrayList<ResPartner> searchReadResPartnerList(String requestUrl, String databaseName, int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<ResPartner> resPartners = null;

        // Get fields
        HashMap fieldsMap = ResPartner.getResPartnerBulkFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        fieldsMap.put("order", "name asc");
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, RES_PARTNER, filter, fieldsMap);
            resPartners = ResPartner.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resPartners;
    }

    public static ResPartner fetchResPartnerDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Get data for a single res partner details
        ArrayList<ResPartner> resPartners = null;
        HashMap fieldsResPartnerMap = ResPartner.getResPartnerDetailFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, RES_PARTNER, itemId, fieldsResPartnerMap);
            resPartners = ResPartner.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Because the data is only a single record, take only the first element from array list
        ResPartner resPartner = null;
        if(resPartners!=null && !resPartners.isEmpty()){
            resPartner = resPartners.get(0);
        }

        return resPartner;
    }
}
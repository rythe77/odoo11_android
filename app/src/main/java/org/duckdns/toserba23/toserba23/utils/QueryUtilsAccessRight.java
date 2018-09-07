package org.duckdns.toserba23.toserba23.utils;

import android.util.Log;

import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.duckdns.toserba23.toserba23.data.DbHelper.LOG_TAG;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.RES_GROUPS;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 23/02/18.
 */

public class QueryUtilsAccessRight {

    public static final String ACCESS_RIGHT = "access_right_model";

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsAccessRight() {
    }

    /**
     * Query the Odoo server for stock picking list.
     */
    public static ArrayList<String> searchReadResGroups(String requestUrl, String databaseName, final int userId, String password, Object[] filter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<String> resGroups = null;

        // Get fields
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "display_name"
        ));
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, RES_GROUPS, filter, map);
            resGroups = parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resGroups;
    }

    private static ArrayList<String> parseJson(String jsonResponse) {
        ArrayList<String> resGroups = new ArrayList<>();

        List<String> fieldResGroups = (List<String>) new ArrayList<String>();
        fieldResGroups.add("id");
        fieldResGroups.add("display_name");

        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);
                    int id = field.optInt(fieldResGroups.get(0), 0);
                    String resGroupName = field.optString(fieldResGroups.get(1));
                    resGroups.add(resGroupName);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ResGroups", "Problem parsing the JSON results", e);
            }
        }
        for (int i = 0; i < resGroups.size(); i++) {
            Log.i(LOG_TAG, "ResGroup Name: " + resGroups.get(i));
        }
        return resGroups;
    }
}
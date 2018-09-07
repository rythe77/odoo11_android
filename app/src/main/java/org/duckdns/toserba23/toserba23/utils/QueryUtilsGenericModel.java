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

public class QueryUtilsGenericModel {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsGenericModel() {
    }

    /**
     * Query the Odoo server for stock picking list.
     */
    public static ArrayList<GenericModel> searchReadModel(String requestUrl, String databaseName, final int userId, String password, String model, Object[] filter, HashMap map, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<GenericModel> models = null;

        // Get fields
        map.put("limit", limit);
        map.put("offset", offsetCounter*limit);
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, model, filter, map);
            models = parseJson(jsonResponse, map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return models;
    }

    private static ArrayList<GenericModel> parseJson(String jsonResponse, HashMap map) {
        ArrayList<GenericModel> models = new ArrayList<>();

        List<String> fieldModels = (List<String>) map.get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);
                    int id = field.optInt(fieldModels.get(0), 0);
                    String name = field.optString(fieldModels.get(1));
                    models.add(new GenericModel(id, name));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("GenericModel", "Problem parsing the JSON results", e);
            }
        }
        return models;
    }
}
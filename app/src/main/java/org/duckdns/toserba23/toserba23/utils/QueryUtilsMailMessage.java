package org.duckdns.toserba23.toserba23.utils;

import static org.duckdns.toserba23.toserba23.utils.QueryUtils.HR_HOLIDAYS;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.MAIL_MESSAGE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.saveRecord;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QueryUtilsMailMessage {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsMailMessage() {
    }

    /**
     * Save single mail message to Odoo server
     **/
    public static List<Integer> saveMailMessage(String requestUrl, String databaseName, int userId, String password, HashMap map) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Save or create hr.holidays
        try {
            String jsonResponse = saveRecord(url.get(2), databaseName, userId, password, MAIL_MESSAGE, 0, map);
            createdIds.add(Integer.parseInt(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdIds;
    }
}

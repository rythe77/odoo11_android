package org.duckdns.toserba23.toserba23.utils;

import static org.duckdns.toserba23.toserba23.utils.QueryUtils.HR_HOLIDAYS;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.MAIL_MESSAGE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.SALE_ORDER_LINE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.readRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.saveRecord;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

import org.duckdns.toserba23.toserba23.model.HrHolidays;
import org.duckdns.toserba23.toserba23.model.MailMessage;
import org.duckdns.toserba23.toserba23.model.SaleOrderLine;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class QueryUtilsHrHolidays {

    public static final int HR_HOLIDAYS_SAVE_CHANGES = 0;
    public static final int HR_HOLIDAYS_CREATE_NEW = 1;
    public static final int HR_HOLIDAYS_APPROVE = 2;
    public static final int HR_HOLIDAYS_VALIDATE = 3;

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsHrHolidays() {
    }

    /**
     * Query the Odoo server for hr holiday list.
     */
    public static ArrayList<HrHolidays> searchReadHrHolidaysList(String requestUrl, String databaseName, final int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<HrHolidays> hrHolidays = null;

        // Get fields
        HashMap fieldsMap = HrHolidays.getHrHolidaysFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter);
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, HR_HOLIDAYS, filter, fieldsMap);
            hrHolidays = HrHolidays.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hrHolidays;
    }

    public static HrHolidays fetchHrHolidaysDetail(String requestUrl, String databaseName, int userId, String password, int itemId) {
        ArrayList<URL> url = createUrl(requestUrl);

        ArrayList<HrHolidays> hrHolidays = null;
        HashMap fieldsHrHolidaysMap = HrHolidays.getHrHolidaysFields();
        try {
            String jsonResponse = readRpcRequest(url.get(2), databaseName, userId, password, HR_HOLIDAYS, itemId, fieldsHrHolidaysMap);
            hrHolidays = HrHolidays.parseJson(jsonResponse);
        } catch (Exception e ) {
            e.printStackTrace();
        }

        HrHolidays hrHoliday = null;
        if (hrHolidays!=null && !hrHolidays.isEmpty()) {
            hrHoliday = hrHolidays.get(0);
        }

        // Get mail message records
        ArrayList<MailMessage> mailMessages = null;
        HashMap fieldsMailMessageMap = MailMessage.getMailMessageFields();
        //Create filter for mail message related to the above hr holidays
        Object[] filter = new Object[] {
                new Object[] {
                        new Object[] {"model", "=", HR_HOLIDAYS},
                        new Object[] {"res_id", "=", hrHoliday.getId()},
                        new Object[] {"message_type", "=", "comment"},
                }
        };
        try {
            String jsonResponses2 = searchReadRpcRequest(url.get(2), databaseName, userId, password, MAIL_MESSAGE, filter, fieldsMailMessageMap);
            mailMessages = MailMessage.parseJson(jsonResponses2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        hrHoliday.setMailMessage(mailMessages);

        return hrHoliday;
    }

    /**
     * Save single hr holidays to Odoo server
     **/
    public static List<Integer> saveHolidays(String requestUrl, String databaseName, int userId, String password, int id, HashMap map) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Save or create hr.holidays
        try {
            String jsonResponse = saveRecord(url.get(2), databaseName, userId, password, HR_HOLIDAYS, id, map);
            createdIds.add(Integer.parseInt(jsonResponse));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return createdIds;
    }

    /**
     * Confirm leave at Odoo server.
     **/
    public static List<Integer> approveLeave(String requestUrl, String databaseName, int userId, String password, int id) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Confirm sale.order
        try {
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, HR_HOLIDAYS, "action_approve", Arrays.asList(id)});
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
     * Validate leave at Odoo server.
     **/
    public static List<Integer> validateLeave(String requestUrl, String databaseName, int userId, String password, int id) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Confirm sale.order
        try {
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, HR_HOLIDAYS, "action_validate", Arrays.asList(id)});
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

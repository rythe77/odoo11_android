package org.duckdns.toserba23.toserba23.utils;

import android.util.Log;

import org.duckdns.toserba23.toserba23.model.GenericModel;
import org.duckdns.toserba23.toserba23.model.HrEmployee;
import org.duckdns.toserba23.toserba23.model.SaleOrder;
import org.duckdns.toserba23.toserba23.model.SaleOrderLine;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.duckdns.toserba23.toserba23.data.DbHelper.LOG_TAG;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.HR_EMPLOYEE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.PRODUCT_TEMPLATE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.SALE_ORDER;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.SALE_ORDER_LINE;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.createUrl;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.readRpcRequest;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.saveRecord;
import static org.duckdns.toserba23.toserba23.utils.QueryUtils.searchReadRpcRequest;

/**
 * Created by ryanto on 29/09/18.
 */

public class QueryUtilsAttendance {

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private QueryUtilsAttendance() {
    }

    /**
     * Punch in attendance record for a single employee
     * return true if successful
     **/
    public static Boolean recordAttendance(String requestUrl, String databaseName, int userId, String password, int employeeId) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        List<Integer> createdIds = new ArrayList<>();

        // Get fields
        HashMap hrEmployeeMap = new HashMap();
        hrEmployeeMap.put("fields", Arrays.asList(
                "attendance_state"
        ));

        // punch in attendance
        try {
            // check attendance state before punching
            String attendanceStateBefore = readRpcRequest(url.get(2), databaseName, userId, password, HR_EMPLOYEE, employeeId, hrEmployeeMap);
            // punch attendance record
            String jsonResponse = QueryUtils.makeXmlRpcRequest(url.get(2), "execute_kw", new Object[] {databaseName, userId, password, QueryUtils.HR_EMPLOYEE, "attendance_action_change", Arrays.asList(employeeId)});
            // check attendance state after punching
            String attendanceStateAfter = readRpcRequest(url.get(2), databaseName, userId, password, HR_EMPLOYEE, employeeId, hrEmployeeMap);
            //Log.i(LOG_TAG, "Json response from server: ____________________________" + attendanceStateBefore + "____________________________" + jsonResponse + "____________________________" + attendanceStateAfter);

            // return success flag
            if (!attendanceStateBefore.equals(attendanceStateAfter)) { return true;
            } else { return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Query the Odoo server for employee list.
     */
    public static ArrayList<HrEmployee> searchReadHrEmployeeList(String requestUrl, String databaseName, final int userId, String password, Object[] filter, int limit, int offsetCounter) {
        // Create URL object
        ArrayList<URL> url = createUrl(requestUrl);

        // Create container for results
        ArrayList<HrEmployee> hrEmployees = null;

        // Get fields
        HashMap fieldsMap = HrEmployee.getHrEmployeeFields();
        fieldsMap.put("limit", limit);
        fieldsMap.put("offset", offsetCounter*limit);
        try {
            String jsonResponse = searchReadRpcRequest(url.get(2), databaseName, userId, password, HR_EMPLOYEE, filter, fieldsMap);
            hrEmployees = HrEmployee.parseJson(jsonResponse);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hrEmployees;
    }
}
package org.duckdns.toserba23.toserba23.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ryanto on 24/02/18.
 */

public class HrEmployee {

    private int mId;
    private String mName;
    private int mPIN;
    private String mAttendanceState;

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getName() { return mName;}
    public int getPIN() { return mPIN; }
    public String getAttendanceState() { return mAttendanceState; }

    /**
     * Main constructor of this class
     */
    public HrEmployee(int id, String name, int PIN, String attendanceState) {
        mId = id;
        mName = name;
        mPIN = PIN;
        mAttendanceState = attendanceState;
    }

    /**
     * hashmap of hr employee fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getHrEmployeeFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "pin",
                "attendance_state"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of HrEmployee object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<HrEmployee> parseJson(String jsonResponse) {
        ArrayList<HrEmployee> hrEmployees = new ArrayList<>();

        List<String> fieldHrEmployee = (List<String>) getHrEmployeeFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldHrEmployee.get(0), 0);
                    String name = field.optString(fieldHrEmployee.get(1));
                    int pin = field.optInt(fieldHrEmployee.get(2), 0);
                    String attendanceState = field.optString(fieldHrEmployee.get(3));
                    hrEmployees.add( new HrEmployee(id, name, pin, attendanceState));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("HrEmployee", "Problem parsing the JSON results", e);
            }
        }
        return hrEmployees;
    }
}
package org.duckdns.toserba23.toserba23.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class HrHolidays {

    private int mId;
    private String mName;
    private String mStatus;
    private GenericModel mHolidayStatus;
    private String mDateFrom;
    private String mDateTo;
    private GenericModel mEmployee;
    private ArrayList<MailMessage> mMailMessages;

    /**
     * String mapping for sale order status
     */
    private static final HashMap<String, String> stateMap = createMap();
    private static HashMap<String, String> createMap()
    {
        HashMap<String, String> stateMap = new HashMap<String,String>();
        stateMap.put("draft", "Akan Dikumpulkan");
        stateMap.put("confirm", "Akan Disetujui");
        stateMap.put("refuse", "Ditolak");
        stateMap.put("validate1", "Persetujuan Kedua");
        stateMap.put("validate", "Disetujui");
        stateMap.put("cancel", "Dibatalkan");
        return stateMap;
    }

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getName() { return mName; }
    public void setName(String name) { mName = name; }
    public String getStatus() { return mStatus; }
    public String getStatusName() { return (mStatus!=null && !mStatus.isEmpty())?stateMap.get(mStatus):null; }
    public GenericModel getHolidayStatus() { return mHolidayStatus; }
    public void setHolidayStatus(GenericModel holidayStatus) { mHolidayStatus = holidayStatus; }
    public String getDateFrom() { return mDateFrom; }
    public void setDateFrom(String dateFrom) { mDateFrom = dateFrom; }
    public String getDateTo() { return mDateTo; }
    public void setDateTo(String dateTo) { mDateTo = dateTo; }
    public GenericModel getEmployee() { return mEmployee; }
    public void setEmployee(GenericModel employee) { mEmployee = employee; }

    /**
     * Series of set and get method for linked fields
     * @return
     */
    public void setMailMessage(ArrayList<MailMessage> mailMessages) {mMailMessages = mailMessages;}
    public ArrayList<MailMessage> getMailMessage() { return mMailMessages; }

    /**
     * Main Constructor of this class
     */
    public HrHolidays(int id, String name, String status, GenericModel holidayStatus, String dateFrom, String dateTo, GenericModel employee) {
        mId = id;
        mName = name;
        mStatus = status;
        mHolidayStatus = holidayStatus;
        mDateFrom = dateFrom;
        mDateTo = dateTo;
        mEmployee = employee;
    }

    /**
     * Constructor for writing data to server
     */
    public HrHolidays(int id, String name, GenericModel holidayStatus, String dateFrom, String dateTo, GenericModel employee) {
        mId = id;
        mName = name;
        mHolidayStatus = holidayStatus;
        mDateFrom = dateFrom;
        mDateTo = dateTo;
        mEmployee = employee;
    }

    /**
     * Constructor for creating new hr holidays
     */
    public HrHolidays(String name, GenericModel holidayStatus, String dateFrom, String dateTo, GenericModel employee) {
        mName = name;
        mHolidayStatus = holidayStatus;
        mDateFrom = dateFrom;
        mDateTo = dateTo;
        mEmployee = employee;
    }

    /**
     * hashmap of hr holidays fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getHrHolidaysFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "state",
                "holiday_status_id",
                "date_from",
                "date_to",
                "employee_id"
        ));
        return map;
    }

    /**
     * hashmap of hr holidays fields to save to server
     * @return
     */
    public static HashMap<String,Arrays> getHrHolidaysFieldsToSave() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "holiday_status_id",
                "date_from",
                "date_to",
                "employee_id"
        ));
        return map;
    }

    /**
     * hashmap of hr holidays fields to create new
     * @return
     */
    public static HashMap<String,Arrays> getHrHolidaysFieldsToCreate() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "name",
                "holiday_status_id",
                "date_from",
                "date_to",
                "employee_id"
        ));
        return map;
    }

    /**
     * Pack data to be saved to server in a hashmap
     * @return
     */
    // create hashmap data pack to be sent to Odoo server
    public HashMap getHashmap() {
        HashMap fieldsArray = getHrHolidaysFieldsToSave();
        List<String> fields = (List<String>) fieldsArray.get("fields");

        HashMap map = new HashMap<>();
        map.put(fields.get(0), getId()!=0?getId():"");
        map.put(fields.get(1), !getName().isEmpty()?getName():"");
        map.put(fields.get(2), getHolidayStatus().getId()!=0?getHolidayStatus().getId():"");
        map.put(fields.get(3), !getDateFrom().isEmpty()?getDateFrom():"");
        map.put(fields.get(4), !getDateTo().isEmpty()?getDateTo():"");
        map.put(fields.get(5), getEmployee().getId()!=0?getEmployee().getId():"");
        return map;
    }

    /**
     * Pack data to be saved to server in a hashmap
     * @return
     */
    // create hashmap data pack to be sent to Odoo server
    public HashMap getHashmapToCreate() {
        HashMap fieldsArray = getHrHolidaysFieldsToCreate();
        List<String> fields = (List<String>) fieldsArray.get("fields");

        HashMap map = new HashMap<>();
        map.put(fields.get(0), !getName().isEmpty()?getName():"");
        map.put(fields.get(1), getHolidayStatus().getId()!=0?getHolidayStatus().getId():"");
        map.put(fields.get(2), !getDateFrom().isEmpty()?getDateFrom():"");
        map.put(fields.get(3), !getDateTo().isEmpty()?getDateTo():"");
        map.put(fields.get(4), getEmployee().getId()!=0?getEmployee().getId():"");
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of HrHolidays object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<HrHolidays> parseJson (String jsonResponse) {
        ArrayList<HrHolidays> hrHolidays = new ArrayList<>();

        List<String> fieldHrHolidays = (List<String>) getHrHolidaysFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldHrHolidays.get(0), 0);
                    String name = field.optString(fieldHrHolidays.get(1));
                    String status = field.optString(fieldHrHolidays.get(2));
                    GenericModel holidayStatusModel = null;
                    if (field.has(fieldHrHolidays.get(3))) {
                        JSONArray holidayStatus = field.optJSONArray(fieldHrHolidays.get(3));
                        if (holidayStatus!=null) {
                            int holidayStatusId = holidayStatus.optInt(0);
                            String holidayStatusName = holidayStatus.optString(1);
                            holidayStatusModel = new GenericModel(holidayStatusId, holidayStatusName);
                        }
                    }
                    String dateFrom = field.optString(fieldHrHolidays.get(4));
                    String dateTo = field.optString(fieldHrHolidays.get(5));
                    GenericModel employeeModel = null;
                    if (field.has(fieldHrHolidays.get(6))) {
                        JSONArray employee = field.optJSONArray(fieldHrHolidays.get(6));
                        if (employee!=null) {
                            int employeeId = employee.optInt(0);
                            String employeeName = employee.optString(1);
                            employeeModel = new GenericModel(employeeId, employeeName);
                        }
                    }
                    hrHolidays.add( new HrHolidays(id, name, status, holidayStatusModel, dateFrom, dateTo, employeeModel));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("HrHolidays", "Problem parsing the JSON results", e);
            }
        }
        return hrHolidays;
    }
}

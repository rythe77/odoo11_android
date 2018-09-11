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
 * Created by ryanto on 11/09/18.
 */

public class StockPicking {
    private int mId;
    private String mScheduledDate;
    private String mState;
    private String mName;
    private String mXNotes;
    private GenericModel mPartner;
    private String mOrigin;
    private String mOperationTypes;
    private String mTransporter;
    private String mIntTransporter;
    private String mXVehicleNotes;
    private int mPriority;
    private String mLocation;
    private String mDestLocation;
    private ArrayList<StockMove> mStockMoves;

    /**
     * String mapping for stock picking status
     */
    private static final HashMap<String, String> stateMap = createMap();
    private static HashMap<String, String> createMap()
    {
        HashMap<String, String> stateMap = new HashMap<String,String>();
        stateMap.put("draft", "Rancangan");
        stateMap.put("waiting", "Menunggu");
        stateMap.put("confirmed", "Belum tersedia");
        stateMap.put("partially_available", "Tersedia sebagian");
        stateMap.put("assigned", "Tersedia");
        stateMap.put("waiting_validation", "Tunggu validasi");
        stateMap.put("done", "Selesai");
        return stateMap;
    }

    /**
     * String mapping for stock picking priority
     */
    private static final HashMap<Integer, String> priorityMap = createMap2();
    private static HashMap<Integer, String> createMap2()
    {
        HashMap<Integer, String> priorityMap = new HashMap<Integer, String>();
        priorityMap.put(0, "Tidak Mendesak");
        priorityMap.put(1, "Biasa");
        priorityMap.put(2, "Mendesak");
        priorityMap.put(3, "Sangat Mendesak");
        return priorityMap;
    }

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getScheduledDate() { return mScheduledDate; }
    public String getState() { return mState; }
    public String getStateName() { return (mState!=null && !mState.isEmpty())?stateMap.get(mState):null; }
    public String getName() { return mName; }
    public String getXNotes() { return mXNotes; }
    public void setPartner(GenericModel partner) { mPartner = partner; }
    public GenericModel getPartner() {
        return mPartner;
    }
    public int getPartnerId() {return mPartner !=null? mPartner.getId():0;}
    public String getPartnerName() {return mPartner !=null? mPartner.getName():"";}
    public String getOrigin() {return mOrigin;}
    public void setOperationTypes(String operationTypes) { mOperationTypes = operationTypes; }
    public String getOperationTypes() { return mOperationTypes; }
    public void setTransporter(String transporter) { mTransporter = transporter; }
    public String getTransporter() { return mTransporter; }
    public void setIntTransporter(String intTransporter) { mIntTransporter = intTransporter; }
    public String getIntTransporter() { return mIntTransporter; }
    public void setXVehicleNotes(String xVehicleNotes) { mXVehicleNotes = xVehicleNotes; }
    public String getXVehicleNotes() { return mXVehicleNotes; }
    public void setPriority(int priority) { mPriority = priority; }
    public String getPriorityName() { return priorityMap.get(mPriority); }
    public int getPriority() { return mPriority; }
    public void setLocation(String location) { mLocation = location; }
    public String getLocation() { return mLocation; }
    public void setDestLocation(String destLocation) { mDestLocation = destLocation; }
    public String getDestLocation() { return mDestLocation; }

    /**
     * Series of set and get method for linked fields
     * @return
     */
    public void setStockMoves(ArrayList<StockMove> stockMoves) {mStockMoves = stockMoves;}
    public ArrayList<StockMove> getStockMoves() { return mStockMoves; }

    /**
     * Main constructor of this class
     */
    public StockPicking(int id, String scheduledDate, String state, String name, String xNotes, GenericModel partner, String origin) {
        mId = id;
        mScheduledDate = scheduledDate;
        mState = state;
        mName = name;
        mXNotes = xNotes;
        mPartner = partner;
        mOrigin = origin;
    }

    /**
     * hashmap of the fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getStockPickingBulkFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "scheduled_date",
                "state",
                "name",
                "x_notes",
                "partner_id",
                "origin"
        ));
        return map;
    }

    /**
     * hashmap of detail fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getStockPickingDetailFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "scheduled_date",
                "state",
                "name",
                "x_notes",
                "partner_id",
                "origin",
                "picking_type_id",
                "transporter_id",
                "int_transporter_id",
                "x_vehicle_notes",
                "priority",
                "location_id",
                "location_dest_id"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<StockPicking> parseJson(String jsonResponse) {
        ArrayList<StockPicking> stockPickings = new ArrayList<>();

        List<String> fieldStockPicking = (List<String>) getStockPickingDetailFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldStockPicking.get(0), 0);
                    String scheduledDate = field.optString(fieldStockPicking.get(1), "");
                    String state = field.optString(fieldStockPicking.get(2), "");
                    String name = field.optString(fieldStockPicking.get(3), "");
                    String xNotes = field.optString(fieldStockPicking.get(4), "");
                    GenericModel partnerModel = null;
                    if (field.has(fieldStockPicking.get(5))) {
                        JSONArray partner = field.optJSONArray(fieldStockPicking.get(5));
                        if (partner!=null) {
                            int partnerId = partner.optInt(0);
                            String partnerName = partner.optString(1);
                            partnerModel = new GenericModel(partnerId, partnerName);
                        }
                    }
                    String origin = field.optString(fieldStockPicking.get(6), "");
                    stockPickings.add( new StockPicking(id, scheduledDate, state, name, xNotes, partnerModel, origin));

                    // Additional fields for product template detail
                    if (field.has(fieldStockPicking.get(7))) {
                        JSONArray picking_type = field.optJSONArray(fieldStockPicking.get(7));
                        String picking_type_name = null;
                        if (picking_type!=null) {
                            picking_type_name = picking_type.optString(1, "");
                        }
                        stockPickings.get(j).setOperationTypes(picking_type_name);
                    }
                    if (field.has(fieldStockPicking.get(8))) {
                        JSONArray transporter = field.optJSONArray(fieldStockPicking.get(8));
                        String transporterName = null;
                        if (transporter!=null) {
                            transporterName = transporter.optString(1, "");
                        }
                        stockPickings.get(j).setTransporter(transporterName);
                    }
                    if (field.has(fieldStockPicking.get(9))) {
                        JSONArray intTransporter = field.optJSONArray(fieldStockPicking.get(9));
                        String intTransporterName = null;
                        if (intTransporter!=null) {
                            intTransporterName = intTransporter.optString(1, "");
                        }
                        stockPickings.get(j).setIntTransporter(intTransporterName);
                    }
                    if (field.has(fieldStockPicking.get(10))) {
                        String xVehicleNotes = field.optString(fieldStockPicking.get(10), "");
                        stockPickings.get(j).setXVehicleNotes(xVehicleNotes);
                    }
                    if (field.has(fieldStockPicking.get(11))) {
                        int priority = field.optInt(fieldStockPicking.get(11), 1);
                        stockPickings.get(j).setPriority(priority);
                    }
                    if (field.has(fieldStockPicking.get(12))) {
                        JSONArray location = field.optJSONArray(fieldStockPicking.get(12));
                        String locationName = null;
                        if (location!=null) {
                            locationName = location.optString(1, "");
                        }
                        stockPickings.get(j).setLocation(locationName);
                    }
                    if (field.has(fieldStockPicking.get(13))) {
                        JSONArray destLocation = field.optJSONArray(fieldStockPicking.get(13));
                        String destLocationName = null;
                        if (destLocation!=null) {
                            destLocationName = destLocation.optString(1, "");
                        }
                        stockPickings.get(j).setDestLocation(destLocationName);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("StockPicking", "Problem parsing the JSON results", e);
            }
        }
        return stockPickings;
    }
}

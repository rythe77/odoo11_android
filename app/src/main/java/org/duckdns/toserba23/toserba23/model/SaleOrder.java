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
 * Created by ryanto on 22/02/18.
 */

public class SaleOrder {
    private int mId;
    private String mDateOrder;
    private String mState;
    private String mName;
    private double mAmountTotal;
    private GenericModel mPartner;
    private GenericModel mWarehouse;
    private String mPaymentTerm;
    private String mPricelist;
    private String mTransporter;
    private String mIntTransporter;
    private String mRequestedDate;
    private String mVehicleNotes;
    private String mOtherNotes;
    private ArrayList<SaleOrderLine> mSaleOrderLines;

    /**
     * String mapping for sale order status
     */
    private static final HashMap<String, String> stateMap = createMap();
    private static HashMap<String, String> createMap()
    {
        HashMap<String, String> stateMap = new HashMap<String,String>();
        stateMap.put("draft", "Penawaran");
        stateMap.put("sent", "Penawaran Terkirim");
        stateMap.put("sale", "Order Penjualan");
        stateMap.put("done", "Selesai");
        stateMap.put("cancel", "Batal");
        return stateMap;
    }

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getDateOrder() { return mDateOrder; }
    public String getState() { return mState; }
    public String getStateName() { return (mState!=null && !mState.isEmpty())?stateMap.get(mState):null; }
    public String getName() { return mName; }
    public double getAmountTotal() { return mAmountTotal; }
    public void setPartner(GenericModel partner) { mPartner = partner; }
    public GenericModel getPartner() {
        return mPartner;
    }
    public int getPartnerId() {return mPartner !=null? mPartner.getId():0;}
    public void setWarehouse(GenericModel warehouse) { mWarehouse = warehouse; }
    public GenericModel getWarehouse() {
        return mWarehouse;
    }
    public int getWarehouseId() {return mWarehouse !=null? mWarehouse.getId():0;}
    public void setPaymentTerm(String paymentTerm) { mPaymentTerm = paymentTerm; }
    public String getPaymentTerm() { return mPaymentTerm; }
    public void setPricelist(String pricelist) { mPricelist = pricelist; }
    public String getPricelist() { return mPricelist; }
    public void setTransporter(String transporter) { mTransporter = transporter; }
    public String getTransporter() { return mTransporter; }
    public void setIntTransporter(String intTransporter) { mIntTransporter = intTransporter; }
    public String getIntTransporter() { return mIntTransporter; }
    public void setRequestedDate(String reqDate) { mRequestedDate = reqDate; }
    public String getRequestedDate() { return mRequestedDate; }
    public void setVehicleNotes(String vehicleNotes) { mVehicleNotes = vehicleNotes; }
    public String getVehicleNotes() { return mVehicleNotes; }
    public void setOtherNotes(String otherNotes) { mOtherNotes = otherNotes; }
    public String getOtherNotes() { return mOtherNotes; }

    /**
     * Series of set and get method for linked fields
     * @return
     */
    public void setSaleOrderLine(ArrayList<SaleOrderLine> saleOrderLines) {mSaleOrderLines = saleOrderLines;}
    public ArrayList<SaleOrderLine> getSaleOrderLines() { return mSaleOrderLines; }

    /**
     * Main constructor of this class
     */
    public SaleOrder(int id, String dateOrder, String state, String name, double amountTotal, GenericModel partner, GenericModel warehouse) {
        mId = id;
        mDateOrder = dateOrder;
        mState = state;
        mName = name;
        mAmountTotal = amountTotal;
        mPartner = partner;
        mWarehouse = warehouse;
    }

    /**
     * Constructor for writing data to server
     */
    public SaleOrder(int id, GenericModel partner, GenericModel warehouse) {
        mId = id;
        mPartner = partner;
        mWarehouse = warehouse;
    }

    /**
     * hashmap of the fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getSaleOrderBulkFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "date_order",
                "state",
                "name",
                "amount_total",
                "partner_id",
                "warehouse_id"
        ));
        return map;
    }

    /**
     * hashmap of detail fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getSaleOrderDetailFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "date_order",
                "state",
                "name",
                "amount_total",
                "partner_id",
                "warehouse_id",
                "payment_term_id",
                "pricelist_id",
                "transporter_id",
                "int_transporter_id",
                "requested_date",
                "x_vehicle_notes",
                "x_notes"
        ));
        return map;
    }

    /**
     * hashmap of sale order fields
     * @return
     */
    public static HashMap<String,Arrays> getSaleOrderFieldsToSave() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "partner_id",
                "warehouse_id"
        ));
        return map;
    }

    /**
     * Pack data to be saved to server in a hashmap
     * @return
     */
    // create hashmap data pack to be sent to Odoo server
    public HashMap getHashmap() {
        HashMap fieldsArray = getSaleOrderFieldsToSave();
        List<String> fields = (List<String>) fieldsArray.get("fields");

        HashMap map = new HashMap<>();
        map.put(fields.get(0), getId()!=0?getId():"");
        map.put(fields.get(1), getPartnerId()!=0?getPartnerId():"");
        map.put(fields.get(2), getWarehouseId()!=0?getWarehouseId():"");
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<SaleOrder> parseJson(String jsonResponse) {
        ArrayList<SaleOrder> saleOrders = new ArrayList<>();

        List<String> fieldSaleOrder = (List<String>) getSaleOrderDetailFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldSaleOrder.get(0), 0);
                    String dateOrder = field.optString(fieldSaleOrder.get(1));
                    String state = field.optString(fieldSaleOrder.get(2));
                    String name = field.optString(fieldSaleOrder.get(3));
                    double amountTotal = field.optDouble(fieldSaleOrder.get(4), 0);
                    GenericModel partnerModel = null;
                    if (field.has(fieldSaleOrder.get(5))) {
                        JSONArray partner = field.optJSONArray(fieldSaleOrder.get(5));
                        if (partner!=null) {
                            int partnerId = partner.optInt(0);
                            String partnerName = partner.optString(1);
                            partnerModel = new GenericModel(partnerId, partnerName);
                        }
                    }
                    GenericModel warehouseModel = null;
                    if (field.has(fieldSaleOrder.get(6))) {
                        JSONArray warehouse = field.optJSONArray(fieldSaleOrder.get(6));
                        if (warehouse!=null) {
                            int warehouseId = warehouse.optInt(0);
                            String warehouseName = warehouse.optString(1);
                            warehouseModel = new GenericModel(warehouseId, warehouseName);
                        }
                    }
                    saleOrders.add( new SaleOrder(id, dateOrder, state, name, amountTotal, partnerModel, warehouseModel));

                    // Additional fields for product template detail
                    if (field.has(fieldSaleOrder.get(7))) {
                        JSONArray paymentTerm = field.optJSONArray(fieldSaleOrder.get(7));
                        String paymentTermName = null;
                        if (paymentTerm!=null) {
                            paymentTermName = paymentTerm.optString(1, "");
                        }
                        saleOrders.get(j).setPaymentTerm(paymentTermName);
                    }
                    if (field.has(fieldSaleOrder.get(8))) {
                        JSONArray pricelist = field.optJSONArray(fieldSaleOrder.get(8));
                        String pricelistName = null;
                        if (pricelist!=null) {
                            pricelistName = pricelist.optString(1, "");
                        }
                        saleOrders.get(j).setPricelist(pricelistName);
                    }
                    if (field.has(fieldSaleOrder.get(9))) {
                        JSONArray transporter = field.optJSONArray(fieldSaleOrder.get(9));
                        String transporterName = null;
                        if (transporter!=null) {
                            transporterName = transporter.optString(1, "");
                        }
                        saleOrders.get(j).setTransporter(transporterName);
                    }
                    if (field.has(fieldSaleOrder.get(10))) {
                        JSONArray intTransporter = field.optJSONArray(fieldSaleOrder.get(10));
                        String intTransporterName = null;
                        if (intTransporter!=null) {
                            intTransporterName = intTransporter.optString(1, "");
                        }
                        saleOrders.get(j).setIntTransporter(intTransporterName);
                    }
                    if (field.has(fieldSaleOrder.get(11))) {
                        String reqDate = field.optString(fieldSaleOrder.get(11), "");
                        saleOrders.get(j).setRequestedDate(reqDate);
                    }
                    if (field.has(fieldSaleOrder.get(12))) {
                        String vehicleNotes = field.optString(fieldSaleOrder.get(12), "");
                        saleOrders.get(j).setVehicleNotes(vehicleNotes);
                    }
                    if (field.has(fieldSaleOrder.get(13))) {
                        String otherNotes = field.optString(fieldSaleOrder.get(13), "");
                        saleOrders.get(j).setOtherNotes(otherNotes);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("SaleOrder", "Problem parsing the JSON results", e);
            }
        }
        return saleOrders;
    }
}

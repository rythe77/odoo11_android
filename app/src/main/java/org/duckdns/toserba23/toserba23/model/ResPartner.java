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
 * Created by ryanto on 30/05/19.
 */

public class ResPartner {
    private int mId;
    private String mName;
    private String mRef;
    private String mCity;
    private String mState;
    private String mDefPricelist;
    private String mPaymentTerm;
    private String mStreet;
    private String mStreet2;
    private String mPhone;
    private String mMobile;
    private String mEmail;
    private String mComment;
    private String mTrust;
    private double mCredit;
    private double mCreditLimit;

    /**
     * String mapping for partner trust
     */
    private static final HashMap<String, String> trustMap = createMap();
    private static HashMap<String, String> createMap()
    {
        HashMap<String, String> trustMap = new HashMap<String,String>();
        trustMap.put("good", "Debitur Baik");
        trustMap.put("normal", "Debitur Biasa");
        trustMap.put("bad", "Debitur Buruk");
        return trustMap;
    }

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getName() { return mName; }
    public String getRef() { return mRef; }
    public String getCity() { return mCity; }
    public String getState() { return mState; }
    public String getDefPricelist() {
        return mDefPricelist;
    }
    public String getPaymentTerm() {
        return mPaymentTerm;
    }
    public void setStreet(String street) { mStreet = street; }
    public String getStreet() { return mStreet; }
    public void setStreet2(String street2) { mStreet2 = street2; }
    public String getStreet2() { return mStreet2; }
    public void setPhone(String phone) { mPhone = phone; }
    public String getPhone() { return mPhone; }
    public void setMobile(String mobile) { mMobile = mobile; }
    public String getMobile() { return mMobile; }
    public void setEmail(String email) { mEmail = email; }
    public String getEmail() { return mEmail; }
    public void setComment(String comment) { mComment = comment; }
    public String getComment() { return mComment; }
    public void setTrust(String trust) { mTrust = trust; }
    public String getTrust() { return mTrust; }
    public String getTrustName() { return (mTrust!=null && !mTrust.isEmpty())?trustMap.get(mTrust):null; }
    public void setCredit(double credit) { mCredit = credit; }
    public double getCredit() {return mCredit; }
    public void setCreditLimit(double creditLimit) { mCreditLimit = creditLimit; }
    public double getCreditLimit() {return mCreditLimit; }

    /**
     * Main constructor of this class
     */
    public ResPartner(int id, String name, String ref, String city, String state, String defPricelist, String paymentTerm) {
        mId = id;
        mName = name;
        mRef = ref;
        mCity = city;
        mState = state;
        mDefPricelist = defPricelist;
        mPaymentTerm = paymentTerm;
    }

    /**
     * hashmap of the fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getResPartnerBulkFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "ref",
                "city",
                "state_id",
                "property_product_pricelist",
                "property_payment_term_id"
        ));
        return map;
    }

    /**
     * hashmap of detail fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getResPartnerDetailFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "name",
                "ref",
                "city",
                "state_id",
                "property_product_pricelist",
                "property_payment_term_id",
                "street",
                "street2",
                "phone",
                "mobile",
                "email",
                "comment",
                "trust",
                "credit",
                "credit_limit"
        ));
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of ResPartner object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<ResPartner> parseJson(String jsonResponse) {
        ArrayList<ResPartner> resPartners = new ArrayList<>();

        List<String> fieldResPartner = (List<String>) getResPartnerDetailFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldResPartner.get(0), 0);
                    String name = field.optString(fieldResPartner.get(1), "");
                    String ref = field.optString(fieldResPartner.get(2), "");
                    String city = field.optString(fieldResPartner.get(3), "");
                    String state = "";
                    JSONArray stateModel = field.optJSONArray(fieldResPartner.get(4));
                    if (stateModel!=null) {
                        state = stateModel.optString(1, "");
                    }
                    String defPricelist = "";
                    JSONArray defPricelistModel = field.optJSONArray(fieldResPartner.get(5));
                    if (defPricelistModel!=null) {
                        defPricelist = defPricelistModel.optString(1, "");
                    }
                    String paymentTerm = "";
                    JSONArray paymentTermModel = field.optJSONArray(fieldResPartner.get(6));
                    if (paymentTermModel!=null) {
                        paymentTerm = paymentTermModel.optString(1, "");
                    }
                    resPartners.add( new ResPartner(id, name, ref, city, state, defPricelist, paymentTerm));

                    // Additional fields for res partner detail
                    if (field.has(fieldResPartner.get(7))) {
                        String street = field.optString(fieldResPartner.get(7), "");
                        resPartners.get(j).setStreet(street);
                    }
                    if (field.has(fieldResPartner.get(8))) {
                        String street2 = field.optString(fieldResPartner.get(8), "");
                        resPartners.get(j).setStreet2(street2);
                    }
                    if (field.has(fieldResPartner.get(9))) {
                        String phone = field.optString(fieldResPartner.get(9), "");
                        resPartners.get(j).setPhone(phone);
                    }
                    if (field.has(fieldResPartner.get(10))) {
                        String mobile = field.optString(fieldResPartner.get(10), "");
                        resPartners.get(j).setMobile(mobile);
                    }
                    if (field.has(fieldResPartner.get(11))) {
                        String email = field.optString(fieldResPartner.get(11), "");
                        resPartners.get(j).setEmail(email);
                    }
                    if (field.has(fieldResPartner.get(12))) {
                        String comment = field.optString(fieldResPartner.get(12), "");
                        resPartners.get(j).setComment(comment);
                    }
                    if (field.has(fieldResPartner.get(13))) {
                        String trust = field.optString(fieldResPartner.get(13), "");
                        resPartners.get(j).setTrust(trust);
                    }
                    if (field.has(fieldResPartner.get(14))) {
                        double credit = field.optInt(fieldResPartner.get(14), 0);
                        resPartners.get(j).setCredit(credit);
                    }
                    if (field.has(fieldResPartner.get(15))) {
                        double creditLimit = field.optInt(fieldResPartner.get(15), 0);
                        resPartners.get(j).setCreditLimit(creditLimit);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("ResPartner", "Problem parsing the JSON results", e);
            }
        }
        return resPartners;
    }
}

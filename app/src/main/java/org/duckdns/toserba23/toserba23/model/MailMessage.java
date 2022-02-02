package org.duckdns.toserba23.toserba23.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MailMessage {

    private int mId;
    private String mDate;
    private String mFrom;
    private String mModel;
    private int mModelId;
    private String mType;
    private int mSubType;
    private String mBody;

    /**
     * Series of get method
     */
    public int getId() { return mId; }
    public String getDate() { return mDate; }
    public String getFrom() { return mFrom; }
    public String getModel() { return mModel; }
    public int getModelId() { return mModelId; }
    public String getType() { return mType; }
    public int getSubType() { return mSubType; }
    public String getBody() { return mBody; }

    /**
     * Main constructor of this class
     */
    public MailMessage(int id, String date, String from, String body) {
        mId = id;
        mDate = date;
        mFrom = from;
        mBody = body;
    }

    /**
     * Constructor for creating new mail message
     */
    public MailMessage(String model, int modelId, String type, int subType, String body) {
        mModel = model;
        mModelId = modelId;
        mType = type;
        mSubType = subType;
        mBody = body;
    }

    /**
     * hashmap of mail message fields
     */
    public static HashMap<String, Arrays> getMailMessageFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "id",
                "date",
                "email_from",
                "body"
        ));
        return map;
    }

    /**
     * hashmap of mail message fields to create new
     */
    public static HashMap<String, Arrays> getMailMessageFieldsToCreate() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList(
                "model",
                "res_id",
                "message_type",
                "subtype_id",
                "body"
        ));
        return map;
    }

    /**
     * Pack data to be saved to server in a hashmap
     * @return
     */
    // create hashmap data pack to be sent to Odoo server
    public HashMap getHashmapToCreate() {
        HashMap fieldsArray = getMailMessageFieldsToCreate();
        List<String> fields = (List<String>) fieldsArray.get("fields");

        HashMap map = new HashMap<>();
        map.put(fields.get(0), !getModel().isEmpty()?getModel():"");
        map.put(fields.get(1), getModelId()!=0?getModelId():"");
        map.put(fields.get(2), !getType().isEmpty()?getType():"");
        map.put(fields.get(3), getSubType()!=0?getSubType():"");
        map.put(fields.get(4), !getBody().isEmpty()?getBody():"");
        return map;
    }

    /**
     * Parse jsonresponse from Odoo server and return it as ArrayList of MailMessage object
     * @param jsonResponse
     * @return
     */
    public static ArrayList<MailMessage> parseJson (String jsonResponse) {
        ArrayList<MailMessage> mailMessages = new ArrayList<>();

        List<String> fieldMailMessages = (List<String>) getMailMessageFields().get("fields");
        if (jsonResponse!=null) {
            try {
                JSONArray fields = new JSONArray(jsonResponse);
                for (int j = 0; j < fields.length(); j++) {
                    JSONObject field = fields.optJSONObject(j);

                    int id = field.optInt(fieldMailMessages.get(0), 0);
                    String date = field.optString(fieldMailMessages.get(1));
                    String from = field.optString(fieldMailMessages.get(2));
                    String body = field.optString(fieldMailMessages.get(3));
                    mailMessages.add( new MailMessage(id, date, from, body));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MailMessage", "Problem parsing the JSON results", e);
            }
        }
        return mailMessages;
    }
}

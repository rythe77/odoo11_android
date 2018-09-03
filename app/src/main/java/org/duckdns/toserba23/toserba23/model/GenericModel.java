package org.duckdns.toserba23.toserba23.model;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by ryanto on 24/02/18.
 */

public class GenericModel {

    private int mId;
    private String mName;

    /**
     * Series of get method
     * @return
     */
    public int getId() { return mId; }
    public String getName() { return mName;}

    /**
     * Main constructor of this class
     */
    public GenericModel(int id, String name) {
        mId = id;
        mName = name;
    }

    /**
     * hashmap of res partner fields
     * should be used to limit returned fields when querying Odoo server
     * @return
     */
    public static HashMap<String,Arrays> getProductCategoryBulkFields() {
        HashMap map = new HashMap();
        map.put("fields", Arrays.asList("id", "name"));
        return map;
    }
}

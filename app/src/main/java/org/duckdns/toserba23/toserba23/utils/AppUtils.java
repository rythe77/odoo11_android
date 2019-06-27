package org.duckdns.toserba23.toserba23.utils;

import android.app.Activity;
import android.content.pm.PackageManager;

public class AppUtils {
    /**
     * Create a private constructor because no one should ever create a {@link AppUtils} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name AppUtils (and an object instance of AppUtils is not needed).
     */
    private AppUtils() {
    }

    public static boolean isAppInstalled(Activity activity, String uri) {
        PackageManager pm = activity.getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        }
        catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }
}

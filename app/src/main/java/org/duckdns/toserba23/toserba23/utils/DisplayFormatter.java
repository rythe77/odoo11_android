package org.duckdns.toserba23.toserba23.utils;

/**
 * Created by ryanto on 22/02/18.
 */

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.DatePicker;
import android.widget.TimePicker;

//import org.duckdns.toserba23.toserba23.R;

import org.duckdns.toserba23.toserba23.R;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Helper methods to display data in a desirable format.
 * Methods return string to be displayed to the textview
 */
public final class DisplayFormatter {
    private static final String LOG_TAG = Context.class.getName();

    /**
     * Create a private constructor because no one should ever create a {@link DisplayFormatter} object.
     * This class is only meant to hold static variables and methods, which can be accessed
     * directly from the class name QueryUtils (and an object instance of QueryUtils is not needed).
     */
    private DisplayFormatter() {
    }

    /**
     * Return the formatted date string (i.e. "3 Mar 1984, 17:30") from a Date object.
     * If Date object is null, return empty string
     */
    public static String formatDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        if (dateString!="false") {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (date!=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd LLL yyyy");
            return dateFormat.format(date);
        } else {
            return "";
        }
    }
    public static String formatDateTime(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        if (dateString!="false") {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (date!=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd LLL yyyy HH:mm");
            return dateFormat.format(date);
        } else {
            return "";
        }
    }
    public static Date parseDate(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        if (dateString!="false") {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
    public static Date parseDateTime(String dateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date date = null;
        if (dateString!="false") {
            try {
                date = sdf.parse(dateString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date;
    }
    public static int getYear(Date dateObject) {
        if (dateObject!=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
            return Integer.parseInt(dateFormat.format(dateObject));
        } else {
            return 0;
        }
    }
    public static int getMonth(Date dateObject) {
        if (dateObject!=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM");
            return Integer.parseInt(dateFormat.format(dateObject));
        } else {
            return 0;
        }
    }
    public static int getDate(Date dateObject) {
        if (dateObject!=null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd");
            return Integer.parseInt(dateFormat.format(dateObject));
        } else {
            return 0;
        }
    }
    public static String getDateTimeFromPicker(DatePicker datePicker, TimePicker timePicker) {
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year =  datePicker.getYear();
        int hour = timePicker.getCurrentHour();
        int minute = timePicker.getCurrentMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, 0);

        Date tes = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

        return sdf.format(calendar.getTime());
    }

    /**
     * Return the formatted quantity decimal format.
     * Should display 1 decimal digit only if it's not zero
     * Use Indonesian style number formatting with comma as decimal separator
     * and dot as grouping separator
     */
    public static String formatQuantity(double qty) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMAN);
        formatter.setMinimumIntegerDigits(1);
        if((int)qty != qty) {
            formatter.setMinimumFractionDigits(1);
        }
        return formatter.format(qty);
    }

    /**
     * Return the formatted currency with currency symbol for Indonesian Rupiah.
     * Use Indonesian style number formatting with comma as decimal separator
     * and dot as grouping separator
     */
    public static String formatCurrency(double price) {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.GERMAN);
        formatter.setMinimumIntegerDigits(1);
        return "Rp " + formatter.format(price);
    }

    /**
     * General string formatter.
     * Return empty string if null
     */
    public static String formatString(String string) {
        if (string == null || string.contains("false")) {
            return "";
        } else {
            return string;
        }
    }

    /**
     * Return the formatted discount on sale order line.
     */
    public static String formatDiscountLine(int qty) {
        if(qty == 0) {
            return "";
        } else {
            return "( Discount " + String.valueOf(qty) + "% ) ";
        }
    }

    /**
     * Return the formatted product description on sale order line.
     */
    public static String formatProductDesc(String productDesc) {
        if (productDesc == null || productDesc.contains("false") || TextUtils.isEmpty(productDesc)) {
            return "";
        } else {
            return "( " + productDesc + " ) ";
        }
    }

    /**
     * Return only product code from the full product name
     * If code is not detected, return empty string
     */
    public static String splitProductCode(String productFullName) {
        String productCode;
        if (productFullName.contains("]")) {
            productCode = productFullName.split("]")[0].split("\\[")[1].trim();
        } else {
            productCode = "";
        }
        return productCode;
    }

    /**
     * Truncate product code from the full product name
     * If code is not detected, return full product name
     */
    public static String splitProductName(String productFullName) {
        String productName;
        if (productFullName.contains("]")) {
            productName = productFullName.split("]")[1].trim();
        } else {
            productName = productFullName;
        }
        return productName;
    }

    /**
     * Return the color ID matching the stock picking status. Use
     * ContextCompat.getColor(getActivity(), stateColorResourceId)
     * on the returned result to get the color ID
     */
    public static int getStockPickingStateColor (String state) {
        int stateColorResourceId;
        switch (state) {
            case "draft":
                stateColorResourceId = R.color.state_draft;
                break;
            case "waiting":
                stateColorResourceId = R.color.state_waiting;
                break;
            case "confirmed":
                stateColorResourceId = R.color.state_confirmed;
                break;
            case "partially_available":
                stateColorResourceId = R.color.state_partially_available;
                break;
            case "assigned":
                stateColorResourceId = R.color.state_assigned;
                break;
            case "waiting_validation":
                stateColorResourceId = R.color.state_waiting_validation;
                break;
            case "done":
                stateColorResourceId = R.color.state_done;
                break;
            default:
                stateColorResourceId = R.color.state_draft;
                break;
        }
        return stateColorResourceId;
    }

    /**
     * Return the color ID matching the stock picking status. Use
     * ContextCompat.getColor(getActivity(), stateColorResourceId)
     * on the returned result to get the color ID
     */
    public static int getAttendanceStateColor (String state) {
        int stateColorResourceId;
        switch (state) {
            case "checked_in":
                stateColorResourceId = R.color.state_checked_in;
                break;
            case "checked_out":
                stateColorResourceId = R.color.state_checked_out;
                break;
            default:
                stateColorResourceId = R.color.state_checked_out;
                break;
        }
        return stateColorResourceId;
    }
}

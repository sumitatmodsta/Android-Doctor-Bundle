package com.modastadoc.doctors.common.utils;

import android.util.Log;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by kunasi on 10/08/17.
 */

public class DateUtil {

    /**
     * Today date.
     *
     * @return String calendar today date in the format of 'dd/MM/yyyy'.
     */
    public static String todayDate() {
        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        return df.format(c.getTime());
    }

    public static String convertToLocalTime(String servertime) {
        try {
            DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");


            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date parsedDateFormat = inputFormat.parse(servertime);

            DateFormat timeFormat = new SimpleDateFormat("hh:mma");
            String convertedTime = timeFormat.format(parsedDateFormat);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String convertedDate = dateFormat.format(parsedDateFormat);

            DateFormat dateFormatDetail = new SimpleDateFormat("MMM dd, yyyy");
            String convertedDateDetail = dateFormatDetail.format(parsedDateFormat);

            long daysDifference = daysBetween(parsedDateFormat, new Date());
            Log.e("DATE_CONVERTION_", "" + convertedDate + " " + convertedTime + " " + daysDifference);

            JSONObject dateInfo = new JSONObject();
            dateInfo.put("TIME", convertedTime);
            dateInfo.put("DAYDIFFERENCE", daysDifference);
            dateInfo.put("DATE", convertedDate);
            dateInfo.put("DATE_DETAIL", convertedDateDetail);

            long diff = new Date().getTime() - parsedDateFormat.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            dateInfo.put("HOURDIFFERENCE", hours);
            dateInfo.put("MINUTEDIFFERENCE", minutes);
            dateInfo.put("SECONDSDIFFERENCE", seconds);

            return dateInfo.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return servertime;
        }
    }


    private static long daysBetween(Date startDate, Date endDate) {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        long daysBetween = 0;
        while (sDate.before(eDate)) {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private static Calendar getDatePart(Date date) {
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    public static String parseSimpleDate( String input ) {
        try {
            java.util.Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(input);
            Calendar cal = Calendar.getInstance();       // get calendar instance

            DateFormat dateFormatDetail = new SimpleDateFormat("MMM dd, yyyy");
            String convertedDateDetail = dateFormatDetail.format(date);

            DateFormat timeFormat = new SimpleDateFormat("hh:mma");
            String convertedTime = timeFormat.format(date);

            return "last updated : " + convertedDateDetail + " " + convertedTime;
        } catch (Exception e ) {
            Log.e( "parseSimpleDate" , "" + e.toString() );
            return "";
        }

    }

    public static String get(String date, String format, String reqFormat) {
        try {
            DateFormat df = new SimpleDateFormat(format);
            Date d = df.parse(date);
            df = new SimpleDateFormat(reqFormat);
            return df.format(d);
        }catch (Exception e){
            e.printStackTrace();
        }
        return date;
    }
}

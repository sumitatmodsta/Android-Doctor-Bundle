package com.modastadoc.doctors.common.utils;

import android.util.Log;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import static org.apache.commons.lang3.StringEscapeUtils.unescapeJava;

/**
 * Created by vijay.hiremath on 12/11/16.
 */
public class UtilityMethods
{
    public static String convertToLocalTime(String servertime)
    {
        try
        {
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
        } catch (Exception e)
        {
            e.printStackTrace();
            return servertime;
        }
    }


    public static long daysBetween(Date startDate, Date endDate)
    {
        Calendar sDate = getDatePart(startDate);
        Calendar eDate = getDatePart(endDate);

        long daysBetween = 0;
        while (sDate.before(eDate))
        {
            sDate.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    public static Calendar getDatePart(Date date)
    {
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        cal.set(Calendar.MINUTE, 0);                 // set minute in hour
        cal.set(Calendar.SECOND, 0);                 // set second in minute
        cal.set(Calendar.MILLISECOND, 0);            // set millisecond in second

        return cal;                                  // return the date part
    }

    public static String parseSimpleDate( String input )
    {
        try
        {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(input);
            Calendar cal = Calendar.getInstance();       // get calendar instance

            DateFormat dateFormatDetail = new SimpleDateFormat("MMM dd, yyyy");
            String convertedDateDetail = dateFormatDetail.format(date);

            DateFormat timeFormat = new SimpleDateFormat("hh:mma");
            String convertedTime = timeFormat.format(date);

            return "last updated : " + convertedDateDetail + " " + convertedTime;
        }
        catch (Exception e )
        {
            Log.e( "parseSimpleDate" , "" + e.toString() );
            return "";
        }
    }

    public static String getLanguageFromLangaugeCode( String langageCode )
    {
        if( langageCode.equalsIgnoreCase("hi") )
        {
            return "Hindi";
        }
        else if( langageCode.equalsIgnoreCase("en") )
        {
            return "English";
        }
        else if( langageCode.equalsIgnoreCase("kn") )
        {
            return "Kannada";
        }
        else if( langageCode.equalsIgnoreCase("te") )
        {
            return "Telugu";
        }

        return "NA"; //default language
    }

    public static boolean checkIfQueryIsLanguagePreferred( String languageCode, String preferredLanguage )
    {
        String[] preferredLanguageArray = preferredLanguage.split(",");
        String queryLanguage = getLanguageFromLangaugeCode( languageCode );
        for( int i = 0 ; i < preferredLanguageArray.length; i++ )
        {
            if( preferredLanguageArray[i].equalsIgnoreCase(queryLanguage))
            {
                return true;
            }
        }

        return false;
    }


    public static ArrayList<String> fillHeightList()
    {
        final ArrayList<String> heightList = new ArrayList<String>();
        heightList.add("Select Height");
        heightList.add("1'  (30.5 cms)");
        heightList.add("1'1\"  (33.5 cms)");
        heightList.add("1'2\"  (36.6 cms)");
        heightList.add("1'3\"  (39.6 cms)");
        heightList.add("1'4\"  (42.7 cms)");
        heightList.add("1'5\"  (45.7 cms)");
        heightList.add("1'6\"  (48.8 cms)");
        heightList.add("1'7\"  (51.8 cms)");
        heightList.add("1'8\"  (54.9 cms)");
        heightList.add("1'9\"  (57.9 cms)");
        heightList.add("1'10\"  (55.88 cms)");
        heightList.add("1'11\"  (33.8 cms)");
        heightList.add("2'  (61 cms)");
        heightList.add("2'1\"  (64 cms)");
        heightList.add("2'2\"  (67.1 cms)");
        heightList.add("2'3\"  (70.1 cms)");
        heightList.add("2'4\"  (73.2 cms)");
        heightList.add("2'5\"  (76.2 cms)");
        heightList.add("2'6\"  (79.2 cms)");
        heightList.add("2'7\"  (82.3 cms)");
        heightList.add("2'8\"  (85.3 cms)");
        heightList.add("2'9\"  (88.4 cms)");
        heightList.add("2'10\"  (64 cms)");
        heightList.add("2'11\"  (64.3 cms)");
        heightList.add("3'  (91.4 cms)");
        heightList.add("3'1\"  (94.5 cms)");
        heightList.add("3'2\"  (97.5 cms)");
        heightList.add("3'3\"  (100.6 cms)");
        heightList.add("3'4\"  (103.6 cms)");
        heightList.add("3'5\"  (106.7 cms)");
        heightList.add("3'6\"  (109.7 cms)");
        heightList.add("3'7\"  (112.8 cms)");
        heightList.add("3'8\"  (115.8 cms)");
        heightList.add("3'9\"  (118.9 cms)");
        heightList.add("3'10\"  (94.5 cms)");
        heightList.add("3'11\"  (94.5 cms)");
        heightList.add("4'  (121.9 cms)");
        heightList.add("4'1\"  (125 cms)");
        heightList.add("4'2\"  (128 cms)");
        heightList.add("4'3\"  (131.1 cms)");
        heightList.add("4'4\"  (134.1 cms)");
        heightList.add("4'5\"  (137.2 cms)");
        heightList.add("4'6\"  (140.2 cms)");
        heightList.add("4'7\"  (143.3 cms)");
        heightList.add("4'8\"  (146.3 cms)");
        heightList.add("4'9\"  (149.4 cms)");
        heightList.add("4'10\"  (147 cms)");
        heightList.add("4'11\"  (150 cms)");
        heightList.add("5'  (152.5 cms)");
        heightList.add("5'1\"  (155 cms)");
        heightList.add("5'2\"  (157.5 cms)");
        heightList.add("5'3\"  (160 cms)");
        heightList.add("5'4\"  (162.5 cms)");
        heightList.add("5'5\"  (165 cms)");
        heightList.add("5'6\"  (167.5 cms)");
        heightList.add("5'7\"  (170 cms)");
        heightList.add("5'8\"  (172.5 cms)");
        heightList.add("5'9\"  (175 cms)");
        heightList.add("5'10\"  (177.5 cms)");
        heightList.add("5'11\"  (180 cms)");
        heightList.add("6'  (183 cms)");
        heightList.add("6'1\"  (185.5 cms)");
        heightList.add("6'2\"  (188 cms)");
        heightList.add("6'3\"  (190.5 cms)");
        heightList.add("6'4\"  (193.04 cms)");
        heightList.add("6'5\"  (195.57 cms)");
        heightList.add("6'6\"  (198.12 cms)");
        heightList.add("6'7\"  (200.66 cms)");
        heightList.add("6'8\"  (203.2 cms)");
        heightList.add("6'9\"  (205.74 cms)");
        heightList.add("6'10\"  (208.28 cms)");
        heightList.add("6'11\"  (210.82 cms)");
        heightList.add("7'1\"  (215.9 cms)");
        heightList.add("7'2\"  (218.44 cms)");
        heightList.add("7'3\"  (220.98 cms)");
        heightList.add("7'4\"  (223.52 cms)");
        heightList.add("7'5\"  (226.06 cms)");
        return heightList;
    }

    public static String removeEscapedCharacters( String params )
    {
        return unescapeJava(params);
    }

}

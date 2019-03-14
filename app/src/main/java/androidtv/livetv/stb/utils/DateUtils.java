package androidtv.livetv.stb.utils;


import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created by rupak on 31/10/2015.
 */
public class DateUtils {
    public static SimpleDateFormat daymonthFormat=new SimpleDateFormat("dd MMM, yyyy",Locale.US);
    public static SimpleDateFormat dateAndTime = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
    public static SimpleDateFormat _12HrsTimeFormat = new SimpleDateFormat("hh:mm",Locale.US);
    public static SimpleDateFormat _24HrsTimeFormat = new SimpleDateFormat("HH:mm",Locale.US);
    public static SimpleDateFormat epgDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss Z",Locale.US);
    public static SimpleDateFormat smalldateFormat = new SimpleDateFormat("E, MMM dd",Locale.US);
    public static SimpleDateFormat fullDayFormat =  new SimpleDateFormat("EEE", Locale.US);

    public static Date convertTimeTo24hrs(String time) throws ParseException {
        if(time.length() > 12 || time.length()< 12){
            Date date = _12HrsTimeFormat.parse(time);
            return date;
        }else {
            Date date = _24HrsTimeFormat.parse(time);
            return date;
        }
    }

    public static Date getCurrentTime() throws ParseException {
            return _24HrsTimeFormat.parse(_24HrsTimeFormat.format(new Date()));
    }
    public static Date convertStringToDate(String dateStr)throws ParseException {

        Date date = dateFormat.parse(dateStr);

        return date;
    }

    public static Date convertStringToTime(String startDate,String startTime) throws ParseException{
        return epgDateFormat.parse(startDate+" "+startTime);
    }
    public static Date convertStringToDateNew(String dateStr)throws ParseException {

        Date date = dateAndTime.parse(dateStr);

        return date;
    }
    public static Date addMinutesToDate(int minutes, Date beforeTime) throws ParseException {
        final long ONE_MINUTE_IN_MILLIS = 60000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = convertTimeTo24hrs(curTimeInMs + (minutes * ONE_MINUTE_IN_MILLIS)+"");
        return afterAddingMins;
    }
    public static String getDayInString(int day){

        switch (day){
            case Calendar.SUNDAY:
                return "Sunday";
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            default:
                return null;
        }
    }
    public static Calendar createCalendar(String date, String time) throws ParseException {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdfDate.parse(date+" "+time));
        return calendar;
    }


}

package androidtv.livetv.stb.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidtv.livetv.stb.entity.EpgItem;
import androidtv.livetv.stb.entity.Epgs;

public class DataUtils {
    public static List<Epgs> getEpgsListFrom(List<EpgItem> epgItemList, String channelId) {
        List<Epgs> epgsList = new ArrayList<>();
        if(epgItemList != null){
            for (EpgItem epgItem : epgItemList) {
                Epgs epgs = new Epgs();
                epgs.setId(String.valueOf(channelId) + epgItem.getStartTime());
                epgs.setDate(getParsedDate(epgItem.getStartTime()));
                epgs.setStartTime(getParsedDate(epgItem.getStartTime()));
                epgs.setEndTime(getParsedDate(epgItem.getEndTime()));
                epgs.setProgramTitle(epgItem.getProgramName());
                epgs.setChannelID(Integer.parseInt(channelId));
                epgs.setTimeZone(getTimeZone(epgItem.getStartTime()));
                epgsList.add(epgs);
            }
        }

        return epgsList;
    }

    public static String getTimeZone(String startTime) {
        if(startTime.contains(" +")) {
            String temp[] = startTime.split(" +");
            return temp[1];
        }else
            return "";

    }


    public static Date getParsedDate(String startTime) {
        Date d = null;
        try {
            d = DateUtils.convertStringToDate(startTime);
            System.out.print("date = "+d.toString());

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        return d;
    }

    public static String getPrgmTime(Date startTime , Date endDate){
        String start = DateUtils._24HrsTimeFormat.format(startTime);
        String end = DateUtils._24HrsTimeFormat.format(endDate);
        return start +" - "+ end;
    }

    public static List<Date> getParsedDateList(String startDate) {
        List<Date> dates = new ArrayList<>();
        Date start = null;
        try {
            start = DateUtils.convertStringToDateNew(startDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar startC = Calendar.getInstance();
        startC.setTime(start);
        int diffDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - startC.get(Calendar.DAY_OF_YEAR);
        for (int i = diffDay; i > 0; i--) {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_WEEK, -i);
            dates.add(calendar.getTime());
        }

        dates.add(Calendar.getInstance().getTime());
        return dates;


    }


}

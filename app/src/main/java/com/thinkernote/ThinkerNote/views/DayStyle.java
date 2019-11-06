package com.thinkernote.ThinkerNote.views;

import java.util.Calendar;

/**
 * 鏃ュ巻鎺т欢鏍峰紡缁樺埗绫?
 * @Description: 鏃ュ巻鎺т欢鏍峰紡缁樺埗绫?
 */
public class DayStyle {
    private final static String[] vecStrWeekDayNames = getWeekDayNames();

    private static String[] getWeekDayNames() {
        String[] vec = new String[10];

        vec[Calendar.SUNDAY] = "鍛ㄦ棩";
        vec[Calendar.MONDAY] = "鍛ㄤ竴";
        vec[Calendar.TUESDAY] = "鍛ㄤ簩";
        vec[Calendar.WEDNESDAY] = "鍛ㄤ笁";
        vec[Calendar.THURSDAY] = "鍛ㄥ洓";
        vec[Calendar.FRIDAY] = "鍛ㄤ簲";
        vec[Calendar.SATURDAY] = "鍛ㄥ叚";

        return vec;
    }

    public static String getWeekDayName(int iDay) {
        return vecStrWeekDayNames[iDay];
    }

    public static int getWeekDay(int index, int iFirstDayOfWeek) {
        int iWeekDay = -1;

        if (iFirstDayOfWeek == Calendar.MONDAY) {
            iWeekDay = index + Calendar.MONDAY;

            if (iWeekDay > Calendar.SATURDAY)
                iWeekDay = Calendar.SUNDAY;
        }

        if (iFirstDayOfWeek == Calendar.SUNDAY) {
            iWeekDay = index + Calendar.SUNDAY;
        }

        return iWeekDay;
    }
}

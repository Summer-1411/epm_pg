package com.fis.epm.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateTimeUtils {
	public final static String DD_MM_YYYY_HH24_MI_SS = "dd/MM/yyyy HH:mm:ss";
	public final static String DD_MM_YYYY_HH12_MI_SS = "dd/MM/yyyy hh:mm:ss";
	public final static String DD_MM_YYYY = "dd/MM/yyyy";
	public final static String MM_DD_YYYY = "MM/dd/yyyy";
	public final static String MM_DD_YYYY_S = "MM-dd-yyyy";
	public final static String YYYY_MM_DD = "yyyy-MM-dd";

	public static String convertDateToString(Date dtValue, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.format(dtValue);
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date convertStringToDate(String sDateValue, String pattern) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.parse(sDateValue);
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date convertLongTimestampToDate(long timestamp) {
		try {
			return new Date(timestamp);
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date addDayToDate(Date dtValue, int day) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.add(day, Calendar.DAY_OF_MONTH);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date addMonthToDate(Date dtValue, int month) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.add(month, Calendar.MONTH);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date addYearToDate(Date dtValue, int year) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.add(Calendar.YEAR, year);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date addHourToDate(Date dtValue, int hour) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.add(Calendar.HOUR_OF_DAY, hour);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date addMinuteToDate(Date dtValue, int minute) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.add(Calendar.MINUTE, minute);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}

	public static Date setTimeToDate(Date dtValue, int calendarType, int timeSet) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dtValue);
			cal.set(calendarType, timeSet);
			return cal.getTime();
		} catch (Exception exp) {
			return null;
		}
	}
}

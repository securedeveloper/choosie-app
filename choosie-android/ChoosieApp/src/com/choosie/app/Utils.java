package com.choosie.app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	private static Utils s_instance;
	
	private Utils() {
	}
	
	public static Utils getInstance() {
		if (s_instance == null)
			s_instance = new Utils();
		return s_instance;
	}
	
	public Date ConvertStringToDate(String str_date) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = new Date();
		
		try {
			date = df.parse(str_date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return date;
	}

}

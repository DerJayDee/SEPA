package de.dlrg_rodenkirchen.sepa.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class Check {

	public static final boolean wrongDate(String datum) {
		SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date execDate = sdfToDate.parse(datum);
			Date now = new Date();
			if (now.after(execDate)) {
				return true;
			}
		} catch (Exception e1) {
			return true;
		}
		return false;
	}

	public static final boolean notInt(String number) {
		return !number.matches("[0-9]+");
	}
	
	public static final boolean arrayContainsString(String[] array, String s){
		boolean contains = false;
		for(String s1 : array){
			if(s1.equals(s)){
				contains = true;
			}
		}
		return contains;
	}

}

package com.fis.epm.dao;

import java.util.Map;
import java.util.Vector;

public interface EPMSearchDao {
	public Map getInfoDebit(String misdn) throws Exception;

	public Map callGetInfo112(String isdn, String xMucDuLieu, String yThangCuoc) throws Exception;

	public Map callGetInfo112Sum(String isdn) throws Exception;
	
	public Vector getMonthPayment(String isdn) throws Exception;
	
	public String checkSubType(String isdn) throws Exception;
	
	public String getLanguage(String isdn) throws Exception;
	
	public String getPromotionOCS() throws Exception;
}

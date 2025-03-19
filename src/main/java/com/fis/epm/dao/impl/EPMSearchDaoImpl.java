package com.fis.epm.dao.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.sql.DataSource;

import com.fis.fw.common.utils.AlertSystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.fis.epm.dao.EPMSearchDao;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

@Repository
public class EPMSearchDaoImpl implements EPMSearchDao {

	private static final Logger log = LoggerFactory.getLogger(EPMSearchDaoImpl.class);

	@Autowired
	private final DataSource dataSource = null;

	@Value("${com.fis.pg.url-alert}")
	private String urlAlert;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${com.fis.ip.server}")
	private String ipServer;

	@Override
	public Map getInfoDebit(String misdn) throws Exception {
		log.info("Param ==> isdn: " + misdn);
		Map mapReturn = new HashMap();
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		String sqlDebit = "DECLARE BEGIN ? :=  USSD_INFO.get_bill_info (?); END;";

		// DECLARE
		Connection conn = null;
		CallableStatement cs = null;

		Double debit = 0D;
		Double debitPreviousPeriod = 0D;

		if (misdn.startsWith("84") && misdn.length() > 10) {
			misdn = misdn.substring(2);
		}

		// lay thong tin no truoc + cuoc phat sinh tinh den thoi diem hien tai
		try {
			conn = dataSource.getConnection();
			cs = conn.prepareCall(sqlDebit);
			cs.registerOutParameter(1, Types.VARCHAR);
			cs.setString(2, misdn + "||");
			cs.execute();
			String resultQueryDebit = StringUtils.nvl(cs.getString(1), "");
			if (!"".equals(resultQueryDebit)) {
				log.info("Query debit from db success:" + resultQueryDebit);
				List<String> listSpilitResultQueryDebit = Arrays.asList(resultQueryDebit.split("\\|"));
				//debitPreviousPeriod = Double.valueOf(listSpilitResultQueryDebit.get(2));
				if(debitPreviousPeriod == 0) {
					debitPreviousPeriod = Double.valueOf(listSpilitResultQueryDebit.get(5));
				}
				debit = Double.valueOf(listSpilitResultQueryDebit.get(3));
				mapReturn.put("debitPreviousPeriod", formatter.format(debitPreviousPeriod));
				mapReturn.put("debit", formatter.format(debit));
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			log.error("DAO getInfoDebit: " + e.getMessage(), e);
			throw new Exception("DAO getInfoDebit:" + e);
		} finally {
			Tools.closeObject(cs);
			Tools.closeObject(conn);
		}

		return mapReturn;
	}

	@Override
	public Map callGetInfo112(String isdn, String xMucDuLieu, String yThangCuoc) throws Exception {
		// TODO Auto-generated method stub
		log.info("Param ==> isdn: " + isdn);
		log.info("Param ==> xMucDuLieu: " + xMucDuLieu);
		log.info("Param ==> yThangCuoc: " + yThangCuoc);
		Map mapReturn = new HashMap();
		if (isdn.startsWith("84") && isdn.length() > 10) {
			isdn = isdn.substring(2);
		}
		CallableStatement callableStatement = null;
		Connection conn = null;

		try {
			String sql = "DECLARE BEGIN ? :=  USSD_INFO.get_info_112 (?, ? ,?); END;";
			conn = dataSource.getConnection();
			callableStatement = conn.prepareCall(sql);

			callableStatement.setString(2, isdn);
			callableStatement.setLong(3, Long.valueOf(xMucDuLieu));
			callableStatement.setLong(4, Long.valueOf(yThangCuoc));
			callableStatement.registerOutParameter(1, OracleTypes.VARCHAR);
			callableStatement.execute();

			// Get output
			String resultMessage = StringUtils.nvl(callableStatement.getString(1), "");
			mapReturn.put("message", resultMessage);

			// resultSet.close();

			return mapReturn;
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("error getInfo112:" + exp.getMessage());
			throw new Exception("error getInfo112:" + exp.getMessage());
		} finally {
			Tools.closeDatabaseObject(callableStatement);
			Tools.closeDatabaseObject(conn);
		}
	}

	@Override
	public Map callGetInfo112Sum(String isdn) throws Exception {
		if (isdn.startsWith("84") && isdn.length() > 10) {
			isdn = isdn.substring(2);
		}
		log.info("Param ==> isdn: " + isdn);

		Connection conn = null;
		CallableStatement cs = null;

		DecimalFormat formatter = new DecimalFormat("###,###,###");

		Map mapReturn = new HashMap();
		String sql = "DECLARE BEGIN ? :=  USSD_INFO.get_info_112_sum (?); END;";

		try {
			conn = dataSource.getConnection();

			// lay thong tin KM
			cs = conn.prepareCall(sql);
			cs.setString(2, isdn);
			cs.registerOutParameter(1, OracleTypes.VARCHAR);
			cs.execute();
			// Get output
			String resultMessage = cs.getString(1);
			log.info("callGetInfo112Sum resultMessage : " + resultMessage);
			List<String> promotionResult = Arrays.asList(resultMessage.split("\\@"));

			List<String> promotionArray = Arrays.asList(promotionResult.get(1).split("\\|"));

			//String promotion = formatter.format(Double.valueOf(StringUtils.nvl(promotionArray.get(1), "0")));

			//mapReturn.put("debitInfo", formatter.format(Double.valueOf(promotionResult.get(0).toString())));
			mapReturn.put("debitInfo", promotionResult.get(0).toString());
			mapReturn.put("promotion", formatter.format(Double.valueOf(promotionArray.get(0).toString())));

		} catch (Exception ex) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + ex.getMessage());
			log.error(ex.getMessage(), ex);
			throw new Exception("error callGetInfo112Sum:" + ex.getMessage());
		} finally {
			Tools.closeDatabaseObject(cs);
			Tools.closeDatabaseObject(conn);
		}

		return mapReturn;
	}

	@Override
	public Vector getMonthPayment(String isdn) throws Exception {
		log.info("Param ==> isdn: " + isdn);
		Vector vtReturn = new Vector();
		if (isdn.startsWith("84") && isdn.length() > 10) {
			isdn = isdn.substring(2);
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;

		try {
			String sql = "";
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, isdn);
			rs = ps.executeQuery();
			vtReturn = Tools.convertToVector(rs);
			return vtReturn;
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("error getMonthPayment:" + exp.getMessage());
			throw new Exception("error getMonthPayment:" + exp.getMessage());
		} finally {
			Tools.closeDatabaseObject(rs);
			Tools.closeDatabaseObject(ps);
			Tools.closeDatabaseObject(conn);
		}
	}

	@Override
	public String checkSubType(String isdn) throws Exception {
		log.info("Param ==> isdn: " + isdn);
		String strReturn = "";
		
		String sql = "DECLARE  BEGIN   ? := USSD_INFO.check_subtype(?); END;";

		Connection conn = null;
		CallableStatement cs = null;


		if (isdn.startsWith("84") && isdn.length() > 10) {
			isdn = isdn.substring(2);
		}

		try {
			conn = dataSource.getConnection();
			cs = conn.prepareCall(sql);
			cs.registerOutParameter(1,Types.VARCHAR);
			cs.setString(2,isdn);
			cs.execute();
			String resultQuery = StringUtils.nvl(cs.getString(1), "");
			if (resultQuery != "") {
				log.info("Query checkSubType from db success:" + resultQuery);
				strReturn = resultQuery;
//				List<String> listSpilitResultQuery = Arrays.asList(resultQuery.split("\\|"));
//				if(listSpilitResultQuery != null && listSpilitResultQuery.size() > 0) {
//					strReturn = listSpilitResultQuery.get(0);
//				}
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			log.error("checkSubType: " + e.getMessage(), e);
			throw new Exception("checkSubType:" + e);
		} finally {
			Tools.closeObject(cs);
			Tools.closeObject(conn);
		}

		return strReturn;
	}
	
	@Override
	public String getLanguage(String isdn) throws Exception {
		log.info("Param ==> isdn: " + isdn);
		String strReturn = "2";
		
		String sql = "DECLARE  BEGIN   ? := USSD_INFO.get_language(?); END;";

		Connection conn = null;
		CallableStatement cs = null;


		if (isdn.startsWith("84") && isdn.length() > 10) {
			isdn = isdn.substring(2);
		}

		try {
			conn = dataSource.getConnection();
			cs = conn.prepareCall(sql);
			cs.registerOutParameter(1,Types.VARCHAR);
			cs.setString(2,isdn);
			cs.execute();
			String resultQuery = StringUtils.nvl(cs.getString(1), "");
			if (resultQuery != "") {
				log.info("Query getLanguage from db success:" + resultQuery);
				strReturn = resultQuery;
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			log.error("getLanguage: " + e.getMessage(), e);
			throw new Exception("getLanguage:" + e);
		} finally {
			Tools.closeObject(cs);
			Tools.closeObject(conn);
		}

		return strReturn;
	}

	@Override
	public String getPromotionOCS() throws Exception {
		String strValue = "";
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection conn = null;

		try {
			String sql = "select par_value from ap_param where par_type = 'VIEW_BUNDLE_ICC' and par_name = 'BUNDLE_ICC' and status = 1";
			conn = dataSource.getConnection();
			ps = conn.prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()) {
				strValue = rs.getString("PAR_VALUE");
			}
			return strValue;
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("error getPromotionOCS:" + exp.getMessage());
			throw new Exception("error getPromotionOCS:" + exp.getMessage());
		} finally {
			Tools.closeDatabaseObject(rs);
			Tools.closeDatabaseObject(ps);
			Tools.closeDatabaseObject(conn);
		}
	}

}

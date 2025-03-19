package com.fis.epm.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import com.fis.epm.entity.EpmUserPG;
import com.fis.epm.models.TokenPG;
import com.fis.epm.repo.ApParamRedisRepo;
import com.fis.fw.common.utils.ValidationUtil;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.epm.models.ApParamRedisModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.fis.epm.entity.UssdMessage;
import com.fis.epm.entity.UssdServiceCode;
import com.fis.epm.entity.UssdSubType;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.pg.common.utils.AppConfigurationProp;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.Tools;

public class EPMBaseCommon {
	/**
	 * cache name config
	 */
	public static String BANK_CACHE_NAME = "BANK_CACHE";
	public static String CHARGE_CACHE_NAME = "CHARGE_CACHE";
	public static String PROVINCE_CACHE_NAME = "PROVINCE_CACHE";
	//mapUssdMessage by session_id + "_" + menu
	public static final Map<String, UssdMessage> mapUssdMessage = new HashMap<String, UssdMessage>();
	//UssdServiceCode by session_id
	public static final Map<String, UssdServiceCode> mapUssdMenu = new HashMap<String, UssdServiceCode>();
	//mapCheck thue bao by session_id
	public static final Map<String, UssdSubType> mapUssdSubType = new HashMap<String, UssdSubType>();
	//mapCheck last_message by session_id
	public static final Map<String, String> mapLastMessage = new HashMap<String, String>();
	
	public static final Map<String, String> mapLanguage = new HashMap<String, String>();

	public static final Map<String, EpmUserPG> mapUserByService = new HashMap<String, EpmUserPG>();

	public static final Map<String, TokenPG> mapTokenPGByUser = new HashMap<String, TokenPG>();
	
	public static Socket socketOcs = null;
	public static InputStream mInputStream = null;
	public static OutputStream mOutputStream = null;
	/**
	 * value
	 */
	@Value(AppConfigurationProp.APP_NAME_PROP)
	protected String appName = "";

	/**
	 * autowired
	 */
	@Autowired
	@Qualifier(EPMApiConstant.APP_CACHE_MANAGER_BEAN)
	public ConcurrentHashMap<String, Object> appCacheManager = null;

	@Autowired
	private ApParamRedisRepo apParamRedisRepo;

	protected <T> T loadCacheData(String cacheName) {
		if (this.appCacheManager == null)
			return null;
		if (this.appCacheManager.containsKey(cacheName))
			return (T) this.appCacheManager.get(cacheName);
		return null;
	}

	protected void pushCacheData(String cacheName, Object data) {
		if (this.appCacheManager == null)
			return;
		this.appCacheManager.put(cacheName, data);
	}

	protected <T> T loadDataInMapCache(String cacheName, String keyValue) {
		if (this.appCacheManager == null)
			return null;
		Map m = new HashMap<>();
		try {
			if (this.appCacheManager.containsKey(cacheName))
				m = (Map) this.appCacheManager.get(cacheName);
			if (m.containsKey(keyValue))
				return (T) m.get(keyValue);
		} catch (Exception exp) {
		}
		m = null;
		return null;
	}

	protected String loadTransactionId(String objectType, String typeProduct) {
		//COMMENT CODE
		if (EPMApiConstant.CREATE_PACKAGE.equals(objectType)){
			return EPMApiConstant.CREATE_PACKAGE + this.appName + Tools.loadSequence();
		}
		if(ValidationUtil.isNullOrEmpty(typeProduct)) {
			if ("POS".equals(objectType)) {
				return EmpStatusConstain.TRAN_POS + this.appName + Tools.loadSequence();
			} else if (EmpStatusConstain.TRAN_AUTO.equals(objectType)) {
				return EmpStatusConstain.TRAN_AUTO + this.appName + Tools.loadSequence();
			}
			return EmpStatusConstain.TRAN_PRE + this.appName + Tools.loadSequence();
		}
		else {
			String codeTran = "FIB";
			ApParamRedisModel apParamRedisModel = apParamRedisRepo.findByParTypeAndParName("CODE_EPM_TRANSACTION", "MOBIFIBER");
			if(apParamRedisModel != null && !ValidationUtil.isNullOrEmpty(apParamRedisModel.getParValue())){
				codeTran = apParamRedisModel.getParValue();
			}
			return codeTran + this.appName + Tools.loadSequence();
		}
	}

	protected String loadParNameCheckBankTypeByAmount(String objectType, String typeProduct) {
		if(ValidationUtil.isNullOrEmpty(typeProduct)) {
			return objectType;
		} else {
			return "FIB";
		}
	}
	
	public static String getIpAddress(HttpServletRequest requestIp) {
		String remoteAddr = "";
		if (requestIp != null) {
			remoteAddr = requestIp.getHeader("X-FORWARDED-FOR");
			System.out.println("X-FORWARDED-FOR: " + remoteAddr);
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = requestIp.getRemoteAddr();
				System.out.println("getRemoteAddr: " + remoteAddr);
				if (remoteAddr.contains("0:0:0:0:0:")) {
					remoteAddr = "127.0.0.1";
				}
			}
			if (remoteAddr != null && !"".equals(remoteAddr)) {
				String[] ipAddress = remoteAddr.split(",");
				if (ipAddress != null && ipAddress.length > 0) {
					remoteAddr = ipAddress[0];
				}
			}
		}
		return remoteAddr;
	}
}

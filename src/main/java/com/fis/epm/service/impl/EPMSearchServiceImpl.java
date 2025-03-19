package com.fis.epm.service.impl;

import java.io.InputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.dao.impl.EPMSearchDaoImpl;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.entity.UssdLog;
import com.fis.epm.entity.UssdMessage;
import com.fis.epm.entity.UssdSubType;
import com.fis.epm.models.USSDRequestModel;
import com.fis.epm.models.USSDResponseModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.service.EPMSearchService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;

@Service
public class EPMSearchServiceImpl implements EPMSearchService {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private EPMSearchDaoImpl ePMSearchDaoImpl;

	int iHttpRequestTimeout = 400;
	@Value(EPMApplicationProp.USSD_SERVICE_CODE)
	private String serviceCode = "";

	@Value(EPMApplicationProp.USSD_MESSAGE_LENGTH)
	int lengthUSSD = 130;
	
	@Value(EPMApplicationProp.USSD_USER_NAME_OCS)
	private String userLoginOCS = "";
	
	@Value(EPMApplicationProp.USSD_USER_PASSWORD_OCS)
	private String passwordOCS = "";
	
	@Value(EPMApplicationProp.USSD_IP_OCS)
	private String ipOCS = "";
	
	@Value(EPMApplicationProp.USSD_PORT_OCS)
	private String portOCS = "";
	
	@Value(EPMApplicationProp.USSD_PROMOTION_CODE_OCS)
	private static String promotionCode = null;

	@Autowired
	@Qualifier(EPMApiConstant.APP_MESSAGE_DICTIONARY_BEAN)
	private ConcurrentHashMap<String, String> messageDictionary = null;
	
	private static Long timeLogin = null;;

	// X = 1: tra cuu thong tin GOI CUOC
	private String xDanhMuc1 = "1";
	// X = 2: tra cuu thong tin CUOC GOI
	private String xDanhMuc2 = "2";
	// X = 3: tra cuu thong tin dich vu GTGT
	private String xDanhMuc3 = "3";
	// X = 4: tra cuu thong tin KHUYEN MAI
	private String xDanhMuc4 = "4";

	// Y = 1: tra cuu Thang n-1
	private String yThang1 = "1";
	// Y = 2: tra cuu Thang n-2
	private String yThang2 = "2";
	// Y = 3: tra cuu Thang n-3
	private String yThang3 = "3";

	// ParName APP param get template response
	private String PARTYPE = "USSD_RESPONSE_MS";
	private String SYNTAX_112 = "SYNTAX_112";
	private String SYNTAX_1121 = "SYNTAX_1121";
	private String SYNTAX_11214 = "SYNTAX_11214";
	private String SYNTAX_112141 = "SYNTAX_112141";
	private String SYNTAX_WRONG = "SYNTAX_WRONG";

	private String loadMessage(String status) {
		if (this.messageDictionary == null)
			return "";
		if (this.messageDictionary.containsKey(status))
			return this.messageDictionary.get(status);
		return "";
	}

	@Override
	public String searchByUssd(USSDRequestModel ussdRequestModel, String inputPage) throws Exception {
		// TODO Auto-generated method stub
		if(promotionCode == null || "".equals(promotionCode)) {
			promotionCode = ePMSearchDaoImpl.getPromotionOCS();
			logger.info("promotionCode OCS : " + promotionCode);
		}
		String serviceCode1 = serviceCode + "1*";
		String serviceCode12 = serviceCode + "2*";
		String serviceCode14 = serviceCode + "1*4*";
		String serviceCode141 = serviceCode + "1*4*1*";
		String serviceCode142 = serviceCode + "1*4*2*";
		String serviceCode143 = serviceCode + "1*4*3*";
		String serviceCode1411 = serviceCode + "1*4*1*1*";
		String serviceCode1412 = serviceCode + "1*4*1*2*";
		String serviceCode1413 = serviceCode + "1*4*1*3*";
		String serviceCode1421 = serviceCode + "1*4*2*1*";
		String serviceCode1422 = serviceCode + "1*4*2*2*";
		String serviceCode1423 = serviceCode + "1*4*2*3*";
		String serviceCode1431 = serviceCode + "1*4*3*1*";
		String serviceCode1432 = serviceCode + "1*4*3*2*";
		String serviceCode1433 = serviceCode + "1*4*3*3*";
		UssdLog logUssd = new UssdLog();
		try {
			String menu = ussdRequestModel.getMenu().trim().toUpperCase();
			logUssd.setStaDateTime(new Date());
			logUssd.setMenu(menu);
			logUssd.setSesssionId(ussdRequestModel.getSession_id());
			logUssd.setIsdn(ussdRequestModel.getMsisdn());
			logUssd.setUserInput(inputPage);
			UssdSubType mapUssdSubType = EPMBaseCommon.mapUssdSubType.get(ussdRequestModel.getSession_id());
			String subtype = "";
			if (mapUssdSubType == null || mapUssdSubType.getSubType() == null
					|| "".equals(mapUssdSubType.getSubType())) {
				subtype = ePMSearchDaoImpl.checkSubType(ussdRequestModel.getMsisdn());
				mapUssdSubType = new UssdSubType();
				mapUssdSubType.setSubType(subtype);
				mapUssdSubType.setCreateTime(System.currentTimeMillis());
				EPMBaseCommon.mapUssdSubType.put(ussdRequestModel.getSession_id(), mapUssdSubType);
			} else {
				subtype = mapUssdSubType.getSubType();
				mapUssdSubType.setCreateTime(System.currentTimeMillis());
				EPMBaseCommon.mapUssdSubType.put(ussdRequestModel.getSession_id(), mapUssdSubType);
			}
			if ("MC".equals(subtype)) {
				USSDResponseModel ussdResponseModel = new USSDResponseModel();
				String text = loadMessage("USSD-MC");
				ussdResponseModel.setText(text);
				ussdResponseModel.setOp("end");
				logUssd.setMessageOutput(EPMSearchServiceImpl.convertObjectToQueryString(ussdResponseModel));
				logUssd.setEndDateTime(new Date());
				return EPMSearchServiceImpl.convertObjectToQueryString(ussdResponseModel);
			}
			
			String language = EPMBaseCommon.mapLanguage.get(ussdRequestModel.getSession_id());			
			if(language == null) {
				language = ePMSearchDaoImpl.getLanguage(ussdRequestModel.getMsisdn());
				//2:VN - 1:EN
				EPMBaseCommon.mapLanguage.put(ussdRequestModel.getSession_id(), language);
			}			
			
			if ("02".equals(inputPage)) {
				logUssd.setMessageOutput(EPMSearchServiceImpl.convertObjectToQueryString(buildResponseWrongSyntax()));
				logUssd.setEndDateTime(new Date());
				return EPMSearchServiceImpl.convertObjectToQueryString(buildResponseWrongSyntax());
			}
			String messageReturn = loadMessage(language + "USSD-" + menu);
			logger.info("messageReturn :" + messageReturn);
			if ("".equals(messageReturn) && !menu.contains(serviceCode141) && !menu.contains(serviceCode142)
					&& !menu.contains(serviceCode143)) {
				logUssd.setMessageOutput(EPMSearchServiceImpl.convertObjectToQueryString(buildResponseWrongSyntax()));
				logUssd.setEndDateTime(new Date());
				return EPMSearchServiceImpl.convertObjectToQueryString(buildResponseWrongSyntax());
			}
			List<String> lstMessage = new ArrayList<String>();
			String message = messageReturn;
			UssdMessage ussdMessage = EPMBaseCommon.mapUssdMessage.get(ussdRequestModel.getSession_id() + "_" + menu);
			USSDResponseModel ussdResponseModel = new USSDResponseModel();
			if (ussdMessage != null) {
				ussdMessage.setCreateTime(System.currentTimeMillis());
				lstMessage = ussdMessage.getLstMessage();
			} else {
				ussdMessage = new UssdMessage();
				if (messageReturn.contains("{{debit}}")) {
					String misdn = ussdRequestModel.getMsisdn();
					if (misdn.startsWith("84") && misdn.length() > 10) {
						misdn = misdn.substring(2);
					}
					Map resultDebit = ePMSearchDaoImpl.getInfoDebit(misdn);
					String promotion = "";
					Map promotionResult = ePMSearchDaoImpl.callGetInfo112Sum(misdn);					
					try {
						if (EPMBaseCommon.socketOcs == null || EPMBaseCommon.socketOcs.isClosed()) {	
							loginOCS();
							timeLogin = System.currentTimeMillis();
						}
						long timeLoginAgain = timeLogin + (1000 * 60);
						if(timeLoginAgain  < System.currentTimeMillis() ) {
							EPMBaseCommon.mInputStream.close();
							EPMBaseCommon.mOutputStream.close();
							EPMBaseCommon.socketOcs = null;														
							loginOCS();
							timeLogin = System.currentTimeMillis();
						}

						if (promotionResult.get("promotion") != null && !"".equals(promotionResult.get("promotion")) && Long.parseLong((String) promotionResult.get("promotion")) > 0) {
							promotion = StringUtils.nvl(loadMessage(language + "USSD-KM"), "") + promotionResult.get("promotion") + "VND";
						}
						String accountOnline = "";
						String[] code = promotionCode.split(";");
						for(int i = 0 ; i < code.length; i++) {
							LogApiResult logIPC = new LogApiResult();
							logIPC.setTranId(misdn);
							logIPC.setSessionId(Integer.parseInt(ussdRequestModel.getSession_id()));
							logIPC.setUri(ipOCS + ":" + portOCS);
							logIPC.setType("USSD_SEND_OCS");
							long start = System.currentTimeMillis();
							String value = getAccountOnline(code[i], misdn, logIPC);
							if (!"".equals(value)) {
								if ("".equals(accountOnline)) {
									accountOnline = value;
								} else {
									accountOnline = accountOnline + ", " + value;
								}
							}
							long end = System.currentTimeMillis();
							long process = end - start;
							logIPC.setProcessTime(StringUtils.nvl(process, ""));
							EPMQueueManager.QUEUE_LOG_API_SEND.enqueueNotify(logIPC);
							Thread.sleep(100);
						}
						if (!"".equals(accountOnline)) {
							if (!"".equals(promotion)) {
								promotion = promotion + ", " + accountOnline + "VND";
							} else {
								promotion = accountOnline + "VND";
							}
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
//					EPMBaseCommon.mInputStream.close();
//					EPMBaseCommon.mOutputStream.close();
					resultDebit.put("promotion", promotion);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = buildResposeText(messageReturn, resultDebit);
				} else if (serviceCode1.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112Sum(ussdRequestModel.getMsisdn());
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = buildResposeText11(messageReturn, resultDebit);
				} else if (serviceCode141.equals(menu) || serviceCode142.equals(menu) || serviceCode143.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112Sum(ussdRequestModel.getMsisdn());
					logger.info("result: " + Tools.convertModeltoJSON(resultDebit));
					message = buildResposeText1141(messageReturn, resultDebit);
				} else if (serviceCode1411.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc1, yThang1);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1412.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc1, yThang2);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1413.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc1, yThang3);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1421.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc3, yThang1);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1422.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc3, yThang2);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1423.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc3, yThang3);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1431.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc4, yThang1);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1432.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc4, yThang2);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (serviceCode1433.equals(menu)) {
					Map resultDebit = ePMSearchDaoImpl.callGetInfo112(ussdRequestModel.getMsisdn(), xDanhMuc4, yThang3);
					logger.info("resultDebit: " + Tools.convertModeltoJSON(resultDebit));
					message = StringUtils.nvl(resultDebit.get("message"), "");
				} else if (!"".equals(messageReturn)) {
					message = messageReturn;
				} else {
					message = EPMBaseCommon.mapLastMessage.get(ussdRequestModel.getSession_id());
					if (message == null || "".equals(message)) {
						return EPMSearchServiceImpl.convertObjectToQueryString(buildResponseWrongSyntax());
					}
				}
				if ("1".equals(language)) {
					lstMessage.add(message);
				} else {
					lstMessage = getPageUssd(message);
				}
				ussdMessage.setLstMessage(lstMessage);
				ussdMessage.setPage(lstMessage.size());
			}

			logger.info("lstMessage : " + Tools.convertModeltoJSON(lstMessage));
			int page = Integer.parseInt(ussdRequestModel.getPage());
			if (lstMessage.size() > 1 && !"".equals(inputPage)) {
				if ("01".equals(inputPage) && page < lstMessage.size()) {
					page++;
				} else if ("00".equals(inputPage) && page > 1) {
					page--;
				}
			}
			String text = "";
			if ("1".equals(language)) {
				text = message;
				ussdResponseModel.setText(text);
				ussdResponseModel.setOp("end");
				ussdResponseModel.setPage("1");
			} else {
				text = setButtonPage(page, lstMessage.size(), lstMessage.get(page - 1));
				ussdResponseModel.setText(text);
				ussdResponseModel.setOp("continue");
				ussdResponseModel.setPage(page + "");
				ussdResponseModel.setInput("1");
			}
			EPMBaseCommon.mapLastMessage.put(ussdRequestModel.getSession_id(), text);
			EPMBaseCommon.mapUssdMessage.put(ussdRequestModel.getSession_id() + "_" + menu, ussdMessage);
			logUssd.setMessageOutput(EPMSearchServiceImpl.convertObjectToQueryString(ussdResponseModel));
			logUssd.setEndDateTime(new Date());
			return EPMSearchServiceImpl.convertObjectToQueryString(ussdResponseModel);

		} catch (Exception exp) {
			logger.error("call api TTDT payment_gateway/searchByUssd FAIL " + exp.getMessage(), exp);
			logUssd.setMessageOutput(EPMSearchServiceImpl.convertObjectToQueryString(buildResponseNoticationError()));
			logUssd.setEndDateTime(new Date());
			return EPMSearchServiceImpl.convertObjectToQueryString(buildResponseNoticationError());
		} finally {
			EPMQueueManager.QUEUE_LOG_USSD.enqueueNotify(logUssd);
		}
	}

	private String callGetInfo112Sum(String isdn) throws Exception {
		Map result = ePMSearchDaoImpl.callGetInfo112Sum(isdn);
		if (result != null) {
			String message = result.get("debitInfo").toString();
			return message;
		}
		return "Khong tim thay thong tin.";
	}

	private String buildResposeText11(String ms, Map result) {
		if (result == null) {
			return "";
		}
		DecimalFormat formatter = new DecimalFormat("###,###,###");
		try {
			String messageResult = StringUtils.nvl(result.get("debitInfo"), "");
			List<String> debitArray = Arrays.asList(messageResult.split("\\|"));
			List<String> detailMonth1 = Arrays.asList(debitArray.get(1).split(":"));
			List<String> detailMonth2 = Arrays.asList(debitArray.get(2).split(":"));
			List<String> detailMonth3 = Arrays.asList(debitArray.get(3).split(":"));

			ms = ms.replaceAll("\\{\\{month\\}\\}", detailMonth3.get(0).substring(3));
			ms = ms.replaceAll("\\{\\{money\\}\\}",
					formatter.format(Double.valueOf(StringUtils.nvl(detailMonth3.get(1), "0"))));
			ms = ms.replaceAll("\\{\\{month1\\}\\}", detailMonth2.get(0).substring(3));
			ms = ms.replaceAll("\\{\\{money1\\}\\}",
					formatter.format(Double.valueOf(StringUtils.nvl(detailMonth2.get(1), "0"))));
			ms = ms.replaceAll("\\{\\{month2\\}\\}", detailMonth1.get(0).substring(3));
			ms = ms.replaceAll("\\{\\{money2\\}\\}",
					formatter.format(Double.valueOf(StringUtils.nvl(detailMonth1.get(1), "0"))));
			return ms;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	private String buildResposeText1141(String ms, Map result) {
		if (result == null) {
			return "";
		}
		try {
			String messageResult = StringUtils.nvl(result.get("debitInfo"), "");
			List<String> debitArray = Arrays.asList(messageResult.split("\\|"));
			List<String> detailMonth1 = Arrays.asList(debitArray.get(1).split(":"));
			List<String> detailMonth2 = Arrays.asList(debitArray.get(2).split(":"));
			List<String> detailMonth3 = Arrays.asList(debitArray.get(3).split(":"));
			ms = ms.replaceAll("\\{\\{month\\}\\}", detailMonth3.get(0).substring(3));
			ms = ms.replaceAll("\\{\\{month1\\}\\}", detailMonth2.get(0).substring(3));
			ms = ms.replaceAll("\\{\\{month2\\}\\}", detailMonth1.get(0).substring(3));
			return ms;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	private String buildResposeText(String ms, Map result) {
		if (result == null) {
			return "";
		}
		try {
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			ms = ms.replaceAll("\\{\\{debit\\}\\}", StringUtils.nvl(result.get("debit"), "0"));
			ms = ms.replaceAll("\\{\\{debitPreviousPeriod\\}\\}",
					StringUtils.nvl(result.get("debitPreviousPeriod"), "0"));
			ms = ms.replaceAll("\\{\\{currentDay\\}\\}", sdf.format(date));
			ms = ms.replaceAll("\\{\\{promotion\\}\\}", StringUtils.nvl(result.get("promotion"), ""));
			return ms;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
		return "";
	}

	private String buildMenu112141(String ms) {
		StringBuilder result = new StringBuilder("");

		Date date = new Date();
		LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		LocalDate localDateMinus1 = localDate.minusMonths(1);
		LocalDate localDateMinus2 = localDate.minusMonths(2);
		LocalDate localDateMinus3 = localDate.minusMonths(3);

		ms = ms.replaceAll("%n1%", convertToMonth(localDateMinus1.getMonthValue()));
		ms = ms.replaceAll("%year1%", String.valueOf(localDateMinus1.getYear()));
		ms = ms.replaceAll("%n2%", convertToMonth(localDateMinus2.getMonthValue()));
		ms = ms.replaceAll("%year2%", String.valueOf(localDateMinus2.getYear()));
		ms = ms.replaceAll("%n3%", convertToMonth(localDateMinus3.getMonthValue()));
		ms = ms.replaceAll("%year3%", String.valueOf(localDateMinus3.getYear()));
		return ms;
	}

	// buildResponse Wrong sytax tra cuu
	private USSDResponseModel buildResponseWrongSyntax() throws Exception {
		USSDResponseModel ussdResponseModel = new USSDResponseModel();
		String text = loadMessage("USSD-Default");
		ussdResponseModel.setText(text);
		ussdResponseModel.setOp("end");
		ussdResponseModel.setPage("1");
		return ussdResponseModel;
	}

	private USSDResponseModel buildResponseNoticationError() throws Exception {
		USSDResponseModel ussdResponseModel = new USSDResponseModel();
		String text = "Cam on quy khach da su dung dich vu cua Mobifone";
		ussdResponseModel.setText(text);
		ussdResponseModel.setOp("end");
		return ussdResponseModel;
	}

	public static String convertObjectToQueryString(Object object) {

		// Object --> map
		ObjectMapper objectMapper = new ObjectMapper();
		Map<String, Object> map = objectMapper.convertValue(object, Map.class);

		StringBuilder qs = new StringBuilder();
		for (String key : map.keySet()) {

			if (map.get(key) == null) {
				continue;
			}
			// key=value&
			qs.append(key);
			qs.append("=");
			qs.append(map.get(key));
			qs.append("&");
		}

		// delete last '&'
		if (qs.length() != 0) {
			qs.deleteCharAt(qs.length() - 1);
		}
		return qs.toString();
	}

	public String convertToMonth(int month) {
		if (String.valueOf(month).length() == 1) {
			return "0" + month;
		}
		return "";
	}

	private List<String> getPageUssd(String text) {
		List<String> lstReturn = new ArrayList<String>();
		text = text.replaceAll("\\\\n", "\n");
		String[] lstSplit = text.split("\n");
		//logger.info("lstSplit : " + Tools.convertModeltoJSON(lstSplit));
		String strTmp = "";
		int i = 0;
		if (lstSplit != null) {
			while (i < lstSplit.length) {
				String textCurr = lstSplit[i];
				if (!"".equals(strTmp)) {
					textCurr = strTmp + "\n" + lstSplit[i];
				}
				//logger.info("textCurr :" + textCurr);
				if (textCurr.length() > lengthUSSD) {
					List<String> lstString = splitText(textCurr);
					for (int j = 0; j < lstString.size() - 1; j++) {
						lstReturn.add(lstString.get(j));
					}
					String strLast = lstString.get(lstString.size() - 1);
					int size = strLast.length();
					if (i < lstSplit.length - 1) {
						size += lstSplit[i + 1].length() + 4;
					}
					if (size >= lengthUSSD) {
						lstReturn.add(strLast);
						strTmp = "";
					} else {
						strTmp = strLast;
					}
				} else {
					String strAdd = textCurr + "\n";
					if (i < lstSplit.length - 1) {
						strAdd += lstSplit[i + 1] + "\n";
					}
					if (strAdd.length() > lengthUSSD) {
						lstReturn.add(textCurr);
						strTmp = "";
					} else {
						strTmp = textCurr;
					}
				}
				if ((i == lstSplit.length - 1) && !"".equals(strTmp))
					lstReturn.add(strTmp);
				i++;
			}
		}
		return lstReturn;
	}

	private String setButtonPage(int page, int pageSize, String mess) {
		if (pageSize > 1) {
			if (page == 1) {
				mess += "\n\n01-Trang sau\n";
				mess += "02-Thoat\n";
			}

			if (page > 1 && page < pageSize) {
				mess += "\n\n00-Trang truoc\n";
				mess += "01-Trang sau\n";
				mess += "02-Thoat\n";
			}

			if (page == pageSize) {
				mess += "\n\n00-Trang truoc\n";
				mess += "02-Thoat\n";
			}
		}
		return mess;
	}

	private List<String> splitText(String text) {
		List<String> lstReturn = new ArrayList<String>();
		String value = text;
		do {
			value = text;
			if (text.length() > lengthUSSD) {
				String tmp = text.substring(0, lengthUSSD);
				text = text.substring(lengthUSSD, text.length());
				int lastSpace = tmp.lastIndexOf(" ");
				String strSubString = tmp.substring(0, lastSpace);
				String strExist = tmp.substring(lastSpace, tmp.length());
				text = strExist + text;
				lstReturn.add(strSubString);
			} else {
				lstReturn.add(text);
			}
		} while (value.length() > lengthUSSD);
		return lstReturn;
	}

	public String load(InputStream is) throws Exception {
		String output = "";
		try {
			byte[] buffer = new byte[1024];
			int read = 0;
			read = is.read(buffer);
			if (read > 0) {
				output = new String(buffer, 0, read);
			}
			//logger.info("read after: " + read + " _ " + output);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return output;
	}

	public void loginOCS() {
		try {
			EPMBaseCommon.socketOcs = new Socket(ipOCS, Integer.parseInt(portOCS));
			EPMBaseCommon.mInputStream = EPMBaseCommon.socketOcs.getInputStream();
			EPMBaseCommon.mOutputStream = EPMBaseCommon.socketOcs.getOutputStream();
			EPMBaseCommon.socketOcs.setSoTimeout(10000);
			logger.info("Start call IPC");
			EPMBaseCommon.mOutputStream.flush();
			long startTime = System.currentTimeMillis();
			long expireTime = startTime + 5000;
			while (true) {
				String message = load(EPMBaseCommon.mInputStream);
				String writeLine = "\r";
				//logger.info("size writeLine: " + writeLine.getBytes().length);
				if (message.contains("login")) {
					String userName = userLoginOCS;
					logger.info("size user: " + userName.getBytes().length);
					EPMBaseCommon.mOutputStream.write(userName.getBytes());
					EPMBaseCommon.mOutputStream.flush();
					EPMBaseCommon.mOutputStream.write(writeLine.getBytes());
					EPMBaseCommon.mOutputStream.flush();
				}else if (message.contains("INPwd")) {
					String password = passwordOCS;
					logger.info("size pass: " + password.getBytes().length);
					EPMBaseCommon.mOutputStream.write(password.getBytes());
					EPMBaseCommon.mOutputStream.flush();
					EPMBaseCommon.mOutputStream.write(writeLine.getBytes());
					EPMBaseCommon.mOutputStream.flush();					
				}else if(message.contains("session") || (expireTime < System.currentTimeMillis())) {
					break;
				}else {
					Thread.sleep(100);
				}
			}			
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(), e);
		}
	}
	
	public String getAccountOnline(String code, String isdn, LogApiResult logIPC) throws Exception {
		String strReturn = "";
		String req = "bundle_display,msisdn=" + isdn + ",usg_ri=" + code +";";
		logger.info("req size : " + req.getBytes().length);	
		logIPC.setRequestBody(req);
		logIPC.setCreateTime(new Date());
		EPMBaseCommon.mOutputStream.write(req.getBytes());
		EPMBaseCommon.mOutputStream.flush(); 
		EPMBaseCommon.mOutputStream.write("\r".getBytes()); 
		EPMBaseCommon.mOutputStream.flush(); 
		String res = "";
		timeLogin = System.currentTimeMillis();
		long startTime = System.currentTimeMillis();
		long expireTime = startTime + 5000;
		while(true) {
			res = load(EPMBaseCommon.mInputStream);
			if(res != null && (res.contains("WARNING") || res.contains("VALPER1")) || res.contains("PPSVNM33P") || (expireTime < System.currentTimeMillis())) {
				break;
			}else {
				Thread.sleep(100);
			}
		}
		logger.info("info 3: " + res);
		if (res != null && res.contains("VALPER1=")) {
			res = res.replaceAll(";", ",");			
			String money = "";
			String[] value = res.split(",");
			if (value != null && value.length > 0) {
				for (int i = value.length - 1; i >= 0; i--) {
					if(value[i].contains("VALPER1=")) {
						money = value[i].replace("VALPER1=", "");
						break;
					}
				}
			}
			if(money != null && !"".equals(money)) {
				strReturn = code + ":" + money;
			}
		}
		logIPC.setResponseBody(res);
		logIPC.setEndTime(new Date());
		return strReturn;
	}
}

package com.fis.epm.business.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.dao.EPMTransactionDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.business.EPMVnpayCommunication;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.entity.Partners;
//import com.fis.epm.mq.services.MQPushDataEnqueueService;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.repo.ApParamRedisRepo;
import com.fis.epm.repo.EpmTransactionRepo;
import com.fis.epm.repo.IsdnInfoRedisRepo;
import com.fis.epm.repo.PromotionRedisRepo;
import com.fis.epm.repo.QueryDRRedisRepo;
import com.fis.epm.service.PartnersService;
import com.fis.epm.utils.ConfigVNPay;
import com.fis.epm.utils.DirectUtils;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.epm.utils.Utils;
import com.fis.epm.web.html.EpmErrorHtml;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.Constants;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.ApParamRedisModel;
import com.fis.pg.epm.models.EpmRequestModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.IsdnInfoRedisModel;

@Service
public class EPMVnpayCommunicationService extends EPMBaseCommon implements EPMVnpayCommunication {
	private static final Logger log = LoggerFactory.getLogger(EPMVnpayCommunicationService.class);

	@Value(EPMApplicationProp.NAPAS_ROOT_URL_PROP)
	private String napasRootUrl = "";
	@Value(EPMApplicationProp.HTTP_REQUEST_TIMEOUT_PROP)
	private int iHttpRequestTimeout = 40;
	@Value(EPMApplicationProp.COM_FIS_GATEWAY_URL)
	private String gatewayUrl = "";

	@Autowired
	private ResttemplateBean resttemplateBean = null;
	private ObjectMapper mapper;

	public EPMVnpayCommunicationService() {
		mapper = new ObjectMapper();
	}

	@Autowired
	private DirectUtils directUtils;

	@Autowired
	private EpmTransactionRepo epmTransactionRepo;

//	@Autowired
//	private MQPushDataEnqueueService mqPushDataEnqueueService;

	@Autowired
	private PartnersService partnersService;

	@Autowired
	private IsdnInfoRedisRepo isdnInfoRedisRepo;

	@Autowired
	private PromotionRedisRepo promotionRedisRepo;

	@Autowired
	private QueryDRRedisRepo queryDRRedisRepo;

	@Autowired
	private EpmErrorHtml epmErrorHtml = null;
	
	@Autowired
	private ApParamRedisRepo apParamRedisRepo;
	
	@Value(EPMApplicationProp.IS_CHECK_IP_PARTNER_PAYMENT)
	private String isCheckIp = "";

	@Autowired
	private EPMTransactionDao epmTransactionDao;

	@Autowired
	private EPMAppDao epmAppDao;

	@Override
	public String payment(HttpServletRequest req) {
		LogApiResult logApiResult = Utils.initLog("POST", "/comm/vnpay", null, null, null);
		try {
			Partners partners = partnersService.findByCode(EmpStatusConstain.PARTNER_CODE_VNPAY);

			String vnp_Version = "2.1.0";
			String vnp_Command = "pay";
			String vnp_OrderInfo = req.getParameter("vnp_OrderInfo");
			String orderType = req.getParameter("ordertype");
			String vnp_TxnRef = req.getParameter("pay_no");
			logApiResult.setTranId(vnp_TxnRef);
			int amountReq = Integer.parseInt(req.getParameter("amount"));
			EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(vnp_TxnRef);
			if (epmTransaction == null) {
				throw new AppException("Không tìm thấy giao dịch");
			}
			int discountAmount = Integer.parseInt(epmTransaction.getDiscountAmount().toString());
			int amount_of_transaction = Integer.parseInt(epmTransaction.getAmount().toString());
			if (amountReq != amount_of_transaction) {
				throw new AppException("Số tiền không khớp");
			}
			amountReq = amountReq - discountAmount;
			int amount = amountReq * 100;
			String vnp_IpAddr = EPMBaseCommon.getIpAddress(req);
			// String vnp_TmnCode = ConfigVNPay.vnp_TmnCode;
			String vnp_TmnCode = partners.getAccessCode();
			Map<String, String> vnp_Params = new HashMap<>();
			vnp_Params.put("vnp_Version", vnp_Version);
			vnp_Params.put("vnp_Command", vnp_Command);
			vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
			vnp_Params.put("vnp_Amount", String.valueOf(amount));
			vnp_Params.put("vnp_CurrCode", "VND");
			String bank_code = req.getParameter("bankcode");
			// Lay lai bank_code trong bang EpmTransaction
			bank_code = epmTransaction.getBankCode();
			if (bank_code != null && !bank_code.isEmpty()) {
				vnp_Params.put("vnp_BankCode", bank_code);
			}
			vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
			vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
//			vnp_Params.put("vnp_OrderType", orderType);
			vnp_Params.put("vnp_OrderType", "other");
			String locate = null;
			// Lay lai language trong bang EpmTransaction
			locate = epmTransaction.getLanguage();
			if (locate != null && !locate.isEmpty()) {
				vnp_Params.put("vnp_Locale", locate.toLowerCase());
			} else {
				vnp_Params.put("vnp_Locale", "vn");
			}
			// vnp_Params.put("vnp_ReturnUrl", ConfigVNPay.vnp_Returnurl);
			vnp_Params.put("vnp_ReturnUrl", partners.getUrlReturn());
			vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
			Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String vnp_CreateDate = formatter.format(cld.getTime());

			vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
			cld.add(Calendar.MINUTE, 15);// 15 ra cau hinh
			String vnp_ExpireDate = formatter.format(cld.getTime());
			// Add Params of 2.0.1 Version
			vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);
			// Billing
			vnp_Params.put("vnp_Bill_Mobile", req.getParameter("txt_billing_mobile"));
			vnp_Params.put("vnp_Bill_Email", req.getParameter("txt_billing_email"));
			if (req.getParameter("txt_billing_fullname") != null) {
				String fullName = (req.getParameter("txt_billing_fullname")).trim();
				if (fullName != null && !fullName.isEmpty()) {
					int idx = fullName.indexOf(' ');
					String firstName = fullName.substring(0, idx);
					String lastName = fullName.substring(fullName.lastIndexOf(' ') + 1);
					vnp_Params.put("vnp_Bill_FirstName", firstName);
					vnp_Params.put("vnp_Bill_LastName", lastName);

				}
			}
			vnp_Params.put("vnp_Bill_Address", req.getParameter("txt_inv_addr1"));
			vnp_Params.put("vnp_Bill_City", req.getParameter("txt_bill_city"));
			vnp_Params.put("vnp_Bill_Country", req.getParameter("txt_bill_country"));
			if (req.getParameter("txt_bill_state") != null && !req.getParameter("txt_bill_state").isEmpty()) {
				vnp_Params.put("vnp_Bill_State", req.getParameter("txt_bill_state"));
			}
			// Invoice
			vnp_Params.put("vnp_Inv_Phone", req.getParameter("txt_inv_mobile"));
			vnp_Params.put("vnp_Inv_Email", req.getParameter("txt_inv_email"));
			vnp_Params.put("vnp_Inv_Customer", req.getParameter("txt_inv_customer"));
			vnp_Params.put("vnp_Inv_Address", req.getParameter("txt_inv_addr1"));
			vnp_Params.put("vnp_Inv_Company", req.getParameter("txt_inv_company"));
			vnp_Params.put("vnp_Inv_Taxcode", req.getParameter("txt_inv_taxcode"));
			vnp_Params.put("vnp_Inv_Type", req.getParameter("cbo_inv_type"));
			// Build data to hash and querystring
			logApiResult.setRequestBody(Tools.convertModeltoJSON(vnp_Params));
			List fieldNames = new ArrayList(vnp_Params.keySet());
			Collections.sort(fieldNames);
			StringBuilder hashData = new StringBuilder();
			StringBuilder query = new StringBuilder();
			Iterator itr = fieldNames.iterator();
			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) vnp_Params.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					// Build hash data
					hashData.append(fieldName);
					hashData.append('=');
					hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					// Build query
					query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
					query.append('=');
					query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					if (itr.hasNext()) {
						query.append('&');
						hashData.append('&');
					}
				}
			}
			String queryUrl = query.toString();
			// String vnp_SecureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.vnp_HashSecret,
			// hashData.toString());
			String vnp_SecureHash = ConfigVNPay.hmacSHA512(partners.getKey(), hashData.toString());
			queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
			// String paymentUrl = ConfigVNPay.vnp_PayUrl + "?" + queryUrl;
			String paymentUrl = partners.getUrl() + "?" + queryUrl;
			logApiResult.setResponseBody(Tools.convertModeltoJSON(paymentUrl));
			return paymentUrl;
		} catch (Exception exp) {
			log.error("EPMVnpayCommunicationService.payment() - error process create order", exp);
			logApiResult.setResponseBody(exp.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(EPMBaseCommon.getIpAddress(req));
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return "error";
	}

	@Override
	public Map acceptVnpay(String vnp_TmnCode, String vnp_TxnRef, String vnp_Amount, String vnp_OrderInfo,
			String vnp_ResponseCode, String vnp_BankCode, String vnp_BankTranNo, String vnp_CardType,
			String vnp_PayDate, String vnp_TransactionNo, String vnp_TransactionStatus, String vnp_SecureHash,
			String ipRequest) {
		LogApiResult logApiResult = Utils.initLog("GET", "/comm/vnpay-ipn", null, null, null);
		String strReturn = this.gatewayUrl + "/comm/error";
		Map mapReturn = new HashMap();
		mapReturn.put("RspCode", "99");
		mapReturn.put("Message", "Unknow error");
		try {
			if("1".equals(isCheckIp)){
				List<ApParamRedisModel> apIp = apParamRedisRepo.findByParType(EPMApiConstant.AP_PAR_TYPE_IP_PARTNER);
				if(apIp == null || apIp.isEmpty()) {
					mapReturn.put("RspCode", "401");
					mapReturn.put("Message", "Ip cua VNPay khong dung.");
					throw new AppException("Ip cua VNPay khong dung.");
				}
				boolean isAccess = false;
				for (ApParamRedisModel ap : apIp) {
					if ("VNPAY".equals(ap.getParName())) {
						if (ap.getParValue() != null && (ap.getParValue().contains("*") || ap.getParValue().contains(ipRequest))) {
							isAccess = true;
							break;
						}
					}
				}
				if(!isAccess) {
					mapReturn.put("RspCode", "401");
					mapReturn.put("Message", "Ip cua VNPay khong dung.");
					log.info("IP Request : " + ipRequest);
					log.info("list Ip Param : " + Tools.convertModeltoJSON(apIp));
					throw new AppException("Ip cua VNPay khong dung.");
				}
			}
			Partners partners = partnersService.findByCode(EmpStatusConstain.PARTNER_CODE_VNPAY);
			Map<String, String> vnp_Params = new HashMap<>();
			vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
			vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
			vnp_Params.put("vnp_Amount", vnp_Amount);
			vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
			vnp_Params.put("vnp_ResponseCode", vnp_ResponseCode);
			vnp_Params.put("vnp_BankCode", vnp_BankCode);
			vnp_Params.put("vnp_BankTranNo", vnp_BankTranNo);
			vnp_Params.put("vnp_CardType", vnp_CardType);
			vnp_Params.put("vnp_PayDate", vnp_PayDate);
			vnp_Params.put("vnp_TransactionNo", vnp_TransactionNo);
			vnp_Params.put("vnp_TransactionStatus", vnp_TransactionStatus);
			Map<String, String> vnp_Params_log = new HashMap<>();
			vnp_Params_log.putAll(vnp_Params);
			vnp_Params_log.put("vnp_SecureHash", vnp_SecureHash);
			log.info("acceptRequest vnPay : " + Tools.convertModeltoJSON(vnp_Params_log));
			logApiResult.setRequestBody(Tools.convertModeltoJSON(vnp_Params_log));
			List fieldNames = new ArrayList(vnp_Params.keySet());
			Collections.sort(fieldNames);
			StringBuilder hashData = new StringBuilder();
			Iterator itr = fieldNames.iterator();
			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) vnp_Params.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					// Build hash data
					hashData.append(fieldName);
					hashData.append('=');
					hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					if (itr.hasNext()) {
						hashData.append('&');
					}
				}
			}
			String secureHash = ConfigVNPay.hmacSHA512(partners.getKey(), hashData.toString());
			if (!secureHash.equalsIgnoreCase(vnp_SecureHash)) {
				mapReturn.put("RspCode", "97");
				mapReturn.put("Message", "Invalid signature");
				throw new AppException("Invalid signature");
			}
			String transactionId = vnp_TxnRef;
			logApiResult.setTranId(vnp_TxnRef);
			EpmTransaction epmTransaction = this.epmTransactionRepo.findByTransactionID(vnp_TxnRef);
			if (epmTransaction == null) {
				mapReturn.put("RspCode", "01");
				mapReturn.put("Message", "Order not found");
				throw new AppException("Order not found");
			}
			if (epmTransaction.getPayAmount().longValue() * 100L != Long.parseLong(vnp_Amount)) {
				mapReturn.put("RspCode", "04");
				mapReturn.put("Message", "invalid amount");
				throw new AppException("invalid amount");
			}
			if (!"START".equals(epmTransaction.getPayStatus())) {
				mapReturn.put("RspCode", "02");
				mapReturn.put("Message", "Order already confirmed");
				throw new AppException("Order already confirmed");
			}
			epmTransaction.setCardNumber(vnp_BankCode);
			epmTransaction.setCardHolder(vnp_BankTranNo);
			epmTransaction.setPayStatus(EmpStatusConstain.FAILED);
			epmTransaction.setIssueStatus(EmpStatusConstain.FAILED);
			epmTransaction.setGateOrderId(StringUtils.nvl(vnp_TransactionNo, ""));
			strReturn = epmTransaction.getDirectUrl() + "&result=FAIL";
			if ("00".equalsIgnoreCase(vnp_ResponseCode) && !EmpStatusConstain.SUCC.equals(epmTransaction.getPayStatus())) {
				epmTransaction.setAmount(Long.parseLong(vnp_Amount));
				epmTransaction.setPayStatus(EmpStatusConstain.SUCC);
				epmTransaction.setIssueStatus(EmpStatusConstain.PENDING);
				try {
					directUtils.sendCallBackUrlSuccess(epmTransaction);
				} catch (Exception e) {
					log.error("loi sendCallBackUrlSuccess " + e.getMessage(), e);
				}
				strReturn = epmTransaction.getDirectUrl() + "&result=SUCCESS";
				Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(epmTransaction.getReference());
				if (isdnInfo.isPresent() && isdnInfo.get() != null) {
					epmTransaction.setCustCode(isdnInfo.get().getCustCode());
				}
				EpmTransactionModel transactionModel = epmTransaction.setEpmTransactionModel(epmTransaction);
				EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(transactionModel);
			} else {
				try {
					directUtils.sendCallBackUrlError(epmTransaction, EmpStatusConstain.PARTNER_CODE_VNPAY,
							vnp_ResponseCode, null);
				} catch (Exception e) {
					log.error("loi sendCallBackUrlSuccess " + e.getMessage(), e);
				}
			}
			EpmTransactionModel transactionModel = epmTransaction.setEpmTransactionModel(epmTransaction);
			epmAppDao.insertMerChantLog(Utils.genMerchantLog(transactionModel));
			epmTransactionDao.updatePaymentEPM(transactionModel);
			queryDRRedisRepo.deleteById(epmTransaction.getTransactionId());
//			EpmRequestModel req = new EpmRequestModel();
//
//			transactionModel.setResponseCode(vnp_ResponseCode);
//			req.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);
//			req.setAppName(this.appName);
//			req.setTransactionData(transactionModel);
//			req.setTransactionId(transactionId);
//			this.mqPushDataEnqueueService.sendRequest(req);
//			this.mqPushDataEnqueueService.sendRequestLog(req, transactionId, Constants.TYPE_RABBIT_MQ_SEND);
			mapReturn.put("RspCode", "00");
			mapReturn.put("Message", "Confirm Success");
			logApiResult.setResponseBody(Tools.convertModeltoJSON(mapReturn));
		} catch (Exception e) {
			log.error("EPMVnpayCommunicationService.acceptVnpay() - error process accept Vnpay", e);
			logApiResult.setResponseBody(Tools.convertModeltoJSON(mapReturn));
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(ipRequest);
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return mapReturn;
	}

	@Override
	public String vnpayCallback(String vnp_TmnCode, String vnp_TxnRef, String vnp_Amount, String vnp_OrderInfo,
			String vnp_ResponseCode, String vnp_BankCode, String vnp_BankTranNo, String vnp_CardType,
			String vnp_PayDate, String vnp_TransactionNo, String vnp_TransactionStatus, String vnp_SecureHash,
			String ipRequest) {
		LogApiResult logApiResult = Utils.initLog("GET", "/comm/vnpay-callback", null, null, null);
		String strReturn = this.gatewayUrl + "/comm/error";
		try {
			Partners partners = partnersService.findByCode(EmpStatusConstain.PARTNER_CODE_VNPAY);
			Map<String, String> vnp_Params = new HashMap<>();
			vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
			vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
			vnp_Params.put("vnp_Amount", vnp_Amount);
			vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
			vnp_Params.put("vnp_ResponseCode", vnp_ResponseCode);
			vnp_Params.put("vnp_BankCode", vnp_BankCode);
			vnp_Params.put("vnp_BankTranNo", vnp_BankTranNo);
			vnp_Params.put("vnp_CardType", vnp_CardType);
			vnp_Params.put("vnp_PayDate", vnp_PayDate);
			vnp_Params.put("vnp_TransactionNo", vnp_TransactionNo);
			vnp_Params.put("vnp_TransactionStatus", vnp_TransactionStatus);
			Map<String, String> vnp_Params_log = new HashMap<>();
			vnp_Params_log.putAll(vnp_Params);
			vnp_Params_log.put("vnp_SecureHash", vnp_SecureHash);
			log.info("acceptRequest vnpayCallback : " + Tools.convertModeltoJSON(vnp_Params_log));
			logApiResult.setRequestBody(Tools.convertModeltoJSON(vnp_Params_log));
			List fieldNames = new ArrayList(vnp_Params.keySet());
			Collections.sort(fieldNames);
			StringBuilder hashData = new StringBuilder();
			Iterator itr = fieldNames.iterator();
			while (itr.hasNext()) {
				String fieldName = (String) itr.next();
				String fieldValue = (String) vnp_Params.get(fieldName);
				if ((fieldValue != null) && (fieldValue.length() > 0)) {
					hashData.append(fieldName);
					hashData.append('=');
					hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
					if (itr.hasNext()) {
						hashData.append('&');
					}
				}
			}
//        String secureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.vnp_HashSecret, hashData.toString());
			String secureHash = ConfigVNPay.hmacSHA512(partners.getKey(), hashData.toString());
			String transactionId = vnp_TxnRef;
			logApiResult.setTranId(vnp_TxnRef);
			EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(vnp_TxnRef);
			if (epmTransaction == null) {
				throw new AppException("Order not found");
			}
			if (!secureHash.equalsIgnoreCase(vnp_SecureHash)) {
				throw new AppException("Invalid signature");
			}
			if ((epmTransaction.getPayAmount() * 100) != Long.parseLong(vnp_Amount)) {
				throw new AppException("invalid amount");
			}

			strReturn = epmTransaction.getDirectUrl() + "&result=FAIL";
			if ("00".equalsIgnoreCase(vnp_ResponseCode)) {
				strReturn = epmTransaction.getDirectUrl() + "&result=SUCCESS";
			}
			logApiResult.setResponseBody(strReturn);
		} catch (Exception e) {
			log.error("EPMVnpayCommunicationService.vnpayCallback() - error process vnpay Callback", e);
			logApiResult.setResponseBody(strReturn);
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(ipRequest);
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return strReturn;
	}

	public static void main(String[] args) {
		String a = "vnp_Amount=100000000&vnp_BankCode=NCB&vnp_BankTranNo=20220401103442&vnp_CardType=ATM&vnp_OrderInfo=test&vnp_PayDate=20220401103437&vnp_ResponseCode=00&vnp_TmnCode=4ARRNMXZ&vnp_TransactionNo=13717010&vnp_TransactionStatus=00&vnp_TxnRef=69093050";
		String secureHash = ConfigVNPay.hmacSHA512(ConfigVNPay.vnp_HashSecret, a);
		System.out.println(secureHash);
	}

}

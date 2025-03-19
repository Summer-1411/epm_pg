package com.fis.epm.business.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fis.epm.models.ApiCheckOutMBMoneyResponse;
import com.fis.epm.models.TransactionMobiMoneyModel;
import com.fis.epm.napas.models.PaymentInterResponseModelOTP;
import com.fis.epm.utils.*;
import com.fis.fw.common.utils.ValidationUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.business.EPMDirectBussiness;
import com.fis.epm.controller.EPMDirectController;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.models.BillTransactionModel;
import com.fis.epm.models.HtmlDirect;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.repo.BankTokenRedisRepo;
import com.fis.epm.repo.EpmTransactionRepo;
import com.fis.epm.repo.PartnerBankRedisRepo;
import com.fis.epm.repo.PartnersRedisRepo;
import com.fis.epm.service.BankService;
import com.fis.epm.service.BankTokenService;
import com.fis.epm.service.PartnerBankService;
import com.fis.epm.service.PartnersService;
import com.fis.epm.web.html.DirectHtml;
import com.fis.epm.web.html.EpmErrorHtml;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.BankRedisModel;
import com.fis.pg.epm.models.PartnerBankRedisModel;
import com.fis.pg.epm.models.PartnersRedisModel;

@Service
public class EPMDirectBussinessImpl implements EPMDirectBussiness {
	
	private static final Logger log = LoggerFactory.getLogger(EPMDirectController.class);

	@Autowired
	private BankService bankService;

	@Autowired
	private PartnerBankService partnerBankService;

	@Autowired
	private PartnersService partnersService;
	
	@Autowired
	private PartnerBankRedisRepo partnerBankRedisRepo;
	
	@Autowired
	private PartnersRedisRepo partnersRedisRepo; 
	
	@Autowired
	private BankTokenRedisRepo bankTokenRedisRepo;
	
	@Autowired
	private BankTokenService bankTokenService;
	
	@Autowired
	private EpmTransactionRepo epmTransactionRepo;
	
	@Autowired
	private DirectHtml directHtml;
	
	@Value("${com.fis.gateway.url}")
	private String gatewayUrl;
	
	@Value(EPMApplicationProp.NAPAS_URL_ERROR_PROP)
	private String napasUrlError = "";

	@Value("${com.fis.mobifone.money.url}")
	private String urlMoney;

	@Value("${com.fis.mobifone.money.partner.code}")
	private String partnerCodeMBFMoney;

	@Value("${com.fis.mobifone.money.callback.url}")
	private String callBackUrlMBFMoney;

	@Value("${com.fis.mobifone.money.bill.code}")
	private String billCodeMBFMoney;

	@Value("${com.fis.mobifone.money.key.3DES}")
	private String key3DESMBFMoney;

	@Value("${com.fis.mobifone.money.username}")
	private String userNameMBFMoney;

	@Value("${com.fis.mobifone.money.password}")
	private String passwordMBFMoney;

	@Value("${com.fis.mobifone.money.bank.code}")
	private String bankCodeMBFMoney;
	
	@Autowired
	private EpmErrorHtml epmErrorHtml  = null;
	
	private ObjectMapper mapper;
	private ResttemplateBean resttemplateBean = new ResttemplateBean();
	
	public EPMDirectBussinessImpl() {
		mapper = new ObjectMapper();
	}

	@Override
	public String transaction(String pay_no, Long amount, String PhoneNumber, String bankCode, String url, String ipRequest) {
		log.info("url, pay_no: "+pay_no+", amount "+ amount + ", PhoneNumber "+ PhoneNumber + ", bankCode "+ bankCode);
		LogApiResult logApiResult = Utils.initLog("POST",url, null, null, null);
		logApiResult.setTranId(pay_no);
		Map reqMap = new HashMap();
		reqMap.put("transactionId", pay_no);
		reqMap.put("amount", amount);
		reqMap.put("toIsdn", PhoneNumber);
		reqMap.put("bankCode", bankCode);
		logApiResult.setRequestBody(Tools.convertModeltoJSON(reqMap));
		try {
			String cardScheme = EmpStatusConstain.CARDSCHEME_ATM;
			EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(pay_no);
			if(epmTransaction == null) {
				log.error("===================>Error :" + "Giao dịch không thành công do không tìm thấy mã giao dịch");
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công do không tìm thấy mã giao dịch");
			}
			long amount_of_transaction = epmTransaction.getAmount();
			if(amount == null) {
				log.error("===================>Error :" + "Giao dịch không thành công số tiền không khớp");
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công số tiền không khớp");
			}
			long amountReq = amount;
			if(amountReq != amount_of_transaction) {
				log.error("===================>Error :" + "Giao dịch không thành công số tiền không khớp");
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công số tiền không khớp");
			}
			String card_type = epmTransaction.getCardType();
			String bankCodeEpm = epmTransaction.getBankCode();
			if(bankCode == null || !bankCodeEpm.equalsIgnoreCase(bankCode)) {
				log.error("===================>Error :" + "Giao dịch không thành công mã ngân hàng không khớp");
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công mã ngân hàng không khớp");
			}
			String phoneNumberEpm = epmTransaction.getReference();
			if(PhoneNumber == null || !phoneNumberEpm.equalsIgnoreCase(PhoneNumber)) {
				log.error("===================>Error :" + "Giao dịch không thành công số điện thoại không khớp");
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công số điện thoại không khớp");
			}
					
			try {
				if(card_type.equalsIgnoreCase(EmpStatusConstain.CT_INTER)) {
					cardScheme = EmpStatusConstain.CARDSCHEME_CREDIT;
//					redirectAttributes.addAttribute("pay_no", pay_no);
//					redirectAttributes.addAttribute("amount", amountReq);
//					redirectAttributes.addAttribute("cardScheme", cardScheme);
//					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
//					return "redirect:"+this.gatewayUrl+"/epm-html/napas-html";
					BillTransactionModel billTransactionModel = BillTransactionModel.builder()
							.pay_no(pay_no)
							.amount(amountReq)
							.cardScheme(cardScheme)
							.build();
					String data = mapper.writeValueAsString(billTransactionModel);
					
					HtmlDirect direct = HtmlDirect.builder()
							.url(this.gatewayUrl+"/epm-html/napas-html")
							.data(data)
							.method("post")
							.build();
					logApiResult.setResponseBody(Tools.convertModeltoJSON(direct));

					log.info("direct.getUrl : {} ", direct.getUrl());
					return directHtml.loadHtml(direct);
				}
	//			/* Lay du lieu tu Database*/
	//			Bank bank = bankService.findAllByBankCode(bankCode);
	//			List<PartnerBank> partnerBank = partnerBankService.findParnerBankCheck(bank.getBankId());
	//			Partners partners = partnersService.findByPartnerId(partnerBank.get(0).getPartnerId());
				
				/* Lay du lieu tu Redis*/
				BankRedisModel bankRedis = bankService.findByBankCode(bankCode.trim().toUpperCase());
				List<PartnerBankRedisModel> partnerBankRedisModels = partnerBankService.findRedisByBankId(bankRedis.getId());
				PartnersRedisModel partners = partnersService.findRedisByPartnerId(partnerBankRedisModels.get(0).getPartnerId());
				log.info("partners.getPartnerName() : {} ", partners.getPartnerName());
				if (partners.getPartnerName().equalsIgnoreCase(EmpStatusConstain.PARTNER_CODE_VNPAY)) {
//					redirectAttributes.addAttribute("pay_no", pay_no);
//					redirectAttributes.addAttribute("vnp_OrderInfo", " thanh toan " + pay_no);
//					redirectAttributes.addAttribute("amount", amountReq);
//					redirectAttributes.addAttribute("bankcode", bankCode);
//					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
//					return "redirect:"+this.gatewayUrl+"/comm/vnpay";
					BillTransactionModel billTransactionModel = BillTransactionModel.builder()
							.pay_no(pay_no)
							.vnp_OrderInfo(" thanh toan " + pay_no)
							.amount(amountReq)
							.bankcode(bankCode)
							.build();
					String data = mapper.writeValueAsString(billTransactionModel);
					HtmlDirect direct = HtmlDirect.builder()
							.url(this.gatewayUrl+"/comm/vnpay")
							.data(data)
							.method("post")
							.build();
					logApiResult.setResponseBody(Tools.convertModeltoJSON(direct));
					return directHtml.loadHtml(direct);

				} else if(partners.getPartnerName().equalsIgnoreCase(bankCodeMBFMoney)) {
					String convertDate = DateTimeUtils.convertDateToString(epmTransaction.getStaDateTime(), "yyyyMMdd");
					TransactionMobiMoneyModel tranMobiMoney = new TransactionMobiMoneyModel();
					epmTransaction.setPartnerTransId(convertDate + pay_no);
					epmTransactionRepo.save(epmTransaction);
					tranMobiMoney.setPartnerCode(partnerCodeMBFMoney);
					tranMobiMoney.setTransactionId(convertDate + pay_no);
					tranMobiMoney.setBillCode(billCodeMBFMoney);
					tranMobiMoney.setBillAmount(amount);
					tranMobiMoney.setProductCatalogue("WL1");
					tranMobiMoney.setBillComment(epmTransaction.getDescription());
					tranMobiMoney.setRedirectUrl(epmTransaction.getCallBackUrl());
					tranMobiMoney.setCallbackUrl(callBackUrlMBFMoney);
					String sign = tranMobiMoney.generateSignature();
					if (sign.endsWith("|")) {
						sign = sign.substring(0, sign.length() - 1);
					}
					log.info("sign chua ma hoa : {} ", sign);
					tranMobiMoney.setSignature(MobiFoneMoneyUtils.signDataBeforeSendRequest(sign));
					log.info("Data chua ma hoa : {} ", Tools.convertModeltoJSON(tranMobiMoney));
					String data = MobiFoneMoneyUtils.encrypt3DES(key3DESMBFMoney, mapper.writeValueAsString(tranMobiMoney));
					ApiCheckOutMBMoneyResponse response =  callAPIMobiMoney(data, tranMobiMoney.getTransactionId());
					if (response != null && response.getDebug() != null &&
							response.getDebug().getResponse() != null &&
							0 == response.getDebug().getResponse().getCode()) {
						return response.getDebug().getResponse().getPaymentRedirectUrl();
					}
					return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Mobifone Money không tạo được giao dịch");
				} else {
//					redirectAttributes.addAttribute("pay_no", pay_no);
//					redirectAttributes.addAttribute("amount", amountReq);
//					redirectAttributes.addAttribute("cardScheme", cardScheme);
//					redirectAttributes.addAttribute("card", "AtmCard");
//					request.setAttribute(View.RESPONSE_STATUS_ATTRIBUTE, HttpStatus.TEMPORARY_REDIRECT);
//					return "redirect:"+this.gatewayUrl+"/epm-html/napas-html";
					BillTransactionModel billTransactionModel = BillTransactionModel.builder()
							.pay_no(pay_no)
							.amount(amountReq)
							.cardScheme(cardScheme)
							.build();
					String data = mapper.writeValueAsString(billTransactionModel);
					HtmlDirect direct = HtmlDirect.builder()
							.url(this.gatewayUrl+"/epm-html/napas-html")
							.data(data)
							.method("post")
							.build();
					logApiResult.setResponseBody(Tools.convertModeltoJSON(direct));
					return directHtml.loadHtml(direct);
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				logApiResult.setResponseBody(e.getMessage());
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công lỗi " + e.getMessage());
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e);
			logApiResult.setResponseBody(e.getMessage());
			return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công lỗi " + e.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(ipRequest);
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
	}

	@Override
	public Object testApi() throws Exception {
//		String request = "Jur9qlhxgFp7/4IehwBJ3X2xTThorbJcYIycSH7pYH0734hlfpwenv0asVhISwcwrjz7PCo/xzF6T5gMZMuD555OBJtROBVcORJxXgtTQsblSxrDZKmr0hVRusRvaPqVtVc1Oy92LHHh346FvWGDQ3BvBABzN3Q73l39O8OiBO28tOeLn3FqsLbEHz3DnhKUK42wiVTQukLYUvjN1CD6myaVFdYhy42i93qnp2SQiid+ySeyMhTsRKZ7bTS9g+FrNRT0TVtQtk/hQAR8+qBDNwuajcdtbY5P+5t7A1qBvtJbXxnyx/7EGmKivnys7TupRU4Bp7TNe5JxNfSYa4dii8vt50qU6kaiKHaAHARwUJvVr+y2NMZvls5WNkASx8QARTCnHVNrzLL0cPNaqqN3uYBBncjCR7/vPWe780mRDp8RvEKRQBttSjn6qZzfZJmAtvAzhpuHnRSNnTtJP+cdD1TPDfH10WaWChwIjmnECUfMSiOVVpgB3B6ThpT0goyghdTym0jx6GdUI/yyEnQe0ya/JvhzWRwqYSk3eYR7cVIZBnkOSjyFJeLF4cFO9jyrUekeJKALBP/cdaxNiI72fWJD5KPWzYjc1FLUfLCczzEmGtF3WzzVl/JxM2ED+9XwewVSiBU/nI375m+dH8aEw0upLUcSCPn1jFjpPVV5SrDkJkIVSpQgiO6uIdN27iThJW1YaovQof+wpDU7psIW4AaES0N4/S2Znih7EuQr+hP1B+AppdijJrxUW6nxcsAR4HOcQpnhy7Ge/x5c3SWoADUPt6k2wGDcbpL5f5Q8ysUPBPN6nPfSrBD4uAzFxECucPlOHDHv8OviycRlwX8Z5mUAAu5BZB1lzyu+VEKsvgQDnNn+I//RnVqMem9JzPAWfGLo6KwW37Ll+W9wh3tGRcIWYiPZRc3z4wlqZGsSMTIdM/TqXo17q6kYONIM5PU8MbvYYmh6d2nwk+++042EyA==";
//		String resign = MobiFoneMoneyUtils.decrypt3DES(key3DESMBFMoney,request);
		String convertDate = DateTimeUtils.convertDateToString(new Date(), "yyyyMMdd");
//		String transactionId = convertDate + "CHECK" +System.currentTimeMillis();
//		String originTransactionId = "20240527FIBER701716804706026";
//		String sign = partnerCodeMBFMoney + "|" + transactionId + "|" + originTransactionId;
//		Map map = new HashMap();
//		map.put("partnerCode", partnerCodeMBFMoney);
//		map.put("transactionId", transactionId);
//		map.put("paymentTransactionId", originTransactionId);
//		map.put("signature", MobiFoneMoneyUtils.signDataBeforeSendRequest(sign));
//		log.info("request chưa mã hóa: {}", Tools.convertModeltoJSON(map));
//		String data = MobiFoneMoneyUtils.encrypt3DES(key3DESMBFMoney, mapper.writeValueAsString(map));
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
//		headers.setBasicAuth(userNameMBFMoney, passwordMBFMoney);
//		Map<String,Object> body = new HashMap<>();
//		body.put("data" , data);
//		log.info("request : {}", Tools.convertModeltoJSON(body));
//		Map response = (Map) resttemplateBean.handleHttpRequest("http://openapi.mobifonepay.telsoft.vn:8000/refund",
//				30000, HttpMethod.POST, headers, body, Map.class, "123");
//		log.info("response {} : ", Tools.convertModeltoJSON(response));
////		Gson gson = new Gson();
////		JsonObject resultObject = gson.fromJson(response.toString(), JsonObject.class);
//		String dataRes = response.get("data").toString().trim().replaceAll("\"","");
//		String responseDecrypt = MobiFoneMoneyUtils.decrypt3DES(key3DESMBFMoney, dataRes);
//		log.info("responseDecrypt : {}", responseDecrypt);
//		return null;
		TransactionMobiMoneyModel tranMobiMoney = new TransactionMobiMoneyModel();
		tranMobiMoney.setPartnerCode(partnerCodeMBFMoney);
		tranMobiMoney.setTransactionId(convertDate + "POS" +System.currentTimeMillis());
		tranMobiMoney.setBillCode(billCodeMBFMoney);
		tranMobiMoney.setBillAmount(10000L);
		tranMobiMoney.setProductCatalogue("WL1");
		tranMobiMoney.setBillComment("Thanh toan test");
		tranMobiMoney.setRedirectUrl(callBackUrlMBFMoney);
		tranMobiMoney.setCallbackUrl(callBackUrlMBFMoney);
		String sign = tranMobiMoney.generateSignature();

		if (sign.endsWith("|")) {
			sign = sign.substring(0, sign.length() - 1);
		}
		log.info("sign chua ma hoa : {} ", sign);
		tranMobiMoney.setSignature(MobiFoneMoneyUtils.signDataBeforeSendRequest(sign));
		log.info("Data chua ma hoa : {} ", Tools.convertModeltoJSON(tranMobiMoney));
		String data = MobiFoneMoneyUtils.encrypt3DES(key3DESMBFMoney, mapper.writeValueAsString(tranMobiMoney));
		ApiCheckOutMBMoneyResponse response =  callAPIMobiMoney(data, tranMobiMoney.getTransactionId());

		if (response != null && response.getDebug() != null &&
				response.getDebug().getResponse() != null &&
				0 == response.getDebug().getResponse().getCode()) {
			return response.getDebug().getResponse().getPaymentRedirectUrl();
		}
		return response;
	}

	private ApiCheckOutMBMoneyResponse callAPIMobiMoney(String data, String tranId) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.setBasicAuth(userNameMBFMoney, passwordMBFMoney);
		Map<String,Object> body = new HashMap<>();
		body.put("data" , data);
		log.info("request : {}", Tools.convertModeltoJSON(body));
		ApiCheckOutMBMoneyResponse response = (ApiCheckOutMBMoneyResponse) resttemplateBean.handleHttpRequest(urlMoney,
				30000, HttpMethod.POST, headers, body, ApiCheckOutMBMoneyResponse.class, tranId);
		log.info("response {} : ", response);
		return response;
	}
}

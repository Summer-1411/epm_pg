package com.fis.epm.business.impl;

import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.dao.EPMTransactionDao;
import com.fis.fw.common.utils.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fis.epm.business.EpmTransactionBusiness;
import com.fis.epm.entity.AgreementToken;
import com.fis.epm.entity.BankToken;
import com.fis.epm.entity.EpmCreateTokenLog;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.entity.Partners;
import com.fis.epm.models.HtmlModel;
import com.fis.epm.models.SubtypeRequestModel;
import com.fis.epm.models.SubtypeResponseModel;
import com.fis.epm.napas.models.AuthenticationRedirectModel;
import com.fis.epm.napas.models.InputParametersModel;
import com.fis.epm.napas.models.NapasResult;
import com.fis.epm.napas.models.NapasResultDecode;
import com.fis.epm.napas.models.OrderModel;
import com.fis.epm.napas.models.PaymentInterRequestModel;
import com.fis.epm.napas.models.PaymentInterResponseModelOTP;
import com.fis.epm.napas.models.PaymentRequestModel;
import com.fis.epm.napas.models.PaymentResponseModel;
import com.fis.epm.napas.models.PaymentResult;
import com.fis.epm.napas.models.PaymentWithOTPRequestModel;
import com.fis.epm.napas.models.PaymentWithOTPResponseModel;
import com.fis.epm.napas.models.Secure3DModel;
import com.fis.epm.napas.models.SourceOfFundsModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.epm.repo.AgreementTokenRepo;
import com.fis.epm.repo.ApParamRedisRepo;
import com.fis.epm.repo.BankTokenRepo;
import com.fis.epm.repo.EpmCreateTokenLogRepo;
import com.fis.epm.repo.EpmTransactionRepo;
import com.fis.epm.repo.IsdnInfoRedisRepo;
import com.fis.epm.repo.PromotionRedisRepo;
import com.fis.epm.repo.QueryDRRedisRepo;
import com.fis.epm.service.EpmCreateTokenLogService;
import com.fis.epm.service.PartnersService;
import com.fis.epm.utils.ConfigVNPay;
import com.fis.epm.utils.DateTimeUtils;
import com.fis.epm.utils.DirectUtils;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.epm.utils.Utils;
import com.fis.epm.web.html.EpmErrorHtml;
import com.fis.epm.web.html.EpmNapasHtml;
import com.fis.pg.common.utils.AppConfigurationProp;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.Encryptor;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.ApParamRedisModel;
import com.fis.pg.epm.models.EpmCreateOrderRequestModel;
import com.fis.pg.epm.models.EpmRequestModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.IsdnInfoRedisModel;
import com.fis.pg.gw.server.models.ResponseModel;
import com.google.gson.Gson;

@Service
public class EpmTransactionBusinessImpl extends EPMBaseCommon implements EpmTransactionBusiness {

	private static final Logger log = LoggerFactory.getLogger(EpmTransactionBusinessImpl.class);

	/**
	 *
	 * Value
	 */
	@Value(EPMApplicationProp.NAPAS_CLIENT_ID_PROP)
	private String clientId = "";
	@Value(EPMApplicationProp.NAPAS_CLIENT_IP_PROP)
	private String clientIP = "";
	@Value(EPMApplicationProp.NAPAS_DEVICE_ID_PROP)
	private String deviceId = "";
	@Value(EPMApplicationProp.NAPAS_EVNR_PROP)
	private String evnr = "";
	@Value(AppConfigurationProp.APP_NAME_PROP)
	private String appName = "";
	@Value(EPMApplicationProp.NAPAS_CLIENT_SECRET_PROP)
	private String napasClientSecret = "";
	@Value(EPMApplicationProp.NAPAS_URL_SUCCESS_PROP)
	private String napasUrlSuccess = "";
	@Value(EPMApplicationProp.NAPAS_URL_ERROR_PROP)
	private String napasUrlError = "";
	@Value(EPMApplicationProp.NAPAS_URL_RETURN_PROP)
	private String napasUrlReturn = "";
	@Value(EPMApplicationProp.NAPAS_CHANNEL_PROP)
	private String napasChannel = "";
	@Value(EPMApplicationProp.NAPAS_ENCRYPTOR_KEY)
	private String napasEncryptorKey = "";
	@Value(EPMApplicationProp.NAPAS_ENCRYPTOR_INITVECTOR)
	private String napasEncryptorInitVector = "";
	@Value(EPMApplicationProp.COM_FIS_GATEWAY_URL)
	private String gatewayUrl = "";
	@Value(EPMApplicationProp.PAYMENTGATEWAY_URL_CHECKSUBTYPE)
    private String paymentgatewayUrlCheckSubtype = "";

	/**
	 * autowired
	 */
	@Autowired
	private EpmNapasHtml epmNapasHtml = null;
	@Autowired
	private EpmErrorHtml epmErrorHtml = null;
	@Autowired
	private EPMNapasCommunicationService epmNapasCommunicationService = null;
//	@Autowired
//	private MQPushDataEnqueueService mqpushDataEnqueueService = null;
	@Autowired
	private HttpServletRequest request = null;
	@Autowired
	private BankTokenRepo bankTokenRepo;

	@Autowired
	private EpmTransactionRepo epmTransactionRepo;

	@Autowired
	private PartnersService partnersService;

	@Autowired
	private EpmCreateTokenLogRepo createTokenLogRepo;

	@Autowired
	private EpmCreateTokenLogService createTokenLogService;

	@Autowired
	private IsdnInfoRedisRepo isdnInfoRedisRepo;

	@Autowired
	private PromotionRedisRepo promotionRedisRepo;

	@Autowired
	private QueryDRRedisRepo queryDrRedisRepo;

	@Autowired
	private DirectUtils directUtils;
	
	@Autowired
	private ApParamRedisRepo apParamRedisRepo;
	
	@Autowired
    private ResttemplateBean resttemplateBean = null;
	
	@Value(EPMApplicationProp.HTTP_REQUEST_TIMEOUT_PROP)
    private int iHttpRequestTimeout = 40;
	
	@Value(EPMApplicationProp.IS_CHECK_IP_PARTNER_PAYMENT)
	private String isCheckIp = "";
	
	@Autowired
	private AgreementTokenRepo agreementTokenRepo;
	
	@Value(EPMApplicationProp.NAPAS_TOKEN_AGREEMENT_TYPE)
	private String agreementType = "UNSCHEDULED";
	
	@Value(EPMApplicationProp.NAPAS_TOKEN_EXPIRE_YEAR)
	private int expireYear = 5;

	@Autowired
	private EPMTransactionDao epmTransactionDao;

	@Autowired
	private EPMAppDao epmAppDao;

	@Override
	public String createTransaction(HttpServletRequest request) {
		// TODO Auto-generated method stub
		LogApiResult logApiResult = Utils.initLog("POST", "/epm-html/napas-html", null, null, null);
		logApiResult.setTranId(StringUtils.nvl(request.getParameter("pay_no"),""));		
		logApiResult.setRequestBody(Tools.convertModeltoJSON(request.getParameterMap()));
		try {
			// Tim kiem doi tac Napas
			Partners partners = partnersService.findByCode(EmpStatusConstain.PARTNER_CODE_NAPAS);
			String clientIPAddress = "192.168.1.1";
			// Lay du lieu truyen len (chi quan tam pay_no
			String pay_no = request.getParameter("pay_no");
			long amount = Long.parseLong(request.getParameter("amount"));
			String cardScheme = request.getParameter("cardScheme");
			String token_id = null;
			String language = null;
			String token_create = null;
			// tim kiem trong bang EpmTransaction
			EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(pay_no);
			if (epmTransaction == null) {
				throw new AppException("Không tìm thấy giao dịch");
			}
			long discountAmount = epmTransaction.getDiscountAmount();
			long amount_of_transaction = epmTransaction.getAmount();
			if (amount != amount_of_transaction) {
				throw new AppException("Số tiền không khớp");
			}
			// Lay lai du lieu trong EpmTransaction
			amount = amount - discountAmount;
			token_id = epmTransaction.getTokenId();
			language = epmTransaction.getLanguage();
			token_create = epmTransaction.getTokenCreate();
			if (epmTransaction.getCardType().equalsIgnoreCase(EmpStatusConstain.CT_INTER)) {
				cardScheme = EmpStatusConstain.CARDSCHEME_CREDIT;
			} else {
				cardScheme = EmpStatusConstain.CARDSCHEME_ATM;
			}
			if (token_id != null && !token_id.equalsIgnoreCase("")) {
				String tokenCodePre = bankTokenRepo.findTokenCode(token_id);
				if (Tools.stringIsNullOrEmty(tokenCodePre)) {
					log.error(
							"=====>Lỗi EpmTransactionBusinessImpl().createTransaction() không tìm thấy tokenCode với token_id = "
									+ token_id);
				}
				log.info("token the truoc khi giai ma: " + tokenCodePre);
				String tokenCode = Encryptor.decrypt(napasEncryptorKey, napasEncryptorInitVector, tokenCodePre);
				log.info("token the sau khi giai ma: " + tokenCode);
				if (cardScheme.equalsIgnoreCase(EmpStatusConstain.CARDSCHEME_CREDIT)) {
					String transactionId = pay_no;
					PaymentInterRequestModel payment = new PaymentInterRequestModel();
					payment.setApiOperation(EmpStatusConstain.APIOPERATION_PAY_WITH_3DS);
					OrderModel order = new OrderModel();
					order.setCurrency("VND");
					order.setAmount(amount);
					payment.setOrder(order);
					payment.setChannel(this.napasChannel);

					Secure3DModel secure3dModel = new Secure3DModel();
					AuthenticationRedirectModel authenticationRedirectModel = new AuthenticationRedirectModel();
					authenticationRedirectModel.setResponseUrl(partners.getUrlReturn());
					secure3dModel.setAuthenticationRedirect(authenticationRedirectModel);
					payment.setSecure3dModel(secure3dModel);

					SourceOfFundsModel sourceOfFundsModel = new SourceOfFundsModel();

					sourceOfFundsModel.setToken(tokenCode);
					payment.setSourceOfFunds(sourceOfFundsModel);					
					PaymentInterResponseModelOTP paymentRes = this.epmNapasCommunicationService
							.paymentAtmInernationalWith3dSercuse(payment, transactionId, transactionId, transactionId);
					log.info("---> 4.1 Thanh toán thẻ quốc tế bằng Token response:"+Tools.convertModeltoJSON(paymentRes));
					logApiResult.setResponseBody(paymentRes.getSecure3dModel().getAuthenticationRedirect().getSimple().getHtmlBodyContent());
					return paymentRes.getSecure3dModel().getAuthenticationRedirect().getSimple().getHtmlBodyContent();
				} else {
					PaymentWithOTPRequestModel paymentWithOTPRequestModel = new PaymentWithOTPRequestModel();
					OrderModel order = new OrderModel();
					String transactionId = pay_no;
					order.setAmount(amount);
					order.setCurrency("VND");
					paymentWithOTPRequestModel.setOrder(order);
					SourceOfFundsModel sourceOfFundsModel = new SourceOfFundsModel();
					sourceOfFundsModel.setToken(tokenCode);
					sourceOfFundsModel.setType("CARD");
					paymentWithOTPRequestModel.setSourceOfFunds(sourceOfFundsModel);
					InputParametersModel inputParameters = new InputParametersModel();
					inputParameters.setClientIP(clientIPAddress);
					inputParameters.setDeviceId(this.deviceId);
					inputParameters.setCardScheme(EmpStatusConstain.CARDSCHEME_ATM);
					inputParameters.setEnable3DSecure(false);
					inputParameters.setEnvironment(this.evnr);
					paymentWithOTPRequestModel.setInputParameters(inputParameters);
					//logApiResult.setRequestBody(Tools.convertModeltoJSON(paymentWithOTPRequestModel));
					PaymentWithOTPResponseModel paymentRes = this.epmNapasCommunicationService
							.paymentAtmLocalWithOtp(paymentWithOTPRequestModel, transactionId, transactionId);
					log.info("---> 4.1 Thanh toán thẻ nội địa bằng Token response:"+Tools.convertModeltoJSON(paymentRes));
					//logApiResult.setResponseBody(Tools.convertModeltoJSON(paymentRes));
					HtmlModel htmlModel = new HtmlModel();
					htmlModel.setApiOperation(EmpStatusConstain.APIOPERATION_PURCHASE_OTP);
					htmlModel.setCardScheme(EmpStatusConstain.CARDSCHEME_ATM);
					htmlModel.setChannel(this.napasChannel);
					htmlModel.setDataKey(paymentRes.getDataKey());
					htmlModel.setEnable3DSecure("false");
					htmlModel.setNapasKey(paymentRes.getNapasKey());
					htmlModel.setOrderAmount(amount);
					htmlModel.setOrderCurrency("VND");
					htmlModel.setOrderId(transactionId);
					htmlModel.setOrderReference("Pay for order id " + transactionId);
					htmlModel.setSourceOfFundsType("CARD");
					htmlModel.setTitle("Pay for order id " + transactionId);
					htmlModel.setClientIP(clientIPAddress);
					htmlModel.setUrlReturn(partners.getUrlReturn());
					htmlModel.setLanguage(language);

					/*
					 * create start transaction in here and push en queue
					 */
					logApiResult.setResponseBody(this.epmNapasHtml.loadHtml(htmlModel));
					return this.epmNapasHtml.loadHtml(htmlModel);
				}
			} else {
				PaymentRequestModel paymentRequestModel = new PaymentRequestModel();
				OrderModel order = new OrderModel();
				String transactionId = pay_no;
				order.setId(transactionId);
				order.setAmount(amount);
				order.setCurrency("VND");
				paymentRequestModel.setOrder(order);

				InputParametersModel inputParameters = new InputParametersModel();

				inputParameters.setClientIP(clientIPAddress);
				inputParameters.setDeviceId(this.deviceId);
				if (cardScheme.equalsIgnoreCase(EmpStatusConstain.CARDSCHEME_CREDIT)) {
					inputParameters.setCardScheme(EmpStatusConstain.CARDSCHEME_CREDIT);
					inputParameters.setEnable3DSecure(true);
					inputParameters.setEnvironment(this.evnr);
					paymentRequestModel.setInputParameters(inputParameters);
					//logApiResult.setRequestBody(Tools.convertModeltoJSON(paymentRequestModel));
					PaymentResponseModel paymentRes = this.epmNapasCommunicationService
							.paymentAtmLocal(paymentRequestModel);
					log.info("---> 4.1 Thanh toán thẻ/Thanh toán thẻ kèm tạo Token/Tạo Token response:"+Tools.convertModeltoJSON(paymentRes));
					//logApiResult.setResponseBody(Tools.convertModeltoJSON(paymentRes));
					HtmlModel htmlModel = new HtmlModel();
					if (token_create.equalsIgnoreCase("1")) {
						// thanh toán và tạo token
						htmlModel.setApiOperation(EmpStatusConstain.APIOPERATION_PAY_WITH_RETURNED_TOKEN);
						Date dateExpire = DateTimeUtils.addYearToDate(new Date(), expireYear);
						String strDate = DateTimeUtils.convertDateToString(dateExpire, DateTimeUtils.YYYY_MM_DD);
						Long id = bankTokenRepo.findSeqBankToken();
						htmlModel.setAgreementType(agreementType);
						htmlModel.setAgreementId(id.toString());
						htmlModel.setAgreementExpiryDate(strDate);
						AgreementToken agreeToken = new AgreementToken();
						agreeToken.setAgreementId(id);
						agreeToken.setAgreementType(agreementType);
						agreeToken.setTransactionId(pay_no);
						agreeToken.setCreateDate(new Date());
						agreeToken.setExpiredDate(dateExpire);
						agreeToken.setTokenId(id.toString());
						agreementTokenRepo.save(agreeToken);
					} else {
						// thanh toán bình thường
						htmlModel.setApiOperation(EmpStatusConstain.APIOPERATION_PAY);
					}
					htmlModel.setCardScheme(EmpStatusConstain.CARDSCHEME_CREDIT);
					htmlModel.setChannel(this.napasChannel);
					htmlModel.setDataKey(paymentRes.getDataKey());
					htmlModel.setEnable3DSecure("true");
					htmlModel.setNapasKey(paymentRes.getNapasKey());
					htmlModel.setOrderAmount(amount);
					htmlModel.setOrderCurrency("VND");
					htmlModel.setOrderId(transactionId);
					htmlModel.setOrderReference("Pay for order id " + transactionId);
					htmlModel.setSourceOfFundsType("CARD");
					htmlModel.setTitle("Pay for order id " + transactionId);
					htmlModel.setClientIP(clientIPAddress);
					htmlModel.setUrlReturn(partners.getUrlReturn());
					htmlModel.setLanguage(language);
					/*
					 * create start transaction in here and push en queue
					 */
					String html = this.epmNapasHtml.loadHtml(htmlModel);
					logApiResult.setResponseBody(this.epmNapasHtml.loadHtml(htmlModel));
					log.info("html tra ra: "+html);
					return html;
				} else {
					inputParameters.setCardScheme(EmpStatusConstain.CARDSCHEME_ATM);
					inputParameters.setEnable3DSecure(false);
					inputParameters.setEnvironment(this.evnr);
					paymentRequestModel.setInputParameters(inputParameters);
					//logApiResult.setRequestBody(Tools.convertModeltoJSON(paymentRequestModel));
					PaymentResponseModel paymentRes = this.epmNapasCommunicationService
							.paymentAtmLocal(paymentRequestModel);
					log.info("---> 4.1 Thanh toán thẻ/Thanh toán thẻ kèm tạo Token/Tạo Token response:"+Tools.convertModeltoJSON(paymentRes));
					//logApiResult.setResponseBody(Tools.convertModeltoJSON(paymentRes));
					HtmlModel htmlModel = new HtmlModel();
					if (token_create.equalsIgnoreCase("1")) {
						// thanh toán và tạo token
						htmlModel.setApiOperation(EmpStatusConstain.APIOPERATION_PAY_WITH_RETURNED_TOKEN);
						Date dateExpire = DateTimeUtils.addYearToDate(new Date(), expireYear);
						String strDate = DateTimeUtils.convertDateToString(dateExpire, DateTimeUtils.YYYY_MM_DD);
						Long id = bankTokenRepo.findSeqBankToken();
						htmlModel.setAgreementType(agreementType);
						htmlModel.setAgreementId(id.toString());
						htmlModel.setAgreementExpiryDate(strDate);
						AgreementToken agreeToken = new AgreementToken();
						agreeToken.setAgreementId(id);
						agreeToken.setAgreementType(agreementType);
						agreeToken.setTransactionId(pay_no);
						agreeToken.setCreateDate(new Date());
						agreeToken.setExpiredDate(dateExpire);
						agreeToken.setTokenId(id.toString());
						agreementTokenRepo.save(agreeToken);
					} else {
						// thanh toán bình thường
						htmlModel.setApiOperation(EmpStatusConstain.APIOPERATION_PAY);
					}
					htmlModel.setCardScheme(EmpStatusConstain.CARDSCHEME_ATM);
					htmlModel.setChannel(this.napasChannel);
					htmlModel.setDataKey(paymentRes.getDataKey());
					htmlModel.setEnable3DSecure("false");
					htmlModel.setNapasKey(paymentRes.getNapasKey());
					htmlModel.setOrderAmount(amount);
					htmlModel.setOrderCurrency("VND");
					htmlModel.setOrderId(transactionId);
					htmlModel.setOrderReference("Pay for order id " + transactionId);
					htmlModel.setSourceOfFundsType("CARD");
					htmlModel.setTitle("Pay for order id " + transactionId);
					htmlModel.setClientIP(clientIPAddress);
					htmlModel.setUrlReturn(partners.getUrlReturn());
					htmlModel.setLanguage(language);
					/*
					 * create start transaction in here and push en queue
					 */
					String html = this.epmNapasHtml.loadHtml(htmlModel);
					logApiResult.setResponseBody(this.epmNapasHtml.loadHtml(htmlModel));
					log.info("html tra ra: "+html);
					return html;
				}
			}
		} catch (Exception exp) {
			log.error("EpmTransactionBusinessImpl.createTransaction() - error process create transaction", exp);
			logApiResult.setResponseBody(exp.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(EPMBaseCommon.getIpAddress(request));
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		request.setAttribute("message", "Giao dịch không thành công");
		return "error";
	}

	@Override
	public ResponseModel createOrder(EpmCreateOrderRequestModel req) {
		// TODO Auto-generated method stub
		ResponseModel res = new ResponseModel();

		try {
//			EpmRequestModel epmRequest = new EpmRequestModel();
			EpmTransactionModel transactionData = Tools.buildTransactionStart(appName, req.getAmount(),
					req.getBankCode(), req.getPartner(), req.getCenCode(), req.getDiscountAmount(),
					req.getFromReference(), this.request.getRemoteAddr(), req.getServiceType(), req.getAreaCode());
//			epmRequest.setAppName(appName);
//			epmRequest.setTransactionData(transactionData);
//			epmRequest.setTransactionId(transactionData.getTransactionId());
//			epmRequest.setActionType(EmpStatusConstain.INSERT_TRANSACCTION_ACTION);
			epmTransactionDao.insertTransaction(transactionData);
//			this.mqpushDataEnqueueService.sendRequest(epmRequest);
			res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
		} catch (Exception exp) {
			res = Tools.buildResponseModel(exp);
			log.error("EpmTransactionBusinessImpl.createOrder() - error process create order", exp);
		}
		return res;
	}

	@Override
	public String acceptRequest(NapasResult napasResult, String ipRequest, String url) {
		// TODO Auto-generated method stub
		log.info("acceptRequest napas not decode : " + Tools.convertModeltoJSON(napasResult));
		LogApiResult logApiResult = Utils.initLog("POST", url, null,
				Tools.convertModeltoJSON(napasResult), null);
		try {
			if ("1".equals(isCheckIp) && url.equals("/napas-ipn-callback")) {
				List<ApParamRedisModel> apIp = apParamRedisRepo.findByParType(EPMApiConstant.AP_PAR_TYPE_IP_PARTNER);
				if (apIp == null || apIp.isEmpty()) {
					throw new AppException("Ip cua NAPAS khong dung.");
				}
				boolean isAccess = false;
				for (ApParamRedisModel ap : apIp) {
					if ("NAPAS".equals(ap.getParName())) {
						if (ap.getParValue() != null
								&& (ap.getParValue().contains("*") || ap.getParValue().contains(ipRequest))) {
							isAccess = true;
							break;
						}
					}
				}
				if (!isAccess) {
					log.info("IP Request : " + ipRequest);
					log.info("list Ip Param : " + Tools.convertModeltoJSON(apIp));
					throw new AppException("Ip cua NAPAS khong dung.");
				}
			}
			//EpmRequestModel epmRequest = new EpmRequestModel();

			NapasResultDecode napasResultDecode = decodeToken(napasResult);
			log.info("acceptRequest napas : " + Tools.convertModeltoJSON(napasResultDecode));
			if (napasResultDecode.getPaymentResult() == null && napasResultDecode.getTokenResult() != null) {
				return acceptRequestToken(napasResult, ipRequest, url);
			}
			if (napasResultDecode.getPaymentResult() == null || napasResultDecode.getPaymentResult().getOrder() == null
					|| (napasResultDecode.getPaymentResult().getOrder().getId() == null && napasResultDecode.getPaymentResult().getOrderId() == null)) {
				log.error("=================>Error Giao dich that bai");
				throw new AppException(EPMMessageCode.API_EXCEPTION_CODE);
			}
			String transactionId = napasResultDecode.getPaymentResult().getOrder().getId();
			if(ValidationUtil.isNullOrEmpty(transactionId)){
				transactionId = napasResultDecode.getPaymentResult().getOrderId();
			}
			EpmTransaction transaction = epmTransactionRepo
					.findByTransactionID(transactionId);
			if (transaction == null) {
				log.error("=================>Khong tim thay du lieu trong bang tran EpmTransaction");
				throw new AppException(EPMMessageCode.API_EXCEPTION_CODE);
			}
			if (transaction.getPayStatus() != null && !EmpStatusConstain.START.equals(transaction.getPayStatus())){
				log.error("=================>Đa co giao dich trong EpmTransaction");
				if (napasResultDecode.getPaymentResult().getResult().equalsIgnoreCase(EmpStatusConstain.SUCCESS)) {
					logApiResult.setTranId(StringUtils.nvl(napasResultDecode.getPaymentResult().getOrder().getId(),""));
					logApiResult.setResponseBody(transaction.getDirectUrl() + "&result=SUCCESS");
					return transaction.getDirectUrl() + "&result=SUCCESS";
				}
				logApiResult.setResponseBody(transaction.getDirectUrl() + "&result=FAIL");
				return transaction.getDirectUrl() + "&result=FAIL";
			}
            logApiResult.setTranId(StringUtils.nvl(transactionId,""));
			EpmTransactionModel empTransactionModel = new EpmTransactionModel();
			transaction.setPayStatus(EmpStatusConstain.FAILED);
			transaction.setIssueStatus(EmpStatusConstain.FAILED);
			empTransactionModel = transaction.setEpmTransactionModel(transaction);
			if(napasResultDecode.getPaymentResult() != null && napasResultDecode.getPaymentResult().getResponse() != null) {
				empTransactionModel.setResponseCode(napasResultDecode.getPaymentResult().getResponse().getAcquirerCode());
			}

			if (napasResultDecode.getPaymentResult().getResult().equalsIgnoreCase(EmpStatusConstain.SUCCESS)
					&& !EmpStatusConstain.SUCC.equals(empTransactionModel.getPayStatus()) ) {
				empTransactionModel.setTransactionId((String) napasResultDecode.getPaymentResult().getOrder().getId());
				empTransactionModel.setPayStatus(EmpStatusConstain.SUCC);
				empTransactionModel.setIssueStatus(EmpStatusConstain.PENDING);
				empTransactionModel.setCardNumber(
						napasResultDecode.getPaymentResult().getSourceOfFunds().getProvided().getCard().getNumber());
				empTransactionModel.setCardHolder(napasResultDecode.getPaymentResult().getSourceOfFunds().getProvided()
						.getCard().getNameOnCard());
				//empTransactionModel.setAmount(napasResultDecode.getPaymentResult().getTransaction().getAmount());
				Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(empTransactionModel.getReference());
				if (isdnInfo.isPresent() && isdnInfo.get() != null) {
					empTransactionModel.setCustCode(isdnInfo.get().getCustCode());
				}
				if (napasResultDecode.getTokenResult() != null
						&& napasResultDecode.getTokenResult().getResult().equalsIgnoreCase(EmpStatusConstain.SUCCESS)) {
					Double subId = checkSubtype(transaction.getFromReference(), napasResultDecode.getPaymentResult().getOrder().getId());
					String token = napasResultDecode.getTokenResult().getToken();
					log.info("token the truoc khi ma hoa: " + token);
					String tokenCode = Encryptor.encrypt(napasEncryptorKey, napasEncryptorInitVector, token);
					log.info("token the sau khi ma hoa: " + tokenCode);
					BankToken bankTokenOld = bankTokenRepo.findByTokenCode(tokenCode);
					if (bankTokenOld == null || bankTokenOld.getId() == null) {
						AgreementToken agree = agreementTokenRepo.findByTranId(napasResultDecode.getPaymentResult().getOrder().getId());
						BankToken bankToken = new BankToken();
						bankToken.setId(agree.getAgreementId());
						bankToken.setTokenName(transaction.getBankCode() + "-"
								+ napasResultDecode.getTokenResult().getCard().getNumber());
						bankToken.setCardType(transaction.getCardType());
						bankToken.setBankCode(transaction.getBankCode());
						bankToken.setReference(StringUtils.nvl(transaction.getFromReference(), "0"));
						bankToken.setStatus("1");
						bankToken.setCreateDate(new Date());
						bankToken.setSubId(subId);
						try {
							bankToken.setTokenCode(tokenCode);
							if (napasResultDecode.getTokenResult().getCard().getExpiry() != null) {
								bankToken.setExpiredMonth(
										napasResultDecode.getTokenResult().getCard().getExpiry().getMonth());
								bankToken.setExpiredYear(
										napasResultDecode.getTokenResult().getCard().getExpiry().getYear());
							}
						} catch (Exception e) {
							log.error("loi " + e.getMessage(), e);
						}
						
						try {
							bankTokenRepo.save(bankToken);
						} catch (Exception e) {
							log.error("loi luu token " + e.getMessage(), e);
						}
						
					}
				}
//				epmRequest.setAppName(appName);
//				epmRequest.setTransactionData(empTransactionModel);
//				epmRequest.setTransactionId(transaction.getTransactionId());
//				epmRequest.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);
//				this.mqpushDataEnqueueService.sendRequest(epmRequest);
//				this.mqpushDataEnqueueService.sendRequestLog(epmRequest, transaction.getTransactionId() , Constants.TYPE_RABBIT_MQ_SEND);
				/*
				 * isdnInfoRedisRepo.deleteById(transaction.getReference());
				 * promotionRedisRepo.deleteById(transaction.getReference());
				 */
				epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
				epmTransactionDao.updatePaymentEPM(empTransactionModel);
				queryDrRedisRepo.deleteById(transaction.getTransactionId());
				EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(empTransactionModel);
				try {
					directUtils.sendCallBackUrlSuccess(transaction);
				} catch (Exception e) {
					log.error("loi sendCallBackUrlSuccess " + e.getMessage(), e);
				}
				logApiResult.setResponseBody(transaction.getDirectUrl() + "&result=SUCCESS");
				return transaction.getDirectUrl() + "&result=SUCCESS";
			}
			/*
			 * isdnInfoRedisRepo.deleteById(transaction.getReference());
			 * promotionRedisRepo.deleteById(transaction.getReference());
			 */
			epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
			epmTransactionDao.updatePaymentEPM(empTransactionModel);
			queryDrRedisRepo.deleteById(transaction.getTransactionId());
//			epmRequest.setAppName(appName);
//			epmRequest.setTransactionData(empTransactionModel);
//			epmRequest.setTransactionId(transaction.getTransactionId());
//			epmRequest.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);

			EpmTransactionModel epmTransactionModel = new EpmTransactionModel();
			epmTransactionModel.setTransactionId(transaction.getTransactionId());
			epmTransactionModel.setUserName(transaction.getUserName());
			epmTransactionModel.setCallBackUrl(transaction.getCallBackUrl());

//			this.mqpushDataEnqueueService.sendRequest(epmRequest);
//			this.mqpushDataEnqueueService.sendRequestLog(epmRequest, transaction.getTransactionId() , Constants.TYPE_RABBIT_MQ_SEND);
			try {
				directUtils.sendCallBackUrlError(transaction, EmpStatusConstain.PARTNER_CODE_NAPAS,
						napasResultDecode.getPaymentResult().getResponse().getAcquirerCode(),
						napasResultDecode.getPaymentResult().getResponse().getAcquirerMessage());
			} catch (Exception e) {
				log.error("loi sendCallBackUrlSuccess " + e.getMessage(), e);
			}
			logApiResult.setResponseBody(transaction.getDirectUrl() + "&result=FAIL");
			return transaction.getDirectUrl() + "&result=FAIL";
		} catch (Exception exp) {
			log.error("EpmTransactionBusinessImpl.acceptRequest() - error process accept Request", exp);
			logApiResult.setResponseBody(exp.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(ipRequest);
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return this.gatewayUrl+"/epm-html/error";
	}

	@Override
	public String createTransactionToken(HttpServletRequest request) {
		LogApiResult logApiResult = Utils.initLog("POST", "/epm-html/napas-token-html", null, null, null);
		//logApiResult.setRequestBody(Tools.convertModeltoJSON(request));
		logApiResult.setRequestBody(Tools.convertModeltoJSON(request.getParameterMap()));
		try {
			String clientIPAddress = ConfigVNPay.getIpAddress(request);
//			long amount = Long.parseLong(request.getParameter("amount"));
//			String cardScheme = request.getParameter("cardScheme");
//			String token_id = request.getParameter("token_id");
//			String pay_no = request.getParameter("pay_no");
//			String language = request.getParameter("language");
//			EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(pay_no);
//			if(epmTransaction == null) {
//				throw new AppException("Không tìm thấy giao dịch");
//			}
//			String token_create = request.getParameter("token_create");

			String cardType = request.getParameter("cardType");
			String phone = request.getParameter("from_msisdn");
			if(Tools.stringIsNullOrEmty(phone)) {
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công số điện thoại không được để trống");
			}
			if(!Tools.checkIsdn2(phone)) {
				return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công", "Giao dịch không thành công số điện thoại không đúng định dạng");
			}
			if (phone.startsWith("0")) {
				phone = phone.substring(1, phone.length());
			}
			String webApp = request.getParameter("environment");

			String transactionId = this.loadTransactionId(EmpStatusConstain.TRAN_AUTO, null);
            logApiResult.setTranId(transactionId);

			EpmCreateTokenLog createTokenLog = new EpmCreateTokenLog();
			createTokenLog.setTransactionId(transactionId);
			createTokenLog.setRequestDatetime(new Date());
			createTokenLog.setPartnerCode(EmpStatusConstain.PARTNER_CODE_NAPAS);
			if (phone != null && !phone.equalsIgnoreCase("")) {
				createTokenLog.setDescription(phone);
			}
			createTokenLogRepo.save(createTokenLog);

			long amount = 5000L;
			String language = "vn";

			PaymentRequestModel paymentRequestModel = new PaymentRequestModel();
			OrderModel order = new OrderModel();
			order.setId(transactionId);
			order.setAmount(amount);
			order.setCurrency("VND");
			paymentRequestModel.setOrder(order);

			InputParametersModel inputParameters = new InputParametersModel();

			inputParameters.setClientIP(clientIPAddress);
			inputParameters.setDeviceId(this.deviceId);
			inputParameters.setCardScheme(EmpStatusConstain.CARDSCHEME_CREDIT);
			inputParameters.setEnable3DSecure(true);
			inputParameters.setEnvironment(this.evnr);
			paymentRequestModel.setInputParameters(inputParameters);
			//logApiResult.setRequestBody(Tools.convertModeltoJSON(paymentRequestModel));
			PaymentResponseModel paymentRes = this.epmNapasCommunicationService.paymentAtmLocal(paymentRequestModel);
			//logApiResult.setResponseBody(Tools.convertModeltoJSON(paymentRes));
			HtmlModel htmlModel = new HtmlModel();
			htmlModel.setCardScheme(EmpStatusConstain.CARDSCHEME_CREDIT);
			htmlModel.setChannel(this.napasChannel);
			htmlModel.setDataKey(paymentRes.getDataKey());
			htmlModel.setEnable3DSecure("true");
			htmlModel.setNapasKey(paymentRes.getNapasKey());
			htmlModel.setOrderAmount(amount);
			htmlModel.setOrderCurrency("VND");
			htmlModel.setOrderId(transactionId);
			htmlModel.setOrderReference("Pay for order id " + transactionId);
			htmlModel.setSourceOfFundsType("CARD");
			htmlModel.setTitle("Pay for order id " + transactionId);
			htmlModel.setClientIP(clientIPAddress);
			htmlModel.setUrlReturn(this.napasUrlReturn);
			htmlModel.setLanguage(language);
			
			Date dateExpire = DateTimeUtils.addYearToDate(new Date(), expireYear);
			String strDate = DateTimeUtils.convertDateToString(dateExpire, DateTimeUtils.YYYY_MM_DD);
			Long id = bankTokenRepo.findSeqBankToken();
			htmlModel.setAgreementType(agreementType);
			htmlModel.setAgreementId(id.toString());
			htmlModel.setAgreementExpiryDate(strDate);
			
			AgreementToken agreeToken = new AgreementToken();
			agreeToken.setAgreementId(id);
			agreeToken.setAgreementType(agreementType);
			agreeToken.setTransactionId(transactionId);
			agreeToken.setCreateDate(new Date());
			agreeToken.setExpiredDate(dateExpire);
			agreeToken.setTokenId(id.toString());
			agreementTokenRepo.save(agreeToken);
			/*
			 * create start transaction in here and push en queue
			 */
			//return this.epmNapasHtml.loadHtml(htmlModel);
			String html = this.epmNapasHtml.loadHtml(htmlModel);
			logApiResult.setResponseBody(this.epmNapasHtml.loadHtml(htmlModel));
			log.info("html tra ra: "+html);
			return html;

		} catch (Exception exp) {
			log.error("EpmTransactionBusinessImpl.createTransactionToken() - error process create Transaction Token",
					exp);
			logApiResult.setResponseBody(exp.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(getIpAddress(request));
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return this.epmErrorHtml.loadHtml(this.napasUrlError, "Giao dịch không thành công",
				"Giao dịch không thành công");
	}

	@Override
	public String acceptRequestToken(NapasResult napasResult, String ipRequest, String url) {
		String result = this.napasUrlError;
		LogApiResult logApiResult = Utils.initLog("POST", url, null,
				Tools.convertModeltoJSON(napasResult), null);
		NapasResultDecode napasResultDecode = decodeToken(napasResult);
		try {
			log.info("acceptRequest napas : " + Tools.convertModeltoJSON(napasResultDecode));
			if (napasResultDecode.getTokenResult() != null) {
				EpmCreateTokenLog createTokenLog = createTokenLogService
						.findByTransactionId(napasResultDecode.getTokenResult().getOrderId());
				log.info("createTokenLog : " + Tools.convertModeltoJSON(createTokenLog));
				Double subId = checkSubtype(createTokenLog.getDescription(), napasResultDecode.getTokenResult().getOrderId());
				logApiResult.setTranId(StringUtils.nvl(napasResultDecode.getTokenResult().getOrderId(), ""));
				String token = napasResultDecode.getTokenResult().getToken();
				log.info("token the truoc khi ma hoa: " + token);
				String tokenCode = Encryptor.encrypt(napasEncryptorKey, napasEncryptorInitVector, token);
				log.info("token the sau khi ma hoa: " + tokenCode);
				if (createTokenLog != null
						&& (createTokenLog.getStatus() == null || "".equals(createTokenLog.getStatus()))) {
					createTokenLog.setStatus(napasResultDecode.getTokenResult().getResult());
					createTokenLog.setResponseDatetime(new Date());
					createTokenLog.setTokenId(token);
					createTokenLog.setTokenCreate(new Date());
					createTokenLog.setCardType("1");
					createTokenLog.setCardNumber(napasResultDecode.getTokenResult().getCard().getNumber());
					createTokenLog.setCardHolder(napasResultDecode.getTokenResult().getCard().getNameOnCard());
					createTokenLogRepo.save(createTokenLog);
				}
				if (EmpStatusConstain.SUCCESS.equalsIgnoreCase(napasResultDecode.getTokenResult().getResult())) {
					BankToken bankTokenOld = bankTokenRepo.findByTokenCode(tokenCode);
					log.info("Tim kiem token trong bank_token " + Tools.convertModeltoJSON(bankTokenOld));
					if (bankTokenOld == null || bankTokenOld.getId() == null) {
						log.info("Bat dau luu BankToken");
						AgreementToken agree = agreementTokenRepo.findByTranId(napasResultDecode.getTokenResult().getOrderId());
						BankToken bankToken = new BankToken();
						bankToken.setId(agree.getAgreementId());
						bankToken.setTokenName(napasResultDecode.getTokenResult().getCard().getScheme() + "-"
								+ napasResultDecode.getTokenResult().getCard().getNumber());
						bankToken.setCardType("1");
						bankToken.setBankCode(napasResultDecode.getTokenResult().getCard().getScheme());
						bankToken.setReference(StringUtils.nvl(createTokenLog.getDescription(), "0"));
						bankToken.setStatus("1");
						bankToken.setCreateDate(new Date());
						bankToken.setSubId(subId);;
						try {
							bankToken.setTokenCode(tokenCode);
							if (napasResultDecode.getTokenResult().getCard().getExpiry() != null) {
								bankToken.setExpiredMonth(
										napasResultDecode.getTokenResult().getCard().getExpiry().getMonth());
								bankToken.setExpiredYear(
										napasResultDecode.getTokenResult().getCard().getExpiry().getYear());
							}
						} catch (Exception e) {
							log.error("loi " + e.getMessage(), e);
						}
						try {
							bankTokenRepo.save(bankToken);
						} catch (Exception e) {
							log.error("loi luu token " + e.getMessage(), e);
						}
						log.info("ket thuc luu BankToken");
					}

					result = this.napasUrlSuccess;
				}
				
			}
			logApiResult.setResponseBody(result);
		} catch (Exception exp) {
			log.error("EpmTransactionBusinessImpl.acceptRequestToken() - error process accept Request Token", exp);
			logApiResult.setResponseBody(exp.getMessage());
		} finally {
			logApiResult.setEndTime(new Date());
			long processTime = logApiResult.getEndTime().getTime() - logApiResult.getCreateTime().getTime();
			logApiResult.setProcessTime(String.valueOf(processTime));
			logApiResult.setIpRequest(ipRequest);
			EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
		}
		return result;
	}

	@Override
	public String error(HttpServletRequest request) {
		String url = napasUrlError;
		String title = "Giao dịch không thành công";
		String message = "Giao dịch không thành công";
		return this.epmErrorHtml.loadHtml(url, title, message);
	}

	public NapasResultDecode decodeToken(NapasResult napasResult2) {
		Partners partners = partnersService.findByCode(EmpStatusConstain.PARTNER_CODE_NAPAS);
//		Gson gsonNapasResult = new Gson();
//		NapasResult napasResult2 = gsonNapasResult.fromJson(napasResult, NapasResult.class);
		String checksum = ConfigVNPay.Sha256(napasResult2.getData() + partners.getKey());
		if (checksum.equalsIgnoreCase(napasResult2.getChecksum())) {
			byte[] decodedBytes = Base64.getDecoder().decode(napasResult2.getData());
			String decodedString = new String(decodedBytes);
			Gson gson = new Gson();
			NapasResultDecode result = gson.fromJson(decodedString, NapasResultDecode.class);
			return result;
		}
		return null;
	}

	@Override
	public String acceptRequestRetryDR(PaymentResult napasResult) {
		try {
			EpmRequestModel epmRequest = new EpmRequestModel();
			log.info("acceptRequestRetryDR napas : " + Tools.convertModeltoJSON(napasResult));
			EpmTransaction transaction = epmTransactionRepo.findByTransactionID(napasResult.getOrder().getId());
			if (transaction == null) {
				log.error("=================>Khong tim thay du lieu trong bang tran EpmTransaction");
				throw new AppException(EPMMessageCode.API_EXCEPTION_CODE);
			}
			EpmTransactionModel empTransactionModel = new EpmTransactionModel();
			transaction.setPayStatus(EmpStatusConstain.FAILED);
			transaction.setIssueStatus(EmpStatusConstain.FAILED);
			empTransactionModel = transaction.setEpmTransactionModel(transaction);
			if (napasResult.getResult().equalsIgnoreCase(EmpStatusConstain.SUCCESS)
					&& !EmpStatusConstain.SUCC.equals(empTransactionModel.getPayStatus())) {
				empTransactionModel.setTransactionId((String) napasResult.getOrder().getId());
				empTransactionModel.setPayStatus(EmpStatusConstain.SUCC);
				empTransactionModel.setIssueStatus(EmpStatusConstain.PENDING);
				empTransactionModel.setCardNumber(napasResult.getSourceOfFunds().getProvided().getCard().getNumber());
				empTransactionModel
						.setCardHolder(napasResult.getSourceOfFunds().getProvided().getCard().getNameOnCard());
				empTransactionModel.setAmount(napasResult.getTransaction().getAmount());
				Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(empTransactionModel.getReference());
				if (isdnInfo.isPresent() && isdnInfo.get() != null) {
					empTransactionModel.setCustCode(isdnInfo.get().getCustCode());
				}
//				if (napasResultDecode.getTokenResult() != null
//						&& napasResultDecode.getTokenResult().getResult().equalsIgnoreCase(EmpStatusConstain.SUCCESS)) {
//					BankToken bankToken = new BankToken();
//					bankToken.setTokenId(napasResultDecode.getTokenResult().getToken());
//					bankToken.setTokenName(
//							transaction.getBankCode() + "-" + napasResultDecode.getTokenResult().getCard().getNumber());
//					bankToken.setCardType(transaction.getCardType());
//					bankToken.setBankCode(transaction.getBankCode());
//					bankToken.setReference(StringUtils.nvl(transaction.getFromReference(), "0"));
//					bankToken.setStatus("1");
//					bankToken.setCreateDate(new Date());
//					bankTokenRepo.save(bankToken);
//				}
				epmRequest.setAppName(appName);
				epmRequest.setTransactionData(empTransactionModel);
				epmRequest.setTransactionId(transaction.getTransactionId());
				epmRequest.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);
				/*
				 * isdnInfoRedisRepo.deleteById(transaction.getReference());
				 * promotionRedisRepo.deleteById(transaction.getReference());
				 */
//				queryDrRedisRepo.deleteById(transaction.getTransactionId());
//				this.mqpushDataEnqueueService.sendRequest(epmRequest);
				EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(empTransactionModel);
				epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
				epmTransactionDao.updatePaymentEPM(empTransactionModel);
				queryDrRedisRepo.deleteById(transaction.getTransactionId());
//				EpmTransactionModel epmTransactionModel = new EpmTransactionModel();
//				epmTransactionModel.setTransactionId(transaction.getTransactionId());
//				epmTransactionModel.setUserName(transaction.getUserName());
//				epmTransactionModel.setCallBackUrl(transaction.getCallBackUrl());
//
//				this.mqpushDataEnqueueService.sendRequest(epmTransactionModel);
//				try {
//					directUtils.sendCallBackUrlSuccess(transaction);
//				} catch (Exception e) {
//					log.error("loi sendCallBackUrlSuccess " + e.getMessage(), e);
//				}
				return transaction.getDirectUrl() + "&result=SUCCESS";
			}
			/*
			 * isdnInfoRedisRepo.deleteById(transaction.getReference());
			 * promotionRedisRepo.deleteById(transaction.getReference());
			 */
//			queryDrRedisRepo.deleteById(transaction.getTransactionId());
			epmAppDao.insertMerChantLog(Utils.genMerchantLog(empTransactionModel));
			epmTransactionDao.updatePaymentEPM(empTransactionModel);
			queryDrRedisRepo.deleteById(transaction.getTransactionId());
//			epmRequest.setAppName(appName);
//			epmRequest.setTransactionData(empTransactionModel);
//			epmRequest.setTransactionId(transaction.getTransactionId());
//			epmRequest.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);
//			this.mqpushDataEnqueueService.sendRequest(epmRequest);
//			this.mqpushDataEnqueueService.sendRequestLog(epmRequest, transaction.getTransactionId() , Constants.TYPE_RABBIT_MQ_SEND);
			return transaction.getDirectUrl() + "&result=FAIL";
		} catch (Exception exp) {
			log.error("EpmTransactionBusinessImpl.acceptRequestRetryDR() - error process accept Request RetryDR", exp);
		}
		return "error";
	}

	@Override
	public ResponseEntity<String> sendUserKyc(Object object) {
		RestTemplate restTemplate = new RestTemplate();
		String url = "";
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<Object> request = new HttpEntity<>(object, headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
		return response;
	}
	
	@Override
	public Double checkSubtype(String p_strsdt, String transactionId){
		log.info("checkSubtype start");
		Double iResult = null;
		try {
			HttpHeaders headers = new HttpHeaders();
	        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
	        SubtypeRequestModel subtypeRequestModel = new SubtypeRequestModel(); 
	        subtypeRequestModel.setP_strsdt(Tools.standardReference(p_strsdt));
	        log.info("url: "+this.paymentgatewayUrlCheckSubtype);
	        SubtypeResponseModel response = (SubtypeResponseModel) this.resttemplateBean.handleHttpRequest(this.paymentgatewayUrlCheckSubtype,
	                		this.iHttpRequestTimeout, HttpMethod.POST, headers, subtypeRequestModel, SubtypeResponseModel.class, transactionId);
	        log.info("checkSubtype response: "+ Tools.convertModeltoJSON(response));
	        String strResult = StringUtils.nvl(response.getData().getP_result(),"");
	        String strReturnValue = StringUtils.nvl(response.getData().getP_error(),"");
	        if (strResult != null && strReturnValue != null) {
				String[] p_subtype = strResult.split("\\|");
				log.info("checkSubtype p_subtype=" + p_subtype);
				return Double.valueOf( p_subtype[4]);
	        }
		} catch (Exception e) {
			log.error("EpmTransactionBusinessImpl.checkSubtype() - error "+e.getMessage(), e);
		}
		return iResult;
	}
}

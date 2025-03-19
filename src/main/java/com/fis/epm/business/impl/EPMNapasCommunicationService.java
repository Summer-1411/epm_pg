package com.fis.epm.business.impl;

import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.dao.EPMTransactionDao;
import com.fis.epm.repo.*;
import com.fis.epm.utils.*;
import com.fis.fw.common.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.entity.BankToken;
import com.fis.epm.entity.EpmTransaction;
//import com.fis.epm.mq.services.MQPushDataEnqueueService;
import com.fis.epm.napas.models.AuthenticationRedirectModel;
import com.fis.epm.napas.models.InputParametersModel;
import com.fis.epm.napas.models.NapasGetTokenModel;
import com.fis.epm.napas.models.NapasLoginResponseModel;
import com.fis.epm.napas.models.NapasRefundDomesticModel;
import com.fis.epm.napas.models.NapasRefundDomesticTransactionModel;
import com.fis.epm.napas.models.NapasResult;
import com.fis.epm.napas.models.NapasResultDecode;
import com.fis.epm.napas.models.OrderModel;
import com.fis.epm.napas.models.PaymentAutopayRequestModel;
import com.fis.epm.napas.models.PaymentAutopayResponseModel;
import com.fis.epm.napas.models.PaymentInterRequestModel;
import com.fis.epm.napas.models.PaymentInterResponseModel;
import com.fis.epm.napas.models.PaymentInterResponseModelOTP;
import com.fis.epm.napas.models.PaymentRequestModel;
import com.fis.epm.napas.models.PaymentResponseModel;
import com.fis.epm.napas.models.PaymentWithOTPRequestModel;
import com.fis.epm.napas.models.PaymentWithOTPResponseModel;
import com.fis.epm.napas.models.Secure3DModel;
import com.fis.epm.napas.models.SourceOfFundsModel;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.EpmRequestModel;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.IsdnInfoRedisModel;
import com.google.gson.Gson;

@Service
public class EPMNapasCommunicationService extends EPMBaseCommon {

    private static final Logger log = LoggerFactory.getLogger(EPMNapasCommunicationService.class);

    /**
     * load prop value
     */
    @Value(EPMApplicationProp.NAPAS_ROOT_URL_PROP)
    private String napasRootUrl = "";
    @Value(EPMApplicationProp.NAPAS_LOGIN_URL_PROP)
    private String napasUrl = "";
    @Value(EPMApplicationProp.NAPAS_PAYMENT_URL_PROP)
    private String napasPaymentUrl = "";
    @Value(EPMApplicationProp.NAPAS_PAYMENT_INTER_URL_PROP)
    private String napasPaymentInterUrl = "";
    @Value(EPMApplicationProp.NAPAS_PAYMENT_INTER_URL_OTP_PROP)
    private String napasPaymentInterOTPUrl = "";
    @Value(EPMApplicationProp.NAPAS_RETRIEVE_DOMESTIC_URL_PROP)
    private String napasRetrieveDomesticUrl = "";
    @Value(EPMApplicationProp.NAPAS_DELETE_TOKEN_URL_PROP)
    private String napasDeleteTokenUrl = "";
    @Value(EPMApplicationProp.NAPAS_GET_TOKEN_URL_PROP)
    private String napasGetTokenUrl = "";
    @Value(EPMApplicationProp.NAPAS_USER_NAME_PROP)
    private String napasUserName = "";
    @Value(EPMApplicationProp.NAPAS_CLIENT_PASSWORD_PROP)
    private String napasPassword = "";
    @Value(EPMApplicationProp.NAPAS_CLIENT_SECRET_PROP)
    private String napasClientSecret = "";
    @Value(EPMApplicationProp.NAPAS_CLIENT_ID_PROP)
    private String napasClientId = "";
    @Value(EPMApplicationProp.NAPAS_GRANT_TYPE_PROP)
    private String napasGrantType = "";
    @Value(EPMApplicationProp.HTTP_REQUEST_TIMEOUT_PROP)
    private int iHttpRequestTimeout = 40;

    /**
     * {@link Autowire}
     */
    @Autowired
    private ResttemplateBean resttemplateBean = null;
    private ObjectMapper mapper;

    @Autowired
    private EpmTransactionRepo epmTransactionRepo;

//    @Autowired
//    private MQPushDataEnqueueService mqPushDataEnqueueService;

    @Autowired
    private BankTokenRepo bankTokenRepo;

    @Autowired
    private IsdnInfoRedisRepo isdnInfoRedisRepo;

    @Autowired
    private PromotionRedisRepo promotionRedisRepo;

    @Autowired
    private EPMTransactionDao epmTransactionDao;

    @Autowired
    private EPMAppDao epmAppDao;

    @Autowired
    private QueryDRRedisRepo queryDrRedisRepo;

    public EPMNapasCommunicationService() {
        mapper = new ObjectMapper();
    }

    public NapasLoginResponseModel login() throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
            map.add("grant_type", this.napasGrantType);
            map.add("client_id", this.napasClientId);
            map.add("client_secret", this.napasClientSecret);
            map.add("username", this.napasUserName);
            map.add("password", this.napasPassword);
            return (NapasLoginResponseModel) this.resttemplateBean.handleHttpRequest(this.napasRootUrl + this.napasUrl,
                    this.iHttpRequestTimeout, HttpMethod.POST, headers, map, NapasLoginResponseModel.class);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentResponseModel paymentAtmLocal(PaymentRequestModel paymentRequestModel) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            PaymentRequestModel payment = new PaymentRequestModel();
            payment.setApiOperation("DATA_KEY");
            OrderModel order = new OrderModel();
//			order.setId("ORD_892646");
//			order.setCurrency("VND");
//			order.setAmount(10000);
            order.setId(paymentRequestModel.getOrder().getId());
            order.setAmount(paymentRequestModel.getOrder().getAmount());
            order.setCurrency(paymentRequestModel.getOrder().getCurrency());
            payment.setOrder(order);

            InputParametersModel inputParameters = new InputParametersModel();

//			inputParameters.setClientIP("192.168.1.1");
//			inputParameters.setDeviceId("0123456789");
//			inputParameters.setCardScheme("AtmCard");
//			inputParameters.setEnable3DSecure(false);
//			inputParameters.setEnvironment("WebApp");
            inputParameters.setClientIP(paymentRequestModel.getInputParameters().getClientIP());
            inputParameters.setDeviceId(paymentRequestModel.getInputParameters().getDeviceId());
            inputParameters.setCardScheme(paymentRequestModel.getInputParameters().getCardScheme());
            inputParameters.setEnable3DSecure(paymentRequestModel.getInputParameters().getEnable3DSecure());
            inputParameters.setEnvironment(paymentRequestModel.getInputParameters().getEnvironment());
            payment.setInputParameters(inputParameters);
            log.info("---> Token-Napas: " + res.getAccess_token());
            log.info("---> 4.1 Thanh toán thẻ/Thanh toán thẻ kèm tạo Token/Tạo Token: " + mapper.writeValueAsString(payment));
            return (PaymentResponseModel) this.resttemplateBean.handleHttpRequest(
                    this.napasRootUrl + this.napasPaymentUrl + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.POST, headers, payment, PaymentResponseModel.class, StringUtils.nvl(paymentRequestModel.getOrder().getId(),""));
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentWithOTPResponseModel paymentAtmLocalWithOtp(PaymentWithOTPRequestModel paymentWithOTPRequestModel, String orderId, String transactionId) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            PaymentWithOTPRequestModel payment = new PaymentWithOTPRequestModel();
            payment.setApiOperation("PURCHASE_OTP");
            OrderModel order = new OrderModel();
//			order.setCurrency("VND");
//			order.setAmount(10000);
            order.setCurrency(paymentWithOTPRequestModel.getOrder().getCurrency());
            order.setAmount(paymentWithOTPRequestModel.getOrder().getAmount());
            payment.setOrder(order);
            payment.setChannel("4121");

            SourceOfFundsModel sourceOfFundsModel = new SourceOfFundsModel();

//			sourceOfFundsModel.setToken("4005550856160019");
//			sourceOfFundsModel.setType("CARD");
            sourceOfFundsModel.setToken(paymentWithOTPRequestModel.getSourceOfFunds().getToken());
            sourceOfFundsModel.setType(paymentWithOTPRequestModel.getSourceOfFunds().getType());
            payment.setSourceOfFunds(sourceOfFundsModel);

            InputParametersModel inputParameters = new InputParametersModel();

            inputParameters.setClientIP(paymentWithOTPRequestModel.getInputParameters().getClientIP());
            inputParameters.setDeviceId(paymentWithOTPRequestModel.getInputParameters().getDeviceId());
            inputParameters.setCardScheme(paymentWithOTPRequestModel.getInputParameters().getCardScheme());
            inputParameters.setEnable3DSecure(paymentWithOTPRequestModel.getInputParameters().getEnable3DSecure());
            inputParameters.setEnvironment(paymentWithOTPRequestModel.getInputParameters().getEnvironment());
            payment.setInputParameters(inputParameters);
            log.info("---> Token-Napas: " + res.getAccess_token());
            log.info("---> 4.4 Thanh toán với token - thẻ nội địa xác thực OTP tại Napas: " + mapper.writeValueAsString(payment));
            return (PaymentWithOTPResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasPaymentInterUrl, orderId, transactionId) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.PUT, headers, payment, PaymentWithOTPResponseModel.class, transactionId);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentInterResponseModel paymentAtmInernational() throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            PaymentInterRequestModel payment = new PaymentInterRequestModel();
            payment.setApiOperation("PAY");
            OrderModel order = new OrderModel();
            order.setCurrency("VND");
            order.setAmount(10000L);
            payment.setOrder(order);
            payment.setChannel("4121");

            SourceOfFundsModel sourceOfFundsModel = new SourceOfFundsModel();

            sourceOfFundsModel.setToken("4005550856160019");
            sourceOfFundsModel.setType("CARD");
            payment.setSourceOfFunds(sourceOfFundsModel);
            return (PaymentInterResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasPaymentInterUrl, "ORD_123456", "TXN_123456")
                            + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.PUT, headers, payment, PaymentInterResponseModel.class);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentInterResponseModelOTP paymentAtmInernationalWith3dSercuse(PaymentInterRequestModel paymentInterRequestModel, String orderId, String transactionId, String secureId) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            PaymentInterRequestModel payment = new PaymentInterRequestModel();
            payment.setApiOperation("PAY_WITH_3DS");
            OrderModel order = new OrderModel();
            order.setCurrency(paymentInterRequestModel.getOrder().getCurrency());
            order.setAmount(paymentInterRequestModel.getOrder().getAmount());
            payment.setOrder(order);
            payment.setChannel("4121");

            Secure3DModel secure3dModel = new Secure3DModel();
            AuthenticationRedirectModel authenticationRedirectModel = new AuthenticationRedirectModel();
            authenticationRedirectModel.setResponseUrl(paymentInterRequestModel.getSecure3dModel().getAuthenticationRedirect().getResponseUrl());
            secure3dModel.setAuthenticationRedirect(authenticationRedirectModel);
            payment.setSecure3dModel(secure3dModel);

            SourceOfFundsModel sourceOfFundsModel = new SourceOfFundsModel();

            sourceOfFundsModel.setToken(paymentInterRequestModel.getSourceOfFunds().getToken());
            payment.setSourceOfFunds(sourceOfFundsModel);
            return (PaymentInterResponseModelOTP) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasPaymentInterOTPUrl, orderId, transactionId, secureId)
                            + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.PUT, headers, payment, PaymentInterResponseModelOTP.class, transactionId);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentWithOTPResponseModel refundDomestic(NapasRefundDomesticModel napasRefundDomesticModel, String orderId, String transactionId) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            NapasRefundDomesticModel refund = new NapasRefundDomesticModel();
            refund.setApiOperation("REFUND_DOMESTIC");
            refund.setChannel("4121");
            NapasRefundDomesticTransactionModel transactionModel = new NapasRefundDomesticTransactionModel();
            transactionModel.setAmount(napasRefundDomesticModel.getTransaction().getAmount());
            transactionModel.setCurrency(napasRefundDomesticModel.getTransaction().getCurrency());
            refund.setTransaction(transactionModel);
            log.info("---> Token-Napas: " + res.getAccess_token());
            log.info("---> 5.3 Hoàn trả - thẻ nội địa: " + mapper.writeValueAsString(refund));
            return (PaymentWithOTPResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasPaymentInterUrl, orderId, transactionId) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.PUT, headers, refund, PaymentWithOTPResponseModel.class, transactionId);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentWithOTPResponseModel retrieveDomestic(String orderId) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            log.info("---> Token-Napas: " + res.getAccess_token());
            return (PaymentWithOTPResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasRetrieveDomesticUrl, orderId) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.GET, headers, null, PaymentWithOTPResponseModel.class);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentWithOTPResponseModel deleteToken(String token) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            log.info("---> Token-Napas: " + res.getAccess_token());
            return (PaymentWithOTPResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasDeleteTokenUrl, token) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.DELETE, headers, null, PaymentWithOTPResponseModel.class);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public NapasGetTokenModel retrieveToken(String token) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            log.info("---> Token-Napas: " + res.getAccess_token());
            return (NapasGetTokenModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasGetTokenUrl, token) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.GET, headers, null, NapasGetTokenModel.class);
        } catch (Exception exp) {
            log.error("error process login on napas system", exp);
            throw exp;
        }
    }

    public PaymentAutopayResponseModel autopay(PaymentAutopayRequestModel paymentAutopayRequestModel, String orderId, String transactionId) throws Exception {
        try {
            NapasLoginResponseModel res = login();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            PaymentAutopayRequestModel payment = new PaymentAutopayRequestModel();
            payment.setApiOperation("PAY");
            OrderModel orderModel = OrderModel.builder()
                    .amount(paymentAutopayRequestModel.getOrder().getAmount())
                    .currency(paymentAutopayRequestModel.getOrder().getCurrency())
                    .build();
            payment.setOrder(orderModel);
            SourceOfFundsModel sourceOfFundsModel = SourceOfFundsModel.builder()
                    .type(paymentAutopayRequestModel.getSourceOfFunds().getType())
                    .token(paymentAutopayRequestModel.getSourceOfFunds().getToken())
                    .build();
            payment.setSourceOfFunds(sourceOfFundsModel);
            payment.setChannel(paymentAutopayRequestModel.getChannel());
            payment.setServiceCode("AUTOPAY");
            log.info("---> Token-Napas: " + res.getAccess_token());
            log.info("---> 4.7 Giao dịch AutoPay – chỉ áp dụng với Token thẻ Quốc tế: " + mapper.writeValueAsString(payment));
            return (PaymentAutopayResponseModel) this.resttemplateBean.handleHttpRequest(
                    String.format(this.napasRootUrl + this.napasPaymentInterUrl, orderId, transactionId) + "?access_token=" + res.getAccess_token(),
                    this.iHttpRequestTimeout, HttpMethod.PUT, headers, payment, PaymentAutopayResponseModel.class, transactionId);
        } catch (Exception exp) {
            log.error("error autopay ", exp);
            throw exp;
        }
    }

//	public String decodeToken(String napasResult) throws Exception {
//		Gson gsonNapasResult = new Gson();
//		NapasResult napasResult2 = gsonNapasResult.fromJson(napasResult, NapasResult.class);
//		byte[] decodedBytes = Base64.getDecoder().decode(napasResult2.getData());
//		String decodedString = new String(decodedBytes);
//		return decodedString;
//	}

    public NapasResultDecode decodeToken(String napasResult) {
        Gson gsonNapasResult = new Gson();
        NapasResult napasResult2 = gsonNapasResult.fromJson(napasResult, NapasResult.class);
        String checksum = ConfigVNPay.Sha256(napasResult2.getData() + napasClientSecret);
        if (checksum.equalsIgnoreCase(napasResult2.getChecksum())) {
            byte[] decodedBytes = Base64.getDecoder().decode(napasResult2.getData());
            String decodedString = new String(decodedBytes);
            Gson gson = new Gson();
            NapasResultDecode result = gson.fromJson(decodedString, NapasResultDecode.class);
            return result;
        }
        return null;
    }

    public String napasResult(String napasResult) throws Exception {
        NapasResultDecode result = decodeToken(napasResult);
        if (result == null) {
            throw new AppException("Thông tin giao dịch không đúng");
        }
        EpmRequestModel req = new EpmRequestModel();
        String transactionId = result.getPaymentResult().getOrder().getId();
        EpmTransaction epmTransaction = epmTransactionRepo.findByTransactionID(transactionId);
        if (epmTransaction == null) {
            throw new AppException("Không tìm thấy giao dịch");
        }
        //insert bank_token nếu emp_transaction có token_create == 1 và có thông tin token trả về
        if ("1".equals(epmTransaction.getTokenCreate())) {
            BankToken bankToken = new BankToken();
            bankToken.setTokenId(result.getTokenResult().getToken());
            bankToken.setTokenName(result.getTokenResult().getCard().getNumber());
            bankToken.setCardType(epmTransaction.getCardType());
            bankToken.setBankCode(epmTransaction.getBankCode());
            bankToken.setReference(StringUtil.nvl(epmTransaction.getFromReference(), "0"));
            bankToken.setStatus("1");
            bankToken.setCreateDate(new Date());
            bankTokenRepo.save(bankToken);
        }
        req.setActionType(EmpStatusConstain.UPDATE_PAYMENT_TRANSACCTION_ACTION);
        req.setAppName(this.appName);
        epmTransaction.setPayStatus(EmpStatusConstain.FAILED);
        String strReturn = "Thất bại";
        if (result.getTokenResult().getResult().equalsIgnoreCase("SUCCESS") && !EmpStatusConstain.SUCC.equals(epmTransaction.getPayStatus())) {
            epmTransaction.setCardNumber(result.getTokenResult().getCard().getNumber());
            epmTransaction.setCardHolder(result.getTokenResult().getCard().getNameOnCard());
            epmTransaction.setPayStatus(EmpStatusConstain.SUCC);
            strReturn = "Thành công";
            Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(epmTransaction.getReference());
            if (isdnInfo.isPresent() && isdnInfo.get() != null) {
                epmTransaction.setCustCode(isdnInfo.get().getCustCode());
            }
            /*
             * isdnInfoRedisRepo.deleteById(epmTransaction.getReference());
             * promotionRedisRepo.deleteById(epmTransaction.getReference());
             */
            EpmTransactionModel transactionModel = epmTransaction.setEpmTransactionModel(epmTransaction);
            EPMQueueManager.QUEUE_EXPORT_ORDER.enqueueNotify(transactionModel);
        }
        EpmTransactionModel transactionModel = epmTransaction.setEpmTransactionModel(epmTransaction);
//        req.setTransactionData(transactionModel);
//        req.setTransactionId(transactionId);
//        this.mqPushDataEnqueueService.sendRequest(req);
        epmAppDao.insertMerChantLog(Utils.genMerchantLog(transactionModel));
        epmTransactionDao.updatePaymentEPM(transactionModel);
        queryDrRedisRepo.deleteById(transactionModel.getTransactionId());
        //remove transaction in cache
        return strReturn;
    }
}

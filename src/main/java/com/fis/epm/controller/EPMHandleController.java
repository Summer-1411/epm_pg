package com.fis.epm.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;


import com.fis.epm.utils.JwtProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.NumberUtils;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.business.EPMBusiness;
import com.fis.epm.business.EpmTransactionBusiness;
import com.fis.epm.entity.BankToken;
import com.fis.epm.entity.LogApiResult;
import com.fis.epm.models.BankModel;
import com.fis.epm.models.CancelAutoDebitModel;
import com.fis.epm.models.ChargeModel;
import com.fis.epm.models.DataCheckSumModel;
import com.fis.epm.models.ParamModel;
import com.fis.epm.models.PosModel;
import com.fis.epm.models.PromotionModel;
import com.fis.epm.models.ProvinceModel;
import com.fis.epm.models.AutoDebitModel;
import com.fis.epm.models.TransactionModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.epm.request.models.AddInvoiceTransRequestModel;
import com.fis.epm.request.models.PrepaidRequestModel;
import com.fis.epm.request.models.PrepaidTokenRequestModel;
import com.fis.epm.request.models.PromotionRequestModel;
import com.fis.epm.service.BankService;
import com.fis.epm.service.BankTokenService;
import com.fis.epm.service.CardListService;
import com.fis.epm.service.PayAreaService;
import com.fis.epm.utils.EPMQueueManager;
import com.fis.epm.utils.Utils;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.gw.server.models.ResponseModel;
import com.google.gson.Gson;

@RestController
@RequestMapping(EPMApiConstant.EPM_ROOT_API_MAPPING)
public class EPMHandleController extends EPMBasicController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    Gson gson = new Gson();
    @Autowired
    private EPMBusiness epmBusiness = null;

    @Autowired
    private BankService bankService;

    @Autowired
    private CardListService cardListService;

    @Autowired
    private PayAreaService payAreaService;

    @Value("${com.fis.epm.api.get-type}")
    private String getType;

    @Autowired
    JwtProvider jwtProvider;
    
    @Value("${com.fis.system.debug.mode}")
	private boolean debugMode;
    
    @Value("${com.fis.system.debug.isdn}")
	private String isdnDebug;
    
    @Value("${com.fis.system.debug.amount}")
	private double amountDebug;
    
    @Autowired
    private EpmTransactionBusiness epmTransactionBusiness;
    
    @Autowired
    private BankTokenService bankTokenService;

    @PostMapping(EPMApiConstant.FIND_ALL_BANK_API_MAPPING)
    public ResponseModel loadAllBank(@RequestHeader(name = "Authorization") String token , HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.FIND_ALL_BANK_API_MAPPING, null, null, userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);

            List<BankModel> lstBank = new ArrayList();
            if (EPMApiConstant.API_GET_TYPE_CACHE.equals(getType)) {
                lstBank = bankService.fillBankModelsByRedis();
            } else {
                lstBank = epmBusiness.findAllBank();
            }
            String checkSum = "";
            String data = "";
            if (lstBank != null && !lstBank.isEmpty()) {
                data = Tools.convertDataToBase64(lstBank);
                checkSum = Tools.getCheckSum(data, key);
            }
            res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.ADD_INVOIVE_TRANS_API_MAPPING)
    public ResponseModel addInvoiveTransaction(@RequestBody DataCheckSumModel request,
                                               @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        String messValid = "";
        String code = "";
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.ADD_INVOIVE_TRANS_API_MAPPING, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            AddInvoiceTransRequestModel requestAddInvoice = gson.fromJson(decodedString,
                    AddInvoiceTransRequestModel.class);
            if (requestAddInvoice.getReference()==null)
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "reference";
            else if (Tools.stringIsNullOrEmty(requestAddInvoice.getReference()))
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "reference";
            else if (requestAddInvoice.getReference().length() > 20)
                messValid = EPMApiConstant.REFERENCE_LENGH_ERROR;
//            else if (checkIsdn(requestAddInvoice.getReference()))
//                code = EPMMessageCode.ERROR_ISDN;
            else if (requestAddInvoice.getAddress()==null)
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "address";
            else if (Tools.stringIsNullOrEmty(requestAddInvoice.getAddress()))
                messValid = EPMApiConstant.NOT_EMPTY + "address";
            else if (requestAddInvoice.getName()==null)
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "name";
            else if (Tools.stringIsNullOrEmty(requestAddInvoice.getName()))
                messValid = EPMApiConstant.NOT_EMPTY + "name";
            else if (requestAddInvoice.getShopId() == null)
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "shopId";
            else if (Tools.stringIsNullOrEmty(requestAddInvoice.getShopId().toString()))
                messValid = EPMApiConstant.NOT_EMPTY + "shopId";
            else if (requestAddInvoice.getTransactionId()==null)
                messValid = EPMApiConstant.NOT_NULL_OR_EMPTY + "transactionId";
            else if (Tools.stringIsNullOrEmty(requestAddInvoice.getTransactionId()))
                messValid = EPMApiConstant.NOT_EMPTY + "transactionId";
            else if (requestAddInvoice.getTransactionId().length() > 100)
                messValid = EPMApiConstant.REFERENCE_LENGH_ERROR;
            else if (!Tools.stringIsNullOrEmty(requestAddInvoice.getEmail()))
                if (requestAddInvoice.getEmail().length() > 50)
                    messValid = EPMApiConstant.EMAIL_LENGH_ERROR;
                else if (StringUtils.checkEmail(requestAddInvoice.getEmail()) == false) {
                    messValid = EPMApiConstant.ERROR_EMAIL;
                }
            if (messValid.equals("") && code.equals(""))
                code = this.epmBusiness.addInvoiveTransaction(requestAddInvoice);
            else if (code.equals("")) {
                if (messValid.indexOf(EPMApiConstant.NOT_NULL_OR_EMPTY) != -1)
                    code = EPMMessageCode.ERROR_REQUIRE_CODE;
                else if (messValid.indexOf(EPMApiConstant.NOT_EMPTY) != -1)
                    code = EPMMessageCode.ERROR_EMPTY_CODE;
                else if (messValid.indexOf(EPMApiConstant.LONG_ERR) != -1)
                    code = EPMMessageCode.ERROR_LONG_CODE;
                else if (messValid.indexOf(EPMApiConstant.EMAIL_ERR) != -1)
                    code = EPMMessageCode.ERROR_EMAIL;
            }
            res = this.buildResValid(messValid, code);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            logApiResult.setTranId(requestAddInvoice.getTransactionId());
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            if (exp.getMessage().toUpperCase().indexOf("STRING") != -1)
                res = this.buildResValid("shopId sai định dạng", EPMMessageCode.ERROR_NUMBER_FORMAT);
            else
                res = this.buildExceptionResponse(exp);
                logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.ADD_PREPAID_TOKEN_API_MAPPING)
    public ResponseModel prepaidTokenHandle(@RequestBody DataCheckSumModel request,
                                            @RequestHeader(name = "Authorization") String token , HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.ADD_PREPAID_TOKEN_API_MAPPING, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            PrepaidTokenRequestModel prepaidRequest = gson.fromJson(decodedString, PrepaidTokenRequestModel.class);
            res = this.epmBusiness.prepaidTokenHandle(prepaidRequest);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.DELETE_TOKEN_API_MAPPING)
    public ResponseModel deleteToken(@RequestBody DataCheckSumModel request,
                                     @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.DELETE_TOKEN_API_MAPPING, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            String tokenId = (String) requestValue.get("token-id");
            String isdn = (String) Tools.stringNvl(requestValue.get("isdn"), "");
            if(isdn.startsWith("0")) {
            	isdn = isdn.substring(1,isdn.length());
    		}
            if (requestValue.get("token-id")==null)
                return this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "token-id", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if(StringUtils.isNullOrEmpty(tokenId))
            	return this.buildResValid(EPMApiConstant.NOT_EMPTY + "token-id", EPMMessageCode.ERROR_EMPTY_CODE);
            else if (requestValue.get("isdn")==null)
                return this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "isdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if(StringUtils.isNullOrEmpty(isdn))
            	return this.buildResValid(EPMApiConstant.NOT_EMPTY + "isdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if(checkIsdn(isdn))
//            	return this.buildResValid(EPMApiConstant.ERROR_ISDN+"isdn", EPMMessageCode.ERROR_ISDN);
            res = this.epmBusiness.deleteToken(tokenId, isdn);
            String checkSum = "";
            String data = "";
            if (res.getPayload() != null && !res.getMessage().equals("FAIL")) {
                data = Tools.convertDataToBase64(res.getPayload());
                checkSum = Tools.getCheckSum(data, key);
                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            }
            else res= this.buildSuccessedResponse(null);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.LOAD_ALL_CHARGER_INFO_API_MAPPING)
    public ResponseModel findAllChargeInfo(@RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_ALL_CHARGER_INFO_API_MAPPING, null, null,
                userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            List<ChargeModel> lstData = new ArrayList();
            if (EPMApiConstant.API_GET_TYPE_CACHE.equals(getType)) {
                lstData = cardListService.findAllCardListRedisModels();
            } else {
                lstData = this.epmBusiness.findAllChargeInfo();
            }
            String checkSum = "";
            String data = "";
            if (lstData != null && !lstData.isEmpty()) {
                data = Tools.convertDataToBase64(lstData);
                checkSum = Tools.getCheckSum(data, key);
            }
            res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_ALL_TOKEN_API_MAPPING)
//    public ResponseModel findAllToken(@RequestParam(name = "fromIsdn") String fromIsdn)
//            throws Exception {
//        ResponseModel res = new ResponseModel();
//        try {
//            if (fromIsdn == null) {
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "fromIsdn",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            } else if (Tools.stringIsNullOrEmty(fromIsdn))
//                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "fromIsdn",
//                        EPMMessageCode.ERROR_EMPTY_CODE);
//
//            else if (checkIsdn(fromIsdn))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"fromIsdn", EPMMessageCode.ERROR_ISDN);
//            else {
//                List<com.fis.epm.models.TokenModel> tokenModels = this.epmBusiness.findAllToken(fromIsdn);
//                res = this.buildSuccessedResponse(tokenModels);
//            }
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_ALL_TOKEN_API_MAPPING_CS)
    public ResponseModel findAllTokenCS(@RequestBody DataCheckSumModel request,
                                        @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) throws Exception {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_ALL_TOKEN_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            String fromIsdn = (String) requestValue.get("fromIsdn");
            if (fromIsdn == null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "số thuê bao fromIsdn",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(fromIsdn))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "số thuê bao fromIsdn",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (checkIsdn(fromIsdn))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"fromIsdn", EPMMessageCode.ERROR_ISDN);
            else {
                List<com.fis.epm.models.TokenModel> tokenModels = this.epmBusiness.findAllToken(fromIsdn);
                if(tokenModels.size()>0)
                res = this.buildSuccessedResponse(tokenModels);
                ResponseModel resTem = res;
                String checkSum = "";
                String data = null;
                if (resTem.getPayload() != null && tokenModels.size()>0) {
                    data = Tools.convertDataToBase64(resTem.getPayload());
                    checkSum = Tools.getCheckSum(data, key);
                    res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
                }else res = this.buildSuccessedResponse(data);
                
                //logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            }
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_DETAIL_TRANSACTION_API_MAPPING)
//    public ResponseModel findDetailTransaction(@RequestParam("transaction-id") String transactionId) throws Exception {
//        ResponseModel res = new ResponseModel();
//        try {
//            if (Tools.stringIsNullOrEmty(transactionId))
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "transaction-id",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (transactionId.length() > 100) {
//                res = this.buildResValid(EPMApiConstant.TRANS_LENGH_ERROR, EPMMessageCode.ERROR_LONG_CODE);
//            } else
//                res = this.buildSuccessedResponse(this.epmBusiness.findDetailTransaction(transactionId));
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_DETAIL_TRANSACTION_API_MAPPING_CS)
    public ResponseModel findDetailTransactionCS(@RequestBody DataCheckSumModel request,
                                                 @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) throws Exception {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_DETAIL_TRANSACTION_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            String transactionId = (String) requestValue.get("transaction-id");
            if (transactionId==null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "transaction-id",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(transactionId))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "transaction-id",
                        EPMMessageCode.ERROR_EMPTY_CODE);
            else if (transactionId.length() > 100)
                res = this.buildResValid(EPMApiConstant.TRANS_LENGH_ERROR, EPMMessageCode.ERROR_REQUIRE_CODE);
            else {
                logApiResult.setTranId(transactionId);
                TransactionModel dataRes = this.epmBusiness.findDetailTransaction(transactionId);
                String checkSum = "";
                String data = "";
                if (dataRes != null) {
                    data = Tools.convertDataToBase64(dataRes);
                    checkSum = Tools.getCheckSum(data, key);
                }
                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            }
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_PARAM_BY_MSISDN_API_MAPPING)
//    public ResponseModel findBillChargeParamByMsisdn(@RequestParam(name = "msisdn") String msisdn) throws Exception {
//        ResponseModel res = new ResponseModel();
//        try {
//            if (msisdn == null)
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "số thuê bao",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (Tools.stringIsNullOrEmty(msisdn))
//                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "số thuê bao",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (msisdn.length() > 10 || checkIsdn(msisdn))
//                res = this.buildResValid("", EPMMessageCode.ERROR_ISDN);
//            else {
//            	ParamModel check = this.epmBusiness.findBillChargeParamByMsisdn(msisdn);
//            	if(check != null && check.getParam()!= null)
//            		res = this.buildSuccessedResponse(check);
//            	else 
//            		res = this.buildSuccessedResponse(null);
//            }
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_PARAM_BY_MSISDN_API_MAPPING_CS)
    public ResponseModel findBillChargeParamByMsisdn(@RequestBody DataCheckSumModel request,
                                                     @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) throws Exception {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_PARAM_BY_MSISDN_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            String msisdn = (String) requestValue.get("msisdn");
            if (msisdn == null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "msisdn",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(msisdn))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "msisdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if (checkIsdn(msisdn))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"msisdn", EPMMessageCode.ERROR_ISDN);
            else {
                ParamModel dataRes = this.epmBusiness.findBillChargeParamByMsisdn(msisdn);
                String checkSum = "";
                String data = "";
                if (dataRes != null) {
                    data = Tools.convertDataToBase64(dataRes);
                    checkSum = Tools.getCheckSum(data, key);
                }
                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
                //logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            }
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_POS_ADDRESS_API_MAPPING)
//    public ResponseModel findPosAddress(@RequestParam(name = "shop-id") Long shopId) {
//        ResponseModel res = new ResponseModel();
//        try {
//            if (shopId == null)
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "shop-id",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else
//                res = this.buildSuccessedResponse(this.epmBusiness.findPosAddress(shopId));
//        } catch (Exception exp) {
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_POS_ADDRESS_API_MAPPING_CS)
    public ResponseModel findPosAddressCS(@RequestBody DataCheckSumModel request,
                                          @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_POS_ADDRESS_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            if(requestValue.get("shop-id")==null)
            	return this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "shop-id",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(requestValue.get("shop-id").toString()))
                return this.buildResValid(EPMApiConstant.NOT_EMPTY + "shop-id",
                        EPMMessageCode.ERROR_EMPTY_CODE);
            String shopId = StringUtils.nvl(requestValue.get("shop-id"), "0");
            List<String> lstData = this.epmBusiness.findPosAddress(Long.parseLong(shopId));
            String checkSum = "";
            String data = "";
            if (lstData != null && !lstData.isEmpty()) {
                data = Tools.convertDataToBase64(lstData);
                checkSum = Tools.getCheckSum(data, key);
                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            }else res = this.buildSuccessedResponse(null);
            
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_POS_API_MAPPING)
//    public ResponseModel findAllPosByProvince(@RequestParam(name = "province-id") Long provinceId) {
//        ResponseModel res = new ResponseModel();
//        try {
//
//            if (provinceId == null)
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "provinceId",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else {
//                res = this.buildSuccessedResponse(this.epmBusiness.findAllPosByProvince(provinceId));
//            }
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_POS_API_MAPPING_CS)
    public ResponseModel findAllPosByProvinceCS(@RequestBody DataCheckSumModel request,
                                                @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_POS_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            if(requestValue.get("province-id")==null)
            	return this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "province-id",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(requestValue.get("province-id").toString()))
                return this.buildResValid(EPMApiConstant.NOT_EMPTY + "province-id",
                        EPMMessageCode.ERROR_EMPTY_CODE);
            String provinceId = StringUtils.nvl(requestValue.get("province-id"), "0");
            List<PosModel> lstData = this.epmBusiness.findAllPosByProvince(Long.parseLong(provinceId));
            String checkSum = "";
            String data = "";
            if (lstData != null && !lstData.isEmpty()) {
                data = Tools.convertDataToBase64(lstData);
                checkSum = Tools.getCheckSum(data, key);
            }
            res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_PROMOTION_API_MAPPING)
//    public ResponseModel loadPromotion(@RequestBody PromotionRequestModel request,
//                                       @RequestHeader(name = "Authorization") String token) {
//        ResponseModel res = new ResponseModel();
//        try {
//        	
//            if (Tools.stringIsNullOrEmty(request.getBankCode()))
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "bankCode",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (bankService.findAllByBankCode(request.getBankCode()) == null)
//                res = this.buildResValid("", EPMMessageCode.ERROR_BANK);
//            else if (Tools.stringIsNullOrEmty(request.getIsdn()))
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "isdn", EPMMessageCode.ERROR_REQUIRE_CODE);
//            else if (request.getIsdn().length() > 10 || checkIsdn(request.getIsdn()))
//                res = this.buildResValid("", EPMMessageCode.ERROR_ISDN);
//            else {
//                String userName = jwtProvider.getUserNameFromTokenRSA(token);
//                res = this.buildSuccessedResponse(this.epmBusiness.loadPromotion(request, userName));
//            }
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_PROMOTION_API_MAPPING_CS)
    public ResponseModel loadPromotionCS(@RequestBody DataCheckSumModel request,
                                         @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_PROMOTION_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            PromotionRequestModel requestValue = gson.fromJson(decodedString, PromotionRequestModel.class);
            if(requestValue.getIsdn()==null)
            	res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "isdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(requestValue.getIsdn()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "isdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if (checkIsdn(requestValue.getIsdn()))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"isdn", EPMMessageCode.ERROR_ISDN);
            else if (requestValue.getBankCode()==null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "bankCode",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(requestValue.getBankCode()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "bankCode",
                        EPMMessageCode.ERROR_EMPTY_CODE);
            else if (bankService.findAllByBankCode(requestValue.getBankCode()) == null)
                res = this.buildResValid("", EPMMessageCode.ERROR_BANK);
            else {
            	requestValue.setBankCode(requestValue.getBankCode().toUpperCase().trim());
                PromotionModel dataReturn = this.epmBusiness.loadPromotion(requestValue, userName);
                //if(dataReturn!=null && dataReturn.getValue()!=null){
	                String checkSum = "";
	                String data = "";
	                if (dataReturn != null) {
	                    data = Tools.convertDataToBase64(dataReturn);
	                    checkSum = Tools.getCheckSum(data, key);
	                }
	                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
	                //logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
                //}
                //else res = this.buildResValid("", EPMMessageCode.ERROR_NOT_FOUND_ISDN);
            }
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

//    @PostMapping(EPMApiConstant.LOAD_PROVINCE_API_MAPPING)
//    public ResponseModel loadProvinceById(@RequestParam(name = "province-id") Long provinceId) {
//        ResponseModel res = new ResponseModel();
//
//        try {
//            if (provinceId == null)
//                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "provinceId",
//                        EPMMessageCode.ERROR_REQUIRE_CODE);
//            else {
//                List<ProvinceModel> provinceModels = new ArrayList();
//                if (EPMApiConstant.API_GET_TYPE_CACHE.equals(getType)) {
//                    if (provinceId != 0) {
//                        ProvinceModel provinceModel = payAreaService.findById(provinceId);
//                        if (provinceModel != null)
//                            provinceModels.add(provinceModel);
//                    } else
//                        provinceModels = payAreaService.findAllAreaRedisModels();
//                } else{
//                    provinceModels = this.epmBusiness.findAllProvince(provinceId);
//                    if(provinceModels!=null && provinceModels.size()==0)
//                    	return res = this.buildSuccessedResponse(null);
//                }
//                res = this.buildSuccessedResponse(provinceModels);
//            }
//        } catch (Exception exp) {
//            logger.error(exp.getMessage(), exp);
//            res = this.buildExceptionResponse(exp);
//        }
//        return res;
//    }

    @PostMapping(EPMApiConstant.LOAD_PROVINCE_API_MAPPING_CS)
    public ResponseModel loadProvinceByIdCS(@RequestBody DataCheckSumModel request,
                                            @RequestHeader(name = "Authorization", required = false) String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.LOAD_PROVINCE_API_MAPPING_CS, null,
                Tools.convertModeltoJSON(request), userID);

        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            if(requestValue.get("province-id")== null)
            	return res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "province-id",
                        EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(requestValue.get("province-id").toString()))
                return res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "province-id",
                        EPMMessageCode.ERROR_EMPTY_CODE);
            Long provinceId = Long.parseLong(StringUtils.nvl(requestValue.get("province-id"), "0"));
            String checkSum = "";
            String data = "";
            if (EPMApiConstant.API_GET_TYPE_CACHE.equals(getType)) {
                if (provinceId != 0) {
                    ProvinceModel provinceModel = payAreaService.findById(provinceId);
                    if (provinceModel != null) {
                        data = Tools.convertDataToBase64(provinceModel);
                        checkSum = Tools.getCheckSum(data, key);
                    }
                } else {
                    List<ProvinceModel> lstData = payAreaService.findAllAreaRedisModels();
                    if (lstData != null && !lstData.isEmpty()) {
                        data = Tools.convertDataToBase64(lstData);
                        checkSum = Tools.getCheckSum(data, key);
                    }
                }
            } else {
                List<ProvinceModel> lstData = this.epmBusiness.findAllProvince(provinceId);
                if (lstData != null && !lstData.isEmpty()) {
                    data = Tools.convertDataToBase64(lstData);
                    checkSum = Tools.getCheckSum(data, key);
                }
            }
            res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.PREPAID_HANDLE_API_MAPPING)
    public ResponseModel prepaidHandle(@RequestBody DataCheckSumModel request,
                                       @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.PREPAID_HANDLE_API_MAPPING, null,
                Tools.convertModeltoJSON(request), userID);
        String mess = "";
        String code = "";
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            PrepaidRequestModel requestCreate = gson.fromJson(decodedString, PrepaidRequestModel.class);
            if (requestCreate.getAmount() == 0)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "amount";
            else if (requestCreate.getPayAmount() == 0)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "payAmount";
            else if (requestCreate.getBankCode()==null)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "bankCode";
            else if (StringUtils.isNullOrEmpty(requestCreate.getBankCode()))
                mess = EPMApiConstant.NOT_EMPTY + "bankCode";
            else if (requestCreate.getBankCode().length() > 50)
                mess = EPMApiConstant.BANKCODE_LENGH_ERROR;
            else if (requestCreate.getFromIsdn()==null||requestCreate.getIsdn()==null)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "số thuê bao (isdn + fromIsdn)";
            else if (StringUtils.isNullOrEmpty(requestCreate.getFromIsdn())
                    || StringUtils.isNullOrEmpty(requestCreate.getIsdn()))
                mess = EPMApiConstant.NOT_EMPTY + "số thuê bao (isdn + fromIsdn)";
//            else if (checkIsdn(requestCreate.getFromIsdn()) || checkIsdn(requestCreate.getIsdn()))
//                mess = EPMApiConstant.ERROR_ISDN+"isdn hoặc fromIsdn";
            else if (requestCreate.getTokenCreated()==null)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "tokenCreated";
            else if (StringUtils.isNullOrEmpty(requestCreate.getTokenCreated()))
                mess = EPMApiConstant.NOT_EMPTY + "tokenCreated";
            else if (requestCreate.getTokenCreated().length() > 1)
                mess = EPMApiConstant.TOKENCREATE_LENGH_ERROR;
            else if (requestCreate.getRedirectUrl()==null)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "redirectUrl";
            else if (StringUtils.isNullOrEmpty(requestCreate.getRedirectUrl()))
                mess = EPMApiConstant.NOT_EMPTY + "redirectUrl";
            else if (requestCreate.getCallBackUrl()==null)
                mess = EPMApiConstant.NOT_NULL_OR_EMPTY + "callBackUrl";
            else if (StringUtils.isNullOrEmpty(requestCreate.getCallBackUrl()))
                mess = EPMApiConstant.NOT_EMPTY + "callBackUrl";
            else if (!StringUtils.isNullOrEmpty(requestCreate.getTokenId()) && !StringUtils.isNullOrEmpty(requestCreate.getFromIsdn())) {
            	Double subId = epmTransactionBusiness.checkSubtype(requestCreate.getFromIsdn(), null);
            	String subIdTr = Tools.stringNvl(subId, "");
            	logger.info("subId "+subId);
            	BankToken bankToken = bankTokenService.findByBankTokenId(requestCreate.getTokenId());
            	logger.info("bankToken "+Tools.convertModeltoJSON(bankToken));
            	String bankTokenSubIdTr = Tools.stringNvl(bankToken.getSubId(), "");
            	if(StringUtils.isNullOrEmpty(bankTokenSubIdTr) || StringUtils.isNullOrEmpty(subIdTr) || !subIdTr.toString().equals(bankTokenSubIdTr.toString()) ) {
            		mess =  EPMApiConstant.ERROR_SUBID;
            	}
            }
            else if(debugMode) {
        		if(!isdnDebug.equalsIgnoreCase(requestCreate.getFromIsdn())) {
        			mess = EPMApiConstant.ISDN_DEBUG ;
        		} else if(amountDebug!=requestCreate.getAmount()) {
        			mess = EPMApiConstant.AMOUNT_DEBUG ;
        		}
        	}
            if (mess.equals("")) {
	            	String ip = getIpAddress(servletRequest);
	                ResponseModel resTemp = this.epmBusiness.prepaidHandle(requestCreate, userName, ip);
	                if (resTemp.getStatus().indexOf("API-000") == -1) {
	                    logApiResult.setResponseBody(Tools.convertModeltoJSON(resTemp));
	                    return resTemp;
	                }
	                String checkSum = "";
	                String data = "";
	                if (resTemp.getPayload() != null) {
	                    EpmTransactionModel transaction = (EpmTransactionModel) resTemp.getPayload();
	                    logApiResult.setTranId(transaction.getTransactionId());
	                    data = Tools.convertDataToBase64(resTemp.getPayload());
	                    checkSum = Tools.getCheckSum(data, key);
	                }
	                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
	                //logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            	
            } else {
                if (mess.indexOf(EPMApiConstant.NOT_EMPTY) != -1)
                    code = EPMMessageCode.ERROR_EMPTY_CODE;
                else if (mess.indexOf(EPMApiConstant.ERROR_ISDN) != -1)
                    code = EPMMessageCode.ERROR_ISDN;
                else if (mess.indexOf(EPMApiConstant.LONG_ERR) != -1)
                    code = EPMMessageCode.ERROR_LONG_CODE;
                else if (mess.indexOf(EPMApiConstant.NOT_NULL_OR_EMPTY) != -1)
                    code = EPMMessageCode.ERROR_REQUIRE_CODE;
                res = this.buildResValid(mess, code);
                //logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            }
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            if (exp.getMessage().toUpperCase().indexOf("STRING") != -1)
                res = this.buildResValid("amount || payAmount sai định dạng", EPMMessageCode.ERROR_NUMBER_FORMAT);
            else
                res = this.buildExceptionResponse(exp);
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
        } finally {
        	logApiResult.setEndTime(new Date());
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            logApiResult.setIpRequest(getIpAddress(servletRequest));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.GEN_DATA_CHECK_SUM)
    public ResponseModel genDataCheckSum(@RequestBody Map request,
                                         @RequestParam(name = "key", required = true) String key) {
        ResponseModel res = new ResponseModel();
        try {
            String checkSum = "";
            String data = "";
            data = Tools.convertDataToBase64(request);
            checkSum = Tools.getCheckSum(data, key);
            res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.REGISTER_AUTO_DEBIT)
    public ResponseModel registerAutoDebit(@RequestBody DataCheckSumModel request,
                                           @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.REGISTER_AUTO_DEBIT, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            ObjectMapper mapper = new ObjectMapper();
            AutoDebitModel requestCreate = mapper.readValue(decodedString, AutoDebitModel.class);
            if (requestCreate.getMsisdn()==null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "msisdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(requestCreate.getMsisdn()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "msisdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if (checkIsdn(requestCreate.getMsisdn()))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"msisdn", EPMMessageCode.ERROR_ISDN);
            else if (requestCreate.getFromIsdn()==null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY + "fromIsdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(requestCreate.getFromIsdn()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "fromIsdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if (checkIsdn(requestCreate.getFromIsdn()))
//                res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"fromIsdn", EPMMessageCode.ERROR_ISDN);
            else if (requestCreate.getSubType()!= null && requestCreate.getSubType().length() > 5)
                res = this.buildResValid("subType vượt quá 5 kí tự", EPMMessageCode.ERROR_LONG_CODE);
            else if (requestCreate.getAmount()!= null && checkNumber(requestCreate.getAmount()))
                res = this.buildResValid("Sai định dạng số: Amount", EPMMessageCode.ERROR_NUMBER_FORMAT);
            else if (requestCreate.getThresholdAmount() != null && checkNumber(requestCreate.getThresholdAmount()))
                res = this.buildResValid("Sai định dạng số: thresholdAmount", EPMMessageCode.ERROR_NUMBER_FORMAT);
            else if (requestCreate.getCodeBank()!=null && requestCreate.getCodeBank().length() > 15)
                res = this.buildResValid("codeBank vượt quá 15 kí tự", EPMMessageCode.ERROR_LONG_CODE);
            else if (requestCreate.getSubId()!= null && requestCreate.getSubId().length() > 15)
                res = this.buildResValid("subId vượt quá 15 kí tự", EPMMessageCode.ERROR_LONG_CODE);
            else if (requestCreate.getTokenizer()!=null&&requestCreate.getTokenizer().length() > 25)
                res = this.buildResValid("tokenizer vượt quá 25 kí tự", EPMMessageCode.ERROR_LONG_CODE);
            else if (requestCreate.getSubIdReg() == null)
                res = this.buildResValid("Trường bắt buộc: subIdReg", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (Tools.stringIsNullOrEmty(requestCreate.getSubIdReg()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY + "SubIdReg", EPMMessageCode.ERROR_EMPTY_CODE);
            else if (checkNumber(requestCreate.getSubIdReg()))
                res = this.buildResValid("Sai định dạng số: SubIdReg", EPMMessageCode.ERROR_NUMBER_FORMAT);
            else {
                boolean check = this.epmBusiness.registerAutoDebit(requestCreate);
                if (check)
                    res = this.buildSuccessedResponse();
                else
                    res = this.buildResValid("", EPMMessageCode.ERROR_EXESTS_AUTODEBIT_INFO);
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
        } finally {
        	logApiResult.setIpRequest(getIpAddress(servletRequest));
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }


    @PostMapping(EPMApiConstant.CHECK_TOKEN_FROM_ISDN)
    public ResponseModel checkTokenFromIsdn(@RequestBody DataCheckSumModel request,
                                            @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.CHECK_TOKEN_FROM_ISDN, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String checkSum = "";
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            if (requestValue.get("msisdn") == null)
                res = this.buildResValid(EPMApiConstant.NOT_NULL_OR_EMPTY+"msisdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(requestValue.get("msisdn").toString()))
                res = this.buildResValid(EPMApiConstant.NOT_EMPTY+"msisdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if(checkIsdn(requestValue.get("msisdn").toString()))
//            	res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"msisdn",EPMMessageCode.ERROR_ISDN);
            else {
                String result = this.epmBusiness.checkTokenFromIsdn(requestValue.get("msisdn").toString());
                if(StringUtils.isNullOrEmpty(result))
                	return this.buildSuccessedResponse(null);
                if (result.length() > 0)
                    result = result.substring(0, result.length() - 1);
                if (result != "") {
                    result = Tools.convertDataToBase64(result);
                    checkSum = Tools.getCheckSum(result, key);
                }
                res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(result).checkSum(checkSum).build());
            }
        } catch (Exception exp) {
            logger.error(exp.getMessage(), exp);
            res = this.buildExceptionResponse(exp);
        } finally {
        	logApiResult.setIpRequest(getIpAddress(servletRequest));
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    @PostMapping(EPMApiConstant.CANCEL_AUTO_DEBIT)
    public ResponseModel cancelAutoDebit(@RequestBody DataCheckSumModel request, @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.CANCEL_AUTO_DEBIT, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String checkSum = "";
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
            if (requestValue.get("msisdn") == null)
                res = this.buildResValid("Trường bắt buộc: msisdn", EPMMessageCode.ERROR_REQUIRE_CODE);
            else if (StringUtils.isNullOrEmpty(requestValue.get("msisdn").toString()))
                res = this.buildResValid("Không được để trống: msisdn", EPMMessageCode.ERROR_EMPTY_CODE);
//            else if(checkIsdn(requestValue.get("msisdn").toString()))
//            	res = this.buildResValid(EPMApiConstant.ERROR_ISDN+"msisdn",EPMMessageCode.ERROR_ISDN);
            else {
                res = this.epmBusiness.cancelAutoDebit(requestValue.get("msisdn").toString(), userName);
                String data = "";
                if (res.getPayload() != null) {
                    data = Tools.convertDataToBase64(res.getPayload());
                    checkSum = Tools.getCheckSum(data, key);
                    res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            res = this.buildExceptionResponse(ex);
        } finally {
        	logApiResult.setIpRequest(getIpAddress(servletRequest));
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }

    // @PostMapping(EPMApiConstant.DECODE_DATA_CHECK_SUM)
    // public ResponseModel decodeDataCheckSum(@RequestBody String request) {
    // ResponseModel res = new ResponseModel();
    // try {
    // byte[] decodedBytes = Base64.getDecoder().decode(request);
    // String decodedString = new String(decodedBytes);
    // Map requestValue = gson.fromJson(decodedString, Map.class);
    // res = this.buildSuccessedResponse(requestValue);
    // } catch (Exception exp) {
    // res = this.buildExceptionResponse(exp);
    // }
    // return res;
    // }
    
    //Check điện thoại
    public boolean checkIsdn(String num) {
        if (num.length() > 0 && num.charAt(0) == '0' || num.length()>10 || num.length()<9)
            return true;
        // Check nếu nhập chữ
        return checkNumber(num);
    }
    
    //Check số
    public boolean checkNumber(String num) {
        // Check nếu nhập chữ
        Pattern pattern = Pattern.compile("\\D");
        return pattern.matcher(num).find();
    }
    
    @PostMapping(EPMApiConstant.GET_INFO_TRANSACTION)
    public ResponseModel getInfoTransaction(@RequestBody DataCheckSumModel request, @RequestHeader(name = "Authorization") String token, HttpServletRequest servletRequest) {
        ResponseModel res = new ResponseModel();
        Integer userID = Integer.parseInt(jwtProvider.getUserIdFromTokenRSA(token));
        LogApiResult logApiResult = Utils.initLog("POST",
                EPMApiConstant.EPM_ROOT_API_MAPPING + EPMApiConstant.GET_INFO_TRANSACTION, null,
                Tools.convertModeltoJSON(request), userID);
        try {
            String checkSum = "";
            String userName = jwtProvider.getUserNameFromTokenRSA(token);
            String key = epmBusiness.getKeyCheckSum(userName);
            if (!Tools.validCheckSum(request.getData(), key, request.getCheckSum())) {
                return this.buildResValid("", EPMMessageCode.ERROR_CHECKSUM);
            }
            byte[] decodedBytes = Base64.getDecoder().decode(request.getData());
            String decodedString = new String(decodedBytes);
            Map requestValue = gson.fromJson(decodedString, Map.class);
			if (StringUtils.isNullOrEmpty(StringUtils.nvl(requestValue.get("trans_id"), ""))) {
				if (requestValue.get("from_time") == null)
					return this.buildResValid("Trường bắt buộc: from_time", EPMMessageCode.ERROR_REQUIRE_CODE);
				else if (StringUtils.isNullOrEmpty(requestValue.get("from_time").toString()))
					return this.buildResValid("Không được để trống: from_time", EPMMessageCode.ERROR_EMPTY_CODE);
				else if (requestValue.get("end_time") == null)
					return this.buildResValid("Trường bắt buộc: end_time", EPMMessageCode.ERROR_REQUIRE_CODE);
				else if (StringUtils.isNullOrEmpty(requestValue.get("end_time").toString()))
					return this.buildResValid("Không được để trống: end_time", EPMMessageCode.ERROR_EMPTY_CODE);
				try {
					String pattern = "dd/MM/YYYY";
					SimpleDateFormat sdf = new SimpleDateFormat(pattern);
					Date fromDate = sdf.parse(requestValue.get("from_time").toString());
					Date toDate = sdf.parse(requestValue.get("end_time").toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					return this.buildResValid("Ngày tháng phải là định dạng dd/MM/YYYY", EPMMessageCode.API_EXCEPTION_CODE);
				}
			}
			
			res = this.buildSuccessedResponse(this.epmBusiness.getInfoTran(
					StringUtils.nvl(requestValue.get("phone"), ""), StringUtils.nvl(requestValue.get("from_time"), ""),
					StringUtils.nvl(requestValue.get("end_time"), ""),
					StringUtils.nvl(requestValue.get("trans_id"), "")));
			String data = "";
                if (res.getPayload() != null) {
                    data = Tools.convertDataToBase64(res.getPayload());
                    checkSum = Tools.getCheckSum(data, key);
                    res = this.buildSuccessedResponse(DataCheckSumModel.builder().data(data).checkSum(checkSum).build());
                }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            res = this.buildExceptionResponse(ex);
        } finally {
        	logApiResult.setIpRequest(getIpAddress(servletRequest));
            logApiResult.setResponseBody(Tools.convertModeltoJSON(res));
            long processTime = System.currentTimeMillis() - logApiResult.getCreateTime().getTime();
            logApiResult.setProcessTime(String.valueOf(processTime));
            EPMQueueManager.QUEUE_LOG_API_RESULT.enqueueNotify(logApiResult);
        }
        return res;
    }
}

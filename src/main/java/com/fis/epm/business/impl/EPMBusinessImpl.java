package com.fis.epm.business.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;

import com.fis.epm.dao.EPMTransactionDao;
import com.fis.epm.repo.*;
import com.fis.epm.utils.ValidationUtils;
import com.fis.fw.common.utils.ValidationUtil;
import com.fis.pg.epm.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Service;

import com.fis.epm.business.EPMBusiness;
import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.entity.AutoDebitInfo;
import com.fis.epm.entity.BankToken;
import com.fis.epm.models.AutoDebitModel;
import com.fis.epm.models.BankModel;
import com.fis.epm.models.CancelAutoDebitModel;
import com.fis.epm.models.ChargeModel;
import com.fis.epm.models.ParamModel;
import com.fis.epm.models.PosModel;
import com.fis.epm.models.PromotionModel;
import com.fis.epm.models.ProvinceModel;
import com.fis.epm.models.TransactionModel;
import com.fis.epm.napas.models.PaymentWithOTPResponseModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.request.models.AddInvoiceTransRequestModel;
import com.fis.epm.request.models.PrepaidRequestModel;
import com.fis.epm.request.models.PrepaidTokenRequestModel;
import com.fis.epm.request.models.PromotionRequestModel;
import com.fis.epm.service.AutoDebitInfoService;
import com.fis.epm.service.BankTokenService;
import com.fis.epm.service.IsdnInfoService;
import com.fis.epm.service.PromotionService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.EPMMessageCode;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.common.utils.Encryptor;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.gw.server.models.ResponseModel;

@Service
public class EPMBusinessImpl extends EPMBaseCommon implements EPMBusiness {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    HashMap<String, String> listParamForPrepaidHandle = new HashMap<String, String>();
    @Autowired
    private EPMAppDao epmAppDao = null;
    @Autowired
    private HttpServletRequest request = null;
    @Autowired
    private BankTokenService bankTokenService;
    @Autowired
    private EPMNapasCommunicationService communicationService;
    @Autowired
    private PromotionService promotionService;
    @Autowired
    private IsdnInfoService isdnInfoService;
    @Autowired
    private PaymentChanelConfigRedisRepo paymentChanelConfigRedisRepo;
    @Autowired
    private QueryDRRedisRepo queryDRRedisRepo;
    @Autowired
    private PartnerCodeBankRedisRepo partnerCodeBankRedisRepo;
    @Autowired
    private AutoDebitInfoService autoDebitInfoService;
    @Autowired
    @Qualifier(EPMApiConstant.APP_MESSAGE_DICTIONARY_BEAN)
    private ConcurrentHashMap<String, String> messageDictionary = null;
    @PersistenceContext
    private EntityManager entityManager;
    @Value(EPMApplicationProp.NAPAS_ENCRYPTOR_KEY)
    private String napasEncryptorKey = "";
    @Value(EPMApplicationProp.NAPAS_ENCRYPTOR_INITVECTOR)
    private String napasEncryptorInitVector = "";
    @Autowired
    private EPMTransactionDao epmTransactionDao;
    @Autowired
    private ApParamRedisRepo apParamRedisRepo;
    @Autowired
    private BankRedisRepo bankRedisRepo;
    @Value("${com.fis.epm.mobifiber.type.product}")
    private String typeProductMobifiber;

    @Override
    public List<BankModel> findAllBank() throws Exception {
        // TODO Auto-generated method stub
        List<BankModel> banks = null;

        try {
            banks = this.epmAppDao.findAllBank();
        } catch (Exception exp) {
            throw exp;
        }
        return banks;
    }

    @Override
    public ParamModel findBillChargeParamByMsisdn(String reference) throws Exception {
        // TODO Auto-generated method stub
        ParamModel params = null;

        try {
            params = this.epmAppDao.findBillChargeParamByMsisdn(reference);
            isdnInfoService.SyncDataRedis(params, reference);
        } catch (Exception exp) {
            throw exp;
        }
        return params;
    }

    @Override
    public List<ChargeModel> findAllChargeInfo() throws Exception {
        // TODO Auto-generated method stub
        List<ChargeModel> charges = null;

        try {
            charges = this.epmAppDao.findAllChargeInfo();
        } catch (Exception exp) {
            throw exp;
        }
        return charges;
    }

    @Override
    public PromotionModel loadPromotion(PromotionRequestModel request, String userName) throws Exception {
        PromotionModel promotionModels = null;
        try {
            promotionModels = this.epmAppDao.loadPromotion(request, userName);
            if (promotionModels != null && promotionModels.getValue() != null) {
                promotionService.syncDataRedis(promotionModels, request.getBankCode() + "_" + request.getIsdn());
            }
        } catch (Exception exp) {
            throw exp;
        }
        return promotionModels;
    }

    @Override
    public ResponseModel prepaidHandle(PrepaidRequestModel request, String userName, String ipAddress) throws Exception {
        // TODO Auto-generated method stub
        ResponseModel res = new ResponseModel();
        try {
            if (request == null)
                throw new AppException(EPMMessageCode.API_INPUT_INVALID);
            double amount = request.getAmount();
            String code = "", mess = "";
            String isdn = Tools.stringNvl(request.getIsdn(), "");
            IsdnInfoRedisModel isdnInfo = new IsdnInfoRedisModel();

            //----------------------------REDIS--------------------------------------------
            //COMMENT CODE
            if(ValidationUtils.isNullOrEmpty(request.getTypeCreateTransaction()) || "1".equals(request.getTypeCreateTransaction())) {
                // L?y th�ng tin thu� bao t? cache v� ki?m tra c�c giao d?ch
                isdnInfo = isdnInfoService.getById(isdn);
                if (isdnInfo == null || (isdnInfo != null && isdnInfo.getParam() == null))
                    return this.buildResValid("", com.fis.epm.prop.EPMMessageCode.ERROR_NOT_FOUND_ISDN);
                else if (isdnInfo.getParam() != null && isdnInfo.getParam().size() > 0) {
                    // L?y danh s�ch tham s? trong ap_param
                    int i = 0;
                    List<ParamDataModel> listParam = isdnInfo.getParam();
                    int paramSize = listParam.size();
                    while (i < paramSize) {
                        String value = listParam.get(i).getValue();
                        if (value == null)
                            value = "0";
                        listParamForPrepaidHandle.put(listParam.get(i).getCode(), value);
                        i++;
                    }
                    if (listParamForPrepaidHandle.size() > 0) {
                        double sum = 0;
                        if (isdnInfo.getTypeSub().equals("POS")) {
                            if (this.getTransNum(request.getIsdn()) > Double.parseDouble(
                                    listParamForPrepaidHandle.get(EPMApiConstant.MAX_TRANSACTION_PER_DAY))) {
                                code = com.fis.epm.prop.EPMMessageCode.ERROR_MAX_TRANS_PER_DAY;
                                mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MAX_TRANS_PER_DAY) + " "
                                        + listParamForPrepaidHandle.get(EPMApiConstant.MAX_TRANSACTION_PER_DAY);
                            } else {
                                if (!StringUtils.isNullOrEmpty(isdnInfo.getParam().get(paramSize - 1).getValue()))
                                    sum = Double.parseDouble(isdnInfo.getParam().get(paramSize - 1).getValue());
                                if (sum > Double
                                        .parseDouble(listParamForPrepaidHandle.get(EPMApiConstant.MAX_AMOUNT_PER_DAY))) {
                                    code = com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_PER_DAY;
                                    mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_PER_DAY)
                                            + listParamForPrepaidHandle.get(EPMApiConstant.MAX_AMOUNT_PER_DAY);
                                } else if (amount < Double
                                        .parseDouble(listParamForPrepaidHandle.get(EPMApiConstant.POSTPAID_MIN_AMOUNT))) {
                                    code = com.fis.epm.prop.EPMMessageCode.ERROR_MIN_AMOUNT_1_TRANS;
                                    mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MIN_AMOUNT_1_TRANS)
                                            + listParamForPrepaidHandle.get(EPMApiConstant.POSTPAID_MIN_AMOUNT);
                                } else if (amount > Double
                                        .parseDouble(listParamForPrepaidHandle.get(EPMApiConstant.POSTPAID_MAX_AMOUNT))) {
                                    code = com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_1_TRANS;
                                    mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_1_TRANS) + " "
                                            + listParamForPrepaidHandle.get(EPMApiConstant.POSTPAID_MAX_AMOUNT);
                                }
                            }
                        } else if (isdnInfo.getTypeSub().equals("PRE")) {
                            if (this.getTransNum(request.getIsdn()) > Double.parseDouble(
                                    listParamForPrepaidHandle.get(EPMApiConstant.MAX_TRANSACTION_PER_DAY))) {
                                code = com.fis.epm.prop.EPMMessageCode.ERROR_MAX_TRANS_PER_DAY;
                                mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MAX_TRANS_PER_DAY)
                                        + listParamForPrepaidHandle.get(EPMApiConstant.MAX_TRANSACTION_PER_DAY);
                            } else {
                                if (!StringUtils.isNullOrEmpty(isdnInfo.getParam().get(paramSize - 1).getValue()))
                                    sum = Double.parseDouble(isdnInfo.getParam().get(paramSize - 1).getValue());
                                if (sum > Double.parseDouble(
                                        listParamForPrepaidHandle.get(EPMApiConstant.PREPAID_MAX_AMOUNT_CHARGE_DAY))) {
                                    code = com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_PER_DAY;
                                    mess = loadMessage(com.fis.epm.prop.EPMMessageCode.ERROR_MAX_AMOUNT_PER_DAY)
                                            + listParamForPrepaidHandle.get(EPMApiConstant.PREPAID_MAX_AMOUNT_CHARGE_DAY);
                                }
                            }
                        }
                    } else {
                        code = com.fis.epm.prop.EPMMessageCode.GET_PARAM_LIST_ERROR;
                        mess = loadMessage(com.fis.epm.prop.EPMMessageCode.GET_PARAM_LIST_ERROR) + ".Không thể kiểm tra thông tin giao dịch của thuê bao";
                    }
                }
                logger.info("Check param done : mess :" + mess + "- code :" + code);
                // Ki?m tra th�ng tin thu� bao h?p l?
                if (!code.equals("") || !mess.equals(""))
                    return this.buildResValid(mess, code);
            //COMMENT CODE
            }

            double discount = request.getDiscount();
            double promotion = request.getPromotion();
            String cardType = Tools.stringNvl(getCardType(request.getBankCode()), "").toUpperCase();
            String bankCode = Tools.stringNvl(request.getBankCode(), "").toUpperCase();
            String detail = Tools.stringNvl(request.getDetail(), "");
            String fromIsdn = Tools.stringNvl(request.getFromIsdn(), "");
            String redirectUrl = Tools.stringNvl(request.getRedirectUrl(), "");
            String language = Tools.stringNvl(request.getLanguage(), "");
            String merchantCode = "";
            String tokenId = Tools.stringNvl(request.getTokenId(), "0");
            String tokenCreate = Tools.stringNvl(request.getTokenCreated(), "");
            if (!StringUtils.isNullOrEmpty(request.getTokenId()))
                tokenCreate = "0";
            String callBackUrl = StringUtils.nvl(request.getCallBackUrl(), "");

            /**
             * validate: all input can not be null value
             */
            String rs = Tools.validateEmptyObject(request);
            if (!cardType.equalsIgnoreCase(EmpStatusConstain.CT_DOMES)
                    && !cardType.equalsIgnoreCase(EmpStatusConstain.CT_INTER))
                return this.buildResValid("", com.fis.epm.prop.EPMMessageCode.PREPAID_TOKEN_CARD_TYPE_IS_NOT_MATCH);

            String partnerCode = "";
            //----------------------------REDIS--------------------------------------------

            Optional<PartnerCodeBankRedisModel> partnerCodeBank = partnerCodeBankRedisRepo.findById(bankCode);
            if (partnerCodeBank.isPresent() && partnerCodeBank.get() != null) {
                partnerCode = partnerCodeBank.get().getPartnerCode();
                merchantCode = partnerCodeBank.get().getMerchantCode();
            }


            if (StringUtils.nvl(partnerCode, "").trim().equals("")) {
                return this.buildResValid("", com.fis.epm.prop.EPMMessageCode.ERROR_BANK);
            }
            /**
             * build transaction
             */

            String objectType = "";
            String cenCode = "";
            String paymentType = "";
            String typeReference = "";
            //COMMENT CODE
            if(ValidationUtils.isNullOrEmpty(request.getTypeCreateTransaction()) || "1".equals(request.getTypeCreateTransaction())){
                if (isdnInfo != null) {
                    logger.info("isdnInfo redis : " + Tools.convertModeltoJSON(isdnInfo));
                    cenCode = isdnInfo.getCenCode();
                    objectType = isdnInfo.getTypeSub();
                    paymentType = isdnInfo.getCustType();
                    typeReference = isdnInfo.getTypeReference();
                }
            //COMMENT CODE
            }else if ("2".equals(request.getTypeCreateTransaction())) {
                objectType = EPMApiConstant.CREATE_PACKAGE;
            }

            //----------------------------REDIS--------------------------------------------

            //COMMENT CODE
            if(ValidationUtils.isNullOrEmpty(request.getTypeCreateTransaction()) || "1".equals(request.getTypeCreateTransaction())) {
                //lấy ra giá trị tiền và loại thanh toán (trả trước , trả sau ) từ cache ap_param
                ApParamRedisModel apParam = apParamRedisRepo.findByParTypeAndParName("BANK_TYPE_AMOUNT_SMALLER", loadParNameCheckBankTypeByAmount(objectType, request.getTypeProduct()));
                // kiểm tra xem có giá trị cấu hình hay không ?
                if (apParam != null && !StringUtils.isNullOrEmpty(apParam.getParValue()) && apParam.getParValue().split(";").length > 1) {
                    // nếu có thì lấy thông tin ngân hàng , loại thẻ thanh toán của mỗi ngân hàng
                    BankRedisModel bankInfo = bankRedisRepo.findByBankCode(bankCode);
                    String[] value = apParam.getParValue().split(";");
                    // lấy ra giá trị tiền
                    Double amountCheck = Double.parseDouble(value[0]);
                    // lấy ra thông tin ngân hàng cần check
                    String bankTypeCheck = value[1];
                    // kiểm tra nếu số tiền thanh toán nhỏ hơn số tiền cấu hình và loại thẻ khác cấu hình thì báo lỗi
                    if (amount < amountCheck && !bankTypeCheck.equals(bankInfo.getBankType())) {
                        return this.buildResValid("", com.fis.epm.prop.EPMMessageCode.BANK_TYPE_AND_AMOUNT_NOT_VALID);
                    }

                }
            //COMMENT CODE
            }


            EpmTransactionModel transaction = new EpmTransactionModel();

            String transactionId = this.loadTransactionId(objectType, request.getTypeProduct());
            transaction.setAmount(amount);
            transaction.setBankCode(bankCode);
            transaction.setCardType(cardType);
            transaction.setDescription(detail);
            transaction.setDiscountAmount(discount);
            transaction.setFromReference(fromIsdn);
            transaction.setIpAddress(ipAddress);
            transaction.setLanguage(language);
            transaction.setObjectType(objectType);
            transaction.setPayStatus(EmpStatusConstain.START);
            transaction.setIssueStatus(EmpStatusConstain.PENDING);
            transaction.setStaDateTime(new Date());
            transaction.setPromAmount(promotion);
            transaction.setReference(isdn);
            transaction.setTransactionId(transactionId);
            transaction.setRedirectUrl(redirectUrl + "?tranid=" + transactionId);
            transaction.setCallBackUrl(callBackUrl);
            transaction.setTokenCreate(tokenCreate);
            transaction.setUserName(userName);
            transaction.setPaymentType(paymentType);
            transaction.setTokenId(tokenId);
            transaction.setMerchantCode(merchantCode);
            transaction.setPartnerCode(partnerCode);
            transaction.setCenCode(cenCode);
            transaction.setTypeProduct(request.getTypeProduct());
            transaction.setTypeReference(typeReference);
            transaction.setPackageCode(request.getPackageCode());

            //----------------------------REDIS--------------------------------------------
            //COMMENT CODE
            if(ValidationUtils.isNullOrEmpty(request.getTypeCreateTransaction()) || "1".equals(request.getTypeCreateTransaction())) {
                PromotionRedisModel promoModel = promotionService.getById(bankCode + "_" + isdn);
                if (promoModel != null) {
                    logger.info("promotion redis of Isdn : " + Tools.convertModeltoJSON(promoModel));
                    String type = StringUtils.nvl(promoModel.getType(), "");
                    String method = StringUtils.nvl(promoModel.getMethod(), "");
                    if ("1".equals(type)) {
                        double discountValue = 0;
                        if ("1".equals(method)) {
                            discountValue = Double.parseDouble(StringUtils.nvl(promoModel.getValue(), "0"));
                        }
                        if ("0".equals(method)) {
                            discountValue = (Double.parseDouble(StringUtils.nvl(promoModel.getValue(), "0")) * amount
                                    / 100);
                            if (discountValue - (int) discountValue > 0.5) {
                                discountValue = (int) discountValue + 1;
                            } else {
                                discountValue = (int) discountValue;
                            }
                        }
                        transaction.setDiscountAmount(discountValue);
                    }

                    if ("2".equals(type)) {
                        double promotionValue = 0;
                        if ("1".equals(method)) {
                            promotionValue = Double.parseDouble(StringUtils.nvl(promoModel.getValue(), "0"));
                        }
                        if ("0".equals(method)) {
                            promotionValue = (Double.parseDouble(StringUtils.nvl(promoModel.getValue(), "0")) * amount
                                    / 100);
                            if (promotionValue - (int) promotionValue > 0.5) {
                                promotionValue = (int) promotionValue + 1;
                            } else {
                                promotionValue = (int) promotionValue;
                            }
                        }
                        transaction.setPromAmount(promotionValue);
                    }
                }
            //COMMENT CODE
            }

            double issueAmount = transaction.getAmount() + transaction.getPromAmount();
            double payAmount = transaction.getAmount() - transaction.getDiscountAmount();
            if (((!ValidationUtil.isNullOrEmpty(transaction.getTypeProduct()) && typeProductMobifiber.equals(transaction.getTypeProduct()))
                    || EmpStatusConstain.OT_POS.equals(objectType)) && transaction.getDiscountAmount() > 0) {
                issueAmount = transaction.getAmount() - transaction.getDiscountAmount();
            }
            int requestAmount = (int) request.getPayAmount();
            int payAmountDB = (int) payAmount;
            int discountAmount = (int) transaction.getDiscountAmount();
            if (payAmount > payAmountDB) {
                payAmountDB++;
                discountAmount = (int) (amount - payAmountDB);
            }
            if (requestAmount == payAmountDB) {
                transaction.setIssueAmount(issueAmount);
                transaction.setPayAmount(payAmountDB);
                QueryDRAuto queryDr = new QueryDRAuto().builder().transactionId(transactionId)
                        .staDateTime(transaction.getStaDateTime().getTime()).ipAddress(this.request.getRemoteAddr())
                        .fromReference(fromIsdn).reference(isdn).amount(amount).cardType(cardType).bankCode(bankCode)
                        .userName(userName).language(language).discountAmount(discountAmount)
                        .promAmount(transaction.getPromAmount()).description(detail).paymentType(paymentType)
                        .redirectUrl(redirectUrl).objectType(objectType).tokenId(tokenId).tokenCreate(tokenCreate)
                        .callBackUrl(callBackUrl).merchantCode(transaction.getMerchantCode()).count(0)
                        .partnerCode(partnerCode).build();
                epmTransactionDao.insertTransaction(transaction);
                queryDRRedisRepo.save(queryDr);

//				EpmRequestModel req = new EpmRequestModel();
//
//				req.setActionType(EmpStatusConstain.INSERT_TRANSACCTION_ACTION);
//				req.setAppName(this.appName);
//				req.setTransactionData(transaction);
//				req.setTransactionId(transactionId);
//				this.mqPushDataEnqueueService.sendRequest(req);
//				this.mqPushDataEnqueueService.sendRequestLog(req, transactionId, Constants.TYPE_RABBIT_MQ_SEND);
                res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
                res.setPayload(transaction);
                res.setTransactionId(transactionId);
            } else
                res = this.buildResValid("", com.fis.epm.prop.EPMMessageCode.ERROR_PAY_AMOUNT);
        } catch (Exception exp) {
            logger.error("ERROR prepaidHandle : " + exp.getMessage(), exp);
            throw exp;
        }
        return res;
    }

    @Override
    public TransactionModel findDetailTransaction(String transactionId) throws Exception {
        // TODO Auto-generated method stub
        TransactionModel transactionModel = null;

        try {
            transactionModel = this.epmAppDao.findDetailTransaction(transactionId);
        } catch (Exception exp) {
            throw exp;
        }
        return transactionModel;
    }

    @Override
    public List<ProvinceModel> findAllProvince(Long provinceId) throws Exception {
        List<ProvinceModel> provinceModels = null;

        try {
            provinceModels = this.epmAppDao.findAllProvince(provinceId);
        } catch (Exception exp) {
            throw exp;
        }
        return provinceModels;
    }

    @Override
    public List<PosModel> findAllPosByProvince(Long provinceId) throws Exception {
        List<PosModel> posByProvince = null;

        try {
            posByProvince = this.epmAppDao.findAllPosByProvince(provinceId);
        } catch (Exception exp) {
            throw exp;
        }
        return posByProvince;
    }

    @Override
    public List<String> findPosAddress(long shopId) throws Exception {
        // TODO Auto-generated method stub
        List<String> posAddress = null;

        try {
            posAddress = this.epmAppDao.findPosAddress(shopId);
        } catch (Exception exp) {
            throw exp;
        }
        return posAddress;
    }

    @Override
    public String addInvoiveTransaction(AddInvoiceTransRequestModel request) throws Exception {
        // TODO Auto-generated method stub
        String code = "";
        try {
            int tg = this.epmAppDao.addInvoiveTransaction(request);
            if (tg == 1)
                code = EPMMessageCode.API_SUCCESSED_CODE;
            else if (tg == 0)
                code = com.fis.epm.prop.EPMMessageCode.API_ERROR_ADD_INVOICE_FALSE;
            else if (tg == 2)
                code = com.fis.epm.prop.EPMMessageCode.API_ERROR_ADD_INVOICE;
            else if (tg == 3)
                code = com.fis.epm.prop.EPMMessageCode.API_ERROR_ALLOW_INVOICE_THIS_MONTH;
        } catch (Exception exp) {
            throw exp;
        }
        return code;
    }

    @Override
    public ResponseModel prepaidTokenHandle(PrepaidTokenRequestModel request) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<com.fis.epm.models.TokenModel> findAllToken(String fromIsdn) throws Exception {
        try {
            return this.epmAppDao.findAllToken(fromIsdn);
        } catch (Exception exp) {
            throw exp;
        }
    }

    @Override
    public ResponseModel deleteToken(String tokenId, String isdn) throws Exception {
        ResponseModel res = new ResponseModel();
        try {
//			List<AutoDebitInfo> autoDebitInfos = autoDebitInfoService.findByMsisdnAndTokenizer(isdn, tokenId);
            List<AutoDebitInfo> autoDebitInfos = autoDebitInfoService.findByTokenizer(tokenId);
            logger.info("Tim kiem danh sach token trong autoDebitInfos " + autoDebitInfos.toString());
            if (autoDebitInfos.size() != 0) {
                logger.info("autoDebitInfos.size()  khac 0");
                res.setMessage(EmpStatusConstain.FAILED);
                res.setStatus(EPMMessageCode.API_EXCEPTION_CODE);
            } else {
                logger.info("autoDebitInfos.size()  bang 0");
                BankToken bankToken = bankTokenService.findByBankTokenIdAndReference(tokenId, isdn);
                PaymentWithOTPResponseModel paymentWithOTPResponseModel = null;
                if (bankToken != null && !Tools.stringIsNullOrEmty(bankToken.getTokenCode())) {
                    String token = Encryptor.decrypt(napasEncryptorKey, napasEncryptorInitVector, bankToken.getTokenCode());
                    paymentWithOTPResponseModel = communicationService.deleteToken(token);
                    if (paymentWithOTPResponseModel != null
                            && EmpStatusConstain.SUCCESS.equalsIgnoreCase(paymentWithOTPResponseModel.getResult())) {
                        BankToken bankTokenDelete = bankTokenService.delete(bankToken);

                        res.setPayload(bankTokenDelete);
                        res.setMessage(EmpStatusConstain.SUCCESS);
                        res.setStatus(EPMMessageCode.API_SUCCESSED_CODE);
                    } else {
                        res.setMessage(EmpStatusConstain.FAILED);
                        res.setStatus(EPMMessageCode.API_EXCEPTION_CODE);
                    }
                } else {
                    res.setMessage(EmpStatusConstain.FAILED);
                    res.setStatus(EPMMessageCode.API_EXCEPTION_CODE);
                }
            }
        } catch (Exception exp) {
            res = Tools.buildResponseModel(exp);
            throw exp;
        }
        return res;
    }

    @Override
    public String getKeyCheckSum(String userName) throws Exception {
        // TODO Auto-generated method stub
        String strKey = "";
        String code = com.fis.epm.prop.EPMMessageCode.ERROR_USER_PERMISS;
        List<PaymentChannelConfigRedisModel> lstConfig = paymentChanelConfigRedisRepo.findByUserName(userName);
        if (lstConfig == null || lstConfig.isEmpty()) {
            this.buildResValid(String.format(loadMessage(code), userName), code);
            return "";
        }
        strKey = lstConfig.get(0).getKey();
        if (StringUtils.nvl(strKey, "").equals("")) {
            this.buildResValid(String.format(loadMessage(code), userName), code);
        }
        return strKey;
    }

    public String getCardType(String bankCode) throws Exception {
        EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        String value = "";

        try {
            String sql = "select bank_type from bank where UPPER(bank_code) = UPPER(?)";
            conn = info.getDataSource().getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, bankCode);
            rs = ps.executeQuery();
            while (rs.next()) {
                value = rs.getString("bank_type");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            Tools.closeObject(rs);
            Tools.closeObject(ps);
            Tools.closeObject(conn);
        }
        return value;
    }


    private String loadMessage(String status) {
        if (this.messageDictionary == null)
            return "";
        if (this.messageDictionary.containsKey(status))
            return this.messageDictionary.get(status);
        return "";
    }

    public int getTransNum(String isdn) throws Exception {
        Query query = null;
        int transNum = 0;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("select COUNT(TRANSACTION_ID) as transNum FROM EPM_TRANSACTION ");
            sb.append("WHERE sta_datetime >= trunc(sysdate) ");
            sb.append("and END_DATETIME < trunc(sysdate) + 1  ");
            sb.append("and pay_status ='SUCC' and issue_status ='SUCC'");
            sb.append("and REFERENCE = :isdn  ");
            query = entityManager.createNativeQuery(sb.toString(), Integer.class);
            query.setParameter("isdn", isdn);
            transNum = query.getFirstResult();
        } catch (Exception exp) {
            throw exp;
        }
        return transNum;
    }

    protected ResponseModel buildResValid(String mess, String code) {
        ResponseModel res = new ResponseModel();
        if (mess.equals(""))
            res.setMessage(loadMessage(code));
        else
            res.setMessage(mess);
        res.setStatus(code);
        return res;
    }

    @Override
    public boolean registerAutoDebit(AutoDebitModel data) throws Exception {
        List<AutoDebitModel> registerAutoDebitModels = null;
        try {
            boolean check = this.epmAppDao.registerAutoDebitModel(data);
            if (check)
                return true;
        } catch (Exception exp) {
            throw exp;
        }
        return false;
    }

    @Override
    public String checkTokenFromIsdn(String msisdn) throws Exception {
        String data = null;
        try {
            data = this.epmAppDao.checkTokenFromIsdn(msisdn);
        } catch (Exception exp) {
            throw exp;
        }
        return data;
    }

    @Override
    public ResponseModel cancelAutoDebit(String msisdn, String userName) throws Exception {
        CancelAutoDebitModel cancelAutoDebitModel = new CancelAutoDebitModel();
        ResponseModel res = new ResponseModel();
        try {
            cancelAutoDebitModel = this.epmAppDao.cancelAutoDebit(msisdn, userName);
            res = this.buildResValid("", cancelAutoDebitModel.getCode());
        } catch (Exception exp) {
            logger.error("error cancelAutoDebit", exp);
            throw exp;
        }

        return res;
    }

    @Override
    public List<Map<String, Object>> getInfoTran(String msisdn, String fromDate, String toDate, String transId)
            throws Exception {
        // TODO Auto-generated method stub
        return epmAppDao.getInfoTran(msisdn, fromDate, toDate, transId);
    }

}

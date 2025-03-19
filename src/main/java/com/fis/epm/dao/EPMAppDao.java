package com.fis.epm.dao;

import java.util.List;
import java.util.Map;

import com.fis.epm.models.BankModel;
import com.fis.epm.models.CancelAutoDebitModel;
import com.fis.epm.models.ChargeModel;
import com.fis.epm.models.ParamModel;
import com.fis.epm.models.PosModel;
import com.fis.epm.models.PromotionModel;
import com.fis.epm.models.ProvinceModel;
import com.fis.epm.models.AutoDebitModel;
import com.fis.epm.models.TokenModel;
import com.fis.epm.models.TransactionModel;
import com.fis.epm.request.models.AddInvoiceTransRequestModel;
import com.fis.epm.request.models.PromotionRequestModel;
import com.fis.pg.gw.server.models.EpmIssueLog;
import com.fis.pg.gw.server.models.EpmMerchantLogModel;

public interface EPMAppDao {
	public List<BankModel> findAllBank() throws Exception;

	public ParamModel findBillChargeParamByMsisdn(String msisdn) throws Exception;

	public List<ChargeModel> findAllChargeInfo() throws Exception;
	
	public TransactionModel findDetailTransaction(String transactionId) throws Exception;
	
	public List<String> findPosAddress(Long shopId) throws Exception;
	
	public List<PosModel> findAllPosByProvince(Long provinceId) throws Exception;
	
	public List<ProvinceModel> findAllProvince(Long provinceId) throws Exception;
	
	public int addInvoiveTransaction(AddInvoiceTransRequestModel addInvoiceTransRequestModel) throws Exception;
	
	public List<TokenModel> findAllToken(String reference) throws Exception;
	
	public PromotionModel loadPromotion(PromotionRequestModel request,String userName) throws Exception;

	public boolean registerAutoDebitModel(AutoDebitModel data) throws Exception;
	
	public String checkTokenFromIsdn(String msisdn)  throws Exception;
	
	public CancelAutoDebitModel cancelAutoDebit(String msisdn, String userName) throws Exception;
	
	public List<Map<String, Object>> getInfoTran(String msisdn, String fromDate, String toDate, String transId) throws Exception;

	public void insertMerChantLog(EpmMerchantLogModel merChange) throws Exception;

	public void insertIssueLog(EpmIssueLog logIssue) throws Exception;
}

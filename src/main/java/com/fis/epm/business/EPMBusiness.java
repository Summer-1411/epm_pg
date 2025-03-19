package com.fis.epm.business;

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
import com.fis.epm.request.models.PrepaidRequestModel;
import com.fis.epm.request.models.PrepaidTokenRequestModel;
import com.fis.epm.request.models.PromotionRequestModel;
import com.fis.pg.gw.server.models.ResponseModel;

public interface EPMBusiness {
	/**
	 * Lay thong tin tat ca cac ngan hang duoc cau hinh phuc vu cho viec thanh toan
	 */
	public List<BankModel> findAllBank() throws Exception;

	/**
	 * Lay ds tham so doi voi cac thue bao tra truoc/sau
	 */
	public ParamModel findBillChargeParamByMsisdn(String reference) throws Exception;

	/**
	 * 
	 * Lay thong tin the nap cua nha mang mbf
	 */
	public List<ChargeModel> findAllChargeInfo() throws Exception;

	/**
	 * 
	 * Kiem tra thong tin ve triet khau
	 */
	public PromotionModel loadPromotion(PromotionRequestModel request,String userName) throws Exception;

	/**
	 * 
	 * Thuc hien thanh toan, tat ca cac tham so dau vao khong duoc de trong
	 */
	public ResponseModel prepaidHandle(PrepaidRequestModel request,String userName, String ipAddress) throws Exception;

	/**
	 * 
	 * Lay thong tin giao dich dua vao transaction id
	 */
	public TransactionModel findDetailTransaction(String transactionId) throws Exception;

	/**
	 * 
	 * Lay thong tin cac tinh, id = 0 - lay tat ca cac tinh
	 */
	public List<ProvinceModel> findAllProvince(Long provinceId) throws Exception;

	/**
	 * 
	 * Lay thong tin cua hang theo tinh
	 */
	public List<PosModel> findAllPosByProvince(Long provinceId) throws Exception;

	/**
	 * 
	 * Lay thong tin dia chi cua hang
	 */
	public List<String> findPosAddress(long posId) throws Exception;

	/**
	 * 
	 * Ham ghi nhan thong tin hoa don cho thue bao tra truoc
	 */
	public String addInvoiveTransaction(AddInvoiceTransRequestModel request) throws Exception;

	/**
	 * 
	 * Ham tao giao dich thanh toan
	 */
	public ResponseModel prepaidTokenHandle(PrepaidTokenRequestModel request) throws Exception;

	/**
	 * 
	 * Lay thong tin danh sach token
	 * */
	public List<TokenModel> findAllToken(String fromIsdn) throws Exception;

	
	 /* 
	 * Xoa thong tin token
	 */
	public ResponseModel deleteToken(String tokenId, String isdn) throws Exception;

	public String getKeyCheckSum(String userName) throws Exception;
	
	public boolean registerAutoDebit(AutoDebitModel data) throws Exception;
	
	public String checkTokenFromIsdn(String msisdn)  throws Exception;
	
	public ResponseModel cancelAutoDebit(String msisdn, String userName) throws Exception;
	
	public List<Map<String, Object>> getInfoTran(String msisdn, String fromDate, String toDate, String transId) throws Exception;
}

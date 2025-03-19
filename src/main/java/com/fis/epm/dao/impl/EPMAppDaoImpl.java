package com.fis.epm.dao.impl;

import java.math.BigDecimal;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.sql.DataSource;
import javax.transaction.Transactional;

import com.fis.epm.entity.Transactions;
import com.fis.epm.utils.ValidationUtils;
import com.fis.fw.common.utils.AlertSystemUtil;
import com.fis.pg.gw.server.models.EpmIssueLog;
import com.fis.pg.gw.server.models.EpmMerchantLogModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.orm.jpa.EntityManagerFactoryInfo;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fis.epm.business.impl.EPMNapasCommunicationService;
import com.fis.epm.dao.EPMAppDao;
import com.fis.epm.entity.EpmTransaction;
import com.fis.epm.models.AutoDebitModel;
import com.fis.epm.models.BankModel;
import com.fis.epm.models.CancelAutoDebitModel;
import com.fis.epm.models.ChargeModel;
import com.fis.epm.models.IsdnInfoApi;
import com.fis.epm.models.ParamDataAPIModel;
import com.fis.epm.models.ParamModel;
import com.fis.epm.models.PosModel;
import com.fis.epm.models.PromotionModel;
import com.fis.epm.models.ProvinceModel;
import com.fis.epm.models.TokenModel;
import com.fis.epm.models.TransactionModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.prop.EPMMessageCode;
import com.fis.epm.request.models.AddInvoiceTransRequestModel;
import com.fis.epm.request.models.PromotionRequestModel;
import com.fis.epm.service.IsdnInfoService;
import com.fis.epm.service.ShopService;
import com.fis.epm.utils.ResttemplateBean;
import com.fis.pg.common.utils.ATMUtil;
import com.fis.pg.common.utils.AppException;
import com.fis.pg.common.utils.AppProcessState;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.epm.models.CreateSaleTransApiModel;
import com.fis.pg.epm.models.CreateSaleTransDetailModel;
import com.fis.pg.epm.models.CreateSaleTransactionRespone;
import com.fis.pg.epm.models.IsdnInfoRedisModel;
import com.fis.pg.epm.models.ParamDataModel;
import com.fis.pg.epm.models.ShopRedisModel;

@Repository
public class EPMAppDaoImpl implements EPMAppDao {

	private static final Logger log = LoggerFactory.getLogger(EPMNapasCommunicationService.class);

	@Autowired
	private ResttemplateBean resttemplateBean = null;

	@Autowired
	private IsdnInfoService isdnInfoService;

	@PersistenceContext
	private EntityManager entityManager;
	
	@Autowired
	private ShopService shopService;


	@Value(EPMApplicationProp.HTTP_REQUEST_TIMEOUT_PROP)
	private int iHttpRequestTimeout = 40;

	@Value(EPMApplicationProp.URL_EPAYMENT_PROP)
	private String urlEpayment;

	@Value(EPMApplicationProp.PG_GET_INFO_ISDN)
	private String urlGetInfoIsdn;

	@Value(EPMApplicationProp.BHTT_URL)
	private String bhttUrl;
	@Value(EPMApplicationProp.BHTT_USERNAME)
	private String bhttUserName;
	@Value(EPMApplicationProp.BHTT_PASSWORD)
	private String bhttPassword;
	@Value(EPMApplicationProp.BHTT_CREATE_SALE_TRANSACTION)
	private String bhttApiCreateSaleTransaction;
	
	@Value(EPMApplicationProp.PARTNER_ID_AUTO_DEBIT_INFO)
	private String partnerID = "";

	@Value("${com.fis.pg.url-alert}")
	private String urlAlert;

	@Value("${spring.application.name}")
	private String appName;

	@Value("${com.fis.ip.server}")
	private String ipServer;

	@Autowired
	private final DataSource dataSource = null;

	private ObjectMapper mapper;

	public EPMAppDaoImpl() {
		mapper = new ObjectMapper();
	}

	@Override
	public List<BankModel> findAllBank() throws Exception {
		List<BankModel> dataTable = new ArrayList<BankModel>();
		Query query = null;
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(
					"SELECT distinct b.BANK_GATE_ID,b.BANK_CODE,b.NAME,pb.PARTNER_BANK_CODE as BANKGATE,b.token,b.bank_type ");
			sb.append("FROM BANK b LEFT JOIN PARTNER_BANK pb ON b.bank_id = pb.bank_id WHERE b.BANK_GATE_ID IS NOT NULL ORDER BY b.BANK_TYPE desc , b.BANK_CODE");
			query = entityManager.createNativeQuery(sb.toString());
			List<Object[]> listData = query.getResultList();
			listData.stream().forEach((record) -> {
				BankModel bankModel = new BankModel();
				bankModel.setId(record[0] != null ? Long.parseLong(record[0].toString()) : null);
				bankModel.setCode(StringUtils.nvl(record[1], ""));
				bankModel.setName(StringUtils.nvl(record[2], ""));
				bankModel.setBankGate(StringUtils.nvl(record[3], ""));
				bankModel.setToken(StringUtils.nvl(record[4], ""));
				bankModel.setType(StringUtils.nvl(record[5], ""));
				dataTable.add(bankModel);
			});
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		} finally {
			Tools.closeDatabaseObject(query);
		}
		return dataTable;
	}

	@Override
	public ParamModel findBillChargeParamByMsisdn(String reference) throws Exception {
		ParamModel paramModel = new ParamModel();
		IsdnInfoApi isdnInfo = new IsdnInfoApi();
		try {
			// TODO Auto-generated method stub
			List<ParamDataModel> paramDataModels = new ArrayList<ParamDataModel>();
			Query query = null;
			String type = "";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
			Map resultApi =  (Map) this.resttemplateBean.handleHttpRequest(urlGetInfoIsdn + "?isdn=" + reference,
					this.iHttpRequestTimeout, HttpMethod.GET, headers, null, Map.class);
			ParamDataAPIModel dataIsdn = new ParamDataAPIModel();
			if(resultApi == null) {
				throw new AppException("Co loi xay ra khi lay thong tin thue bao");
			}
			Map data = (Map) resultApi.get(AppProcessState.DATA_RESPONSE_API);
			isdnInfo.setDebit(StringUtils.nvl(data.get("debit"), ""));
			isdnInfo.setInfo(StringUtils.nvl(data.get("info"), ""));
			if(isdnInfo.getInfo() != null && !"".equals(isdnInfo.getInfo())) {
				String[] value = ATMUtil.toStringArray(isdnInfo.getInfo(),"|");
				if(value.length == 0) {
					throw new AppException("Khong co thong tin khach hang");
				}				
				dataIsdn.setTypeSub("MF".equals(value[0]) ? "TS" : "TT" );
				dataIsdn.setSubId(value[4]);
				dataIsdn.setCustType(value[5]);	
				dataIsdn.setCenCode(value[6]);
				dataIsdn.setCustCode(value[7]);
				if(isdnInfo.getInfo().startsWith("FIBER")){
					dataIsdn.setTypeSub("FIB");
					String s_sub = isdnInfo.getInfo().substring(6, isdnInfo.getInfo().indexOf("|"));
					dataIsdn.setTypeReference(s_sub);
				}
							
			}
			if(isdnInfo.getDebit() != null && !"".equals(isdnInfo.getDebit())) {
				String[] value = ATMUtil.toStringArray(isdnInfo.getDebit(),"|");
				long debit = Long.parseLong(StringUtils.nvl(value[2], "0"));
				if(debit > 0 ) {
					dataIsdn.setDebit(value[2]);
				}				
			}
			paramModel.setData(dataIsdn);
			if (paramModel != null && paramModel.getData().getTypeSub() != null && !paramModel.getData().getTypeSub().equals("")) {
				if (paramModel.getData().getTypeSub().equals("TS")) {
					paramModel.getData().setTypeSub("POS");
					type = EPMApiConstant.SUB_TYPE_POSTPAID;
				} else if (paramModel.getData().getTypeSub().equals("TT")) {
					paramModel.getData().setTypeSub("PRE");
					type = EPMApiConstant.SUB_TYPE_PREPAID;
				} else if (paramModel.getData().getTypeSub().equals("FIB")) {
					paramModel.getData().setTypeSub("FIB");
					type = EPMApiConstant.SUB_TYPE_POSTPAID;
				}
				StringBuilder sb = new StringBuilder();
				sb.append("SELECT PAR_NAME,to_char (PAR_VALUE,'fm999,999,999,999') as PAR_VALUE FROM AP_PARAM WHERE par_type = :type ");
				sb.append("UNION ALL ");
				sb.append("SELECT 'SUM' as PAR_TYPE,to_char(SUM(AMOUNT),'fm999,999,999,999') as PAR_VALUE ");
				sb.append("FROM EPM_TRANSACTION  ");
				sb.append("WHERE sta_datetime >= trunc(sysdate) ");
				sb.append("and END_DATETIME < trunc(sysdate) + 1  ");
				sb.append("and pay_status ='SUCC' ");
				sb.append("and issue_status ='SUCC' ");
				sb.append("and REFERENCE = :reference ");
				query = entityManager.createNativeQuery(sb.toString());
				query.setParameter("type", type);
				query.setParameter("reference", reference);
				List<Object[]> result = query.getResultList();
				result.stream().forEach(item -> {
					ParamDataModel paramDataModel = new ParamDataModel();
					paramDataModel.setCode(StringUtils.nvl(item[0], ""));
					if (item[1] != null) {
						paramDataModel.setValue(StringUtils.nvl(item[1], "").replaceAll(",", ""));
						paramDataModel.setValueF(StringUtils.nvl(item[1], ""));
					}
					paramDataModels.add(paramDataModel);
				});
				paramModel.setParam(paramDataModels);
				paramModel.getData().setUrlEpayment(urlEpayment);
			}
		} catch (Exception ex) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + ex.getMessage());
			throw ex;
		}
		return paramModel;
	}

	@Override
	public List<ChargeModel> findAllChargeInfo() throws Exception {
		// TODO Auto-generated method stub
		List<ChargeModel> dataTable = new ArrayList<ChargeModel>();
		Query query = null;

		try {
			List<Object[]> listData = entityManager
					.createNativeQuery("SELECT CARD_NAME,CARD_VALUE,STATUS FROM CARD_LIST WHERE STATUS ='1'")
					.getResultList();
			listData.stream().forEach((item) -> {
				ChargeModel chargeModel = new ChargeModel();
				chargeModel.setName(StringUtils.nvl(item[0], ""));
				chargeModel.setValue(item[1] != null ? Long.parseLong(StringUtils.nvl(item[1], "")) : null);
				chargeModel.setStatus(StringUtils.nvl(item[2], ""));
				dataTable.add(chargeModel);
			});
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		}
		return dataTable;
	}

	@Override
	public TransactionModel findDetailTransaction(String transactionId) throws Exception {
		Query query = null;
		TransactionModel transactionModel = new TransactionModel();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT REFERENCE,AMOUNT,CARD_TYPE, ");
			sb.append("STA_DATETIME,BANK_CODE,GATE_ORDER_ID,PARTNER_CODE,LANGUAGE,DISCOUNT_AMOUNT, ");
			sb.append("PAY_STATUS,PROM_AMOUNT FROM EPM_TRANSACTION ");
			sb.append("WHERE TRANSACTION_ID = :trans");
			query = entityManager.createNativeQuery(sb.toString());
			query.setParameter("trans", transactionId);
			query.setMaxResults(1);
			List<Object[]> listData = query.getResultList();
			if (listData.size() > 0) {
				Object[] item = listData.get(0);
				transactionModel.setToIsdn(StringUtils.nvl(item[0], ""));
				transactionModel.setAmount(item[1] != null ? Long.parseLong(item[1].toString()) : null);
				transactionModel.setCardType(StringUtils.nvl(item[2], ""));
				transactionModel.setStaDate(StringUtils.nvl(item[3], ""));
				transactionModel.setBankCode(StringUtils.nvl(item[4], ""));
				transactionModel.setGateOrderId(StringUtils.nvl(item[5], ""));
				transactionModel.setPartnerCode(StringUtils.nvl(item[6], ""));
				transactionModel.setLanguage(StringUtils.nvl(item[7], ""));
				transactionModel.setDiscountAmount(item[8] != null ? Double.parseDouble(item[8].toString()) : null);
				transactionModel.setPayStatus(StringUtils.nvl(item[9], ""));
				transactionModel.setPromotionAmount(StringUtils.nvl(item[10], ""));
			}
			else return null;
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		}
		return transactionModel;
	}

	@Override
	public List<String> findPosAddress(Long shopId) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> posAdress = new ArrayList<String>();
		try {
			conn = info.getDataSource().getConnection();
			pstmt = conn.prepareStatement("SELECT SHOP_ADDRESS FROM SHOP WHERE SHOP_ID = ?");
			pstmt.setFetchSize(1);
			pstmt.setLong(1, shopId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				posAdress.add(rs.getString("SHOP_ADDRESS"));
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		} finally {
			Tools.closeDatabaseObject(rs);
			Tools.closeDatabaseObject(pstmt);
			Tools.closeDatabaseObject(conn);
		}
		return posAdress;
	}

	@Override
	public List<PosModel> findAllPosByProvince(Long provinceId) throws Exception {
		Query query = null;
		List<PosModel> posModels = new ArrayList<PosModel>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT s.shop_id, s.shop_code, s.shop_name, s.shop_address ");
			sb.append("FROM shop s WHERE s.province_code = ");
			sb.append("(SELECT pay_area_code FROM pay_area WHERE pay_area_id  = :province_id) ");
			sb.append("AND s.shop_status =1 and s.shop_type = '3' and s.center_code BETWEEN '1' and '9' ");
			sb.append("ORDER BY SHOP_ID ");
			query = entityManager.createNativeQuery(sb.toString());
			query.setParameter("province_id", provinceId);
			List<Object[]> listData = query.getResultList();
			listData.stream().forEach(item -> {
				PosModel posModel = new PosModel();
				posModel.setShopId(item[0] != null ? Long.parseLong(item[0].toString()) : null);
				posModel.setShopCode(StringUtils.nvl(item[1], ""));
				posModel.setShopName(StringUtils.nvl(item[2], ""));
				posModel.setShopAddress(StringUtils.nvl(item[3], ""));
				posModels.add(posModel);
			});
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		}
		return posModels;
	}

	@Override
	public List<ProvinceModel> findAllProvince(Long provinceId) throws Exception {
		Query query = null;
		List<ProvinceModel> provinceModels = new ArrayList<ProvinceModel>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT pay_area_id, pay_area_code, name ");
			sb.append("FROM pay_area ");
			sb.append("WHERE province IS NOT NULL ");
			sb.append("AND district IS NULL ");
			sb.append("AND status = 1 ");
			sb.append("AND precinct IS NULL ");
			sb.append("AND (:province = 0 OR pay_area_id = :province) ");
			sb.append("ORDER BY order_id ");
			query = entityManager.createNativeQuery(sb.toString());
			query.setParameter("province", provinceId);
			List<Object[]> listData = query.getResultList();
			listData.stream().forEach(item -> {
				ProvinceModel provinceModel = new ProvinceModel();
				provinceModel.setPayAreaId(item[0] != null ? Long.parseLong(item[0].toString()) : null);
				provinceModel.setPayAreaCode(StringUtils.nvl(item[1], ""));
				provinceModel.setName(StringUtils.nvl(item[2], ""));
				provinceModels.add(provinceModel);
			});
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		}
		return provinceModels;
	}

	@Transactional
	@Override
	public int addInvoiveTransaction(AddInvoiceTransRequestModel addInvoiceTransRequestModel) throws Exception {
		// TODO Auto-generated method stub
		Query query = null;
		int check = 0;
		int count = 0;
		try {
			if(ValidationUtils.isNullOrEmpty(addInvoiceTransRequestModel.getType()) || "1".equals(addInvoiceTransRequestModel.getType())) {
				log.info("start addInvoiceTransaction");
				EpmTransaction epmTransaction = checkAddInvoiceInEpmTrans(addInvoiceTransRequestModel.getTransactionId(),addInvoiceTransRequestModel.getReference());
				if(epmTransaction.getTransactionId()!=null && epmTransaction.getStaDateTime() != null && epmTransaction.getTransactionId().equals(addInvoiceTransRequestModel.getTransactionId()))
					check = -1;
				if (check != 0) {
					//Điều kiện chỉ dc xuất hóa đơn trong tháng hiện tại
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
					LocalDateTime parsedDate = LocalDateTime.parse(epmTransaction.getStaDateTime().toString(), formatter);
					int currentYear = LocalDateTime.now().getYear();
					int currentMonth = LocalDateTime.now().getMonthValue();

					int dayYear = parsedDate.getYear();
					int dayMonth = parsedDate.getMonthValue();
					if (currentYear != dayYear || currentMonth != dayMonth) {
						return 3;
					}
					check = checkAddInvoiceInEpmInvoice(addInvoiceTransRequestModel.getTransactionId(),addInvoiceTransRequestModel.getReference());
					if (check != 2) {
						String shopName ="",shopCode="";
						ShopRedisModel shopRedisModel = shopService.findById(addInvoiceTransRequestModel.getShopId());
						if(shopRedisModel != null){
							shopName = shopRedisModel.getShopName();
							shopCode = shopRedisModel.getShopCode();
							addInvoiceTransRequestModel.setShopCode(shopCode);
							addInvoiceTransRequestModel.setShopName(shopName);
						}
						int i=1;
						StringBuilder sb = new StringBuilder();
						sb.append("insert into EPM_INVOICE(INVOICE_ID,TRANSACTION_ID,INVOICE_NO, ");
						sb.append("TAX_CODE,REFERENCE,ADDRESS,Name, ");
						sb.append("province_id,shop_name,shop_id,EVENT_DATE,EMAIL,STATUS,SHOP_CODE,AMOUNT,TYPE)values(epm_invoice_seq.nextVal,?,?,?,?,?,?,?,?,?,sysdate,?,?,?,?,?) ");
						query = entityManager.createNativeQuery(sb.toString());
						query.setParameter(i++, addInvoiceTransRequestModel.getTransactionId());
						query.setParameter(i++, "Y");
						query.setParameter(i++, addInvoiceTransRequestModel.getTaxCode());
						query.setParameter(i++, addInvoiceTransRequestModel.getReference());
						query.setParameter(i++, addInvoiceTransRequestModel.getAddress());
						query.setParameter(i++, addInvoiceTransRequestModel.getName());
						query.setParameter(i++, addInvoiceTransRequestModel.getProvinceId());
						query.setParameter(i++,shopName);
						query.setParameter(i++, addInvoiceTransRequestModel.getShopId() != null
								? Long.parseLong(addInvoiceTransRequestModel.getShopId().toString()) : null);
						query.setParameter(i++, addInvoiceTransRequestModel.getEmail());
						query.setParameter(i++, "0");
						query.setParameter(i++, shopCode);
						query.setParameter(i++, epmTransaction.getAmount());
						query.setParameter(i++, "1");
						count = query.executeUpdate();

						if (count > 0) {
							boolean success = this.callCreateSaleTransactions(addInvoiceTransRequestModel, EPMApiConstant.EPM_TRANSACTION);
							if(success) {
								String sql = "UPDATE EPM_INVOICE set status = '1' where TRANSACTION_ID = ? ";
								query = entityManager.createNativeQuery(sql);
								query.setParameter(1, addInvoiceTransRequestModel.getTransactionId());
								query.executeUpdate();
							}
							check = 1;
						}
						log.info("addInvoice succsess");
					}
				}
			}
			else if(addInvoiceTransRequestModel.getType().equals("2")){
				log.info("start addInvoiceTransaction");
				Transactions transactions = checkAddInvoiceInTransactions(addInvoiceTransRequestModel.getTransactionId());
				if(transactions.getPartnerTransactionId()!=null && transactions.getPartnerTransactionId().equals(addInvoiceTransRequestModel.getTransactionId())){
					check = -1;
				}
				if (transactions.getPartnerTransactionId()!=null && transactions.getSettlementDate() != null) {
					//Điều kiện chỉ dc xuất hóa đơn trong tháng hiện tại
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

					LocalDateTime parsedDate = LocalDateTime.parse(transactions.getSettlementDate().toString(), formatter);

					int currentYear = LocalDateTime.now().getYear();
					int currentMonth = LocalDateTime.now().getMonthValue();

					int dayYear = parsedDate.getYear();
					int dayMonth = parsedDate.getMonthValue();
					if (currentYear != dayYear || currentMonth != dayMonth) {
						return 3;
					}
					check = checkAddInvoiceInEpmInvoice(addInvoiceTransRequestModel.getTransactionId(),addInvoiceTransRequestModel.getReference());
					if (check != 2) {
						String shopName ="",shopCode="";
						ShopRedisModel shopRedisModel = shopService.findById(addInvoiceTransRequestModel.getShopId());
						if(shopRedisModel != null){
							shopName = shopRedisModel.getShopName();
							shopCode = shopRedisModel.getShopCode();
							addInvoiceTransRequestModel.setShopCode(shopCode);
							addInvoiceTransRequestModel.setShopName(shopName);
						}
						int i=1;
						StringBuilder sb = new StringBuilder();
						sb.append("insert into EPM_INVOICE(INVOICE_ID,TRANSACTION_ID,INVOICE_NO, ");
						sb.append("TAX_CODE,REFERENCE,ADDRESS,Name, ");
						sb.append("province_id,shop_name,shop_id,EVENT_DATE,EMAIL,STATUS,SHOP_CODE,AMOUNT,TYPE)values(epm_invoice_seq.nextVal,?,?,?,?,?,?,?,?,?,sysdate,?,?,?,?,?) ");
						query = entityManager.createNativeQuery(sb.toString());
						query.setParameter(i++, addInvoiceTransRequestModel.getTransactionId());
						query.setParameter(i++, "Y");
						query.setParameter(i++, addInvoiceTransRequestModel.getTaxCode());
						query.setParameter(i++, addInvoiceTransRequestModel.getReference());
						query.setParameter(i++, addInvoiceTransRequestModel.getAddress());
						query.setParameter(i++, addInvoiceTransRequestModel.getName());
						query.setParameter(i++, addInvoiceTransRequestModel.getProvinceId());
						query.setParameter(i++,shopName);
						query.setParameter(i++, addInvoiceTransRequestModel.getShopId() != null
								? Long.parseLong(addInvoiceTransRequestModel.getShopId().toString()) : null);
						query.setParameter(i++, addInvoiceTransRequestModel.getEmail());
						query.setParameter(i++, "0");
						query.setParameter(i++, shopCode);
						query.setParameter(i++, transactions.getAmount());
						query.setParameter(i++, "2");
						count = query.executeUpdate();
						if (count > 0) {
							boolean success = this.callCreateSaleTransactions(addInvoiceTransRequestModel, EPMApiConstant.TRANSACTIONS);
							if(success) {
								String sql = "UPDATE EPM_INVOICE set status = '1' where TRANSACTION_ID = ? ";
								query = entityManager.createNativeQuery(sql);
								query.setParameter(1, addInvoiceTransRequestModel.getTransactionId());
								query.executeUpdate();
							}
							check = 1;
						}
						log.info("addInvoice succsess");
					}
				}
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error addInvoiceTrans "+exp.getMessage(), exp);
			throw exp;
		}
		return check;
	}

	public int checkAddInvoiceInEpmInvoice(String transactionId,String reference) throws SQLException {
		Query query = null;
		try {
			log.info("Start check invoice in epm_invoice");
			// Kiểm tra đã tồn tại trong bảng Invoice chưa ? (Nếu có thì => đã
			// xuất hóa đơn)
			query = entityManager.createNativeQuery("SELECT * FROM EPM_INVOICE WHERE TRANSACTION_ID = :trans and reference = :reference");
			query.setParameter("trans", transactionId);
			query.setParameter("reference", reference);
			query.setMaxResults(1);
			List results = query.getResultList();
			if (!results.isEmpty() && results.size()>0)
				return 2;
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error check invoice in epm_invoice"+ exp.getMessage(), exp);
			throw exp;
		}
		return -1;
	}

	public EpmTransaction checkAddInvoiceInEpmTrans(String transactionId,String reference) throws SQLException {
		Query query = null;
		EpmTransaction epmTransaction = new EpmTransaction();
		try {
			log.info("Start check invoice in epm_transaction");
			// Kiểm tra xem đã tồn tại trong bảng EPM chưa ? Chưa => Chưa tt
			// thành công =>
			LocalDate currentDate = LocalDate.now();
			query = entityManager.createNativeQuery(
					"SELECT AMOUNT, STA_DATETIME  FROM EPM_TRANSACTION WHERE TRANSACTION_ID = :trans AND reference = :reference AND PAY_STATUS ='SUCC'");
			query.setParameter("trans", transactionId);
			query.setParameter("reference", reference);
			query.setMaxResults(1);

			// Lấy kết quả
			List<Object[]> result = query.getResultList();

			if (!result.isEmpty()){
//				BigDecimal val = (BigDecimal)result.get(0);
//				epmTransaction.setAmount(Long.parseLong(val.toString()));
//				epmTransaction.setTransactionId(transactionId);

				Object[] row = result.get(0);
				BigDecimal val = (BigDecimal) row[0];
				epmTransaction.setAmount(Long.parseLong(val.toString()));
				epmTransaction.setTransactionId(transactionId);
				Date staDateTime = (Date) row[1]; // Chuyển đổi STA_DATETIME
				epmTransaction.setStaDateTime(staDateTime);
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error check invoice in epm_transaction" + exp.getMessage(), exp);
			throw exp;
		}
		return epmTransaction;
	}

	public Transactions checkAddInvoiceInTransactions(String transactionId) throws SQLException {
		Query query = null;
		Transactions transactions = new Transactions();
		try {
			log.info("Start check invoice in Transactions");
			 query = entityManager.createNativeQuery(
					"SELECT AMOUNT, SETTLEMENT_DATE FROM TRANSACTIONS " +
							"WHERE PARTNER_TRANSACTION_ID = :trans " +
							"AND STATUS = :status " +
							"AND PARTNER_TRANSACTION_ID NOT LIKE '%PRE%' " +
							"AND PARTNER_TRANSACTION_ID NOT LIKE '%POS%' " +
							"AND PARTNER_TRANSACTION_ID NOT LIKE '%FIB%'");
			query.setParameter("trans", transactionId);
			query.setParameter("status", "1");
			query.setMaxResults(1);
			// Lấy kết quả
			List<Object[]> result = query.getResultList();
			if (!result.isEmpty()){
				Object[] row = result.get(0);
//				BigDecimal val = (BigDecimal)result.get(0);
//				transactions.setAmount(Long.parseLong(val.toString()));
//				transactions.setPartnerTransactionId(transactionId);
//				transactions.setSettlementDate(result.get(1).toString());

				BigDecimal val = (BigDecimal) row[0];
				transactions.setAmount(Long.parseLong(val.toString()));
				transactions.setPartnerTransactionId(transactionId);
				Date settlementDate = (Date) row[1]; // Chuyển đổi SETTLEMENT_DATE
				transactions.setSettlementDate(settlementDate);
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error check invoice in Transactions" + exp.getMessage(), exp);
			throw exp;
		}
		return transactions;
	}


	@Override
	public List<TokenModel> findAllToken(String fromIsdn) throws Exception {
		Query query = null;
		List<TokenModel> tokenModels = new ArrayList<TokenModel>();
		try {
			StringBuilder sb = new StringBuilder();
			sb.append("SELECT TOKEN_ID,TOKEN_NAME,CARD_TYPE,BANK_CODE,BANK_OTP,REFERENCE,CREATE_DATE,CASE WHEN expired_month IS NOT NULL AND expired_year IS NOT NULL THEN  TO_CHAR(TO_DATE(expired_month||'/'||expired_year,'MM/yy'),'MM/yyyy') ELSE '' END expired_date ");
			sb.append(" FROM BANK_TOKEN ");
			sb.append(" WHERE STATUS = '1' ");
			sb.append(" AND REFERENCE= ? ");
			query = entityManager.createNativeQuery(sb.toString());
			query.setParameter(1, fromIsdn);
			List<Object[]> listData = query.getResultList();
			listData.stream().forEach(item -> {
				TokenModel tokenModel = new TokenModel();
				tokenModel.setTokenId(StringUtils.nvl(item[0], ""));
				tokenModel.setTokenName(StringUtils.nvl(item[1], ""));
				tokenModel.setCardType(StringUtils.nvl(item[2], ""));
				tokenModel.setBankCode(StringUtils.nvl(item[3], ""));
				tokenModel.setBankOtp(StringUtils.nvl(item[4], ""));
				tokenModel.setFromIsdn(StringUtils.nvl(item[5], ""));
				tokenModel.setCreateDate(StringUtils.nvl(item[6], ""));
				tokenModel.setExpiredDate(StringUtils.nvl(item[7],""));
				try {
					tokenModel.setAutoDebitInfo(getAutoDebitInfo(tokenModel.getTokenId()));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tokenModels.add(tokenModel);
			});
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		}

		return tokenModels;
	}

	@Override
	public PromotionModel loadPromotion(PromotionRequestModel request, String userName) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		String pattern = "dd-MM-YYYY";
		String bankCode="";
		ResultSet rs = null;
		CallableStatement ps = null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		PromotionModel promotionModel = new PromotionModel();
		log.info("Start loadPromotion");
		try {
			conn = info.getDataSource().getConnection();
			IsdnInfoRedisModel infoRedisModel = isdnInfoService.getById(request.getIsdn());
			if (infoRedisModel != null) {
				bankCode = request.getBankCode();
				String sql = "{?=call compute_discount_info(?,?,to_date(?,'dd/mm/yyyy'),?,?,?,?)}";
				ps = conn.prepareCall(sql);
				ps.registerOutParameter(1, Types.VARCHAR);
				ps.setString(2, request.getIsdn());
				ps.setString(3, infoRedisModel.getTypeSub());
				ps.setString(4, date);
				ps.setString(5, bankCode);
				ps.setString(6, this.getPartnerCode(bankCode));
				ps.setString(7, this.getChannel(userName));
				ps.registerOutParameter(8, Types.VARCHAR);
				ps.execute();
				String pOut = ps.getString(1);
				String returnValue = ps.getString(8);
				String[] tg = pOut.split("\\|");
				promotionModel.setPReturn(returnValue);
				promotionModel.setType(tg[0]);
				promotionModel.setValue(tg[1]);
				promotionModel.setMethod(tg[2]);
			}
			else
			{
				promotionModel.setPReturn("");
				promotionModel.setType("0");
				promotionModel.setValue("0");
				promotionModel.setMethod("");
			}
			
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.info("Error loadPromotion "+exp.getMessage(), exp);
			throw exp;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
			log.info("Close loadPromotion");

		}
		return promotionModel;
	}

	// Get partner code(merchanCode) cho hàm get promotion
	public String getPartnerCode(String bankCode) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String value = "";

		try {
			String sql = "select p.code,pb.partner_bank_code from partners p, partner_bank pb, bank b  "
					+ "where p.partner_id = pb.partner_id and b.bank_id = pb.bank_id and pb.status = 1 and p.status = 1 "
					+ "and b.bank_code = ?";
			conn = info.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, bankCode);
			rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getString("code");
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			throw e;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return value;
	}

	// GET CHANEL CHO HÀM PROMOTION
	public String getChannel(String userName) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String value = "";
		try {
			String sql = "SELECT CHANEL_CODE FROM payment_chanel_config WHERE USER_NAME = ?";
			conn = info.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, userName);
			rs = ps.executeQuery();
			while (rs.next()) {
				value = rs.getString("CHANEL_CODE");
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			throw e;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return value;
	}

	public Boolean callCreateSaleTransactions(AddInvoiceTransRequestModel request, String type) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String pattern = "dd/MM/YYYY";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		String date = simpleDateFormat.format(new Date());
		CreateSaleTransApiModel billingTransAPIModel = new CreateSaleTransApiModel();
		try {
			conn = info.getDataSource().getConnection();
			// Luồng cũ (1) sẽ lấy thông tin từ bảng EPM_TRANSACTION để call BHTT
			if(EPMApiConstant.EPM_TRANSACTION.equals(type)){
				String sql = "SELECT * FROM EPM_TRANSACTION WHERE TRANSACTION_ID = ?";
				ps = conn.prepareStatement(sql);
				ps.setFetchSize(1);
				ps.setString(1, request.getTransactionId());
				rs = ps.executeQuery();
				while (rs.next()) {
					billingTransAPIModel.setSaleTransDate(date);
					billingTransAPIModel.setSaleTransType("8");
					billingTransAPIModel.setShopCode(request.getShopCode());
					billingTransAPIModel.setStaffCode("");
					billingTransAPIModel.setCustomerName(request.getName());
					billingTransAPIModel.setTelNumber(request.getReference());
					billingTransAPIModel.setEmail(request.getEmail());
					billingTransAPIModel.setCompany("");
					billingTransAPIModel.setAddress(request.getAddress());
					billingTransAPIModel.setTin(request.getTaxCode());
					billingTransAPIModel.setNote("");
					billingTransAPIModel.setAmount(rs.getDouble("AMOUNT"));
					billingTransAPIModel.setVat(rs.getLong("VAT"));
					billingTransAPIModel.setDiscount(rs.getLong("DISCOUNT_AMOUNT"));
					billingTransAPIModel.setReasonCode("BH");
					billingTransAPIModel.setPayMethod("2");
					CreateSaleTransDetailModel detail = new CreateSaleTransDetailModel();
					detail.setGoodCode("ETOPUP");
					detail.setPrice(rs.getDouble("AMOUNT"));
					detail.setQuantity(1d);
					billingTransAPIModel.getLtsDetail().add(detail);
				}
				// Luồng mới (2) sẽ lấy thông tin từ bảng TRANSACTIONS để call BHTT
			} else if (EPMApiConstant.TRANSACTIONS.equals(type)) {
				String sql = "SELECT * FROM TRANSACTIONS WHERE PARTNER_TRANSACTION_ID = ?";
				ps = conn.prepareStatement(sql);
				ps.setFetchSize(1);
				ps.setString(1, request.getTransactionId());
				rs = ps.executeQuery();
				while (rs.next()) {
					billingTransAPIModel.setSaleTransDate(date);
					billingTransAPIModel.setSaleTransType("8");
					billingTransAPIModel.setShopCode(request.getShopCode());
					billingTransAPIModel.setStaffCode("");
					billingTransAPIModel.setCustomerName(request.getName());
					billingTransAPIModel.setTelNumber(request.getReference());
					billingTransAPIModel.setEmail(request.getEmail());
					billingTransAPIModel.setCompany("");
					billingTransAPIModel.setAddress(request.getAddress());
					billingTransAPIModel.setTin(request.getTaxCode());
					billingTransAPIModel.setNote("");
					billingTransAPIModel.setAmount(rs.getDouble("AMOUNT"));
					billingTransAPIModel.setVat(10L);
					billingTransAPIModel.setDiscount(rs.getLong("DISCOUNT_VALUE"));
					billingTransAPIModel.setReasonCode("BH");
					billingTransAPIModel.setPayMethod("2");
					CreateSaleTransDetailModel detail = new CreateSaleTransDetailModel();
					detail.setGoodCode("ETOPUP");
					detail.setPrice(rs.getDouble("AMOUNT"));
					detail.setQuantity(1d);
					billingTransAPIModel.getLtsDetail().add(detail);
				}
			}

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.setBasicAuth(bhttUserName, bhttPassword);

			log.info("---> call api bhtt CreateSaleTransactions: " + mapper.writeValueAsString(billingTransAPIModel));

			CreateSaleTransactionRespone object = (CreateSaleTransactionRespone) this.resttemplateBean
					.handleHttpRequest(bhttUrl + bhttApiCreateSaleTransaction, this.iHttpRequestTimeout,
							HttpMethod.POST, headers, billingTransAPIModel, CreateSaleTransactionRespone.class, request.getTransactionId());
			log.info("Result call to bhtt : "+object);
			if (object != null && object.getCode().equals("API000") && object.getMessage().equals("Success!"))
				return true;
		}

		catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			return false;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return false;
	}

	@Override
	public boolean registerAutoDebitModel(AutoDebitModel data) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		boolean check = false;
		try {
			if(checkAutodebitInfo(data.getMsisdn()) == null){
				conn = info.getDataSource().getConnection();
				int i=1;
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO auto_debit_info(auto_debit_id,msisdn,sub_type,amount,threshold_amount ");
				sb.append(",payment_day,tokenizer,migs,code_bank,sub_id,from_isdn,status,start_date,sub_id_reg,partner_id) ");
				sb.append("VALUES(auto_debit_info_seq.nextVal,?,?,?,?,?,?,?,?,?,?,'1',sysdate,?, ? ) ");
				ps = conn.prepareStatement(sb.toString());
				ps.setString(i++, StringUtils.nvl(data.getMsisdn(), ""));
				ps.setString(i++, StringUtils.nvl(data.getSubType(), ""));
				String amount = data.getAmount();
				String thresholdAmount= data.getThresholdAmount();
				if(amount != null)
					ps.setLong(i++, Long.parseLong(amount));
				else 
					ps.setNull(i++, Types.NULL);
				if(thresholdAmount != null)
					ps.setLong(i++, Long.parseLong(thresholdAmount));
				else 
					ps.setNull(i++, Types.NULL);
				if(data.getSubType()!= null &&data.getSubType().equals("MF"))
					ps.setString(i++, StringUtils.nvl(data.getPaymentDay(), ""));
				else
					ps.setString(i++,"");
				ps.setString(i++, StringUtils.nvl(data.getTokenizer(), ""));
				ps.setString(i++, StringUtils.nvl(data.getMigs(), ""));
				ps.setString(i++, StringUtils.nvl(data.getCodeBank(), ""));
				ps.setString(i++, StringUtils.nvl(data.getSubId(), ""));
				ps.setString(i++, StringUtils.nvl(data.getFromIsdn(), ""));
				ps.setString(i++, StringUtils.nvl(data.getSubIdReg(), ""));
				ps.setString(i++, StringUtils.nvl(partnerID, "-1"));
				int count = ps.executeUpdate();
				if(count>0)
					check = true;
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.info("Error register_auto_debit"+exp.getMessage(), exp);
			return false;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return check;
	}

	@Override
	public String checkTokenFromIsdn(String msisdn) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		String value = "";

		try {
			String sql = "select tokenizer from auto_debit_info where msisdn = ? and status ='1'";
			conn = info.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, msisdn);
			rs = ps.executeQuery();
			while (rs.next()) {
				value += rs.getString("tokenizer") + ",";
			}
		} catch (Exception e) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
			throw e;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return value;
	}

	public List<AutoDebitModel> getAutoDebitInfo(String token) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		List<AutoDebitModel> listData = new ArrayList<AutoDebitModel>();

		try {
			
			String sql = "select * from auto_debit_info where tokenizer = ? and status = '1'";
			conn = info.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, token);
			rs = ps.executeQuery();
			while (rs.next()) {
				AutoDebitModel autoDebitModel = new AutoDebitModel();
				autoDebitModel.setMsisdn(StringUtils.nvl(rs.getString("msisdn"), ""));
				autoDebitModel.setSubType(StringUtils.nvl(rs.getString("sub_type"), ""));
				autoDebitModel.setAmount(StringUtils.nvl(rs.getString("amount"), ""));
				autoDebitModel.setStartDate(StringUtils.nvl(rs.getString("start_date"), ""));
				autoDebitModel.setPaymentDay(StringUtils.nvl(rs.getString("payment_day"), ""));
				autoDebitModel.setTokenizer(StringUtils.nvl(rs.getString("tokenizer"), ""));
				autoDebitModel.setFromIsdn(StringUtils.nvl(rs.getString("from_isdn"), ""));
				listData.add(autoDebitModel);
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			throw exp;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return listData;
	}

	@Override
	public CancelAutoDebitModel cancelAutoDebit(String msisdn, String userName) throws Exception {	
		CancelAutoDebitModel cancelAutoDebitModel = new CancelAutoDebitModel();
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		PreparedStatement ps = null;
		int count = 0;
		try {
			Long autoDebitId = checkAutodebitInfo(msisdn);
			if(autoDebitId != null){
				if(checkCancelDebitInfo(msisdn)){
				String sql = "update auto_debit_info set status = '0' ,end_date=sysdate where msisdn =? and end_date is null";
				conn = info.getDataSource().getConnection();
				ps = conn.prepareStatement(sql);
				ps.setString(1,msisdn);
				count = ps.executeUpdate();
				if (count>0) {
						insertLogAuditDebit(conn, userName, autoDebitId);
						cancelAutoDebitModel.setCode(EPMMessageCode.API_SUCCESSED_CODE);
					}
				}
				else cancelAutoDebitModel.setCode(EPMMessageCode.ERROR_AUTODEBIT_3MONTH);
			}
			else 
				cancelAutoDebitModel.setCode(EPMMessageCode.ERROR_NOT_FOUND_AUTODEBIT);	
		}catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("error cancelAutoDebit ", exp);
			throw exp;
		} finally {
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return cancelAutoDebitModel;
	}

	// Check tồn tại auto debit
	public Long checkAutodebitInfo(String msisdn) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement ps = null;
		Long autoDebitId = null;
		try {
			String sql = "select auto_debit_id from auto_debit_info where msisdn = ? and status ='1' and end_date is null";
			conn = info.getDataSource().getConnection();
			ps = conn.prepareStatement(sql);
			ps.setString(1, msisdn);
			rs = ps.executeQuery();
			if (rs.next()) {
				autoDebitId = rs.getLong(1);
			}
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("error checkAutodebitInfo ", exp);
			throw exp;
		} finally {
			Tools.closeObject(rs);
			Tools.closeObject(ps);
			Tools.closeObject(conn);
		}
		return autoDebitId;
		
	}
	// Check hủy trc 3 tháng
		public boolean checkCancelDebitInfo(String msisdn) throws Exception {
			EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;

			try {
				String sql = "SELECT 1 FROM auto_debit_info Where trunc(ADD_MONTHS(start_date,(select par_value from ap_param where par_name='AUTO_PAY_CANCEL' and par_type = 'AUTO_PAY'))) <=trunc(sysdate) and msisdn = ? and status ='1'";
				conn = info.getDataSource().getConnection();
				ps = conn.prepareStatement(sql);
				ps.setString(1, msisdn);
				rs = ps.executeQuery();
				if (rs.next()) {
					return true;
				}
			} catch (Exception exp) {
				AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
				log.error("error checkCancelDebitInfo ", exp);
				throw exp;
			} finally {
				Tools.closeObject(rs);
				Tools.closeObject(ps);
				Tools.closeObject(conn);
			}
			return false;
			
		}
		
		private void insertLogAuditDebit(Connection conn, String userName , Long autoDebitId) throws Exception {
			PreparedStatement ps = null;
			try {
				String sql = "insert into auto_debit_audit(AUDIT_ID,AUTO_DEBIT_ID,ACTION_ID,USER_NAME,CREATE_DATETIME,EXCEPTION)"
						+ "values(auto_debit_audit_seq.NEXTVAL,?,?,?,sysdate,?)";
				ps = conn.prepareStatement(sql);
				ps.setLong(1, autoDebitId);
				ps.setString(2, "3");
				ps.setString(3, userName);
				ps.setString(4, "Huy do NSD huy dich vu");
				ps.executeQuery();
			} catch (Exception exp) {
				AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
				log.error("error insertLogAuditDebit ", exp);
				throw exp;
			} finally {
				Tools.closeObject(ps);
				Tools.closeObject(conn);
			}
			
		}

		@Override
		public List<Map<String, Object>> getInfoTran(String msisdn, String fromDate, String toDate, String transId)
				throws Exception {
			EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
			Connection conn = null;
			ResultSet rs = null;
			PreparedStatement ps = null;
			List<Map<String, Object>> lstMap = new ArrayList<Map<String,Object>>();
			try {
				if (!Tools.stringIsNullOrEmty(msisdn) && msisdn.length() >= 10) {
					if (msisdn.startsWith("84")) {
						msisdn = msisdn.substring(2);
					}
					if (msisdn.startsWith("0")) {
						msisdn = msisdn.substring(1);
					}
					if (msisdn.startsWith("+84")) {
						msisdn = msisdn.substring(3);
					}
				}
				conn = info.getDataSource().getConnection();
				String sql = "SELECT TRANSACTION_ID trans_id, FROM_REFERENCE from_phone, REFERENCE to_phone , TO_CHAR(STA_DATETIME,'DD/MM/YYYY HH24:MI:SS') start_time , "
						+ " TO_CHAR(END_DATETIME,'DD/MM/YYYY HH24:MI:SS') end_time , BANK_CODE bank_code, token_id, token_create,"
						+ " PAYMENT_CHANEL_CODE payment_chanel_code, AMOUNT amount, PAY_AMOUNT pay_amount, PARTNER_CODE partner_code, PAY_STATUS pay_status, ISSUE_STATUS issue_status "
						+ "  FROM EPM_TRANSACTION WHERE  1 = 1   ";
				if (!Tools.stringIsNullOrEmty(transId)) {
					sql = sql + " AND TRANSACTION_ID = '" + transId + "'";
				} else {
					if (!Tools.stringIsNullOrEmty(fromDate)) {
						sql = sql + "AND STA_DATETIME >= TO_DATE('" + fromDate + "','DD/MM/YYYY') ";
					}
					if (!Tools.stringIsNullOrEmty(toDate)) {
						sql = sql + "AND STA_DATETIME < (TO_DATE('" + toDate + "','DD/MM/YYYY') + 1) ";
					}
					if (!Tools.stringIsNullOrEmty(msisdn)) {
						sql = sql + " AND FROM_REFERENCE = '" + msisdn + "' AND REFERENCE = '" + msisdn + "'";
					}
				}
				sql = sql + " ORDER BY FROM_REFERENCE, REFERENCE , STA_DATETIME desc ";
				ps = conn.prepareStatement(sql);		
				
				rs = ps.executeQuery();
				while (rs.next()) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("trans_id", rs.getString("trans_id"));
					map.put("from_phone", rs.getString("from_phone"));
					map.put("to_phone", rs.getString("to_phone"));
					map.put("start_time", rs.getString("start_time"));
					map.put("end_time", rs.getString("end_time"));
					map.put("bank_code", rs.getString("bank_code"));
					map.put("token_id", rs.getString("token_id"));
					map.put("token_create", rs.getString("token_create"));
					map.put("payment_chanel_code", rs.getString("payment_chanel_code"));
					map.put("amount", rs.getString("amount"));
					map.put("pay_amount", rs.getString("pay_amount"));
					map.put("partner_code", rs.getString("partner_code"));
					map.put("pay_status", rs.getString("pay_status"));
					map.put("issue_status", rs.getString("issue_status"));
					lstMap.add(map);
				}				
			}

			catch (Exception e) {
				AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + e.getMessage());
				log.error(e.getMessage(), e);
				throw e;
			} finally {
				Tools.closeObject(rs);
				Tools.closeObject(ps);
				Tools.closeObject(conn);
			}
			return lstMap;
		}

	@Override
	public void insertMerChantLog(EpmMerchantLogModel merChange) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = info.getDataSource().getConnection();
			String sql = "Insert into EPM_MERCHANT_LOG "
					+ " (MERCHANT_LOG_ID,TRANSACTION_ID,USER_NAME,BANK_CODE,PAY_STATUS,REQUEST_DATETIME,RESPONSE_DATETIME,TOKEN_ID,TOKEN_CREATE,CARD_TYPE,DESCRIPTION,AMOUNT,CARD_NUMBER,CARD_HOLDER,PARTNER_CODE,RESPONSE_CODE) "
					+ " values (EPM_MERCHANT_LOG_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
			pstmt = conn.prepareStatement(sql);
			int index = 0;
			pstmt.setString(++index, merChange.getTransactionId());
			pstmt.setString(++index, merChange.getUserName());
			pstmt.setString(++index, merChange.getBankCode());
			pstmt.setString(++index, merChange.getPayStatus());
			pstmt.setTimestamp(++index, new Timestamp(merChange.getRequestDate().getTime()));
			pstmt.setTimestamp(++index, new Timestamp(merChange.getResponseDate().getTime()));
			pstmt.setString(++index, merChange.getTokenId());
			pstmt.setTimestamp(++index, merChange.getTokenCreate() != null ? new Timestamp(merChange.getTokenCreate().getTime()) : null);
			pstmt.setString(++index, merChange.getCardType());
			pstmt.setString(++index, merChange.getDescription());
			pstmt.setDouble(++index, merChange.getAmount());
			pstmt.setString(++index, merChange.getCardNumber());
			pstmt.setString(++index, merChange.getCardHolder());
			pstmt.setString(++index, merChange.getPartnerCode());
			pstmt.setString(++index, merChange.getResponseCode());
			pstmt.executeQuery();
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error insert MerchantLog TransactionID " + merChange.getTransactionId() + " with message :"+ exp.getMessage(), exp);
		} finally {
			Tools.closeDatabaseObject(pstmt);
			Tools.closeDatabaseObject(conn);
		}
	}

	@Override
	public void insertIssueLog(EpmIssueLog issue) throws Exception {
		EntityManagerFactoryInfo info = (EntityManagerFactoryInfo) entityManager.getEntityManagerFactory();
		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = info.getDataSource().getConnection();
			String sql = "Insert into EPM_ISSUE_LOG "
					+ " (ISSUE_LOG_ID,TRANSACTION_ID,USER_NAME,BILL_CYCLE_ID,ISSUE_STATUS,AMOUNT,DISCOUNT_AMOUNT,"
					+ " PROM_AMOUNT,BANK_CODE,OBJECT_TYPE,CUST_CODE,REQUEST_DATETIME,RESPOND_DATETIME,CEN_CODE,REFERENCE) "
					+ " values (EPM_ISSUE_LOG_SEQ.NEXTVAL,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ";
			pstmt = conn.prepareStatement(sql);
			int index = 0;
			pstmt.setString(++index, issue.getTransactionId());
			pstmt.setString(++index, issue.getUserName());
			pstmt.setString(++index, issue.getBillCycleId());
			pstmt.setString(++index, issue.getIssueStatus());
			pstmt.setDouble(++index, issue.getAmount());
			if(issue.getDiscountAmount() != null) {
				pstmt.setDouble(++index, issue.getDiscountAmount());
			}else {
				pstmt.setNull(++index, Types.NULL);
			}
			if(issue.getPromAmount() != null) {
				pstmt.setDouble(++index, issue.getPromAmount());
			}else {
				pstmt.setNull(++index, Types.NULL);
			}
			pstmt.setString(++index, issue.getBankCode());
			pstmt.setString(++index, issue.getObjectType());
			pstmt.setString(++index, issue.getCustCode());
			pstmt.setTimestamp(++index, new Timestamp(issue.getRequestDate().getTime()));
			pstmt.setTimestamp(++index, new Timestamp(issue.getResponseDate().getTime()));
			pstmt.setString(++index, issue.getCenCode());
			pstmt.setString(++index, issue.getReference());
			pstmt.executeQuery();
		} catch (Exception exp) {
			AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + exp.getMessage());
			log.error("Error insert issueLog TransactionID " + issue.getTransactionId() + " with message :"+ exp.getMessage(), exp);
		} finally {
			Tools.closeDatabaseObject(pstmt);
			Tools.closeDatabaseObject(conn);
		}
	}

}

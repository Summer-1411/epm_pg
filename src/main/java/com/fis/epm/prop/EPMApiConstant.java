package com.fis.epm.prop;

public class EPMApiConstant {
	//api name config
	public final static String EPM_ROOT_API_MAPPING = "/epm";
	public final static String FIND_ALL_BANK_API_MAPPING = "/get-all-banks";
	public final static String LOAD_PARAM_BY_MSISDN_API_MAPPING = "/get-param-by-msisdn";
	public final static String LOAD_PARAM_BY_MSISDN_API_MAPPING_CS = "/get-param-by-msisdn-cs";
	public final static String LOAD_ALL_CHARGER_INFO_API_MAPPING = "/get-all-charges";
	public final static String LOAD_PROMOTION_API_MAPPING = "/get-promotion";
	public final static String LOAD_PROMOTION_API_MAPPING_CS = "/get-promotion-cs";
	public final static String PREPAID_HANDLE_API_MAPPING = "/create-transaction";
	public final static String LOAD_DETAIL_TRANSACTION_API_MAPPING = "/get-detail-trans";
	public final static String LOAD_DETAIL_TRANSACTION_API_MAPPING_CS = "/get-detail-trans-cs";
	public final static String LOAD_PROVINCE_API_MAPPING = "/get-province";
	public final static String LOAD_PROVINCE_API_MAPPING_CS = "/get-province-cs";
	public final static String LOAD_POS_API_MAPPING = "/get-pos";
	public final static String LOAD_POS_API_MAPPING_CS = "/get-pos-cs";
	public final static String LOAD_POS_ADDRESS_API_MAPPING = "/get-pos-address";
	public final static String LOAD_POS_ADDRESS_API_MAPPING_CS = "/get-pos-address-cs";
	public final static String ADD_INVOIVE_TRANS_API_MAPPING = "/add-invoice-trans";
	public final static String ADD_PREPAID_TOKEN_API_MAPPING = "/add-prepaid-token";
	public final static String LOAD_ALL_TOKEN_API_MAPPING = "/get-all-token";
	public final static String LOAD_ALL_TOKEN_API_MAPPING_CS = "/get-all-token-cs";
	public final static String DELETE_TOKEN_API_MAPPING = "/delete-token";
	public final static String EPM_COMM_ROOT_API_MAPPING = "/comm";
	public final static String GEN_DATA_CHECK_SUM = "/gen-data-check-sum";
	public final static String GEN_LIST_DATA_CHECK_SUM = "/gen-list-data-check-sum";
	public final static String DECODE_DATA_CHECK_SUM = "/decode-data-check-sum";
	public final static String REGISTER_AUTO_DEBIT ="/register-auto-debit";
	public final static String CANCEL_AUTO_DEBIT="/cancel-auto-debit";
	public final static String CHECK_TOKEN_FROM_ISDN="/check-token-from-isdn";
	public final static String GET_INFO_TRANSACTION="/get-info-transaction";
	
	public final static String SEARCH_BY_USSD_API_MAPPING = "/search-by-USSD";
	
	//bean name config
	public final static String APP_MESSAGE_DICTIONARY_BEAN = "messageDictionaryBean";
	public final static String ASYN_EXECUTOR_BEAN = "asyncExecutor";
	public final static String APP_CACHE_MANAGER_BEAN = "appCacheManagerBean";
	public final static String ACCEPT_PAYMENT_REQUEST_BEAN = "acceptPaymentRequestQueue";
	
	//app prop
	public final static String MESSAGE_DICTIONARY_FILE_PATH = "${com.fis.epm.message-file-path}";
	public final static String CORE_POOL_NUMBER_PROP = "${com.fis.epm.core-pool}";
	public final static String MAX_POOL_NUMBER_PROP = "${com.fis.epm.max-pool}";
	public final static String QUEUE_CAPACITY_NUMBER_PROP = "${com.fis.epm.queue-capacity}";
	public final static String CROSS_ORIGIN_PROP = "${com.fis.epm.cross-origin}";
	public final static String JWT_PRIVATE_KEY_PROP="${com.fis.epm.jwt.private.key}";
	public final static String JWT_PUBLIC_KEY_PROP="${com.fis.epm.jwt.public.key}";

	//getType
	public final static String API_GET_TYPE_CACHE = "cache";
	
	//API(EPM HandelController)
	public final static String NOT_NULL_OR_EMPTY ="Trường bắt buộc: ";
	public final static String NOT_EMPTY ="Không được để trống: ";
	public final static String LONG_ERR ="kí tự";
	public final static String EMAIL_ERR="email";
	public final static String ERROR_SUBTYPE ="Sub-type không đúng";
	public final static String CUS_TYPE_0="Khách hàng cá nhân";
	public final static String CUS_TYPE_1="Khách hàng doanh nghiệp";
	public final static String SUB_TYPE_POSTPAID="PARAM BILL";
	public final static String SUB_TYPE_PREPAID="PARAM CHARGE";
	public final static String EMAIL_LENGH_ERROR="Email vượt quá 50 kí tự";
	public final static String TRANS_LENGH_ERROR="Mã giao dịch vượt quá 100 kí tự";
	public final static String BANKCODE_LENGH_ERROR="bankCode vượt quá 50 kí tự";
	public final static String REFERENCE_LENGH_ERROR="Mã giao dịch vượt quá 20 kí tự";
	public final static String TOKENCREATE_LENGH_ERROR="Token create vượt quá 1 kí tự";
	public final static String ERROR_EMAIL="Sai định dạng email";
	public final static String ERROR_ISDN="Sai định dạng số thuê bao: ";
	public final static String ISDN_DEBUG="Số thuê bao debug sai: ";
	public final static String AMOUNT_DEBUG="Số tiền debug sai: ";
	public final static String ERROR_SUBID ="SUB_ID không đúng";

	//Hạn mức thanh toán 
	//Trả sau
	public final static String POSTPAID_MIN_AMOUNT="POSTPAID_MIN_AMOUNT";
	public final static String POSTPAID_MAX_AMOUNT="POSTPAID_MAX_AMOUNT";
	public final static String MAX_AMOUNT_PER_DAY="MAX_AMOUNT_PER_DAY";
	//Trả trc
	public final static String PREPAID_MAX_AMOUNT_CHARGE_DAY="PREPAID_MAX_AMOUNT_CHARGE_DAY";
	public final static String MAX_TRANSACTION_PER_DAY="MAX_TRANSACTION_PER_DAY";
	
	public final static String AP_PAR_TYPE_IP_PARTNER="IP_PARTNER";

	public final static String EPM_TRANSACTION="EPM_TRANSACTION";
	public final static String TRANSACTIONS="TRANSACTIONS";

	// Đăng ký gói cước
	public final static String CREATE_PACKAGE="SPA";


	
}

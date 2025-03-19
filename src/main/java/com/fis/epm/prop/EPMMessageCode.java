package com.fis.epm.prop;

public class EPMMessageCode {
	public final static String API_SUCCESSED_CODE = "API-000";
	public final static String API_EXCEPTION_CODE = "API-999";
	public final static String QUEUE_ACCEPT_IS_NULL_OR_FULL_CODE = "API-001";
	
	public final static String API_ERROR_ADD_INVOICE_FALSE="API-011";
	public final static String API_ERROR_ADD_INVOICE="API-012";
	public final static String API_ERROR_ALLOW_INVOICE_THIS_MONTH ="API-013";
	public final static String ERROR_EMPTY_CODE = "API-110";
	public final static String ERROR_LONG_CODE ="API-111";
	public final static String ERROR_REQUIRE_CODE="API-112";
	public final static String ERROR_EMAIL="API-113";
	public final static String ERROR_ISDN="API-114";
	public final static String ERROR_MAX_AMOUNT_PER_DAY="API-115";
	public final static String ERROR_MAX_TRANS_PER_DAY="API-116";
	public final static String ERROR_MIN_AMOUNT_1_TRANS="API-122";
	public final static String ERROR_MAX_AMOUNT_1_TRANS="API-117";
	public final static String ERROR_BANK="API-118";
	public final static String ERROR_CHECKSUM="API-119";
	public final static String ERROR_USER_PERMISS="API-120";
	public final static String ERROR_PAY_AMOUNT="API-121";
	public final static String ERROR_NOT_FOUND_ISDN ="API-125";
	public final static String ERROR_NOT_FOUND_AUTODEBIT="API-126";
	//Check thuê bao
	public final static String ERROR_AUTODEBIT_3MONTH="API-127";
	
	public final static String ERROR_EXESTS_AUTODEBIT_INFO="API-128";
	public final static String ERROR_NUMBER_FORMAT="API-129";
	
	public final static String ERROR_PAY_NAPAS="API-123";
	public final static String ERROR_PAY_VNPAY="API-124";
	public final static String PREPAID_TOKEN_CARD_TYPE_IS_NOT_MATCH = "API-130";
	
	//Lấy param từ ap_param
	public final static String GET_PARAM_LIST_ERROR="API-131";
	public final static String BANK_TYPE_AND_AMOUNT_NOT_VALID = "API-132";
	
}

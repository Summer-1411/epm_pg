package com.fis.epm.utils;

import com.fis.epm.entity.LogApiResult;
import com.fis.epm.entity.UssdLog;
import com.fis.pg.common.utils.LinkQueue;
import com.fis.pg.epm.models.EpmTransactionModel;
import com.fis.pg.epm.models.ObjSendResultEPMToMBF;

public class EPMQueueManager {
	public static final LinkQueue<LogApiResult> QUEUE_LOG_API_RESULT = new LinkQueue<>();
	public static final LinkQueue<LogApiResult> QUEUE_LOG_API_SEND = new LinkQueue<>();
	public static final LinkQueue<UssdLog> QUEUE_LOG_USSD = new LinkQueue<>();
	public static final LinkQueue<EpmTransactionModel> QUEUE_EXPORT_ORDER = new LinkQueue<>();
	public static final LinkQueue<ObjSendResultEPMToMBF> QUEUE_SEC_RESULT_EPM_TO_MBF = new LinkQueue<>();

}

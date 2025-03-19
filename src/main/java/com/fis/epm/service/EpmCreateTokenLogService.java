package com.fis.epm.service;

import com.fis.epm.entity.EpmCreateTokenLog;

public interface EpmCreateTokenLogService {
	EpmCreateTokenLog findByTransactionId(String transactionId);
}

package com.fis.epm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.entity.EpmCreateTokenLog;
import com.fis.epm.repo.EpmCreateTokenLogRepo;
import com.fis.epm.service.EpmCreateTokenLogService;

@Service
public class EpmCreateTokenLogServiceImpl implements EpmCreateTokenLogService{

	@Autowired
	private EpmCreateTokenLogRepo createTokenLogRepo;
	
	@Override
	public EpmCreateTokenLog findByTransactionId(String transactionId) {
		// TODO Auto-generated method stub
		return createTokenLogRepo.findByTransactionId(transactionId);
	}

}

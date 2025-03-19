package com.fis.epm.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.entity.AutoDebitInfo;
import com.fis.epm.repo.AutoDebitInfoRepo;
import com.fis.epm.service.AutoDebitInfoService;

@Service
public class AutoDebitInfoServiceImpl implements AutoDebitInfoService{

	@Autowired
	private AutoDebitInfoRepo autoDebitInfoRepo;
	
	@Override
	public List<AutoDebitInfo> findByMsisdnAndTokenizer(String msisdn, String tokenizer) {
		// TODO Auto-generated method stub
		return autoDebitInfoRepo.findByMsisdnAndTokenizer(msisdn, tokenizer);
	}

	@Override
	public List<AutoDebitInfo> findByTokenizer(String tokenizer) {
		// TODO Auto-generated method stub
		return autoDebitInfoRepo.findByTokenizer(tokenizer);
	}

}

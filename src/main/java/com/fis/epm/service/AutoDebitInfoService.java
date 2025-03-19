package com.fis.epm.service;

import java.util.List;

import com.fis.epm.entity.AutoDebitInfo;

public interface AutoDebitInfoService {
	
	public List<AutoDebitInfo> findByMsisdnAndTokenizer(String msisdn, String tokenizer);
	
	public List<AutoDebitInfo> findByTokenizer(String tokenizer);

}

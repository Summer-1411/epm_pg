package com.fis.epm.service;


import com.fis.epm.entity.BankToken;
import com.fis.pg.epm.models.BankTokenRedisModel;

public interface BankTokenService {

	public BankToken insert(BankToken bankToken);
	
	public BankToken findByBankTokenIdAndReference(String tokenId, String reference);
	
	public BankToken delete (BankToken bankToken);
	
	Iterable<BankTokenRedisModel> findAllBankTokenRedis();
	
	public BankToken findByBankTokenId(String tokenId); 
}

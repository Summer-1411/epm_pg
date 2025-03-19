package com.fis.epm.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.entity.BankToken;
import com.fis.epm.repo.BankTokenRedisRepo;
import com.fis.epm.repo.BankTokenRepo;
import com.fis.epm.service.BankTokenService;
import com.fis.pg.epm.models.BankTokenRedisModel;

@Service
public class BankTokenServiceImpl implements BankTokenService{
	@Autowired
	private BankTokenRepo bankTokenRepo;
	
	@Autowired
	private BankTokenRedisRepo bankTokenRedisRepo;
	
	
	@Override
	public BankToken insert(BankToken bankToken) {
		// TODO Auto-generated method stub
		BankToken bankFind = bankTokenRepo.findByBankTokenIdAndReference(bankToken.getTokenId(), bankToken.getReference());
		if(bankFind!=null) {
			return bankFind;
		}
		return bankTokenRepo.save(bankToken);
	}

	@Override
	public BankToken findByBankTokenIdAndReference(String tokenId, String reference) {
		// TODO Auto-generated method stub
		return bankTokenRepo.findByBankTokenIdAndReference(tokenId, reference);
	}

	@Override
	public BankToken delete(BankToken bankToken) {
		bankToken.setStatus("0");
		bankToken.setDeleteDate(new Date());
		return bankTokenRepo.save(bankToken);
	}

	@Override
	public Iterable<BankTokenRedisModel> findAllBankTokenRedis() {
		return bankTokenRedisRepo.findAll();
	}

	@Override
	public BankToken findByBankTokenId(String tokenId) {
		// TODO Auto-generated method stub
		return bankTokenRepo.findByBankTokenId(tokenId);
	}

}

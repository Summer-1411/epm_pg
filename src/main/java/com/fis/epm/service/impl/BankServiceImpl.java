package com.fis.epm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.entity.Bank;
import com.fis.epm.models.BankModel;
import com.fis.epm.repo.BankRedisRepo;
import com.fis.epm.repo.BankRepo;
import com.fis.epm.service.BankService;
import com.fis.pg.epm.models.BankRedisModel;

@Service
public class BankServiceImpl implements BankService {
    @Autowired
    private BankRepo bankRepo;
    
    @Autowired
    private BankRedisRepo bankRedisRepo;
    
    @Override
    public List<Bank> findAll() {
        return bankRepo.findAll();
    }
    
    @Override
    public Bank findAllByBankCode(String bankCode) {
        return bankRepo.findAllByBankCode(bankCode);
    }

    @Override
	public List<BankModel> fillBankModelsByRedis() {
	Iterable<BankRedisModel> bankRedisModels =  bankRedisRepo.findAll();
	List<BankModel> bankModels = new ArrayList<BankModel>();
	for(BankRedisModel item:bankRedisModels){
		BankModel bankModel = new BankModel();
		bankModel.setBankGate(item.getBankGate());
		bankModel.setCode(item.getBankCode());
		bankModel.setId(item.getBankId());
		bankModel.setName(item.getName());
		bankModel.setToken(item.getToken());
		bankModel.setType(item.getBankType());
		bankModels.add(bankModel);
	}
		return bankModels;
	}

	@Override
	public BankRedisModel findByBankCode(String bankCode) {
		return bankRedisRepo.findByBankCode(bankCode);
	}
}

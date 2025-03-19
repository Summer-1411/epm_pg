package com.fis.epm.service;

import com.fis.epm.entity.Bank;
import com.fis.epm.models.BankModel;
import com.fis.pg.epm.models.BankRedisModel;


import java.util.List;

public interface BankService {

	public List<Bank> findAll();

    public Bank findAllByBankCode(String bankCode);
    
    
    List<BankModel> fillBankModelsByRedis();
    
    public BankRedisModel findByBankCode(String bankCode);
}

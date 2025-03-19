package com.fis.epm.service;

import com.fis.epm.entity.PartnerBank;
import com.fis.pg.epm.models.PartnerBankRedisModel;

import java.util.List;

public interface PartnerBankService {
    
	public List<PartnerBank> findByBankId(Long bankId);
    
	List<PartnerBank> findParnerBankCheck(Long bankId);
	
	List<PartnerBankRedisModel> findRedisByBankId(Long bankId);
    
	List<PartnerBankRedisModel> findAllRedis();
}

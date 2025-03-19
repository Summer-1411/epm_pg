package com.fis.epm.service.impl;

import com.fis.epm.entity.PartnerBank;
import com.fis.epm.repo.PartnerBankRedisRepo;
import com.fis.epm.repo.PartnerBankRepo;
import com.fis.epm.service.PartnerBankService;
import com.fis.pg.epm.models.PartnerBankRedisModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerBankServiceImpl implements PartnerBankService {
    @Autowired
    private PartnerBankRepo partnerBankRepo;
    
    @Autowired
    private PartnerBankRedisRepo partnerBankRedisRepo;

    @Override
    public List<PartnerBank> findByBankId(Long bankId) {
        return partnerBankRepo.findByBankId(bankId);
    }

	@Override
	public List<PartnerBank> findParnerBankCheck(Long bankId) {
		// TODO Auto-generated method stub
		return partnerBankRepo.findParnerBankCheck(bankId);
	}

	@Override
	public List<PartnerBankRedisModel> findRedisByBankId(Long bankId) {
		// TODO Auto-generated method stub
		return partnerBankRedisRepo.findByBankId(bankId);
	}

	@Override
	public List<PartnerBankRedisModel> findAllRedis() {
		// TODO Auto-generated method stub
		return (List<PartnerBankRedisModel>) partnerBankRedisRepo.findAll();
	}
}

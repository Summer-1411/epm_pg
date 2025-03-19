package com.fis.epm.service.impl;

import com.fis.epm.entity.Partners;
import com.fis.epm.repo.PartnersRedisRepo;
import com.fis.epm.repo.PartnersRepo;
import com.fis.epm.service.PartnersService;
import com.fis.pg.epm.models.PartnersRedisModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartnersServiceImpl implements PartnersService {
    @Autowired
    private PartnersRepo partnersRepo;
    
    @Autowired
    private PartnersRedisRepo partnersRedisRepo;

    @Override
    public Partners findByPartnerId(Long partnerId) {
        return partnersRepo.findByPartnerId(partnerId);
    }

	@Override
	public Partners findByCode(String code) {
		// TODO Auto-generated method stub
		return partnersRepo.findByCode(code);
	}

	@Override
	public PartnersRedisModel findRedisByPartnerId(Long partnerId) {
		// TODO Auto-generated method stub
		return partnersRedisRepo.findByPartnerId(partnerId);
	}
}

package com.fis.epm.service.impl;



import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.models.PromotionModel;
import com.fis.epm.repo.PromotionRedisRepo;
import com.fis.epm.service.PromotionService;
import com.fis.pg.epm.models.PromotionRedisModel;
@Service
public class PromotionServiceImpl implements PromotionService{

	@Autowired
	PromotionRedisRepo promotionRedisRepo;
	
	@Override
	public void syncDataRedis(PromotionModel promotionModel,String isdn) {
		PromotionRedisModel redisModel = new PromotionRedisModel();
		redisModel.setCreateDate(new Date().getTime());
		redisModel.setMethod(promotionModel.getMethod());
		redisModel.setPReturn(promotionModel.getPReturn());
		redisModel.setType(promotionModel.getType());
		redisModel.setValue(promotionModel.getValue());
		redisModel.setIsdn(isdn);
		promotionRedisRepo.save(redisModel);
	}

	@Override
	public PromotionRedisModel getById(String id) {
		// TODO Auto-generated method stub
		Optional<PromotionRedisModel> promotion = promotionRedisRepo.findById(id);
		return !promotion.isPresent() ? null : promotion.get();
	}



}

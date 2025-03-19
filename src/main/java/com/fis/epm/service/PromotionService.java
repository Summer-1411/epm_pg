package com.fis.epm.service;


import com.fis.epm.models.PromotionModel;
import com.fis.pg.epm.models.PromotionRedisModel;


public interface PromotionService {
	void syncDataRedis(PromotionModel promotionModel,String isdn);
	PromotionRedisModel getById(String id);
}

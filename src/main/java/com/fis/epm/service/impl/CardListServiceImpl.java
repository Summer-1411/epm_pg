package com.fis.epm.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.models.ChargeModel;
import com.fis.epm.repo.CardListRedisRepo;
import com.fis.epm.service.CardListService;
import com.fis.pg.epm.models.CardListRedisModel;

@Service
public class CardListServiceImpl implements CardListService{

	@Autowired
	CardListRedisRepo cardListRedisRepo;

	@Override
	public List<ChargeModel> findAllCardListRedisModels() {
		Iterable<CardListRedisModel> iterable = cardListRedisRepo.findAll();
		List<ChargeModel> chargeModels = new ArrayList<ChargeModel>();
		for(CardListRedisModel item:iterable){
			ChargeModel chargeModel = new ChargeModel();
			chargeModel.setName(item.getCardName());
			chargeModel.setValue(item.getCardValue());
			chargeModel.setStatus(item.getStatus());
			chargeModels.add(chargeModel);
		}
		return chargeModels;
	}
	
	

	
	

}

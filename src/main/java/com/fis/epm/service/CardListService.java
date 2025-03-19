package com.fis.epm.service;


import java.util.List;

import com.fis.epm.models.ChargeModel;

public interface CardListService {
	List<ChargeModel> findAllCardListRedisModels();
}

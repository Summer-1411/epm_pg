package com.fis.epm.service;

import java.util.List;

import com.fis.epm.models.PosModel;
import com.fis.pg.epm.models.ShopRedisModel;

public interface ShopService {
	Iterable<ShopRedisModel> findAll();
	List<PosModel> findAllPosRedisModel(String provinceCode);
	ShopRedisModel findById(Long id);
}

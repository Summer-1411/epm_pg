package com.fis.epm.service;

import java.util.List;

import com.fis.epm.models.ProvinceModel;

public interface PayAreaService {
	List<ProvinceModel> findAllAreaRedisModels();
	ProvinceModel findById(Long payAreaId); //load-all-province
}

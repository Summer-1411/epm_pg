package com.fis.epm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.models.PosModel;
import com.fis.epm.models.ProvinceModel;
import com.fis.epm.repo.PayAreaRedisRepo;
import com.fis.epm.repo.ShopRedisRepo;
import com.fis.epm.service.PayAreaService;
import com.fis.pg.epm.models.PayAreaRedisModel;
import com.fis.pg.epm.models.ShopRedisModel;

@Service
public class PayAreaServiceImpl implements PayAreaService{

	
	@Autowired
	PayAreaRedisRepo payAreaRedisRepo;
	
	@Autowired
	ShopRedisRepo shopRedisRepo;
	
	@Override
	public List<ProvinceModel> findAllAreaRedisModels() {
		Iterable<PayAreaRedisModel> iterable = payAreaRedisRepo.findAll();
		List<ProvinceModel> provinceModels = new ArrayList<>();
		for(PayAreaRedisModel item:iterable){
			ProvinceModel provinceModel = new ProvinceModel();
			provinceModel.setName(item.getName());
			provinceModel.setPayAreaCode(item.getPayAreaCode());
			provinceModel.setPayAreaId(item.getPayAreaId());
			provinceModels.add(provinceModel);
		}
		return provinceModels;
	}


	@Override
	public ProvinceModel findById(Long payAreaId) {
		Optional<PayAreaRedisModel> optional = payAreaRedisRepo.findById(payAreaId);
		if(optional.isPresent()){
		ProvinceModel provinceModel =new ProvinceModel();
		PayAreaRedisModel payAreaRedisModel = optional.get();
		provinceModel.setName(payAreaRedisModel.getName());
		provinceModel.setPayAreaCode(payAreaRedisModel.getPayAreaCode());
		provinceModel.setPayAreaId(payAreaRedisModel.getPayAreaId());
		return provinceModel;
		}
		return null;
		
	}



}

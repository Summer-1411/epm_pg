package com.fis.epm.service.impl;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.models.ParamModel;
import com.fis.epm.repo.IsdnInfoRedisRepo;
import com.fis.epm.service.IsdnInfoService;
import com.fis.pg.epm.models.IsdnInfoRedisModel;

@Service
public class IsdnInfoServiceImpl implements IsdnInfoService{

	@Autowired
	IsdnInfoRedisRepo isdnInfoRedisRepo;
	
	@Override
	public void SyncDataRedis(ParamModel paramModel,String isdn) {
		IsdnInfoRedisModel redisModel = new IsdnInfoRedisModel();
		redisModel.setCenCode(paramModel.getData().getCenCode());
		redisModel.setCustCode(paramModel.getData().getCustCode());
		redisModel.setCustType(paramModel.getData().getCustType());
		redisModel.setDebit(paramModel.getData().getDebit());
		redisModel.setIsdn(isdn);
		redisModel.setTypeSub(paramModel.getData().getTypeSub());
		redisModel.setCreateTime(new Date().getTime());
		redisModel.setParam(paramModel.getParam());
		redisModel.setSubId(paramModel.getData().getSubId());
		redisModel.setTypeReference(paramModel.getData().getTypeReference());
		isdnInfoRedisRepo.save(redisModel);
	}

	@Override
	public IsdnInfoRedisModel getById(String id) {
		// TODO Auto-generated method stub
		Optional<IsdnInfoRedisModel> isdnInfo = isdnInfoRedisRepo.findById(id);
		return !isdnInfo.isPresent() ? null : isdnInfo.get();
	}

}

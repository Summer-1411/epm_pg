package com.fis.epm.service;

import com.fis.epm.models.ParamModel;
import com.fis.pg.epm.models.IsdnInfoRedisModel;

public interface IsdnInfoService {
	void SyncDataRedis(ParamModel paramModel,String isdn);
	IsdnInfoRedisModel getById(String id);
}

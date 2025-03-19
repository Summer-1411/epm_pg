package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.ApParamRedisModel;

@Repository
public interface ApParamRedisRepo extends CrudRepository<ApParamRedisModel, Long> {
	List<ApParamRedisModel> findByParType(String parType);

	ApParamRedisModel findByParTypeAndParName(String parType, String parName);
}

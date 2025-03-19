package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.PayAreaRedisModel;

import io.lettuce.core.dynamic.annotation.Param;

@Repository
public interface PayAreaRedisRepo extends CrudRepository<PayAreaRedisModel,Long>{
	
	List<PayAreaRedisModel> findAllAreaRedisModels();
	
	@Query(value="SELECT pay_area_id, pay_area_code, name "+
			"FROM pay_area "+
			"WHERE province IS NOT NULL "+
			"AND district IS NULL "+
			"AND status = 1 "+
			"AND precinct IS NULL "+
			"AND pay_area_id = :provinceId "+
			"ORDER BY order_id",nativeQuery = true)
	List<PayAreaRedisModel> findPayAreaRedisModelsByPayAreaId(@Param("provinceId")Long provinceId);
}

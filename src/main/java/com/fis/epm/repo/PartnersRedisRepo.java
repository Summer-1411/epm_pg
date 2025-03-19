package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;

import com.fis.pg.epm.models.PartnersRedisModel;

public interface PartnersRedisRepo extends CrudRepository<PartnersRedisModel, Long>{
	public PartnersRedisModel findByPartnerId(Long partnerId);
}

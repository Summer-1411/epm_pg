package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.fis.pg.epm.models.PartnerBankRedisModel;

public interface PartnerBankRedisRepo extends CrudRepository<PartnerBankRedisModel, Long>{
	List<PartnerBankRedisModel> findByBankId(Long bankId);
}

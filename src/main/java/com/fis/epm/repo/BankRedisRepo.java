package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.BankRedisModel;

@Repository
public interface BankRedisRepo extends CrudRepository<BankRedisModel, Long>{
	BankRedisModel findByBankCode(String bankCode);
}

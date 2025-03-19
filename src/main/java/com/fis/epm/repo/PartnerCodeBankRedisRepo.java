package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.PartnerCodeBankRedisModel;

@Repository
public interface PartnerCodeBankRedisRepo extends CrudRepository<PartnerCodeBankRedisModel, String>{

}

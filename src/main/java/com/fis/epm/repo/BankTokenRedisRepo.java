package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.BankTokenRedisModel;

@Repository
public interface BankTokenRedisRepo extends CrudRepository<BankTokenRedisModel, String>{

}


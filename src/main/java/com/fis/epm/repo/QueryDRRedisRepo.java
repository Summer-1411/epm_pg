package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.QueryDRAuto;

@Repository
public interface QueryDRRedisRepo extends CrudRepository<QueryDRAuto, String>{

}

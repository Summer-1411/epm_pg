package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.IsdnInfoRedisModel;

@Repository
public interface IsdnInfoRedisRepo  extends CrudRepository<IsdnInfoRedisModel, String>{

}

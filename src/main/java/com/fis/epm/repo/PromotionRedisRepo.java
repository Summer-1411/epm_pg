package com.fis.epm.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.PromotionRedisModel;


@Repository
public interface PromotionRedisRepo extends CrudRepository<PromotionRedisModel, String>{

}

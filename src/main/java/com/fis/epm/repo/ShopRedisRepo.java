package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.ShopRedisModel;

@Repository
public interface ShopRedisRepo  extends CrudRepository<ShopRedisModel, Long>{
	List<ShopRedisModel> findByProvinceCode(String provinceCode);
}

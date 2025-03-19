package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.fis.pg.epm.models.CardListRedisModel;

@Repository
public interface CardListRedisRepo extends CrudRepository<CardListRedisModel, Long>{
}

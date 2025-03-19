package com.fis.epm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.LogApiResult;

@Repository
public interface LogApiResultRepo extends JpaRepository<LogApiResult, Long>{

}

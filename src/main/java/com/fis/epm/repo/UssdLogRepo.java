package com.fis.epm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.UssdLog;

@Repository
public interface UssdLogRepo extends JpaRepository<UssdLog, Long>{

}

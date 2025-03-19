package com.fis.epm.repo;


import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fis.epm.entity.EpmCreateTokenLog;

@Repository
public interface EpmCreateTokenLogRepo extends JpaRepository<EpmCreateTokenLog, Long>{
	
	@Query(value = "select * from EPM_CREATE_TOKEN_LOG where TRANSACTION_ID = :transactionId",nativeQuery = true)
	EpmCreateTokenLog findByTransactionId(@Param("transactionId") String transactionId);

}

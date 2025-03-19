package com.fis.epm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.fis.epm.entity.EpmTransaction;

import java.util.List;

@Repository
public interface EpmTransactionRepo extends JpaRepository<EpmTransaction, String>{
	@Query(value = "SELECT * FROM EPM_TRANSACTION WHERE TRANSACTION_ID = :transactionId", nativeQuery = true)
	EpmTransaction findByTransactionID(@Param("transactionId") String transactionId);

	@Query(value = "SELECT * FROM EPM_TRANSACTION WHERE TRANSACTION_ID in (:lstId)", nativeQuery = true)
	List<EpmTransaction> findListTransactionId(@Param("lstId") List<String> lstId);
}

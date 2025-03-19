package com.fis.epm.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.AgreementToken;

@Repository
public interface AgreementTokenRepo extends JpaRepository<AgreementToken,Long>{
	 @Query(value="SELECT * FROM AGREEMENT_TOKEN WHERE UPPER(TRANSACTION_ID) = UPPER(:transId)",nativeQuery = true)
	 AgreementToken findByTranId(@Param("transId") String transId);
}

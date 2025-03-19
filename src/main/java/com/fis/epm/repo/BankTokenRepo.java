package com.fis.epm.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.BankToken;

@Repository
public interface BankTokenRepo extends JpaRepository<BankToken,Long>{
	@Query(value="SELECT * FROM BANK_TOKEN WHERE TOKEN_ID=:TOKEN_ID and REFERENCE = :REFERENCE",nativeQuery = true)
	BankToken findByBankTokenIdAndReference(@Param("TOKEN_ID") String tokenId, @Param("REFERENCE") String reference);
	
	@Query(value="UPDATE BANK_TOKEN SET STATUS=0 where id=:id DELETE_DATE=:DELETE_DATE",nativeQuery = true)
	void updateStatus(@Param("id") Long id, @Param("DELETE_DATE") Date deleteDate);
	
	@Query(value="SELECT TOKEN_CODE FROM BANK_TOKEN WHERE TOKEN_ID=:TOKEN_ID",nativeQuery = true)
	String findTokenCode(@Param("TOKEN_ID") String tokenId);
	
	@Query(value="SELECT * FROM BANK_TOKEN WHERE TOKEN_CODE=:TOKEN_CODE",nativeQuery = true)
	BankToken findByTokenCode(@Param("TOKEN_CODE") String tokenCode);
	
	@Query(value="SELECT * FROM BANK_TOKEN WHERE TOKEN_ID=:TOKEN_ID",nativeQuery = true)
	BankToken findByBankTokenId(@Param("TOKEN_ID") String tokenId);
	
	@Query(value="SELECT BANK_TOKEN_SEQ.nextval FROM dual",nativeQuery = true)
	Long findSeqBankToken();
}

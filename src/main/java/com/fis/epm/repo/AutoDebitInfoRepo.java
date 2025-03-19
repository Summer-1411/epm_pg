package com.fis.epm.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.AutoDebitInfo;

@Repository
public interface AutoDebitInfoRepo extends JpaRepository<AutoDebitInfo, Long>{
	@Query(value="select * from AUTO_DEBIT_INFO where MSISDN = :MSISDN and TOKENIZER = :TOKENIZER and STATUS = '1'",nativeQuery = true)
	List<AutoDebitInfo> findByMsisdnAndTokenizer(@Param("MSISDN") String msisdn, @Param("TOKENIZER") String tokenizer);
	
	@Query(value="select * from AUTO_DEBIT_INFO where TOKENIZER = :TOKENIZER and STATUS = '1'",nativeQuery = true)
	List<AutoDebitInfo> findByTokenizer(@Param("TOKENIZER") String tokenizer);

}

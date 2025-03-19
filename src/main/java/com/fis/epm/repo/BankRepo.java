package com.fis.epm.repo;

import com.fis.epm.entity.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BankRepo extends JpaRepository<Bank, Long> {

    @Query(value="SELECT * FROM Bank WHERE UPPER(BANK_CODE) = UPPER(:bankCode)",nativeQuery = true)
    Bank findAllByBankCode(@Param("bankCode") String bankCode);
}

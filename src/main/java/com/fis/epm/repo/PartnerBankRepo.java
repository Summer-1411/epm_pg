package com.fis.epm.repo;

import com.fis.epm.entity.PartnerBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PartnerBankRepo extends JpaRepository<PartnerBank, Long> {
    public List<PartnerBank> findByBankId(Long bankId);
    
    @Query(value="select * from PARTNER_BANK where BANK_ID = :bankId and STATUS = 1",nativeQuery = true)
    public List<PartnerBank> findParnerBankCheck(@Param("bankId") Long bankId);
}

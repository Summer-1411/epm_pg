package com.fis.epm.repo;
import com.fis.epm.entity.Partners;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnersRepo extends JpaRepository<Partners, Long>{
    public Partners findByPartnerId(Long partnerId);
    
    @Query(value="select * from partners where STATUS = 1 and code=:code", nativeQuery = true)
    public Partners findByCode(@Param("code") String code);
}

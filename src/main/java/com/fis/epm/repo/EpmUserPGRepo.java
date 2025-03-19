package com.fis.epm.repo;

import com.fis.epm.entity.EpmUserPG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpmUserPGRepo extends JpaRepository<EpmUserPG, String> {
    @Query(value = "SELECT * FROM EPM_USER_PG WHERE status = '1'", nativeQuery = true)
    List<EpmUserPG> findAllByActive();
}

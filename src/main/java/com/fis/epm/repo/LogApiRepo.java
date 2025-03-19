package com.fis.epm.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fis.epm.entity.LogAPI;

@Repository
public interface LogApiRepo extends JpaRepository<LogAPI,Long>{

}

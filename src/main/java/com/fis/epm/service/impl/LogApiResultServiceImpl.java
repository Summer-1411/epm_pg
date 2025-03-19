package com.fis.epm.service.impl;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fis.epm.entity.LogApiResult;
import com.fis.epm.repo.LogApiResultRepo;
import com.fis.epm.service.LogApiResultService;

@Service
@Transactional
public class LogApiResultServiceImpl implements LogApiResultService {
	@Autowired
	private LogApiResultRepo logApiResultRepo;

	@Override
	public void saveLog(List<LogApiResult> request) {
		// TODO Auto-generated method stub
		logApiResultRepo.saveAll(request);
	}

}

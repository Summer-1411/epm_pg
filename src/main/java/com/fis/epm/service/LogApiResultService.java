package com.fis.epm.service;

import java.util.List;

import com.fis.epm.entity.LogApiResult;

public interface LogApiResultService {
	void saveLog(List<LogApiResult> request);
}

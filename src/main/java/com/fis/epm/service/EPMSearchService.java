package com.fis.epm.service;

import com.fis.epm.models.USSDRequestModel;
import com.fis.pg.gw.server.models.ResponseModel;

public interface EPMSearchService {
    
	String searchByUssd(USSDRequestModel ussdRequestModel, String inputPage) throws Exception;
}

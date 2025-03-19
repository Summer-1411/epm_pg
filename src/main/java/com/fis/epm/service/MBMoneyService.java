package com.fis.epm.service;

import com.fis.pg.gw.server.models.ResponseModel;

import java.util.Map;

public interface MBMoneyService {
    public ResponseModel callback(String url, String ipAddress, Map request);
}

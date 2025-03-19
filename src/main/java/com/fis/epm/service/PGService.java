package com.fis.epm.service;

import com.fis.pg.epm.models.EpmTransactionModel;

import java.util.List;

public interface PGService {
    public String loginPG(String user, String password);
    public void autoExecutePG(EpmTransactionModel obj, String user, String pass) throws Exception;
    public void manualExecutePG(List<String> lsyTranId) throws Exception;
}

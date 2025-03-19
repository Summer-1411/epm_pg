package com.fis.epm.dao;

import com.fis.pg.epm.models.EpmTransactionModel;

public interface EPMTransactionDao {
    public boolean insertTransaction(EpmTransactionModel transaction) throws Exception;
    public int updatePaymentEPM(EpmTransactionModel transactionData) throws Exception;
    public void updatePaymentPG(EpmTransactionModel transactionData) throws Exception;
}

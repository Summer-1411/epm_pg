package com.fis.epm.dao.impl;

import com.fis.epm.dao.EPMTransactionDao;
import com.fis.fw.common.utils.AlertSystemUtil;
import com.fis.fw.common.utils.StringUtil;
import com.fis.pg.common.utils.EmpStatusConstain;
import com.fis.pg.epm.models.EpmTransactionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

@Repository
public class EPMTransactionDaoImpl implements EPMTransactionDao {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final DataSource dataSource;

    @Value("${com.fis.pg.url-alert}")
    private String urlAlert;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${com.fis.ip.server}")
    private String ipServer;

    public EPMTransactionDaoImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean insertTransaction(EpmTransactionModel transaction) throws Exception {
        String strSQL = "INSERT INTO EPM_TRANSACTION ( TRANSACTION_ID ,STA_DATETIME ,END_DATETIME ,USER_NAME ,IP_ADDRESS ,GATE_ORDER_ID ,"
                + "FROM_REFERENCE ,REFERENCE ,AMOUNT ,CARD_TYPE ,BANK_CODE ,MERCHANT_CODE ,LANGUAGE ,DISCOUNT_AMOUNT ,"
                + "PROM_AMOUNT ,PAY_STATUS ,ISSUE_STATUS ,DESCRIPTION ,REFUND_STATUS ,QUERYDR_STATUS ,PAYMENT_TYPE ,CARD_NUMBER ,CARD_HOLDER ,CEN_CODE ,"
                + "OBJECT_TYPE ,TOKEN_ID ,REDIRECT_URL,CALL_BACK_URL,PAY_AMOUNT, VAT, TOKEN_PAY, TOKEN_CREATE, PARTNER_CODE, PAYMENT_CHANEL_CODE , issue_amount, TYPE_PRODUCT, TYPE_REFERENCE, PACKAGE ) "
                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,(select par_value from ap_param where par_name = 'VAT' and par_type = 'VAT' and status = 1), ? , ? ,?,"
                + " (select CHANEL_CODE from payment_chanel_config where user_name = ?), ? ,? ,?, ?   )";
//        String strSQL = "INSERT INTO EPM_TRANSACTION ( TRANSACTION_ID ,STA_DATETIME ,END_DATETIME ,USER_NAME ,IP_ADDRESS ,GATE_ORDER_ID ,"
//                + "FROM_REFERENCE ,REFERENCE ,AMOUNT ,CARD_TYPE ,BANK_CODE ,MERCHANT_CODE ,LANGUAGE ,DISCOUNT_AMOUNT ,"
//                + "PROM_AMOUNT ,PAY_STATUS ,ISSUE_STATUS ,DESCRIPTION ,REFUND_STATUS ,QUERYDR_STATUS ,PAYMENT_TYPE ,CARD_NUMBER ,CARD_HOLDER ,CEN_CODE ,"
//                + "OBJECT_TYPE ,TOKEN_ID ,REDIRECT_URL,CALL_BACK_URL,PAY_AMOUNT, VAT, TOKEN_PAY, TOKEN_CREATE, PARTNER_CODE, PAYMENT_CHANEL_CODE , issue_amount, TYPE_PRODUCT, TYPE_REFERENCE ) "
//                + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,(select par_value from ap_param where par_name = 'VAT' and par_type = 'VAT' and status = 1), ? , ? ,?,"
//                + " (select CHANEL_CODE from payment_chanel_config where user_name = ?), ? ,? ,?   )";
        if (transaction == null)
            return false;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(strSQL)) {
            int index = 1;
            pstmt.setString(index++, transaction.getTransactionId());
            Date dtStaDatetime = transaction.getStaDateTime();
            pstmt.setTimestamp(index++, dtStaDatetime != null ? new Timestamp(dtStaDatetime.getTime()) : null);
            Date dtEndDatetime = transaction.getEndDateTime();
            pstmt.setTimestamp(index++, dtEndDatetime != null ? new Timestamp(dtEndDatetime.getTime()) : null);
            pstmt.setString(index++, transaction.getUserName());
            pstmt.setString(index++, transaction.getIpAddress());
            pstmt.setString(index++, transaction.getGateOrderId());
            pstmt.setString(index++, transaction.getFromReference());
            pstmt.setString(index++, transaction.getReference());
            pstmt.setDouble(index++, transaction.getAmount());
            pstmt.setString(index++, transaction.getCardType());
            pstmt.setString(index++, transaction.getBankCode());
            pstmt.setString(index++, transaction.getMerchantCode());
            pstmt.setString(index++, transaction.getLanguage());
            pstmt.setDouble(index++, transaction.getDiscountAmount());
            pstmt.setDouble(index++, transaction.getPromAmount());
            pstmt.setString(index++, transaction.getPayStatus());
            pstmt.setString(index++, transaction.getIssueStatus());
            pstmt.setString(index++, transaction.getDescription());
            pstmt.setString(index++, transaction.getRefundStatus());
            pstmt.setString(index++, transaction.getQueryDrStatus());
            pstmt.setString(index++, transaction.getPaymentType());
            pstmt.setString(index++, transaction.getCardNumber());
            pstmt.setString(index++, transaction.getCardHolder());
            pstmt.setString(index++, transaction.getCenCode());
            pstmt.setString(index++, transaction.getObjectType());
            pstmt.setString(index++, transaction.getTokenId());
            pstmt.setString(index++, transaction.getRedirectUrl());
            pstmt.setString(index++, transaction.getCallBackUrl());
            pstmt.setDouble(index++, transaction.getPayAmount());
            pstmt.setString(index++, transaction.getTokenPay());
            pstmt.setString(index++, transaction.getTokenCreate());
            pstmt.setString(index++, transaction.getPartnerCode());
            pstmt.setString(index++, transaction.getUserName());
            pstmt.setDouble(index++, transaction.getIssueAmount());
            pstmt.setString(index++, transaction.getTypeProduct());
            pstmt.setString(index++, transaction.getTypeReference());
            //COMMENT CODE
            pstmt.setString(index++, transaction.getPackageCode());
            pstmt.executeUpdate();
        } catch (Exception ex) {
            AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        return true;
    }

    @Override
    public int updatePaymentEPM(EpmTransactionModel transactionData) throws Exception {
        int count = 0;
        String sqlInsert = "UPDATE EPM_TRANSACTION SET PAY_STATUS = ?, ISSUE_STATUS = ?, END_DATETIME = sysdate, GATE_ORDER_ID = ? WHERE TRANSACTION_ID = ? AND PAY_STATUS = 'START'";
        if(EmpStatusConstain.SUCC.equals(transactionData.getPayStatus())) {
            sqlInsert = "UPDATE EPM_TRANSACTION SET PAY_STATUS = ?, ISSUE_STATUS = ?, CARD_NUMBER = ?, CARD_HOLDER = ?, GATE_ORDER_ID = ? WHERE TRANSACTION_ID = ? AND PAY_STATUS = 'START'";
        }
        if (transactionData == null)
            return count;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            pstmt.setString(1, transactionData.getPayStatus());
            pstmt.setString(2, transactionData.getIssueStatus());
            if(EmpStatusConstain.SUCC.equals(transactionData.getPayStatus())) {
                pstmt.setString(3, transactionData.getCardNumber());
                pstmt.setString(4, transactionData.getCardHolder());
                pstmt.setString(5, transactionData.getGateOrderId());
                pstmt.setString(6, transactionData.getTransactionId());
            }else {
                pstmt.setString(3, transactionData.getGateOrderId());
                pstmt.setString(4, transactionData.getTransactionId());
            }
            count = pstmt.executeUpdate();
        } catch (Exception ex) {
            AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
        return count;
    }

    @Override
    public void updatePaymentPG(EpmTransactionModel transactionData) throws Exception {
        String sqlInsert = "UPDATE EPM_TRANSACTION SET ISSUE_STATUS = ?, END_DATETIME = sysdate WHERE TRANSACTION_ID = ?";
        if(EmpStatusConstain.SUCC.equals(transactionData.getIssueStatus())) {
            sqlInsert = "UPDATE EPM_TRANSACTION SET ISSUE_STATUS = ?, END_DATETIME = sysdate, CEN_CODE = ? WHERE TRANSACTION_ID = ?";
        }
        if (transactionData == null)
            return;
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlInsert)) {
            pstmt.setString(1, transactionData.getIssueStatus());
            if(EmpStatusConstain.SUCC.equals(transactionData.getIssueStatus())) {
                pstmt.setString(2, transactionData.getCenCode());
                pstmt.setString(3, transactionData.getTransactionId());
            }else {
                pstmt.setString(2, transactionData.getTransactionId());
            }
            pstmt.executeUpdate();
        } catch (Exception ex) {
            AlertSystemUtil.sendAlert(urlAlert, "AppName: " + appName + " - Server: " + ipServer + " - Error : " + ex.getMessage());
            logger.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}

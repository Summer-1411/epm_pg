package com.fis.epm.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * com.pg.fis.entity.EpmTransaction
 * Author CRUD Generator
 * Fri Mar 04 11:10:03 ICT 2022
 */

@Data
@Entity
@Table(name = "TRANSACTIONS" )
public class Transactions {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "TRANSACTION_ID")
    private String transactionId;
    @Column(name = "REQUEST_DATE")
    private Date requestDate;
    @Column(name = "REFERENCE")
    private String reference;
    @Column(name = "AMOUNT")
    private Long amount;
    @Column(name = "PARTNER_ID")
    private String partnerId;
    @Column(name = "PARTNER_TRANSACTION_ID")
    private String partnerTransactionId;
    @Column(name = "BANK_ID")
    private String bankId;
    @Column(name = "STATUS")
    private String status;
    @Column(name = "SETTLEMENT_DATE")
    private Date settlementDate;
}
package com.fis.epm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "AUTO_DEBIT_INFO")
public class AutoDebitInfo {
	 private static final long serialVersionUID = 1L;
	 
	 @Id
	 @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "AUTO_DEBIT_INFO_SEQ")
	 @SequenceGenerator(sequenceName = "AUTO_DEBIT_INFO_SEQ", name = "AUTO_DEBIT_INFO_SEQ", allocationSize = 1)
	 @Column(name = "AUTO_DEBIT_ID")
	 private Long autoDebitId;
	 
	 @Column(name = "MSISDN")
	 private String msisdn;
	 
	 @Column(name = "SUB_TYPE")
	 private String subType;
	 
	 @Column(name = "PARTNER_ID")
	 private Long partnerId;
	 
	 @Column(name = "AMOUNT")
	 private Long amount;
	 
	 @Column(name = "THRESHOLD_AMOUNT")
	 private Long thresholdAmount;
	 
	 @Column(name = "START_DATE")
	 private Date startDate;
	 
	 @Column(name = "END_DATE")
	 private Date endDate;
	 
	 @Column(name = "STATUS")
	 private String status;
	 
	 @Column(name = "SUB_ID")
	 private String subId;
	 
	 @Column(name = "PAYMENT_DAY")
	 private String paymentDay;
	 
	 @Column(name = "EXPIRE_DATE")
	 private Date expireDate;
	 
	 @Column(name = "TOKENIZER")
	 private String tokenizer;
	 
	 @Column(name = "MIGS")
	 private String migs;
	 
	 @Column(name = "CODE_BANK")
	 private String codeBank;
	 
	 @Column(name = "FROM_ISDN")
	 private String fromIsdn;
	 
	 @Column(name = "RETRY_DATE")
	 private Date retryDate;
	 
	 @Column(name = "RETRY_FLAG")
	 private String retryFlag;
	 
	 @Column(name = "RETRY_COUNT")
	 private Long retryCount;
	 
	 @Column(name = "SUB_ID_REG")
	 private Long subIdReg;
}

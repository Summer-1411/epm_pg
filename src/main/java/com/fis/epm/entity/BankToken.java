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
@Table(name = "BANK_TOKEN")
public class BankToken {
	
	private static final long serialVersionUID = 1L;
	
	@Id
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BANK_TOKEN_SEQ")
//    @SequenceGenerator(sequenceName = "BANK_TOKEN_SEQ", name = "BANK_TOKEN_SEQ", allocationSize = 1)
    @Column(name = "ID")
	private Long id;
	
	@Column(name = "TOKEN_ID")
    private String tokenId;
	
	@Column(name = "TOKEN_NAME")
    private String tokenName;
	
	@Column(name = "CARD_TYPE")
    private String cardType;
	
	@Column(name = "BANK_CODE")
    private String bankCode;
	
	@Column(name = "BANK_OTP")
    private String bankOtp;
	
	@Column(name = "REFERENCE")
    private String reference;
	
	@Column(name = "STATUS")
    private String status;
	
	@Column(name = "CREATE_DATE")
    private Date createDate;
	
	@Column(name = "DELETE_DATE")
    private Date deleteDate;
	
	@Column(name = "DELELE_USER_IP")
    private String deleteUserIp;
	
	@Column(name = "TOKEN_CODE")
    private String tokenCode;
	
	@Column(name = "EXPIRED_MONTH")
    private String expiredMonth;
	
	@Column(name = "EXPIRED_YEAR")
    private String expiredYear;
	
	@Column(name = "SUB_ID")
    private Double subId;
}

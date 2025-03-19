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
@Table(name = "EPM_CREATE_TOKEN_LOG")
public class EpmCreateTokenLog {
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EPM_CREATE_TOKEN_LOG_SEQ")
    @SequenceGenerator(sequenceName = "EPM_CREATE_TOKEN_LOG_SEQ", name = "EPM_CREATE_TOKEN_LOG_SEQ", allocationSize = 1)
    @Column(name = "ID")
	private Long id;
	
	@Column(name = "TRANSACTION_ID")
    private String transactionId;
	
	@Column(name = "USER_NAME")
    private String userName;
	
	@Column(name = "STATUS")
    private String status;
	
	@Column(name = "REQUEST_DATETIME")
    private Date requestDatetime;
	
	@Column(name = "RESPONSE_DATETIME")
    private Date responseDatetime;
	
	@Column(name = "TOKEN_ID")
    private String tokenId;
	
	@Column(name = "TOKEN_CREATE")
    private Date tokenCreate;
	
	@Column(name = "CARD_TYPE")
    private String cardType;
	
	@Column(name = "DESCRIPTION")
    private String description;
	
	@Column(name = "CARD_NUMBER")
    private String cardNumber;
	
	@Column(name = "CARD_HOLDER")
    private String cardHolder;
	
	@Column(name = "PARTNER_CODE")
    private String partnerCode;
}

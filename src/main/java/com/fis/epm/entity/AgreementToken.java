package com.fis.epm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "AGREEMENT_TOKEN")
@Getter
@Setter
public class AgreementToken {
	 @Id
	 @Column(name = "AGREEMENT_ID")
	 private Long agreementId;
	 
	 @Column(name = "TOKEN_ID")
	 private String tokenId;
	 
	 @Column(name = "TRANSACTION_ID")
	 private String transactionId;	 
	 
	 @Column(name = "AGREEMENT_TYPE")
	 private String agreementType;
	 
	 @Column(name = "EXPIRED_DATE")
	 private Date expiredDate;
	 
	 @Column(name = "AGREEMENT_DAY")
	 private Long agreementDay;
	 
	 @Column(name = "CREATE_DATE")
	 private Date createDate;
}

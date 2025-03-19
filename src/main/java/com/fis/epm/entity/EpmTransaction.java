package com.fis.epm.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fis.pg.epm.models.EpmTransactionModel;

import lombok.Data;

@Data
@Entity
@Table(name = "EPM_TRANSACTION")
public class EpmTransaction implements Serializable {
	@Id
	@Column(name = "TRANSACTION_ID")
	private String transactionId;
	@Column(name = "STA_DATETIME")
	private Date staDateTime;
	@Column(name = "END_DATETIME")
	private Date endDateTime;
	@Column(name = "PAYMENT_CHANEL_CODE")
	private String paymentChanelCode;
	@Column(name = "IP_ADDRESS")
	private String ipAddress;
	@Column(name = "GATE_ORDER_ID")
	private String gateOrderId;
	@Column(name = "FROM_REFERENCE")
	private String fromReference;
	@Column(name = "REFERENCE")
	private String reference;
	@Column(name = "AMOUNT")
	private Long amount;
	@Column(name = "CARD_TYPE")
	private String cardType;
	@Column(name = "BANK_CODE")
	private String bankCode;
	@Column(name = "PARTNER_CODE")
	private String partnerCode;
	@Column(name = "USER_NAME")
	private String userName;
	@Column(name = "LANGUAGE")
	private String language;
	@Column(name = "DISCOUNT_AMOUNT")
	private Long discountAmount;
	@Column(name = "PROM_AMOUNT")
	private Long promAmount;
	@Column(name = "PAY_STATUS")
	private String payStatus;
	@Column(name = "ISSUE_STATUS")
	private String issueStatus;
	@Column(name = "DESCRIPTION")
	private String description;
	@Column(name = "REFUND_STATUS")
	private String refundStatus;
	@Column(name = "QUERYDR_STATUS")
	private String queryDrStatus;
	@Column(name = "PAYMENT_TYPE")
	private String paymentType;
	@Column(name = "CARD_NUMBER")
	private String cardNumber;
	@Column(name = "CARD_HOLDER")
	private String cardHolder;
	@Column(name = "CEN_CODE")
	private String cenCode;
	@Column(name = "OBJECT_TYPE")
	private String objectType;
	@Column(name = "TOKEN_ID")
	private String tokenId;
	@Column(name = "CALL_BACK_URL")
	private String callBackUrl;
	@Column(name = "REDIRECT_URL")
	private String directUrl;
	@Column(name = "TOKEN_PAY")
	private String tokenPay;
	@Column(name = "VAT")
	private Long vat;
	@Column(name = "TOKEN_CREATE")
	private String tokenCreate;
	@Column(name = "MERCHANT_CODE")
	private String merchantCode;
	@Column(name = "PAY_AMOUNT")
	private Long payAmount;
	@Column(name = "ISSUE_AMOUNT")
	private Long issueAMount;
	@Transient
	private String custCode;
	@Column(name = "TYPE_PRODUCT")
	private String typeProduct;
	@Column(name = "PARTNER_TRANS_ID")
	private String partnerTransId;

	@Column(name = "PACKAGE")
	private String packageCode;

	public EpmTransactionModel setEpmTransactionModel(EpmTransaction emp) {
		EpmTransactionModel model = new EpmTransactionModel();
		model.setTransactionId(emp.getTransactionId());
		model.setStaDateTime(emp.getStaDateTime());
		model.setEndDateTime(emp.getEndDateTime());
		model.setPaymentChanelCode(emp.getPaymentChanelCode());
		model.setIpAddress(emp.getIpAddress());
		model.setGateOrderId(emp.getGateOrderId());
		model.setFromReference(emp.getFromReference());
		model.setReference(emp.getReference());
		model.setAmount(emp.getAmount());
		model.setCardType(emp.getCardType());
		model.setBankCode(emp.getBankCode());
		model.setPartnerCode(emp.getPartnerCode());
		model.setUserName(emp.getUserName());
		model.setLanguage(emp.getLanguage());
		model.setDiscountAmount(emp.getDiscountAmount());
		model.setPromAmount(emp.getPromAmount());
		model.setPayStatus(emp.getPayStatus());
		model.setIssueStatus(emp.getIssueStatus());
		model.setDescription(emp.getDescription());
		model.setRefundStatus(emp.getRefundStatus());
		model.setQueryDrStatus(emp.getQueryDrStatus());
		model.setPaymentType(emp.getPaymentType());
		model.setCardNumber(emp.getCardNumber());
		model.setCardHolder(emp.getCardHolder());
		model.setCenCode(emp.getCenCode());
		model.setObjectType(emp.getObjectType());
		model.setTokenId(emp.getTokenId());
		model.setCallBackUrl(emp.getCallBackUrl());
		model.setTokenPay(emp.getTokenPay());
		model.setVat(emp.getVat());
		model.setTokenCreate(emp.getTokenCreate());
		model.setMerchantCode(emp.getMerchantCode());
		model.setCustCode(emp.getCustCode());
		model.setPayAmount(emp.getPayAmount());
		model.setIssueAmount(emp.getIssueAMount());
		model.setTypeProduct(emp.getTypeProduct());
		return model;
	}
}

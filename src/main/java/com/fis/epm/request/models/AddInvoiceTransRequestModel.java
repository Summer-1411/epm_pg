package com.fis.epm.request.models;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddInvoiceTransRequestModel implements Serializable{
//	private String invoiceNo;
	private String shopCode;
	private String shopName;
	private String taxCode;
	private String reference;
	private String address;
	private String name;
	private String provinceId;
	private Long shopId ;
	private String transactionId ;
	private String email;
	private String status;
	private String type; // null là luồng cũ, 2 là luồng mới
	
}

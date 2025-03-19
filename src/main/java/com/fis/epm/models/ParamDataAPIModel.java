package com.fis.epm.models;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParamDataAPIModel implements Serializable{
	//private String paymentType; // Khách hàng cá nhân or doanh nghiệp
	private String typeSub; //loại thuê bao
	private String cenCode; //1 /2
	private String custCode; //0000835328
	private String debit;// 0.0
	private String custType;//0
	private String urlEpayment;
	private String subId;
	private String typeReference;
}

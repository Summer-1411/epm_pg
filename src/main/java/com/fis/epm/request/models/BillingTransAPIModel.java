package com.fis.epm.request.models;

import java.io.Serializable;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BillingTransAPIModel implements Serializable{
	@NotBlank(message ="Ngày giao dịch không được để trống")
	private String saleTransDate;
	@NotBlank(message ="Mã giao dịch không được để trống")
	private String saleTransType;
	@NotBlank(message ="Mã đơn vị bán hàng không được để trống")
	private String shopCode;
	private String staffCode;
	private String customerName;
	private String telNumber;
	private String email;
	private String company;
	private String address;
	private String tin;
	private String note;
	@NotNull(message = "Tổng tiền thanh toán của giao dịch không được để trống")
	private Double amonut;
	private Long vat;
	private Long discount;
	private String reasonCode;
	private String payMethod;
	private Long price;
	private Object ltsDetail;
}

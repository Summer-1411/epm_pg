package com.fis.epm.respone;

import lombok.Getter;
import lombok.Setter;

//Res trả về khi call api taok giao dịch bán hàng ở BHTT
@Getter
@Setter
public class CreateSaleTransactionRespone {
	private String code;
	private String message;
}

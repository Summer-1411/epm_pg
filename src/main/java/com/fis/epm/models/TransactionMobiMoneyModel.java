package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionMobiMoneyModel {
    private String partnerCode; // Mã đối tác được khai báo trên hệ thống MobiFone Money
    private String transactionId; // Mã giao dịch không trùng nhau. Định dạng: yyyyMMdd_xxxxxxxxxxxx
    private String productCatalogue; // Mã danh mục sản phẩm dịch vụ, dùng để tính phí, hoa hồng
    private String billCode; // Mã hóa đơn của đối tác
    private Long billAmount; // Số tiền cần thanh toán
    private String billComment; // Nội dung thanh toán
    private String acceptableAccount; // Tham số dự phòng cho tương lai, chưa hỗ trợ tại thời điểm hiện tại
    private String paymentType; // Tham số dự phòng cho tương lai, chưa hỗ trợ tại thời điểm hiện tại
    private String redirectUrl; // Đường dẫn redirect về giao diện của đối tác sau khi khách hàng thanh toán xong
    private String callbackUrl; // URL api callback của đối tác để phản hồi kết quả thanh toán
    private String signature; // signData = partnerCode | transactionId | billCode | billAmount | billComment | acceptableAccount | paymentType | redirectUrl | callbackUrl | productCatalogue

    public String generateSignature() {
        return partnerCode + "|" + transactionId + "|" + billCode + "|" + billAmount + "|" + billComment + "|" +
                (acceptableAccount != null ? (acceptableAccount + "|") : "") +
                (paymentType != null ? (paymentType + "|") : "") + redirectUrl + "|" + callbackUrl + "|" +
                (productCatalogue != null ? productCatalogue : "");
    }
}

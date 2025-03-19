package com.fis.epm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "PARTNER_BANK")
public class PartnerBank {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PARTNER_BANK_SEQ")
    @SequenceGenerator(sequenceName = "PARTNER_BANK_SEQ", name = "PARTNER_BANK_SEQ", allocationSize = 1)
    @Column(name = "PARTNER_BANK_ID")
    private Long partnerBankId;
    @Column(name = "BANK_ID")
    private Long bankId;
    @Column(name = "PARTNER_ID")
    private Long partnerId;
    @Column(name = "PAYMENT_TYPE")
    private String paymentType;

    @JsonFormat(pattern="dd/MM/yyyy")
    @Column(name = "FROM_DATE")
    private Date fromDate;

    @JsonFormat(pattern="dd/MM/yyyy")
    @Column(name = "TO_DATE")

    private Date toDate;
    @Column(name = "STATUS")
    private String status;

    @Transient
    private String bankCode;

    @Transient
    private String partnerName;

}

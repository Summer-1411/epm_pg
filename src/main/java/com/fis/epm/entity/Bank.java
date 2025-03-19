package com.fis.epm.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "BANK")
public class Bank {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BANK_SEQ")
    @SequenceGenerator(sequenceName = "BANK_SEQ", name = "BANK_SEQ", allocationSize = 1)
    @Column(name = "BANK_ID")
    private Long bankId;
    @Column(name = "NAME")
    private String name;
    @Column(name = "ADDRESS")
    private String address;
    @Column(name = "TEL_NUMBER")
    private String telNumber;
    @Column(name = "FAX_NUMBER")
    private String faxNumber;
    @Column(name = "CONTACT_NAME")
    private String contactName;
    @Column(name = "BANK_CODE")
    private String bankCode;
    @Column(name = "BANK_SHORT_CODE")
    private String bankShortCode;
    @Column(name = "BANK_SHORTNAME")
    private String bankShortname;
    @Column(name = "TOKEN")
    private String token;
    @Column(name = "BANK_TYPE")
    private String bankType;

}

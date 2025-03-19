package com.fis.epm.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "PARTNERS")
public class Partners {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMON_SEQ")
    @SequenceGenerator(sequenceName = "COMMON_SEQ", name = "COMMON_SEQ", allocationSize = 1)
    @Column(name = "PARTNER_ID")
    private Long partnerId;
    @Column(name = "PARTNER_NAME")
    private String partnerName;
    @Column(name = "CODE")
    private String code;
    @Column(name = "DESCRIPTION")
    private String description;
    @Column(name = "STATUS")
    private String status;

    @Column(name = "KEY")
    private String key;
    @Column(name = "URL")
    private String url;
    @Column(name = "URL_RETURN")
    private String urlReturn;
    @Column(name = "ACCESS_CODE")
    private String accessCode;
    @Column(name = "ACCESS_NAME")
    private String accessName;

}

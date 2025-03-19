package com.fis.epm.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "EPM_USER_PG")
@Getter
@Setter
public class EpmUserPG {
    @Id
    @Column(name = "SERVICE_CODE")
    private String serviceCode;

    @Column(name = "USER_NAME")
    private String userName;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "STATUS")
    private String status;
}

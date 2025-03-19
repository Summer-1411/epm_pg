package com.fis.epm.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

@Data
@Entity
@Table(name="LOG_API")
public class LogApiResult implements Serializable{
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "LOG_API_ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_API_SEQ")
    @SequenceGenerator( sequenceName = "LOG_API_SEQ", name = "LOG_API_SEQ", allocationSize = 1)
    private Integer logId;
    @Column(name = "METHOD")
    private String method;
    @Column(name = "URI")
    private String uri;
    @Column(name = "REQUEST_HEADER")
    private String requestHeader;
    @Column(name = "REQUEST_BODY")
    private String requestBody;
    @Column(name = "RESPONSE_STATUS")
    private String responseStatus;
    @Column(name = "RESPONSE_BODY")
    private String responseBody;
    @Column(name = "PROCESS_TIME")
    private String processTime;
    @Column(name = "CREATE_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;
    @Column(name = "END_TIME")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;
    @Column(name = "SESSION_ID")
    private Integer sessionId;
    @Column(name = "USER_ID")
    private Integer userId;
    @Column(name = "TYPE")
    private String type;
    @Column(name = "TRAN_ID")
    private String tranId;
    @Column(name = "IP_SERVER")
    private String ipServer;
    @Column(name = "IP_REQUEST")
    private String ipRequest;
    
    public com.fis.pg.epm.models.LogApiResult setApiResult(LogApiResult log) {
    	com.fis.pg.epm.models.LogApiResult api = new com.fis.pg.epm.models.LogApiResult();
    	api.setMethod(log.getMethod());
    	api.setUri(log.getUri());
    	api.setRequestHeader(log.getRequestHeader());
    	api.setRequestBody(log.getRequestBody());
    	api.setResponseStatus(log.getResponseStatus());
    	api.setResponseBody(log.getResponseBody());
    	api.setProcessTime(log.getProcessTime());
    	api.setCreateTime(log.getCreateTime());
    	api.setEndTime(log.getEndTime());
    	api.setSessionId(log.getSessionId());
    	api.setUserId(log.getUserId());
    	api.setType(log.getType());
    	api.setTranId(log.getTranId());
    	api.setIpServer(log.getIpServer());
    	api.setIpRequest(log.getIpRequest());
    	return api;
    }
}
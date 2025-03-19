package com.fis.epm.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "LOG_API")
public class LogAPI implements Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOG_API_SEQ")
	@SequenceGenerator(sequenceName = "LOG_API_SEQ", name = "LOG_API_SEQ", allocationSize = 1)
	@Column(name = "LOG_API_ID")
	private Long logApiId;

	@Column(name = "METHOD")
	private String method;

	@Column(name = "URI")
	private String uri;

	@Column(name = "REQUEST_HEADER")
	private String requestHeader;

	@Column(name = "REQUEST_BODY", columnDefinition = "CLOB")
	@Lob
	private String requestBody;

	@Column(name = "RESPONSE_STATUS")
	private String responseStatus;

	@Column(name = "RESPONSE_BODY", columnDefinition = "CLOB")
	@Lob
	private String responseBody;

	@Column(name = "PROCESS_TIME")
	private String processTime;

	@Column(name = "CREATE_TIME")
	private Date createTime;

	@Column(name = "SESSION_ID")
	private Long sessionId;

	@Column(name = "USER_ID")
	private Long userId;

	@Column(name = "REQUEST_PAIR")
	private String requestPair;

}

package com.fis.epm.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "USSD_LOG")
@Getter
@Setter
public class UssdLog {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "USSD_LOG_SEQ")
	@SequenceGenerator(sequenceName = "USSD_LOG_SEQ", name = "USSD_LOG_SEQ", allocationSize = 1)
	@Column(name = "LOG_ID")
	private Long autoDebitId;

	@Column(name = "SESSION_ID")
	private String sesssionId;

	@Column(name = "ISDN")
	private String isdn;

	@Column(name = "MENU_USSD")
	private String menu;

	@Column(name = "USER_INPUT")
	private String userInput;

	@Column(name = "MESSAGE_OUTPUT")
	private String messageOutput;

	@Column(name = "STA_DATETIME")
	private Date staDateTime;

	@Column(name = "END_DATETIME")
	private Date endDateTime;
}

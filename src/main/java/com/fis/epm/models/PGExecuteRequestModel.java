package com.fis.epm.models;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PGExecuteRequestModel implements Serializable {
	private String token;
	private List<PGExecuteBodyModel> body;
}

package com.fis.epm.models;

import java.io.Serializable;
import java.util.List;

import com.fis.pg.epm.models.ParamDataModel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParamModel implements Serializable{
	private List<ParamDataModel> param;
	private ParamDataAPIModel data;
}

package com.fis.epm.models;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DirectModel implements Serializable {
	@JsonProperty("data")
	private String data;
	
	@JsonProperty("checksum")
	private String checksum;

}

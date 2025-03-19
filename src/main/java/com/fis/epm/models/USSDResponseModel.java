package com.fis.epm.models;

import java.util.List;

import lombok.Data;

@Data
public class USSDResponseModel {
	
	private String text;
    
    private String op;
    
    private String input;
    
    private String next;
    
    private String user_input;
    
    private String page;
    
    private String charge;
}

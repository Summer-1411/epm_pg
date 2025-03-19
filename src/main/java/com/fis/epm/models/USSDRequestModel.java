package com.fis.epm.models;

import lombok.Data;

@Data
public class USSDRequestModel {
	
    private String menu;
    
    private String msisdn;
    
    private String session_id;
    
    private String input;
    
    private String user_input;
    
    private String page;
    
    private String charge;
}

package com.fis.epm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fis.epm.entity.UssdServiceCode;
import com.fis.epm.models.USSDRequestModel;
import com.fis.epm.prop.EPMApiConstant;
import com.fis.epm.prop.EPMApplicationProp;
import com.fis.epm.request.models.PrepaidRequestModel;
import com.fis.epm.service.EPMSearchService;
import com.fis.epm.utils.EPMBaseCommon;
import com.fis.pg.common.utils.StringUtils;
import com.fis.pg.common.utils.Tools;
import com.fis.pg.gw.server.models.ResponseModel;

@RestController
@RequestMapping(EPMApiConstant.EPM_ROOT_API_MAPPING)
public class EPMSearchController {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private EPMSearchService ePMSearchService;
	
	@Value(EPMApplicationProp.USSD_SERVICE_CODE)
	private String serviceCode = "";

	@GetMapping(EPMApiConstant.SEARCH_BY_USSD_API_MAPPING)
	public String searchByUssd(@RequestParam(required = false) String menu,
			@RequestParam(required = true) String msisdn, @RequestParam(required = false) String session_id,
			@RequestParam(required = false) String input, @RequestParam(required = false) String user_input,
			@RequestParam(required = false) String page, @RequestParam(required = false) String charge) {
		logger.info("API /search-by-USSD BEGIN:");
		logger.info("Request : menu: " + menu + " - msisdn: " + msisdn + " - session_id: " + session_id 
				+ " - input: " + input + " - user_input: " + user_input + " - page: " + page + " - charge:" + charge);
//		int leng = (serviceCode + "1*").length();
//		if(menu.startsWith(serviceCode + "1*") && menu.length() > (leng + 2)) {
//			String tmp = menu.substring(serviceCode.length() + 2, menu.length() - 1);
//			if(checkNumber(tmp) > 2) {
//				menu = serviceCode + "1*1*";
//			}
//		}
		String inputPage = "";
		UssdServiceCode ussdMenu = EPMBaseCommon.mapUssdMenu.get(session_id);
		if (ussdMenu == null) {
			ussdMenu = new UssdServiceCode();
			ussdMenu.setServiceCode(menu);
		} else {
			menu = ussdMenu.getServiceCode();
			if (StringUtils.nvl(user_input, "").length() == 1) {
				page = "1";
				menu += user_input + "*";
				ussdMenu.setServiceCode(menu);
			}
			if (StringUtils.nvl(user_input, "").length() == 2) {
				inputPage = user_input;
			}			
		}
		ussdMenu.setCreateDate(System.currentTimeMillis());
		EPMBaseCommon.mapUssdMenu.put(session_id, ussdMenu);
//		logger.info("Menu after gen :" + menu);
//		logger.info("inputPage after gen :" + inputPage);
		
//		String[] usedInput = menu.split("\\*");
//		if(usedInput.length > 1 && usedInput[usedInput.length - 1].length() == 2) {
//			inputPage = usedInput[usedInput.length - 1];
//			menu = menu.substring(0,menu.length() - 3);
//		}
		//logger.info("mapUssdMessage : " + Tools.convertModeltoJSON(EPMBaseCommon.mapUssdMessage));
		//logger.info("ussdMenu : " + Tools.convertModeltoJSON(EPMBaseCommon.mapUssdMenu));	
		USSDRequestModel ussdRequestModel = new USSDRequestModel();
		ussdRequestModel.setMenu(StringUtils.nvl(menu, ""));
		ussdRequestModel.setMsisdn(StringUtils.nvl(msisdn, ""));
		ussdRequestModel.setSession_id(StringUtils.nvl(session_id, ""));
		ussdRequestModel.setInput(StringUtils.nvl(input, ""));
		ussdRequestModel.setUser_input(StringUtils.nvl(user_input, ""));
		ussdRequestModel.setPage(StringUtils.nvl(page, "1"));
		if("".equals(ussdRequestModel.getPage())) {
			ussdRequestModel.setPage("1");
		}
		ussdRequestModel.setCharge(StringUtils.nvl(charge, ""));
		
		String response = "";
		try {
			response = ePMSearchService.searchByUssd(ussdRequestModel, inputPage);
		} catch (Exception exp) {
			logger.error(exp.getMessage(),exp);
//			res = this.buildExceptionResponse(exp);
		}
		logger.info("Response :" + response);
		logger.info("API /search-by-USSD END:");
		return response;
	}
	
	private int checkNumber(String value) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			// TODO: handle exception
			return -1;
		}
	}
}
